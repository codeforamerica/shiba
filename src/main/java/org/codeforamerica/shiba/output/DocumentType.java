package org.codeforamerica.shiba.output;

public enum DocumentType {
    CAF, CCAP, NONE;

    @Override
    public String toString() {
        return this == NONE ? "" : super.toString();
    }
}
