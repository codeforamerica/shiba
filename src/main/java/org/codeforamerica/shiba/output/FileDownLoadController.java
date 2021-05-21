package org.codeforamerica.shiba.output;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.codeforamerica.shiba.output.Recipient.*;
import static org.codeforamerica.shiba.output.Document.*;

@Controller
@Slf4j
public class FileDownLoadController {
    private final XmlGenerator xmlGenerator;
    private final PdfGenerator pdfGenerator;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ApplicationData applicationData;
    private final ApplicationRepository applicationRepository;

    public FileDownLoadController(
            XmlGenerator xmlGenerator,
            PdfGenerator pdfGenerator,
            ApplicationEventPublisher applicationEventPublisher,
            ApplicationData applicationData,
            ApplicationRepository applicationRepository) {
        this.xmlGenerator = xmlGenerator;
        this.pdfGenerator = pdfGenerator;
        this.applicationEventPublisher = applicationEventPublisher;
        this.applicationData = applicationData;
        this.applicationRepository=applicationRepository;
    }

    @GetMapping("/download")
    ResponseEntity<byte[]> downloadPdf() {
        ApplicationFile applicationFile = pdfGenerator.generate(applicationData.getId(), CAF, CLIENT);
        return createResponse(applicationFile);
    }

    @GetMapping("/download-ccap")
    ResponseEntity<byte[]> downloadCcapPdf() {
        ApplicationFile applicationFile = pdfGenerator.generate(applicationData.getId(), CCAP, CLIENT);
        return createResponse(applicationFile);
    }

    @GetMapping("/download-ccap/{applicationId}")
    ResponseEntity<byte[]> downloadCcapPdfWithApplicationId(
            @PathVariable String applicationId,
            HttpServletRequest request
    ) {
        String requestIpHeader = Optional.ofNullable(request.getHeader("X-FORWARDED-FOR")).orElse("");
        String[] ipAddresses = requestIpHeader.split(",");
        String requestIp = ipAddresses.length > 1 ? ipAddresses[ipAddresses.length - 2].trim() : "<blank>";
        // TODO: Change this to a CCAP PDF Download Notification
        applicationEventPublisher.publishEvent(new DownloadCafEvent(applicationId, requestIp));
        ApplicationFile applicationFile = pdfGenerator.generate(applicationId, CCAP, CASEWORKER);

        return createResponse(applicationFile);
    }

    @GetMapping("/download-xml")
    ResponseEntity<byte[]> downloadXml() {
        ApplicationFile applicationFile = xmlGenerator.generate(applicationData.getId(), CAF, CLIENT);
        return createResponse(applicationFile);
    }

    @GetMapping("/download-caf/{applicationId}")
    ResponseEntity<byte[]> downloadPdfWithApplicationId(
            @PathVariable String applicationId,
            HttpServletRequest request
    ) {
        String requestIpHeader = Optional.ofNullable(request.getHeader("X-FORWARDED-FOR")).orElse("");
        String[] ipAddresses = requestIpHeader.split(",");
        String requestIp = ipAddresses.length > 1 ? ipAddresses[ipAddresses.length - 2].trim() : "<blank>";

        applicationEventPublisher.publishEvent(new DownloadCafEvent(applicationId, requestIp));
        ApplicationFile applicationFile = pdfGenerator.generate(applicationId, CAF, CASEWORKER);

        return createResponse(applicationFile);
    }

    @GetMapping("/download-docs/{applicationId}")
    ResponseEntity<byte[]> downloadDocsWithApplicationId(
            @PathVariable String applicationId,
            HttpServletRequest request
    ) throws IOException {
        String requestIpHeader = Optional.ofNullable(request.getHeader("X-FORWARDED-FOR")).orElse("");
        String[] ipAddresses = requestIpHeader.split(",");
        String requestIp = ipAddresses.length > 1 ? ipAddresses[ipAddresses.length - 2].trim() : "<blank>";

        // TODO: Change this to a Doc download event
        applicationEventPublisher.publishEvent(new DownloadCafEvent(applicationId, requestIp));


        Application application = applicationRepository.find(applicationId);
        List<UploadedDocument> uploadedDocs = application.getApplicationData().getUploadedDocs();

        List<ApplicationFile> applicationFiles = new ArrayList<>();
        byte[] coverPage = pdfGenerator.generate(application, UPLOADED_DOC, CASEWORKER).getFileBytes();
        for (int i = 0; i < uploadedDocs.size(); i++) {
            UploadedDocument uploadedDocument = uploadedDocs.get(i);
            ApplicationFile fileToSend = pdfGenerator.generateForUploadedDocument(uploadedDocument, i, application, coverPage);

            if (fileToSend.getFileBytes().length > 0) {
                applicationFiles.add(fileToSend);
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        applicationFiles.forEach( file -> {
            ZipEntry entry = new ZipEntry(file.getFileName());
            entry.setSize(file.getFileBytes().length);
            try {
                zos.putNextEntry(entry);
                zos.write(file.getFileBytes());
                zos.closeEntry();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        zos.close();
        return createResponse(baos.toByteArray());
    }

    private ResponseEntity<byte[]> createResponse(ApplicationFile applicationFile) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", applicationFile.getFileName()))
                .body(applicationFile.getFileBytes());
    }
    private ResponseEntity<byte[]> createResponse(byte[] files) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "filename=files.zip")
                .body(files);
    }

}
