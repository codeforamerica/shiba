package org.codeforamerica.shiba.pages.enrichment;

import java.util.Optional;

public interface LocationClient {
    Optional<Address> validateAddress(Address address);
}
