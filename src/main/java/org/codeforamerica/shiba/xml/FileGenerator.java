package org.codeforamerica.shiba.xml;

import org.codeforamerica.shiba.ApplicationFile;
import org.codeforamerica.shiba.ApplicationInput;

import java.util.List;

public interface FileGenerator {
    ApplicationFile generate(List<ApplicationInput> applicationInputs);
}
