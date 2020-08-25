package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.MnitEsbWebServiceClient;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMappers;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

class MnitDocumentConsumerTest {
    MnitEsbWebServiceClient mnitClient = mock(MnitEsbWebServiceClient.class);

    PdfGenerator pdfGenerator = mock(PdfGenerator.class);

    ApplicationInputsMappers mappers = mock(ApplicationInputsMappers.class);

    XmlGenerator xmlGenerator = mock(XmlGenerator.class);

    MnitDocumentConsumer documentConsumer = new MnitDocumentConsumer(
            mnitClient,
            pdfGenerator,
            xmlGenerator,
            mappers);

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
        when(mappers.map(application)).thenReturn(applicationInputs);

        documentConsumer.process(application);

        verify(pdfGenerator).generate(applicationInputs);
    }

    @Test
    void generatesTheXmlFromTheApplicationData() {
        Application application = new Application("someId", ZonedDateTime.now(), new ApplicationData(), County.OLMSTED);
        when(mappers.map(application)).thenReturn(applicationInputs);

        documentConsumer.process(application);
        verify(xmlGenerator).generate(applicationInputs);
    }

    @Test
    void sendsTheGeneratedXmlAndPdf() {
        ApplicationFile pdfApplicationFile = new ApplicationFile("my pdf".getBytes(), "someFile.pdf");
        when(pdfGenerator.generate(anyList())).thenReturn(pdfApplicationFile);
        ApplicationFile xmlApplicationFile = new ApplicationFile("my xml".getBytes(), "someFile.xml");
        when(xmlGenerator.generate(anyList())).thenReturn(xmlApplicationFile);

        Application application = new Application("someId", ZonedDateTime.now(), new ApplicationData(), County.OLMSTED);
        documentConsumer.process(application);

        verify(mnitClient).send(pdfApplicationFile, County.OLMSTED);
        verify(mnitClient).send(xmlApplicationFile, County.OLMSTED);
    }
}