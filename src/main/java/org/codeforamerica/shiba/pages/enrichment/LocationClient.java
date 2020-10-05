package org.codeforamerica.shiba.pages.enrichment;

import java.util.Optional;

public interface LocationClient {
    Optional<String> getCounty(Address address);

    Optional<Address> validateAddress(Address address);
}
