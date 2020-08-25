package org.codeforamerica.shiba;

import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMappers;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.ZonedDateTime;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "shiba.username=someUsername",
        "shiba.password=somePassword"
})
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class SecurityConfigurationTest {
    MockMvc mockMvc;

    @Autowired
    WebApplicationContext webApplicationContext;

    @Value("${shiba.username}")
    String username;

    @Value("${shiba.password}")
    String password;

    @MockBean
    ApplicationInputsMappers mappers;

    @MockBean
    ApplicationRepository applicationRepository;

    @MockBean
    PdfGenerator pdfGenerator;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        when(applicationRepository.find(any())).thenReturn(new Application("foo", ZonedDateTime.now(), null, null));
        when(mappers.map(any())).thenReturn(List.of());
        when(pdfGenerator.generate(any(), any())).thenReturn(new ApplicationFile("".getBytes(), "someFileName"));
    }

    @Test
    void requiresBasicAuthenticationOnDownloadCafEndpoint() throws Exception {
        mockMvc.perform(get("/download-caf/9870000123"))
                .andExpect(unauthenticated());

        mockMvc.perform(get("/download-caf/9870000123")
                .with(httpBasic(username, password)))
                .andExpect(authenticated());
    }

    @Test
    void doesNotRequireBasicAuthenticationOnAnyOtherEndpoint() throws Exception {
        mockMvc.perform(get("/download"))
                .andExpect(status().is2xxSuccessful());
    }
}