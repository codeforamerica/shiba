package org.codeforamerica.shiba.pages.data;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class Subworkflow extends ArrayList<Iteration> {

  @Serial
  private static final long serialVersionUID = -8623969544656997967L;

  public Subworkflow(List<PagesData> pagesData) {
    pagesData.forEach(this::add);
  }

  public void add(PagesData pagesData) {
    add(new Iteration(pagesData));
  }

  public int indexOf(PagesData pagesData) {
    for (int i = 0; i < size(); i++) {
      if (this.get(i).getPagesData().equals(pagesData)) {
        return i;
      }
    }
    return -1;
  }
}
