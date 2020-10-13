package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.pages.data.ApplicationData;

import java.util.List;
import java.util.Optional;

public interface DerivedValueConfiguration {
    Optional<List<String>> resolveOptional(ApplicationData data);
}
