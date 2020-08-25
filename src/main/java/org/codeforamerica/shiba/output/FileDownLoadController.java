package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.ApplicationRepository;
import org.codeforamerica.shiba.ConfirmationData;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMappers;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class FileDownLoadController {
    private final PdfGenerator pdfGenerator;
    private final XmlGenerator xmlGenerator;
    private final ApplicationInputsMappers mappers;
    private final ConfirmationData confirmationData;
    private final ApplicationRepository applicationRepository;

    public FileDownLoadController(
            PdfGenerator pdfGenerator,
            XmlGenerator xmlGenerator,
            ApplicationInputsMappers mappers,
            ConfirmationData confirmationData,
            ApplicationRepository applicationRepository) {
        this.pdfGenerator = pdfGenerator;
        this.xmlGenerator = xmlGenerator;
        this.mappers = mappers;
        this.confirmationData = confirmationData;
        this.applicationRepository = applicationRepository;
    }

    @GetMapping("/download")
    ResponseEntity<byte[]> downloadPdf() {
        List<ApplicationInput> applicationInputs = mappers.map(applicationRepository.find(confirmationData.getId()));
        ApplicationFile applicationFile = pdfGenerator.generate(applicationInputs);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", applicationFile.getFileName()))
                .body(applicationFile.getFileBytes());
    }

    @GetMapping("/download-xml")
    ResponseEntity<byte[]> downloadXml() {
        List<ApplicationInput> applicationInputs = mappers.map(applicationRepository.find(confirmationData.getId()));
        ApplicationFile applicationFile = xmlGenerator.generate(applicationInputs);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", applicationFile.getFileName()))
                .body(applicationFile.getFileBytes());
    }

    @GetMapping("/download-caf/{applicationId}")
    ResponseEntity<byte[]> downloadPdfWithApplicationId(@PathVariable String applicationId) {
        List<ApplicationInput> applicationInputs = mappers.map(applicationRepository.find(applicationId));
        ApplicationFile applicationFile = pdfGenerator.generate(applicationInputs);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", applicationFile.getFileName()))
                .body(applicationFile.getFileBytes());
    }
}
