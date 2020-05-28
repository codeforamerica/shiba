package org.codeforamerica.shiba;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.codeforamerica.shiba.BenefitProgram.*;
import static org.codeforamerica.shiba.Language.*;
import static org.codeforamerica.shiba.MaritalStatus.*;
import static org.codeforamerica.shiba.Sex.FEMALE;
import static org.codeforamerica.shiba.Sex.MALE;

@Configuration
@PropertySource(value = "classpath:xml-mappings.yaml", factory = YamlPropertySourceFactory.class)
public class XmlMappingConfiguration {
    @Bean
    @ConfigurationProperties(prefix = "xml.mappings")
    Map<String, String> xmlConfigMap() {
        return new HashMap<>();
    }

    @Bean
    Map<String, String> xmlEnum() {
        Map<String, String> xmlEnumConfigMap = new HashMap<>();
        xmlEnumConfigMap.put(NEVER_MARRIED.name(), "Never Married");
        xmlEnumConfigMap.put(MARRIED_LIVING_WITH_SPOUSE.name(), "Married Living w/Spouse");
        xmlEnumConfigMap.put(MARRIED_NOT_LIVING_WITH_SPOUSE.name(), "Separated (Married but living apart)");
        xmlEnumConfigMap.put(LEGALLY_SEPARATED.name(), "Legally Separated");
        xmlEnumConfigMap.put(DIVORCED.name(), "Divorced");
        xmlEnumConfigMap.put(WIDOWED.name(), "Widowed");
        xmlEnumConfigMap.put(MALE.name(), "Male");
        xmlEnumConfigMap.put(FEMALE.name(), "Female");
        xmlEnumConfigMap.put(ENGLISH.name(), "English");
        xmlEnumConfigMap.put(SPANISH.name(), "Spanish");
        xmlEnumConfigMap.put(SOOMAALI.name(), "Somali");
        xmlEnumConfigMap.put(VIETNAMESE.name(), "Vietnamese");
        xmlEnumConfigMap.put(RUSSIAN.name(), "Russian");
        xmlEnumConfigMap.put(HMOOB.name(), "Hmong");
        xmlEnumConfigMap.put(FOOD.name(), "Supplemental Nutrition Assistance Program");
        xmlEnumConfigMap.put(CASH.name(), "Cash");
        xmlEnumConfigMap.put(EMERGENCY.name(), "Emergency Help");
        xmlEnumConfigMap.put(CHILD_CARE.name(), "Child Care");

        return xmlEnumConfigMap;
    }
}
