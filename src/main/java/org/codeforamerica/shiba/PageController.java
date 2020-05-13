package org.codeforamerica.shiba;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.Valid;
import java.util.Locale;

@Controller
public class PageController {
    private final BenefitsApplication benefitsApplication;
    private MessageSource messageSource;

    public PageController(BenefitsApplication benefitsApplication, MessageSource messageSource) {
        this.benefitsApplication = benefitsApplication;
        this.messageSource = messageSource;
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
                benefitsApplication.getLanguagePreferences()
                        .orElse(new LanguagePreferences());
        return new ModelAndView("language-preferences", "languagePreferences", languagePreferences);
    }

    @PostMapping("/language-preference")
    RedirectView postLanguagePreferencePage(@ModelAttribute LanguagePreferences languagePreferences) {
        benefitsApplication.setLanguagePreferences(languagePreferences);
        return new RedirectView("/choose-programs");
    }

    @GetMapping("/choose-programs")
    ModelAndView chooseProgramPage() {
        ProgramSelection programSelection = benefitsApplication.getProgramSelection()
                .orElse(new ProgramSelection());
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
        return benefitsApplication.getProgramSelection()
                .map(programSelection -> new ModelAndView(
                        "how-it-works",
                        "programSelection",
                        new ProgramSelectionPresenter(programSelection, messageSource, locale)))
                .orElse(new ModelAndView("redirect:/choose-programs"));
    }

    @GetMapping("/intro-basic-info")
    String introBasicInfo() {
        return "intro-basic-info";
    }

    @GetMapping("/test-final-page")
    String testFinalPage() {
        return "test-final-page";
    }
}
