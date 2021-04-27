package org.codeforamerica.shiba.pages.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.TestUtils.getAbsoluteFilepath;

class ApplicationDataSerializationTest {
    private byte[] serializedApplicationDataFromOldSession;

    @BeforeEach
    void setUp() throws IOException {
        /*
         * This fixture is the value of spring_session_attributes.attribute_bytes for one session in the database.
         *
         * That session was created while running the code from this commit: 1088f79c08dab374ebe5d6fb8a113a5c779cf004
         *
         * The application corresponding to that session had made it all the way through the application process and
         * was in the process of uploading documents when its attribute_bytes were captured in this fixture
         */
        serializedApplicationDataFromOldSession =  Files.readAllBytes(getAbsoluteFilepath("sessionApplicationDataFixture.txt"));
    }

    @Test
    void maintainsBackwardCompatibilityWhenDeserializingOldSessions() throws IOException, ClassNotFoundException {
        var applicationData = deserializeObjectFromByteArray(serializedApplicationDataFromOldSession);

        assertThat(applicationData).isInstanceOf(ApplicationData.class);
    }

    private Object deserializeObjectFromByteArray(byte[] b) throws IOException, ClassNotFoundException {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(b))) {
            return objectInputStream.readObject();
        }
    }
}
