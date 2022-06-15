package org.codeforamerica.shiba;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class ServicingAgencyMap<T> {

  T defaultValue;
  Map<ServicingAgency, T> agencies = new HashMap<>();

  public T get(ServicingAgency servicingAgency) {
    return agencies.getOrDefault(servicingAgency, defaultValue);
  }

  public void put(ServicingAgency servicingAgency, T routingDestination) {
    agencies.put(servicingAgency, routingDestination);
  }
}
