package org.codeforamerica.shiba.pages.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;

@EqualsAndHashCode(callSuper = true)
@Data
public class Subworkflow extends ArrayList<Iteration> {

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
