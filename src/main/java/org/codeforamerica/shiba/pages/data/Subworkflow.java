package org.codeforamerica.shiba.pages.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;

@EqualsAndHashCode(callSuper = true)
@Data
public class Subworkflow extends ArrayList<PagesData> {
}
