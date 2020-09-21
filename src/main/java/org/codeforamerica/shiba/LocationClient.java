package org.codeforamerica.shiba;

import java.util.Optional;

public interface LocationClient {
    Optional<String> getCounty(Address address);
}
