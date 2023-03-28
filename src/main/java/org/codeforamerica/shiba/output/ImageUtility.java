package org.codeforamerica.shiba.output;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * Code found on GitHub
 * App for shrinking PDF files by applying jpeg compression
 * https://github.com/bnanes/shrink-pdf
 * @author Benjamin Nanes, bnanes@emory.edu
 *
 */
public class ImageUtility {

	public static byte[] compressImagesInPDF(byte[] imageBytes) throws IOException {
		final PDDocument doc = PDDocument.load(imageBytes);
		final PDPageTree pages = doc.getPages();
		final ImageWriter imgWriter;
		final ImageWriteParam iwp;

		final Iterator<ImageWriter> jpgWriters = ImageIO.getImageWritersByFormatName("jpeg");
		imgWriter = jpgWriters.next();
		iwp = imgWriter.getDefaultWriteParam();
		iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		iwp.setCompressionQuality(0.5f);

		for (PDPage p : pages) {
			scanResources(p.getResources(), doc, imgWriter, iwp);
		}
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		doc.save(outputStream);
		
		// Modifying the original logic, adding this close() request as described
		// in the documentation for PDDocument
		doc.close();
		
		return outputStream.toByteArray();

	}

	private static void scanResources(final PDResources rList, final PDDocument doc, final ImageWriter imgWriter,
			final ImageWriteParam iwp) throws FileNotFoundException, IOException {

		Iterable<COSName> xNames = rList.getXObjectNames();
		for (COSName xName : xNames) {
			final PDXObject xObj = rList.getXObject(xName);
			if (xObj instanceof PDFormXObject) {
				scanResources(((PDFormXObject) xObj).getResources(), doc, imgWriter, iwp);
			}
			if (!(xObj instanceof PDImageXObject)) {
				continue;
			}
			final PDImageXObject img = (PDImageXObject) xObj;
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			imgWriter.setOutput(ImageIO.createImageOutputStream(baos));
			BufferedImage bi = img.getImage();
			
			// Modifying the original logic to not change the image transparency property.
			// Original logic is left commented out.
			// Changing a translucent image to opaque causes the image to appear black.
			IIOImage iioi = new IIOImage(bi, null, null);
			//IIOImage iioi;
			//if (bi.getTransparency() == BufferedImage.OPAQUE) {
			//	iioi = new IIOImage(bi, null, null);
			//} else if (bi.getTransparency() == BufferedImage.TRANSLUCENT) {
			//	iioi = new IIOImage(img.getOpaqueImage(), null, null);
			//} else {
			//	iioi = new IIOImage(img.getOpaqueImage(), null, null);
			//}
			
			imgWriter.write(null, iioi, iwp);
			final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			final PDImageXObject imgNew = JPEGFactory.createFromStream(doc, bais);
			rList.put(xName, imgNew);
		}
	}

}
