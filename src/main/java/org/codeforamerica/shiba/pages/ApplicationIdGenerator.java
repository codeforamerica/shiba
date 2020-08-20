package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.ApplicationRepository;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class ApplicationIdGenerator {
    private final ApplicationRepository applicationRepository;

    public ApplicationIdGenerator(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public String generate() {
        int random3DigitNumber = new Random().nextInt(900) + 100;
        String id = applicationRepository.getNextId().toString();
        int numberOfZeros = 7 - id.length();
        StringBuilder idBuilder = new StringBuilder();
        while (idBuilder.length() < numberOfZeros) {
            idBuilder.append('0');
        }

        return random3DigitNumber + idBuilder.toString() + id;
    }
}
