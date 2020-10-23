package org.codeforamerica.shiba.pages.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode()
@Data
public class Iteration {

    UUID id;
    PagesData pagesData;

    public Iteration() {
        id = UUID.randomUUID();
        pagesData = new PagesData();
    }

    public Iteration(PagesData pagesData) {
        id = UUID.randomUUID();
        this.pagesData = pagesData;
    }

}
