package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pdf.PdfFieldFiller;
import org.codeforamerica.shiba.pdf.PdfFieldMapper;
import org.codeforamerica.shiba.xml.XmlGenerator;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.Valid;
import java.util.Locale;

import static org.codeforamerica.shiba.PersonalInfoForm.fromPersonalInfo;

@Controller
public class PageController {
    private final BenefitsApplication benefitsApplication;
    private final MessageSource messageSource;
    private final PdfFieldFiller pdfFieldFiller;
    private final PdfFieldMapper pdfFieldMapper;
    private final XmlGenerator xmlGenerator;

    public PageController(BenefitsApplication benefitsApplication,
                          MessageSource messageSource,
                          PdfFieldFiller pdfFieldFiller,
                          PdfFieldMapper pdfFieldMapper,
                          XmlGenerator xmlGenerator) {
        this.benefitsApplication = benefitsApplication;
        this.messageSource = messageSource;
        this.pdfFieldFiller = pdfFieldFiller;
        this.pdfFieldMapper = pdfFieldMapper;
        this.xmlGenerator = xmlGenerator;
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
        LanguagePreferences languagePreferences =
                benefitsApplication.getLanguagePreferences();
        return new ModelAndView("language-preferences", "languagePreferences", languagePreferences);
    }

    @PostMapping("/language-preference")
    RedirectView postLanguagePreferencePage(@ModelAttribute LanguagePreferences languagePreferences) {
        benefitsApplication.setLanguagePreferences(languagePreferences);
        return new RedirectView("/choose-programs");
    }

    @GetMapping("/choose-programs")
    ModelAndView chooseProgramPage() {
        ProgramSelection programSelection = benefitsApplication.getProgramSelection();
        return new ModelAndView("choose-programs", "programSelection", programSelection);
    }

    @PostMapping("/choose-programs")
    ModelAndView postChooseProgramsPage(@Valid @ModelAttribute ProgramSelection programSelection, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("choose-programs", "programSelection", programSelection);
        }
        benefitsApplication.setProgramSelection(programSelection);
        return new ModelAndView("redirect:/how-it-works");
    }

    @GetMapping("/how-it-works")
    ModelAndView howItWorksPage(Locale locale) {
        if (benefitsApplication.getProgramSelection().getPrograms().isEmpty()) {
            //noinspection SpringMVCViewInspection
            return new ModelAndView("redirect:/choose-programs");
        } else {
            return new ModelAndView(
                    "how-it-works",
                    "programSelection",
                    new ProgramSelectionPresenter(benefitsApplication.getProgramSelection(), messageSource, locale));
        }
    }

    @GetMapping("/intro-basic-info")
    String introBasicInfo() {
        return "intro-basic-info";
    }

    @GetMapping("/personal-info")
    ModelAndView personalInfo() {
        PersonalInfoForm personalInfoForm = fromPersonalInfo(benefitsApplication.getPersonalInfo());
        return new ModelAndView("personal-info", "personalInfoForm", personalInfoForm);
    }

    @PostMapping("/personal-info")
    ModelAndView postPersonalInfo(@Valid @ModelAttribute PersonalInfoForm personalInfoForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("personal-info", "personalInfoForm", personalInfoForm);
        }
        benefitsApplication.setPersonalInfo(personalInfoForm.mapToPersonalInfo());
        return new ModelAndView("redirect:/success");
    }

    @GetMapping("/download")
    ResponseEntity<byte[]> downloadPdf() {
        ApplicationFile applicationFile = pdfFieldFiller.fill(pdfFieldMapper.map(benefitsApplication));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", applicationFile.getFileName()))
                .body(applicationFile.getFileBytes());
    }

    @GetMapping("/download-xml")
    ResponseEntity<byte[]> downloadXml() {
        ApplicationFile applicationFile = xmlGenerator.generate(benefitsApplication);
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
