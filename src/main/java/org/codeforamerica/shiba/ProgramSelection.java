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
    private List<String> programs = new ArrayList<>();

    public boolean includesProgram(String program) {
        return programs.contains(program);
    }
}
