package org.codeforamerica.shiba;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.stereotype.Component;

@Component
public class FlywayMigrationStrategyImpl implements FlywayMigrationStrategy {

  @Override
  public void migrate(Flyway flyway) {
//    flyway.clean();
//    flyway.repair();
    flyway.migrate();
  }
}
