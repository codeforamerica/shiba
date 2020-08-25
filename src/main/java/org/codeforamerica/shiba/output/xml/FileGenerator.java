package org.codeforamerica.shiba.output.xml;

import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.ApplicationInput;

import java.util.List;

public interface FileGenerator {
    ApplicationFile generate(List<ApplicationInput> applicationInputs, String applicationId);
}
