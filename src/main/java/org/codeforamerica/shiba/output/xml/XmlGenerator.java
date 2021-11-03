package org.codeforamerica.shiba.output.xml;

import static org.apache.commons.text.StringEscapeUtils.escapeXml10;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.text.StringEscapeUtils;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMappers;
import org.codeforamerica.shiba.output.caf.FilenameGenerator;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class XmlGenerator implements FileGenerator {

  private final Resource xmlConfiguration;
  private final Map<String, String> config;
  private final Map<String, String> enumMappings;
  private final ApplicationRepository applicationRepository;
  private final ApplicationInputsMappers mappers;
  private final FilenameGenerator fileNameGenerator;

  public XmlGenerator(
      @Value("classpath:XmlConfiguration.xml") Resource xmlConfiguration,
      Map<String, String> xmlConfigMap,
      Map<String, String> xmlEnum,
      ApplicationRepository applicationRepository,
      ApplicationInputsMappers mappers,
      FilenameGenerator fileNameGenerator) {
    this.xmlConfiguration = xmlConfiguration;
    this.config = xmlConfigMap;
    this.enumMappings = xmlEnum;
    this.applicationRepository = applicationRepository;
    this.mappers = mappers;
    this.fileNameGenerator = fileNameGenerator;
  }

  @Override
  public ApplicationFile generate(String applicationId, Document document, Recipient recipient) {
    Application application = applicationRepository.find(applicationId);
    List<ApplicationInput> applicationInputs = mappers.map(application, null, recipient);

    try {
      List<ApplicationInput> nonEmptyApplicationInputs = applicationInputs.stream()
          .filter(input -> !input.getValue().isEmpty())
          .toList();

      List<XmlEntry> xmlEntries = new ArrayList<>();
      for (ApplicationInput applicationInput : nonEmptyApplicationInputs) {
        List<XmlEntry> entriesForThisInput = convertApplicationInputToXmlEntries(
            applicationInput);
        xmlEntries.addAll(entriesForThisInput);
      }

      String contentsAfterReplacement;
      try (InputStream xmlConfigInputStream = xmlConfiguration.getInputStream()) {
        String partiallyReplacedContent = new String(xmlConfigInputStream.readAllBytes());
        for (XmlEntry entry : xmlEntries) {
          String regex = getTokenRegex(entry.xmlToken());
          String replacement = entry.escapedInputValue();
          partiallyReplacedContent = partiallyReplacedContent.replaceAll(regex, replacement);
        }
        contentsAfterReplacement = partiallyReplacedContent;
      }

      String finishedXML = contentsAfterReplacement.replaceAll(
          "\\s*<\\w+:\\w+>\\{\\{\\w+}}</\\w+:\\w+>", "");
      byte[] fileContent = finishedXML.getBytes();
      String filename = fileNameGenerator.generateXmlFilename(application);
      return new ApplicationFile(fileContent, filename);
    } catch (IOException e) {
      // TODO never, ever, ever convert a checked exception to a runtime exception
      throw new RuntimeException(e);
    }
  }

  @NotNull
  private String getTokenRegex(String xmlToken) {
    return Pattern.quote("{{%s}}".formatted(xmlToken));
  }

  private String getXmlToken(ApplicationInput input, String xmlToken) {
    return input.getIteration() != null ? xmlToken + "_" + input.getIteration() : xmlToken;
  }

  private record XmlEntry(String xmlToken, String escapedInputValue) {

  }

  private List<XmlEntry> convertApplicationInputToXmlEntries(ApplicationInput input) {
    String defaultXmlConfigKey = String.join(".", input.getGroupName(), input.getName());
    final String singleValueXmlToken = getXmlToken(input, config.get(defaultXmlConfigKey));

    Stream<XmlEntry> xmlEntryStream = switch (input.getType()) {
      case DATE_VALUE -> Stream.of(new XmlEntry(singleValueXmlToken, dateToXmlString(input)));
      case ENUMERATED_SINGLE_VALUE -> Optional.ofNullable(enumMappings.get(input.getValue(0)))
          .map(value -> new XmlEntry(singleValueXmlToken, escapeXml10(value)))
          .stream();
      case ENUMERATED_MULTI_VALUE -> input.getValue().stream().map(value -> new XmlEntry(
              getXmlToken(input, config.get(
                  String.join(".", defaultXmlConfigKey, escapeXml10(value)))),
              enumMappings.get(value)))
          .filter(entry -> entry.escapedInputValue() != null);
      default -> Stream.of(new XmlEntry(singleValueXmlToken, escapeXml10(input.getValue(0))));
    };
    return xmlEntryStream.filter(entry -> entry.xmlToken() != null).toList();
  }

  @NotNull
  private String dateToXmlString(ApplicationInput input) {
    return input.getValue().stream().map(StringEscapeUtils::escapeXml10)
        .collect(Collectors.joining("/"));
  }
}
