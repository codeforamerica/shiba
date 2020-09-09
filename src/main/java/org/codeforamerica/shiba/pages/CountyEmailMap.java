package org.codeforamerica.shiba.pages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.codeforamerica.shiba.County;

import java.util.HashMap;

@Data
@EqualsAndHashCode(callSuper=true)
public class CountyEmailMap extends HashMap<County, String> {
}
