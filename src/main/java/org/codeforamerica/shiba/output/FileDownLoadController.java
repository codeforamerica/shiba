package org.codeforamerica.shiba.output;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.codeforamerica.shiba.output.Recipient.*;
import static org.codeforamerica.shiba.output.DocumentType.*;

@Controller
@Slf4j
public class FileDownLoadController {
    private final XmlGenerator xmlGenerator;
    private final PdfGenerator pdfGenerator;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ApplicationData applicationData;

    public FileDownLoadController(
            XmlGenerator xmlGenerator,
            PdfGenerator pdfGenerator,
            ApplicationEventPublisher applicationEventPublisher,
            ApplicationData applicationData) {
        this.xmlGenerator = xmlGenerator;
        this.pdfGenerator = pdfGenerator;
        this.applicationEventPublisher = applicationEventPublisher;
        this.applicationData = applicationData;
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

    private ResponseEntity<byte[]> createResponse(ApplicationFile applicationFile) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", applicationFile.getFileName()))
                .body(applicationFile.getFileBytes());
    }
}
