package org.codeforamerica.shiba;

import lombok.Data;

import java.util.List;

@Data
public class ProjectionTarget {
    String groupName;
    List<String> inputs;
    Condition condition;
}
