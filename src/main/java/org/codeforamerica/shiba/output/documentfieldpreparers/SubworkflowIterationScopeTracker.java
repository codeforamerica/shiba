package org.codeforamerica.shiba.output.documentfieldpreparers;

import java.util.*;
import java.util.Map.Entry;
import lombok.Value;
import org.codeforamerica.shiba.inputconditions.Condition;
import org.codeforamerica.shiba.pages.config.PageGroupConfiguration;
import org.codeforamerica.shiba.pages.data.Iteration;

// TODO delete this class
@Deprecated
public class SubworkflowIterationScopeTracker {

  private final Map<String, List<UUID>> scopesToIterations;

  public SubworkflowIterationScopeTracker() {
    scopesToIterations = new HashMap<>();
  }

  public IterationScopeInfo getIterationScopeInfo(PageGroupConfiguration pageGroupConfiguration,
      Iteration iteration) {
    String scope = scopeForIteration(pageGroupConfiguration, iteration);
    if (scope != null) {
      if (!scopesToIterations.containsKey(scope)) {
        scopesToIterations.put(scope, new LinkedList<>());
      }
      List<UUID> iterationsInScope = scopesToIterations.get(scope);
      if (!iterationsInScope.contains(iteration.getId())) {
        iterationsInScope.add(iteration.getId());
      }
      return new IterationScopeInfo(scope, iterationsInScope.indexOf(iteration.getId()));
    } else {
      return null;
    }
  }

  private String scopeForIteration(PageGroupConfiguration pageGroupConfiguration,
      Iteration iteration) {
    Map<String, Condition> scopes = null;// = pageGroupConfiguration.getAddedScope();

    if (scopes == null) {
      return null;
    }

    Optional<String> result = scopes.entrySet().stream()
        .filter(
            entry -> Optional.ofNullable(
                    iteration.getPagesData().get(entry.getValue().getPageName()))
                .map(pageData -> entry.getValue().matches(pageData, iteration.getPagesData()))
                .orElse(false)
        ).findAny()
        .map(Entry::getKey);

    return result.orElse(null);
  }

  @Value
  public static class IterationScopeInfo {

    String scope;
    int index;

    public IterationScopeInfo(String scope, int index) {
      this.scope = scope;
      this.index = index;
    }
  }

}
