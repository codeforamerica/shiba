package org.codeforamerica.shiba;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class ProgramSelection {
    @NotEmpty
    private Set<BenefitProgram> programs = new HashSet<>();

    public boolean includesProgram(BenefitProgram program) {
        return programs.contains(program);
    }
}
