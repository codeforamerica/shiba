package org.codeforamerica.shiba;

public interface MonitoringService {
    void setPage(String pageName, String message);

    void sendEvent(String message);

    void setApplicationId(String applicationId);
}
