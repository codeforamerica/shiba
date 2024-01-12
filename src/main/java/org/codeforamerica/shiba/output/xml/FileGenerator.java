package org.codeforamerica.shiba.output.xml;

import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;

public interface FileGenerator {

	  ApplicationFile generate(String applicationId, Document document, Recipient recipient);
	  ApplicationFile generate(String applicationId, Document document, Recipient recipient, RoutingDestination routingDestination);
}
