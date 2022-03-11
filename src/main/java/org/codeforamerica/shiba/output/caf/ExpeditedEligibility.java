package org.codeforamerica.shiba.output.caf;

public enum ExpeditedEligibility {
	  SNAP("SNAP"),
	  CCAP("CCAP"),
	  UNDETERMINED("UNDETERMINED");
	
	private final String displayName;

	ExpeditedEligibility(String displayName) {
	    this.displayName = displayName;
	  }

}
