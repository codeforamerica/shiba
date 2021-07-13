package org.codeforamerica.shiba;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.mnit.MnitCountyInformation;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.emails.MailGunEmailClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
public class ResubmissionService {

    private final ApplicationRepository applicationRepository;
    private final MailGunEmailClient emailClient;
    private final CountyMap<MnitCountyInformation> countyMap;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public ResubmissionService(ApplicationRepository applicationRepository, MailGunEmailClient emailClient, CountyMap<MnitCountyInformation> countyMap){
        this.applicationRepository=applicationRepository;
        this.emailClient=emailClient;
        this.countyMap=countyMap;
    }

    @Scheduled(fixedDelayString = "${resubmission.interval.milliseconds}")
    public void resubmitFailedApplications(){
        log.info("Now running resubmitted failed application scheduler");
        Map<Document, List<String>> failedApplications = applicationRepository.getFailedSubmissions();
        failedApplications.forEach((k,v) -> v.forEach( id -> {
            log.info("Now resubmitting failed application id " + id);
            Application a = applicationRepository.find(id);
            emailClient.resubmitFailedEmail(countyMap.get(a.getCounty()).getEmail(), k,a, Locale.ENGLISH);
        }));
    }
}
