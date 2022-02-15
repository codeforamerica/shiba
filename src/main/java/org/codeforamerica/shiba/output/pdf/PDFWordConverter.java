package org.codeforamerica.shiba.output.pdf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PDFWordConverter {
	
	
	public PDFWordConverter() {
		 @Value("${itext.license}") String licenseKey;
	}
	
	

}
