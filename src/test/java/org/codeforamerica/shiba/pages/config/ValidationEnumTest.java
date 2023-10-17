package org.codeforamerica.shiba.pages.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ValidationEnumTest {
	
	private final String maliciousString1 = "en\"OR/**/1=1)/**/AND/**/ISNULL(ASCII(SUBSTRING(CAST((SELECT/**/@@version)AS/**/varchar(8000)),1,1)),0)>25";
	
	  @Test
	  public void testNoneEnum() {
		  //NONE always returns true
		  List<String> value = new ArrayList<String>();
		  assertTrue(Validation.NONE.apply(value));
		  value.add("");
		  assertTrue(Validation.NONE.apply(value));
		  value.clear();
		  value.add("something");
		  assertTrue(Validation.NONE.apply(value));
	  }
	  
	  @Test
	  public void testShouldBeBlankEnum() {
		  List<String> value = new ArrayList<String>();
		  assertTrue(Validation.SHOULD_BE_BLANK.apply(value));
		  value.add("");
		  assertTrue(Validation.SHOULD_BE_BLANK.apply(value));
		  value.clear();
		  value.add("something");
		  assertFalse(Validation.SHOULD_BE_BLANK.apply(value));
	  }
	  
	  @Test
	  public void testNotBlankEnum() {
		  List<String> value = new ArrayList<String>();
		  assertFalse(Validation.NOT_BLANK.apply(value));
		  value.add("");
		  assertFalse(Validation.NOT_BLANK.apply(value));
		  value.clear();
		  value.add("something");
		  assertTrue(Validation.NOT_BLANK.apply(value));
	  }
	  
	  @Test
	  public void testCountyEnum() {
		  List<String> value = new ArrayList<String>();
		  value.add("Anoka");
		  assertTrue(Validation.COUNTY.apply(value));
		  value.clear();
		  value.add("not a county");
		  assertFalse(Validation.COUNTY.apply(value));
		  value.clear();
		  value.add("LakeOfTheWoods");
		  assertTrue(Validation.COUNTY.apply(value));
		  value.clear();
		  value.add("LakeOfTheWoodsHasBigFish");
		  assertFalse(Validation.COUNTY.apply(value));
		  value.clear();
		  value.add("MilleLacs");
		  assertTrue(Validation.COUNTY.apply(value));
	  }
	  
	  @Test
	  public void testCountyEnumMaliciousString() {
		  List<String> value = new ArrayList<String>();
		  value.add(maliciousString1);
		  assertFalse(Validation.COUNTY.apply(value));
	  }
	  
	  @Test
	  public void testTribalNationEnumMaliciousString() {
		  List<String> value = new ArrayList<String>();
		  value.add(maliciousString1);
		  assertFalse(Validation.TRIBAL_NATION.apply(value));
	  }
	  
	  @Test
	  public void testTribalNationEnum() {
		  List<String> value = new ArrayList<String>();
		  value.add("Leech Lake");
		  assertTrue(Validation.TRIBAL_NATION.apply(value));
		  value.clear();
		  value.add("not a tribal nation");
		  assertFalse(Validation.TRIBAL_NATION.apply(value));
	  }
	  
	  // Test with uppercase and lowercase Of
	  @Test
	  public void testTribalNationMilleLacsBandEnum() {
		  String mlbOo = "Mille Lacs Band Of Ojibwe";
  	  List<String> value = new ArrayList<String>();
		  value.add(mlbOo);
		  assertTrue(Validation.TRIBAL_NATION.apply(value));
		  String mlboo = "Mille Lacs Band of Ojibwe";
		  value.clear();
		  value.add(mlboo);
		  assertTrue(Validation.TRIBAL_NATION.apply(value));
	  }
	  
	  @ParameterizedTest
	  @CsvSource(value = {
			  "Aitkin",
			  "Anoka",
			  "Becker",
			  "Beltrami",
			  "Benton",
			  "Big Stone",
			  "Blue Earth",
			  "Brown",
			  "Carlton",
			  "Carver",
			  "Cass",
			  "Chippewa",
			  "Chisago",
			  "Clay",
			  "Clearwater",
			  "Cook",
			  "Cottonwood",
			  "Crow Wing",
			  "Dakota",
			  "Dodge",
			  "Douglas",
			  "Faribault",
			  "Fillmore",
			  "Freeborn",
			  "Goodhue",
			  "Grant",
			  "Hennepin",
			  "Houston",
			  "Hubbard",
			  "Isanti",
			  "Itasca",
			  "Jackson",
			  "Kanabec",
			  "Kandiyohi",
			  "Kittson",
			  "Koochiching",
			  "Lac Qui Parle",
			  "Lake",
			  "Lake Of The Woods",
			  "Le Sueur",
			  "Lincoln",
			  "Lyon",
			  "Mahnomen",
			  "Marshall",
			  "Martin",
			  "McLeod",
			  "Meeker",
			  "Mille Lacs",
			  "Morrison",
			  "Mower",
			  "Murray",
			  "Nicollet",
			  "Nobles",
			  "Norman",
			  "Olmsted",
			  "Otter Tail",
			  "Pennington",
			  "Pine",
			  "Pipestone",
			  "Polk",
			  "Pope",
			  "Ramsey",
			  "Red Lake",
			  "Redwood",
			  "Renville",
			  "Rice",
			  "Rock",
			  "Roseau",
			  "Scott",
			  "Sherburne",
			  "Sibley",
			  "Stearns",
			  "Steele",
			  "Stevens",
			  "StLouis",
			  "Swift",
			  "Todd",
			  "Traverse",
			  "Wabasha",
			  "Wadena",
			  "Waseca",
			  "Washington",
			  "Watonwan",
			  "Wilkin",
			  "Winona",
			  "Wright",
			  "Yellow Medicine"
	  })
	  void testAllCounties(String county) {
		  List<String> value = new ArrayList<String>();
		  value.add(county);
		  assertTrue(Validation.COUNTY.apply(value));
	  }
	  
}
