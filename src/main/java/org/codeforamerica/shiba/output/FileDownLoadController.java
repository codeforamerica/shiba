package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.ConfirmationData;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;

@Controller
public class FileDownLoadController {
    private final XmlGenerator xmlGenerator;
    private final ConfirmationData confirmationData;
    private final PdfGenerator pdfGenerator;

    public FileDownLoadController(
            XmlGenerator xmlGenerator,
            ConfirmationData confirmationData,
            PdfGenerator pdfGenerator) {
        this.xmlGenerator = xmlGenerator;
        this.confirmationData = confirmationData;
        this.pdfGenerator = pdfGenerator;
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
    ResponseEntity<byte[]> downloadPdfWithApplicationId(@PathVariable String applicationId) {
        ApplicationFile applicationFile = pdfGenerator.generate(applicationId, CASEWORKER);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", applicationFile.getFileName()))
                .body(applicationFile.getFileBytes());
    }
}
