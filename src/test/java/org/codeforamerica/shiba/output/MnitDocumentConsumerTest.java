package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.MnitEsbWebServiceClient;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Map;

import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.mockito.Mockito.*;

class MnitDocumentConsumerTest {
    MnitEsbWebServiceClient mnitClient = mock(MnitEsbWebServiceClient.class);

    XmlGenerator xmlGenerator = mock(XmlGenerator.class);

    PdfGenerator pdfGenerator = mock(PdfGenerator.class);

    MnitDocumentConsumer documentConsumer = new MnitDocumentConsumer(
            mnitClient,
            xmlGenerator,
            pdfGenerator);

    ApplicationData appData = new ApplicationData();

    @BeforeEach
    void setUp() {
        appData.setPagesData(new PagesData(Map.of("somePage", new PageData())));
    }

    @Test
    void generatesThePDFFromTheApplicationData() {
        Application application = Application.builder()
                .id("someId")
                .completedAt(ZonedDateTime.now())
                .applicationData(new ApplicationData())
                .county(County.OLMSTED)
                .timeToComplete(null)
                .build();

        documentConsumer.process(application);

        verify(pdfGenerator).generate(application.getId(), CASEWORKER);
    }

    @Test
    void generatesTheXmlFromTheApplicationData() {
        Application application = Application.builder()
                .id("someId")
                .completedAt(ZonedDateTime.now())
                .applicationData(new ApplicationData())
                .county(County.OLMSTED)
                .timeToComplete(null)
                .build();

        documentConsumer.process(application);
        verify(xmlGenerator).generate(application.getId(), CASEWORKER);
    }

    @Test
    void sendsTheGeneratedXmlAndPdf() {
        ApplicationFile pdfApplicationFile = new ApplicationFile("my pdf".getBytes(), "someFile.pdf");
        when(pdfGenerator.generate(any(), any())).thenReturn(pdfApplicationFile);
        ApplicationFile xmlApplicationFile = new ApplicationFile("my xml".getBytes(), "someFile.xml");
        when(xmlGenerator.generate(any(), any())).thenReturn(xmlApplicationFile);

        Application application = Application.builder()
                .id("someId")
                .completedAt(ZonedDateTime.now())
                .applicationData(new ApplicationData())
                .county(County.OLMSTED)
                .timeToComplete(null)
                .build();
        documentConsumer.process(application);

        verify(mnitClient).send(pdfApplicationFile, County.OLMSTED);
        verify(mnitClient).send(xmlApplicationFile, County.OLMSTED);
    }
}