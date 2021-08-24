package org.codeforamerica.shiba.pages.data;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode()
@Data
public class Iteration implements Serializable {

  @Serial
  private static final long serialVersionUID = -2727197075398274841L;

  UUID id;
  PagesData pagesData;

  public Iteration() {
    this(new PagesData());
  }

  public Iteration(PagesData pagesData) {
    id = UUID.randomUUID();
    this.pagesData = pagesData;
  }

}
