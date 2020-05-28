package org.codeforamerica.shiba;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.util.SortedSet;
import java.util.TreeSet;

@Getter
@Setter
public class ProgramSelection {
    @NotEmpty
    private SortedSet<BenefitProgram> programs = new TreeSet<>();

    public boolean includesProgram(BenefitProgram program) {
        return programs.contains(program);
    }
}
