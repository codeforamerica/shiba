package org.codeforamerica.shiba;

import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.research.ResearchData;
import org.codeforamerica.shiba.research.ResearchDataRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@Sql(statements = {"TRUNCATE TABLE research"})
@Tag("db")
class ResearchDataRepositoryTest {
    @Autowired
    ResearchDataRepository researchDataRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void savesResearchData() {
        ResearchData researchData = ResearchData.builder()
                .spokenLanguage("someSpokenLanguage")
                .writtenLanguage("someWrittenLanguage")
                .sex("someSex")
                .snap(true)
                .cash(false)
                .housing(true)
                .emergency(false)
                .childcare(true)
                .firstName("someFirstName")
                .lastName("someLastName")
                .dateOfBirth(LocalDate.now())
                .phoneNumber("somePhoneNumber")
                .email("someEmail")
                .phoneOptIn(false)
                .emailOptIn(true)
                .zipCode("someZipCode")
                .hasHousehold(true)
                .moneyMadeLast30Days(123.31)
                .payRentOrMortgage(false)
                .homeExpensesAmount(431.51)
                .areYouWorking(true)
                .selfEmployment(false)
                .socialSecurity(true)
                .SSI(false)
                .veteransBenefits(true)
                .unemployment(false)
                .workersCompensation(true)
                .retirement(false)
                .childOrSpousalSupport(true)
                .tribalPayments(false)
                .householdSize(123)
                .enteredSsn(true)
                .flow(FlowType.EXPEDITED)
                .applicationId("someApplicationId")
                .county("someCounty")
                .build();
        researchDataRepository.save(researchData);

        jdbcTemplate.query("SELECT * FROM research", resultSet -> {
            assertThat(resultSet.getString("spoken_language")).isEqualTo(researchData.getSpokenLanguage());
            assertThat(resultSet.getString("written_language")).isEqualTo(researchData.getWrittenLanguage());
            assertThat(resultSet.getString("sex")).isEqualTo(researchData.getSex());
            assertThat(resultSet.getString("first_name")).isEqualTo(researchData.getFirstName());
            assertThat(resultSet.getString("last_name")).isEqualTo(researchData.getLastName());
            assertThat(resultSet.getString("phone_number")).isEqualTo(researchData.getPhoneNumber());
            assertThat(resultSet.getString("email")).isEqualTo(researchData.getEmail());
            assertThat(resultSet.getString("zip_code")).isEqualTo(researchData.getZipCode());
            assertThat(resultSet.getString("application_id")).isEqualTo(researchData.getApplicationId());
            assertThat(resultSet.getBoolean("snap")).isEqualTo(researchData.getSnap());
            assertThat(resultSet.getBoolean("cash")).isEqualTo(researchData.getCash());
            assertThat(resultSet.getBoolean("housing")).isEqualTo(researchData.getHousing());
            assertThat(resultSet.getBoolean("emergency")).isEqualTo(researchData.getEmergency());
            assertThat(resultSet.getBoolean("childcare")).isEqualTo(researchData.getChildcare());
            assertThat(resultSet.getBoolean("phone_opt_in")).isEqualTo(researchData.getPhoneOptIn());
            assertThat(resultSet.getBoolean("email_opt_in")).isEqualTo(researchData.getEmailOptIn());
            assertThat(resultSet.getBoolean("has_household")).isEqualTo(researchData.getHasHousehold());
            assertThat(resultSet.getBoolean("pay_rent_or_mortgage")).isEqualTo(researchData.getPayRentOrMortgage());
            assertThat(resultSet.getBoolean("are_you_working")).isEqualTo(researchData.getAreYouWorking());
            assertThat(resultSet.getBoolean("self_employment")).isEqualTo(researchData.getSelfEmployment());
            assertThat(resultSet.getBoolean("social_security")).isEqualTo(researchData.getSocialSecurity());
            assertThat(resultSet.getBoolean("ssi")).isEqualTo(researchData.getSSI());
            assertThat(resultSet.getBoolean("veterans_benefits")).isEqualTo(researchData.getVeteransBenefits());
            assertThat(resultSet.getBoolean("unemployment")).isEqualTo(researchData.getUnemployment());
            assertThat(resultSet.getBoolean("workers_compensation")).isEqualTo(researchData.getWorkersCompensation());
            assertThat(resultSet.getBoolean("retirement")).isEqualTo(researchData.getRetirement());
            assertThat(resultSet.getBoolean("child_or_spousal_support")).isEqualTo(researchData.getChildOrSpousalSupport());
            assertThat(resultSet.getBoolean("tribal_payments")).isEqualTo(researchData.getTribalPayments());
            assertThat(resultSet.getBoolean("entered_ssn")).isEqualTo(researchData.getEnteredSsn());
            assertThat(resultSet.getDouble("money_made_last30_days")).isEqualTo(researchData.getMoneyMadeLast30Days());
            assertThat(resultSet.getDouble("home_expenses_amount")).isEqualTo(researchData.getHomeExpensesAmount());
            assertThat(resultSet.getDate("date_of_birth").toLocalDate()).isEqualTo(researchData.getDateOfBirth());
            assertThat(resultSet.getInt("household_size")).isEqualTo(researchData.getHouseholdSize());
            assertThat(resultSet.getString("flow")).isEqualTo(researchData.getFlow().name());
        });
    }
}