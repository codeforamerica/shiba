package org.codeforamerica.shiba.xml;

import org.codeforamerica.shiba.ApplicationFile;
import org.codeforamerica.shiba.ApplicationInput;

import java.util.List;
import java.util.Map;

public interface FileGenerator {
    ApplicationFile generate(Map<String, List<ApplicationInput>> formInputsMap);
}
