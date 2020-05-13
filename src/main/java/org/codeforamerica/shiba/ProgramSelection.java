package org.codeforamerica.shiba;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ProgramSelection {
    @NotEmpty
    private List<BenefitProgram> programs = new ArrayList<>();

    public boolean includesProgram(BenefitProgram program) {
        return programs.contains(program);
    }
}
