package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pdf.PdfGenerator;
import org.codeforamerica.shiba.xml.FileGenerator;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Locale;
import java.util.Map;

@Controller
public class PageController {
    private final Map<String, FormData> data;
    private final MessageSource messageSource;
    private final FileGenerator xmlGenerator;
    private final Screens screens;
    private final PdfGenerator pdfGenerator;

    public PageController(Screens screens,
                          Map<String, FormData> data,
                          MessageSource messageSource,
                          FileGenerator xmlGenerator,
                          PdfGenerator pdfGenerator) {
        this.data = data;
        this.messageSource = messageSource;
        this.xmlGenerator = xmlGenerator;
        this.screens = screens;
        this.pdfGenerator = pdfGenerator;
    }

    @GetMapping("/")
    String landingPage() {
        return "landing";
    }

    @GetMapping("/prepare-to-apply")
    String prepareToApplyPage() {
        return "prepare-to-apply";
    }

    @GetMapping("/language-preference")
    ModelAndView languagePreferencePage() {
        Form form = screens.get("language-preferences");
        return new ModelAndView("form-page",
                Map.of(
                        "form", form,
                        "data", data.getOrDefault("language-preferences", FormData.create(form))));
    }

    @PostMapping("/language-preference")
    RedirectView postLanguagePreferencePage(@RequestBody MultiValueMap<String, String> model) {
        String screenName = "language-preferences";
        Form form = screens.get(screenName);
        FormData formData = FormData.create(form, model);
        data.put(screenName, formData);

        return new RedirectView(form.getNextPage());
    }

    @GetMapping("/choose-programs")
    ModelAndView chooseProgramPage() {
        Form form = screens.get("choose-programs");
        return new ModelAndView("form-page",
                Map.of("form", form,
                        "data", data.getOrDefault("choose-programs", FormData.create(form))));
    }

    @PostMapping("/choose-programs")
    ModelAndView postChooseProgramsPage(@RequestBody(required = false) MultiValueMap<String, String> model) {
        String screenName = "choose-programs";
        Form form = screens.get(screenName);
        FormData formData = FormData.create(form, model);
        data.put(screenName, formData);
        if (formData.isValid()) {
            return new ModelAndView(form.getNextPage());
        } else {
            return new ModelAndView("form-page",
                    Map.of("form", form,
                            "data", formData));
        }
    }

    @GetMapping("/how-it-works")
    ModelAndView howItWorksPage(Locale locale) {
        FormData formData = data.get("choose-programs");

        if (!formData.isValid()) {
            //noinspection SpringMVCViewInspection
            return new ModelAndView("redirect:/choose-programs");
        } else {
            return new ModelAndView(
                    "how-it-works",
                    "programSelection",
                    new ProgramSelectionPresenter(messageSource, locale, formData.get("programs").getValue()));
        }
    }

    @GetMapping("/intro-basic-info")
    String introBasicInfo() {
        return "intro-basic-info";
    }

    @GetMapping("/personal-info")
    ModelAndView personalInfo() {
        Form form = screens.get("personal-info");
        return new ModelAndView("personal-info",
                Map.of(
                        "form", form,
                        "data", data.getOrDefault("personal-info", FormData.create(form))));
    }

    @PostMapping("/personal-info")
    ModelAndView postPersonalInfo(@RequestBody MultiValueMap<String, String> model) {
        String screenName = "personal-info";
        Form form = screens.get(screenName);
        FormData formData = FormData.create(form, model);
        data.put(screenName, formData);

        if (formData.isValid()) {
            return new ModelAndView("redirect:/success");
        } else {
            return new ModelAndView("personal-info",
                    Map.of(
                            "form", form,
                            "data", formData));
        }
    }

    @GetMapping("/download")
    ResponseEntity<byte[]> downloadPdf() {
        ApplicationFile applicationFile = pdfGenerator.generate(ApplicationInputs.from(screens, data));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", applicationFile.getFileName()))
                .body(applicationFile.getFileBytes());
    }

    @GetMapping("/download-xml")
    ResponseEntity<byte[]> downloadXml() {
        ApplicationFile applicationFile = xmlGenerator.generate(ApplicationInputs.from(screens, data));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", applicationFile.getFileName()))
                .body(applicationFile.getFileBytes());
    }

    @GetMapping("/success")
    String success() {
        return "success";
    }
}
