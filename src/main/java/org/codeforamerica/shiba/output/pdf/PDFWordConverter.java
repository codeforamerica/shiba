package org.codeforamerica.shiba.output.pdf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.itextpdf.licensing.base.LicenseKey;
import com.itextpdf.pdfoffice.OfficeConverter;
import com.itextpdf.pdfoffice.exceptions.PdfOfficeException;

@Component
public class PDFWordConverter {
	@SuppressWarnings("unused")
	private final String licensePath;

	public PDFWordConverter(@Value("${itext.license}") String licensePath) {
		this.licensePath = licensePath;
		LicenseKey.loadLicenseFile(new File(licensePath));
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
