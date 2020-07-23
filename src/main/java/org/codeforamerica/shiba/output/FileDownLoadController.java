package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class FileDownLoadController {
    private final ApplicationData data;
    private final PdfGenerator pdfGenerator;
    private final XmlGenerator xmlGenerator;
    private final List<ApplicationInputsMapper> applicationInputsMappers;

    public FileDownLoadController(
            ApplicationData data,
            PdfGenerator pdfGenerator,
            XmlGenerator xmlGenerator,
            List<ApplicationInputsMapper> applicationInputsMappers
    ) {
        this.data = data;
        this.pdfGenerator = pdfGenerator;
        this.xmlGenerator = xmlGenerator;
        this.applicationInputsMappers = applicationInputsMappers;
    }

    @GetMapping("/download")
    ResponseEntity<byte[]> downloadPdf() {
        List<ApplicationInput> applicationInputs = applicationInputsMappers.stream()
                .flatMap(mapper -> mapper.map(this.data).stream())
                .collect(Collectors.toList());
        ApplicationFile applicationFile = pdfGenerator.generate(applicationInputs);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", applicationFile.getFileName()))
                .body(applicationFile.getFileBytes());
    }

    @GetMapping("/download-xml")
    ResponseEntity<byte[]> downloadXml() {
        List<ApplicationInput> applicationInputs = applicationInputsMappers.stream()
                .flatMap(mapper -> mapper.map(this.data).stream())
                .collect(Collectors.toList());
        ApplicationFile applicationFile = xmlGenerator.generate(applicationInputs);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", applicationFile.getFileName()))
                .body(applicationFile.getFileBytes());
    }
}
