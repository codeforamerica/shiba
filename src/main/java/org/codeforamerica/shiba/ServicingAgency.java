package org.codeforamerica.shiba;

public interface ServicingAgency {

	/**
	 * Replaces space or period with blank.
	 * @param name
	 * @return
	 */
  static String nameFromString(String name) {
    return name.replace(" ", "").replace(".", "");
  }
}
