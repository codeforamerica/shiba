package org.codeforamerica.shiba.xml;

import org.codeforamerica.shiba.ApplicationFile;
import org.codeforamerica.shiba.FormInput;

import java.util.List;
import java.util.Map;

public interface FileGenerator {
    ApplicationFile generate(Map<String, List<FormInput>> formInputsMap);
}
