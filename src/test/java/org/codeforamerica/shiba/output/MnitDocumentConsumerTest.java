package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.MnitEsbWebServiceClient;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMappers;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MnitDocumentConsumerTest {
    MnitEsbWebServiceClient mnitClient = mock(MnitEsbWebServiceClient.class);

    Clock clock = mock(Clock.class);

    Instant now = Instant.now();

    PdfGenerator pdfGenerator = mock(PdfGenerator.class);

    ApplicationInputsMappers mappers = mock(ApplicationInputsMappers.class);

    XmlGenerator xmlGenerator = mock(XmlGenerator.class);

    MnitDocumentConsumer documentSender = new MnitDocumentConsumer(
            mnitClient,
            clock,
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
        when(clock.instant()).thenReturn(now);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    @Test
    void generatesThePDFFromTheApplicationData() {
        when(mappers.map(appData)).thenReturn(applicationInputs);

        documentSender.process(appData);

        verify(pdfGenerator).generate(applicationInputs);
    }

    @Test
    void generatesTheXmlFromTheApplicationData() {
        when(mappers.map(appData)).thenReturn(applicationInputs);

        documentSender.process(appData);
        verify(xmlGenerator).generate(applicationInputs);
    }

    @Test
    void sendsTheGeneratedXmlAndPdf() {
        ApplicationFile pdfApplicationFile = new ApplicationFile("my pdf".getBytes(), "someFile.pdf");
        when(pdfGenerator.generate(anyList())).thenReturn(pdfApplicationFile);
        ApplicationFile xmlApplicationFile = new ApplicationFile("my xml".getBytes(), "someFile.xml");
        when(xmlGenerator.generate(anyList())).thenReturn(xmlApplicationFile);

        documentSender.process(appData);

        verify(mnitClient).send(pdfApplicationFile);
        verify(mnitClient).send(xmlApplicationFile);
    }

    @Test
    void returnsTheCurrentTimestamp() {
        assertThat(documentSender.process(appData))
                .isEqualTo(ZonedDateTime.ofInstant(now, ZoneOffset.UTC));
    }
}