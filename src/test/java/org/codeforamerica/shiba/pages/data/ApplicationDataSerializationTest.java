package org.codeforamerica.shiba.pages.data;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.TestUtils.getAbsoluteFilepath;

class ApplicationDataSerializationTest {
    @Test
    void canBeDeserialized() throws IOException, ClassNotFoundException {
        var sessionBytes = Files.readAllBytes(getAbsoluteFilepath("sessionApplicationDataFixture.txt"));
        var applicationData = deserializeObjectFromByteArray(sessionBytes);

        assertThat(applicationData).isInstanceOf(ApplicationData.class);
    }

    private Object deserializeObjectFromByteArray(byte[] b) throws IOException, ClassNotFoundException {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(b))) {
            return objectInputStream.readObject();
        }
    }
}