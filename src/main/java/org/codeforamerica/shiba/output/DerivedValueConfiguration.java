package org.codeforamerica.shiba.output;

import java.util.List;
import java.util.Optional;
import org.codeforamerica.shiba.pages.data.ApplicationData;

public interface DerivedValueConfiguration {

  Optional<List<String>> resolveOptional(ApplicationData data);
}
