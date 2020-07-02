package org.codeforamerica.shiba.output;

import lombok.Data;
import org.codeforamerica.shiba.pages.Condition;

import java.util.List;

@Data
public class ProjectionTarget {
    String groupName;
    List<String> inputs;
    Condition condition;
}
