package org.codeforamerica.shiba.output.caf;

class HourlyJobIncomeInformation implements JobIncomeInformation {
    Double hourlyWage;
    Double hoursAWeek;
    int iteration;

    public HourlyJobIncomeInformation(String hourlyWage, String hoursAWeek, int iteration) {
        this.hourlyWage = hourlyWage.isEmpty() ? null : Double.valueOf(hourlyWage);
        this.hoursAWeek = hoursAWeek.isEmpty() ? null : Double.valueOf(hoursAWeek);
        this.iteration = iteration;
    }

    @Override
    public boolean isComplete() {
        return hourlyWage != null && hoursAWeek != null;
    }

    @Override
    public Double grossMonthlyIncome() {
        return hourlyWage * hoursAWeek * 4;
    }

    @Override
    public int getIteration() {
        return iteration;
    }
}
