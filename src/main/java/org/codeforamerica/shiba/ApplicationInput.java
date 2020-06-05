package org.codeforamerica.shiba;

import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Value
public class ApplicationInput {
    String groupName;
    @NotNull List<String> value;
    String name;
    ApplicationInputType type;
}
