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
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.documentfieldpreparers.DocumentFieldPreparers;
import org.codeforamerica.shiba.output.caf.FilenameGenerator;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class XmlGenerator implements FileGenerator {

  private final Resource xmlConfiguration;
  private final Map<String, String> config;
  private final Map<String, String> enumMappings;
  private final ApplicationRepository applicationRepository;
  private final DocumentFieldPreparers preparers;
  private final FilenameGenerator fileNameGenerator;

  public XmlGenerator(
      @Value("classpath:XmlConfiguration.xml") Resource xmlConfiguration,
      Map<String, String> xmlConfigMap,
      Map<String, String> xmlEnum,
      ApplicationRepository applicationRepository,
      DocumentFieldPreparers preparers,
      FilenameGenerator fileNameGenerator) {
    this.xmlConfiguration = xmlConfiguration;
    this.config = xmlConfigMap;
    this.enumMappings = xmlEnum;
    this.applicationRepository = applicationRepository;
    this.preparers = preparers;
    this.fileNameGenerator = fileNameGenerator;
  }
  
  /**
   * This version of method generate is used by tests 
   */
  @Override
  public ApplicationFile generate(String applicationId, Document document, Recipient recipient) {
	
    return generate(applicationId, document, recipient, null);
  }

  @Override
  public ApplicationFile generate(String applicationId, Document document, Recipient recipient, RoutingDestination routingDestination) {
    Application application = applicationRepository.find(applicationId);
    List<DocumentField> documentFields = preparers.prepareDocumentFields(application, null,
        recipient);
    /*
     * Putting this regex outside of the try block so it can be recorded if errors occur.
     */
    String regex = null;
    try {
      List<DocumentField> nonEmptyDocumentFields = documentFields.stream()
          .filter(input -> !input.getValue().isEmpty())
          .toList();

      List<XmlEntry> xmlEntries = new ArrayList<>();
      for (DocumentField documentField : nonEmptyDocumentFields) {
        List<XmlEntry> entriesForThisInput = convertApplicationInputToXmlEntries(
            documentField);
        xmlEntries.addAll(entriesForThisInput);
      }

      String contentsAfterReplacement;
      
      //try with resources ensures the input stream is closed at the end of the try statement
      try (InputStream xmlConfigInputStream = xmlConfiguration.getInputStream()) {
        String partiallyReplacedContent = new String(xmlConfigInputStream.readAllBytes());
        
        for (XmlEntry entry : xmlEntries) {
          regex = getTokenRegex(entry.xmlToken());
          String replacement = entry.escapedInputValue();
          partiallyReplacedContent = partiallyReplacedContent.replaceAll(regex, replacement);
        }
        contentsAfterReplacement = partiallyReplacedContent;
      }

      String finishedXML = contentsAfterReplacement.replaceAll(
          "\\s*<\\w+:\\w+>\\{\\{\\w+}}</\\w+:\\w+>", "");
      byte[] fileContent = finishedXML.getBytes();
      String filename = fileNameGenerator.generateXmlFilename(application, routingDestination );
      return new ApplicationFile(fileContent, filename);
    } catch (IOException ioe) {
      // TODO never, ever, ever convert a checked exception to a runtime exception (original comment)
    	// Lots of opinions, don't know why they did this, but no way to recover, so let it throw RTE?
    	log.error(StringEscapeUtils.escapeJava("XML Generator IOException for ApplicationID " + applicationId), ioe);
      throw new RuntimeException(ioe);
    }catch (Exception e) {
    	log.error(StringEscapeUtils.escapeJava("Xml Generator Exception for ApplicationID " + applicationId + "  regex " + regex), e);
    	throw new RuntimeException(e);
    }
  }

  @NotNull
  private String getTokenRegex(String xmlToken) {
    return Pattern.quote("{{%s}}".formatted(xmlToken));
  }

  private String getXmlToken(DocumentField input, String xmlToken) {
    return input.getIteration() != null ? xmlToken + "_" + input.getIteration() : xmlToken;
  }

  private record XmlEntry(String xmlToken, String escapedInputValue) {

  }

  private List<XmlEntry> convertApplicationInputToXmlEntries(DocumentField input) {
    String defaultXmlConfigKey = String.join(".", input.getGroupName(), input.getName());
    final String singleValueXmlToken = getXmlToken(input, config.get(defaultXmlConfigKey));

    Stream<XmlEntry> xmlEntryStream = switch (input.getType()) {
      case DATE_VALUE -> Stream.of(new XmlEntry(singleValueXmlToken, dateToXmlString(input)));
      case ENUMERATED_SINGLE_VALUE -> Optional.ofNullable(checkForEnumValue(input))
          .map(value -> new XmlEntry(singleValueXmlToken,value))
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
  private String dateToXmlString(DocumentField input) {
    return input.getValue().stream().map(StringEscapeUtils::escapeXml10)
        .collect(Collectors.joining("/"));
  }
  
  /**
   * The value of some DocumentFields of type ENUMERATED_SINGLE_VALUE are ENUMs
   * (e.g., "NEVER_MARRIED"), others are actual input values (e.g., "aunt or uncle").
   * We can't simple create an ENUM for a value with white space "aunt or uncle" and
   * rather than rework a number of ENUMERATED_SINGLE_VALUE inputs (e.g., county,
   * selectedTribe, relationship, etc.) we will use this method to work-around the issue.
   * This method checks the input against the ENUM list, if it exists as an ENUM
   * then it returns the ENUM's string value, if not it returns the actual input itself.
   * @param input - this is the DocumentField object which provides the end user's input.
   * @return - a String, the value to place in the XML document
   */
  @NotNull
  private String checkForEnumValue(DocumentField input) {
	  String documentFieldValue = input.getValue(0);
	  String enumValue = enumMappings.get(documentFieldValue);
	  return enumValue != null? enumValue: documentFieldValue;
	  }

}
