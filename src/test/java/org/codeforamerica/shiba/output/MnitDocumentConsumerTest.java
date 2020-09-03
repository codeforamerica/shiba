package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.MnitEsbWebServiceClient;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMappers;
import org.codeforamerica.shiba.output.pdf.PdfField;
import org.codeforamerica.shiba.output.pdf.PdfFieldFiller;
import org.codeforamerica.shiba.output.pdf.PdfFieldMapper;
import org.codeforamerica.shiba.output.pdf.SimplePdfField;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.mockito.Mockito.*;

class MnitDocumentConsumerTest {
    MnitEsbWebServiceClient mnitClient = mock(MnitEsbWebServiceClient.class);

    ApplicationInputsMappers mappers = mock(ApplicationInputsMappers.class);

    XmlGenerator xmlGenerator = mock(XmlGenerator.class);

    PdfFieldFiller pdfFieldFiller = mock(PdfFieldFiller.class);

    PdfFieldMapper pdfFieldMapper = mock(PdfFieldMapper.class);

    MnitDocumentConsumer documentConsumer = new MnitDocumentConsumer(
            mnitClient,
            xmlGenerator,
            mappers,
            pdfFieldMapper,
            pdfFieldFiller);

    ApplicationData appData = new ApplicationData();

    List<ApplicationInput> applicationInputs = List.of(
            new ApplicationInput(
                    "someGroupName",
                    "someName",
                    List.of("someValue"),
                    ApplicationInputType.SINGLE_VALUE));

    @BeforeEach
    void setUp() {
        appData.setPagesData(new PagesData(Map.of("somePage", new PageData())));
    }

    @Test
    void generatesThePDFFromTheApplicationData() {
        Application application = new Application("someId", ZonedDateTime.now(), new ApplicationData(), County.OLMSTED);
        when(mappers.map(application, CASEWORKER)).thenReturn(applicationInputs);
        List<PdfField> pdfFields = List.of(new SimplePdfField("field", "value"));
        when(pdfFieldMapper.map(applicationInputs)).thenReturn(pdfFields);

        documentConsumer.process(application);

        verify(pdfFieldFiller).fill(pdfFields, "someId");
    }

    @Test
    void generatesTheXmlFromTheApplicationData() {
        Application application = new Application("someId", ZonedDateTime.now(), new ApplicationData(), County.OLMSTED);
        when(mappers.map(application, CASEWORKER)).thenReturn(applicationInputs);

        documentConsumer.process(application);
        verify(xmlGenerator).generate(applicationInputs, application.getId());
    }

    @Test
    void sendsTheGeneratedXmlAndPdf() {
        ApplicationFile pdfApplicationFile = new ApplicationFile("my pdf".getBytes(), "someFile.pdf");
        when(pdfFieldFiller.fill(any(), any())).thenReturn(pdfApplicationFile);
        ApplicationFile xmlApplicationFile = new ApplicationFile("my xml".getBytes(), "someFile.xml");
        when(xmlGenerator.generate(anyList(), any())).thenReturn(xmlApplicationFile);

        Application application = new Application("someId", ZonedDateTime.now(), new ApplicationData(), County.OLMSTED);
        documentConsumer.process(application);

        verify(mnitClient).send(pdfApplicationFile, County.OLMSTED);
        verify(mnitClient).send(xmlApplicationFile, County.OLMSTED);
    }
}