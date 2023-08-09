package org.codeforamerica.shiba.output;

import static org.codeforamerica.shiba.application.FlowType.LATER_DOCS;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Document.CERTAIN_POPS;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.slf4j.MDC;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpSession;

@Controller
@Slf4j
public class FileDownloadController {

  private static final String NOT_FOUND_MESSAGE = "Could not find any application with this ID for download";
  private static final String UNSUBMITTED_APPLICATION_MESSAGE = "Submitted time was not set for this application. It is either still in progress or the submitted time was cleared for some reason.";
  private static final String DOWNLOAD_DOCUMENT_ZIP = "Download zip file for application ID %s";
  private static final String NO_DOWNLOAD_DOCUMENT_ZIP = "No documents to download in zip file for application ID %s";
  private static final String OLD_LATER_DOCS = "Later Docs application %s is older than 60 days, supporting documents have been deleted.";
  private final XmlGenerator xmlGenerator;
  private final PdfGenerator pdfGenerator;
  private final ApplicationData applicationData;
  private final ApplicationRepository applicationRepository;
  private final UploadedDocsPreparer uploadedDocsPreparer;

  public FileDownloadController(
      XmlGenerator xmlGenerator,
      PdfGenerator pdfGenerator,
      ApplicationData applicationData,
      ApplicationRepository applicationRepository,
      UploadedDocsPreparer uploadedDocsPreparer) {
    this.xmlGenerator = xmlGenerator;
    this.pdfGenerator = pdfGenerator;
    this.applicationData = applicationData;
    this.applicationRepository = applicationRepository;
    this.uploadedDocsPreparer = uploadedDocsPreparer;
  }

  @GetMapping("/download")
  ResponseEntity<byte[]> downloadPdf(HttpSession httpSession) throws IOException {
    if (applicationData == null || applicationData.getId() == null) {
      log.info(
          "Application is empty or the applicationId is null when client attempts to download pdfs.");
      return createRootPageResponse();
    }
    String applicationId = applicationData.getId();

    MDC.put("applicationId", applicationId);
    MDC.put("sessionId", httpSession.getId());
    log.info("Client with session: " + httpSession.getId() + " Downloading application with id: "
        + applicationId);

    Application application = applicationRepository.find(applicationId);

    if (application.getCompletedAt() != null && application.getCompletedAt()
        .isBefore(ZonedDateTime.now().minusDays(60))
        && application.getFlow() == LATER_DOCS) {
      return ResponseEntity.ok().body(String.format(OLD_LATER_DOCS, applicationId).getBytes());
    }

    List<ApplicationFile> applicationFiles = getApplicationDocuments(applicationId, application,
        CLIENT);

    return createZipFileFromApplications(applicationFiles, applicationId);
  }

  @GetMapping("/download/{applicationId}")
  ResponseEntity<byte[]> downloadAllDocumentsWithApplicationId(@PathVariable String applicationId,
      HttpSession httpSession)
      throws Exception {
    Application application;
    try {
      application = applicationRepository.find(applicationId);
      if (application.getCompletedAt() == null) {
        // The submitted time was not set - The application is still in progress or the time was
        // cleared somehow
        log.info(UNSUBMITTED_APPLICATION_MESSAGE + " for application id " + applicationId);
        return ResponseEntity.ok().body(UNSUBMITTED_APPLICATION_MESSAGE.getBytes());
      }
      MDC.put("applicationId", application.getApplicationData().getId());
      MDC.put("sessionId", httpSession.getId());
      log.info("Client with session: " + httpSession.getId() + " Downloading application with id: "
          + applicationData.getId());

      List<ApplicationFile> applicationFiles = getApplicationDocuments(applicationId,
          application, CASEWORKER);

      return createZipFileFromApplications(applicationFiles, applicationId);
    } catch (EmptyResultDataAccessException e) {
      log.info(NOT_FOUND_MESSAGE);
      return ResponseEntity.ok().body(NOT_FOUND_MESSAGE.getBytes());
    }
  }

  private List<ApplicationFile> getApplicationDocuments(String applicationId,
      Application application,
      Recipient recipient) {
    List<ApplicationFile> applicationFiles = new ArrayList<>();
    if (application.getApplicationData().isCAFApplication()) {
      ApplicationFile applicationFileCAF = pdfGenerator.generate(applicationId, CAF, recipient);
      if (null != applicationFileCAF && applicationFileCAF.getFileBytes().length > 0) {
        applicationFiles.add(applicationFileCAF);
      }
    }
    if (application.getApplicationData().isCCAPApplication()) {
      ApplicationFile applicationFileCCAP = pdfGenerator.generate(applicationId, CCAP, recipient);
      if (null != applicationFileCCAP && applicationFileCCAP.getFileBytes().length > 0) {
        applicationFiles.add(applicationFileCCAP);
      }
    }
    if (application.getApplicationData().isCertainPopsApplication()) {
      ApplicationFile applicationFileCP = pdfGenerator.generate(applicationId, CERTAIN_POPS,
          recipient);
      if (null != applicationFileCP && applicationFileCP.getFileBytes().length > 0) {
        applicationFiles.add(applicationFileCP);
      }
    }
    applicationFiles.addAll(uploadedDocsPreparer.prepare(
        application.getApplicationData().getUploadedDocs(), application));

    return applicationFiles;
  }

  @GetMapping("/download-xml")
  ResponseEntity<byte[]> downloadXml() {
    if (applicationData.getId() == null) {
      return createRootPageResponse();
    }
    ApplicationFile applicationFile = xmlGenerator.generate(applicationData.getId(), CAF, CLIENT);
    return createResponse(applicationFile);
  }

  private ResponseEntity<byte[]> createZipFileFromApplications(
      List<ApplicationFile> applicationFiles,
      String applicationId) throws IOException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos)) {

      applicationFiles.forEach(file -> {
        ZipEntry entry = new ZipEntry(file.getFileName());
        entry.setSize(file.getFileBytes().length);
        try {
          zos.putNextEntry(entry);
          zos.write(file.getFileBytes());
          zos.closeEntry();
        } catch (IOException e) {
          log.error("Unable to write file, " + file.getFileName(), e);
        }
      });

      zos.close();
      baos.close();

      // The minimum size of a .ZIP file is 22 bytes even when empty because of metadata
      if (baos.size() > 22) {
        String msg = String.format(DOWNLOAD_DOCUMENT_ZIP, applicationId);
        log.info(msg);
        return createResponse(baos.toByteArray(), "MNB_application_" + applicationId + ".zip");
      } else {
        // Applicant should not have been able to "submit" documents without uploading any.
        String msg = String.format(NO_DOWNLOAD_DOCUMENT_ZIP, applicationId);
        log.warn(msg);
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
      }
    }
  }

  private ResponseEntity<byte[]> createResponse(ApplicationFile applicationFile) {
    return createResponse(applicationFile.getFileBytes(), applicationFile.getFileName());
  }

  private ResponseEntity<byte[]> createResponse(byte[] fileBytes, String fileName) {
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .header(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", fileName))
        .body(fileBytes);
  }

  /**
   * Builds & returns a response that will cause a redirect to the landing page.
   */
  private ResponseEntity<byte[]> createRootPageResponse() {
    return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/")).build();
  }
}
