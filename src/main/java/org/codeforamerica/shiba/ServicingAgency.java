package org.codeforamerica.shiba;

public interface ServicingAgency {

  static String nameFromString(String name) {
    return name.replace(" ", "").replace(".", "");
  }
}
