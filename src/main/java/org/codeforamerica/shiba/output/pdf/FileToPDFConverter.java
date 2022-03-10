package org.codeforamerica.shiba.output.pdf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.itextpdf.licensing.base.LicenseKey;
import com.itextpdf.pdfoffice.OfficeConverter;
import com.itextpdf.pdfoffice.exceptions.PdfOfficeException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FileToPDFConverter {
	@SuppressWarnings("unused")
	private final String licensePath;
	private final FeatureFlagConfiguration featureFlags;

	public FileToPDFConverter(@Value("${itext.license}") String licensePath,
			FeatureFlagConfiguration featureFlagConfiguration) {
		
		this.licensePath = licensePath;
		this.featureFlags = featureFlagConfiguration;
		boolean flagIsNotNull = featureFlags != null && featureFlags.get("word-to-pdf") != null; //need this for tests
		if(flagIsNotNull && featureFlags.get("word-to-pdf").isOn()) {
			LicenseKey.loadLicenseFile(new File(licensePath));
		}else {
			log.info("iText license not loaded, word-to-pdf featureFlag is off.");
		}
		
	}

	public byte[] convertWordDocToPDFwithStreams(InputStream inputStream) throws IOException  {
		try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			OfficeConverter.convertOfficeDocumentToPdf(inputStream, outputStream);
			return outputStream.toByteArray();
		} catch (PdfOfficeException | IOException e) {
			throw e;
		}
	}
}
