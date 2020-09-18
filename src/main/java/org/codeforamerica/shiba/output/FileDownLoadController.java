package org.codeforamerica.shiba.output;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.ConfirmationData;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;

import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;

@Controller
@Slf4j
public class FileDownLoadController {
    private final XmlGenerator xmlGenerator;
    private final ConfirmationData confirmationData;
    private final PdfGenerator pdfGenerator;
    private final ApplicationEventPublisher applicationEventPublisher;

    public FileDownLoadController(
            XmlGenerator xmlGenerator,
            ConfirmationData confirmationData,
            PdfGenerator pdfGenerator,
            ApplicationEventPublisher applicationEventPublisher) {
        this.xmlGenerator = xmlGenerator;
        this.confirmationData = confirmationData;
        this.pdfGenerator = pdfGenerator;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @GetMapping("/download")
    ResponseEntity<byte[]> downloadPdf() {
        ApplicationFile applicationFile = pdfGenerator.generate(confirmationData.getId(), CLIENT);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", applicationFile.getFileName()))
                .body(applicationFile.getFileBytes());
    }

    @GetMapping("/download-xml")
    ResponseEntity<byte[]> downloadXml() {
        ApplicationFile applicationFile = xmlGenerator.generate(confirmationData.getId(), CLIENT);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", applicationFile.getFileName()))
                .body(applicationFile.getFileBytes());
    }

    @GetMapping("/download-caf/{applicationId}")
    ResponseEntity<byte[]> downloadPdfWithApplicationId(
            @PathVariable String applicationId,
            HttpServletRequest request
    ) {
        log.debug("request.getHeader(\"X-Forwarded-For\") = " + request.getHeader("X-FORWARDED-FOR"));
        log.debug("request.getRemoteAddr()" + request.getRemoteAddr());
//        applicationEventPublisher.publishEvent(new DownloadCafEvent(applicationId, request.getRemoteAddr()));
        ApplicationFile applicationFile = pdfGenerator.generate(applicationId, CASEWORKER);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", applicationFile.getFileName()))
                .body(applicationFile.getFileBytes());
    }
}
