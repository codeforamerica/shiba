package org.codeforamerica.shiba;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ResearchDataRepository {
    private final JdbcTemplate jdbcTemplate;

    public ResearchDataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(ResearchData researchData) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("research");
        MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource("spoken_language", researchData.getSpokenLanguage())
                .addValue("written_language", researchData.getWrittenLanguage())
                .addValue("sex", researchData.getSex())
                .addValue("first_name", researchData.getFirstName())
                .addValue("last_name", researchData.getLastName())
                .addValue("phone_number", researchData.getPhoneNumber())
                .addValue("email", researchData.getEmail())
                .addValue("zip_code", researchData.getZipCode())
                .addValue("application_id", researchData.getApplicationId())
                .addValue("county", researchData.getCounty())
                .addValue("snap", researchData.getSnap())
                .addValue("cash", researchData.getCash())
                .addValue("housing", researchData.getHousing())
                .addValue("emergency", researchData.getEmergency())
                .addValue("phone_opt_in", researchData.getPhoneOptIn())
                .addValue("email_opt_in", researchData.getEmailOptIn())
                .addValue("live_alone", researchData.getLiveAlone())
                .addValue("pay_rent_or_mortgage", researchData.getPayRentOrMortgage())
                .addValue("are_you_working", researchData.getAreYouWorking())
                .addValue("self_employment", researchData.getSelfEmployment())
                .addValue("social_security", researchData.getSocialSecurity())
                .addValue("ssi", researchData.getSSI())
                .addValue("veterans_benefits", researchData.getVeteransBenefits())
                .addValue("unemployment", researchData.getUnemployment())
                .addValue("workers_compensation", researchData.getWorkersCompensation())
                .addValue("retirement", researchData.getRetirement())
                .addValue("child_or_spousal_support", researchData.getChildOrSpousalSupport())
                .addValue("tribal_payments", researchData.getTribalPayments())
                .addValue("entered_ssn", researchData.getEnteredSsn())
                .addValue("date_of_birth", researchData.getDateOfBirth())
                .addValue("money_made_last30_days", researchData.getMoneyMadeLast30Days())
                .addValue("home_expenses_amount", researchData.getHomeExpensesAmount())
                .addValue("household_size", researchData.getHouseholdSize())
                .addValue("flow", researchData.getFlow());
        jdbcInsert.execute(sqlParameterSource);
    }

    public List<ResearchData> findAll() {
        return jdbcTemplate.query("SELECT * FROM research", (resultSet, rowNum) ->
                ResearchData.builder()
                        .spokenLanguage(resultSet.getString("spoken_language"))
                        .build());
    }
}
