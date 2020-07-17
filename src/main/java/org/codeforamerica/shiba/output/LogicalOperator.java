package org.codeforamerica.shiba.output;

import java.util.List;

public enum LogicalOperator {
    OR, AND;

    boolean apply(List<Boolean> predicates) {
        return switch (this) {
            case OR -> predicates.contains(true);
            case AND -> !predicates.contains(false);
        };
    }
}
