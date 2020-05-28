package org.codeforamerica.shiba.xml;

import org.codeforamerica.shiba.ApplicationFile;

public interface XmlGenerator {
    ApplicationFile generate(Object sourceObject);
}
