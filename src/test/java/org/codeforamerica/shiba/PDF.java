package org.codeforamerica.shiba;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.pdfbox.multipdf.Overlay;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

public class PDF {

  /**
   * @param args the command line arguments
   * @throws java.io.IOException
   */
  public static void main(String[] args) throws IOException {
      PDDocument document1 = getFirstDoc();
      PDDocument document2 = getSecondDoc();
   
      Overlay overlay = new Overlay();
      overlay.setOverlayPosition(Overlay.Position.FOREGROUND);
      overlay.setInputPDF(document1);
      overlay.setAllPagesOverlayPDF(document2);
   
      Map<Integer, String> ovmap = new HashMap<Integer, String>();            
      overlay.overlay(ovmap);
   
      document1.save("here.pdf");
   
      document1.close();
      document2.close();
  }
   
  static PDDocument getFirstDoc() throws IOException {
      PDDocument document = new PDDocument();
      PDPage page = new PDPage(PDRectangle.A4);
   
      document.addPage(page);
   
      PDPageContentStream contentStream = new PDPageContentStream(document, page);
   
      contentStream.setNonStrokingColor(Color.RED);
      contentStream.addRect(0, 0, page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
      contentStream.fill();
   
      contentStream.close();
   
      return document;
  }
   
  static PDDocument getSecondDoc() throws IOException {
      PDDocument document = new PDDocument();
      PDPage page = new PDPage(PDRectangle.A4);
   
      document.addPage(page);
   
      PDPageContentStream contentStream = new PDPageContentStream(document, page);
   
      contentStream.beginText(); 
      PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
      contentStream.setFont(font, 12);
      contentStream.newLineAtOffset(250, 750);
      contentStream.showText("Hello World");
   
      contentStream.endText();
   
      contentStream.close();
   
      return document;
  }

}
