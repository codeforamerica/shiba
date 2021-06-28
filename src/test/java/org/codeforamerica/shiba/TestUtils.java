package org.codeforamerica.shiba;

import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class TestUtils {
    public static Path getAbsoluteFilepath(String resourceFilename) {
        return Paths.get(getAbsoluteFilepathString(resourceFilename));
    }

    public static String getAbsoluteFilepathString(String resourceFilename) {
        URL resource = TestUtils.class.getClassLoader().getResource(resourceFilename);
        if (resource != null) {
            return (new File(resource.getFile())).getAbsolutePath();
        }
        return "";
    }

    public static byte[] getFileContentsAsByteArray(String filename) throws IOException {
        return Files.readAllBytes(getAbsoluteFilepath(filename));
    }

    public static void assertPdfFieldEquals(String fieldName, String expectedVal, PDAcroForm pdf) {
        var pdfFieldText = pdf.getField(fieldName).getValueAsString();
        assertThat(pdfFieldText).isEqualTo(expectedVal);
    }

    public static void assertPdfFieldIsEmpty(String fieldName, PDAcroForm pdf) {
        var pdfFieldText = pdf.getField(fieldName).getValueAsString();
        assertThat(pdfFieldText).isEmpty();
    }
}
