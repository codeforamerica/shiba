package org.codeforamerica.shiba.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;

public class HowItWorksPage extends IntermediaryPage<ChooseProgramsPage, IntermediaryPage<HowItWorksPage, PersonalInfoPage>> {
    public HowItWorksPage(RemoteWebDriver driver, ChooseProgramsPage previousPage) {
        super(previousPage, driver);
    }

    public boolean headerIncludesProgram(String program) {
        return driver.findElement(By.tagName("h2")).getText().contains(program);
    }

    @Override
    public IntermediaryPage<HowItWorksPage, PersonalInfoPage> getNextPage() {
        return new IntermediaryPage<>(this, driver) {
            @Override
            public PersonalInfoPage getNextPage() {
                return new PersonalInfoPage(this, driver);
            }
        };
    }
}
