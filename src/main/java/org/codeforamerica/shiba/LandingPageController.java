package org.codeforamerica.shiba;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LandingPageController {
    @GetMapping("/")
    String landingPage() {
        return "Landing";
    }
}
