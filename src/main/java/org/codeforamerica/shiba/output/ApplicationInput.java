package org.codeforamerica.shiba.output;

import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Value
public class ApplicationInput {
    String groupName;
    String name;
    @NotNull List<String> value;
    ApplicationInputType type;
}
