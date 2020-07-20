package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.pages.ApplicationData;

import java.util.List;

public interface DerivedValueConfiguration {
    List<String> resolve(ApplicationData data);
}
