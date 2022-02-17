package org.codeforamerica.shiba.output.pdf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.commons.utils.Base64.OutputStream;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.licensing.base.LicenseKey;
import com.itextpdf.pdfoffice.OfficeConverter;
import com.itextpdf.pdfoffice.OfficeDocumentConverterProperties;
import com.itextpdf.pdfoffice.OfficePageRange;
import com.itextpdf.pdfoffice.exceptions.PdfOfficeException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PDFWordConverter {
	private final String licensePath;
	
	public PDFWordConverter(@Value("${itext.license}") String licensePath) {
		this.licensePath = licensePath;
		LicenseKey.loadLicenseFile(new File(licensePath));
	}
	
	public byte[] convertWordDocToPDFwithStreams(InputStream inputStream) {
 	 
  	OutputStream outputStream = null; 
  	byte[] outputByteArray = null;
      try {
    	  OfficeConverter.convertOfficeDocumentToPdf(inputStream, outputStream);
    	  ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
    	  baos.writeTo(outputStream); 
    	  outputByteArray = baos.toByteArray();
	} catch (PdfOfficeException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
      return outputByteArray;
	}
	
	public byte[] convertWordDocToPDFwithFiles(File inputFile) {
		 String pathFile  = System.getProperty("java.io.tmpdir") + "temp.pdf";
	  	File outputFile = new File(pathFile); 
	  	byte[] outputByteArray = null;
	      try {
	    	  OfficeConverter.convertOfficeDocumentToPdf(inputFile, outputFile);
	    	  outputByteArray = Files.readAllBytes(Paths.get(pathFile ));
		} catch (PdfOfficeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      return outputByteArray;
		}
	
}
	

