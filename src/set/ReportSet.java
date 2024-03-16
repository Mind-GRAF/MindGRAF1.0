package set;

import java.util.HashSet;
import java.util.Set;
import mgip.Report;

public class ReportSet {
    private Set<Report> reports;

    public ReportSet() {
        reports = new HashSet<>();
    }

    public void addReport(Report report) {
        reports.add(report);
    }

    public void removeReport(Report report) {
        reports.remove(report);
    }

    public Set<Report> getReports() {
        return reports;
    }

}

