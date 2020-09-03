package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.ApplicationRepository;
import org.codeforamerica.shiba.ConfirmationData;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMappers;
import org.codeforamerica.shiba.output.pdf.PdfFieldFiller;
import org.codeforamerica.shiba.output.pdf.PdfFieldMapper;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

import static org.codeforamerica.shiba.output.Recipient.CLIENT;

@Controller
public class FileDownLoadController {
    private final XmlGenerator xmlGenerator;
    private final ApplicationInputsMappers mappers;
    private final ConfirmationData confirmationData;
    private final ApplicationRepository applicationRepository;
    private final PdfFieldMapper pdfFieldMapper;
    private final PdfFieldFiller cafWithCoverPageFieldFiller;
    private final PdfFieldFiller cafFieldFiller;

    public FileDownLoadController(
            XmlGenerator xmlGenerator,
            ApplicationInputsMappers mappers,
            ConfirmationData confirmationData,
            ApplicationRepository applicationRepository,
            PdfFieldMapper pdfFieldMapper,
            PdfFieldFiller cafWithCoverPageFieldFiller,
            PdfFieldFiller cafFieldFiller) {
        this.xmlGenerator = xmlGenerator;
        this.mappers = mappers;
        this.confirmationData = confirmationData;
        this.applicationRepository = applicationRepository;
        this.pdfFieldMapper = pdfFieldMapper;
        this.cafWithCoverPageFieldFiller = cafWithCoverPageFieldFiller;
        this.cafFieldFiller = cafFieldFiller;
    }

    @GetMapping("/download")
    ResponseEntity<byte[]> downloadPdf() {
        List<ApplicationInput> applicationInputs = mappers.map(applicationRepository.find(confirmationData.getId()), CLIENT);
        ApplicationFile applicationFile = cafFieldFiller.fill(pdfFieldMapper.map(applicationInputs), confirmationData.getId());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", applicationFile.getFileName()))
                .body(applicationFile.getFileBytes());
    }

    @GetMapping("/download-xml")
    ResponseEntity<byte[]> downloadXml() {
        List<ApplicationInput> applicationInputs = mappers.map(applicationRepository.find(confirmationData.getId()), CLIENT);
        ApplicationFile applicationFile = xmlGenerator.generate(applicationInputs, confirmationData.getId());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", applicationFile.getFileName()))
                .body(applicationFile.getFileBytes());
    }

    @GetMapping("/download-caf/{applicationId}")
    ResponseEntity<byte[]> downloadPdfWithApplicationId(@PathVariable String applicationId) {
        List<ApplicationInput> applicationInputs = mappers.map(applicationRepository.find(applicationId), Recipient.CASEWORKER);
        ApplicationFile applicationFile = cafWithCoverPageFieldFiller.fill(pdfFieldMapper.map(applicationInputs), applicationId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", applicationFile.getFileName()))
                .body(applicationFile.getFileBytes());
    }
}
