package org.codeforamerica.shiba;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class PageController {
    private final BenefitsApplication benefitsApplication;

    public PageController(BenefitsApplication benefitsApplication) {
        this.benefitsApplication = benefitsApplication;
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
    String postLanguagePreferencePage(@ModelAttribute LanguagePreferences languagePreferences) {
        benefitsApplication.setLanguagePreferences(languagePreferences);
        return "redirect:/choose-programs";
    }

    @GetMapping("/choose-programs")
    String chooseProgramPage() {
        return "choose-programs";
    }
}
