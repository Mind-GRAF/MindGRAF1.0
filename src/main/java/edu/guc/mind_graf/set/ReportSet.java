package edu.guc.mind_graf.set;

import java.util.HashSet;
import java.util.Set;
import edu.guc.mind_graf.mgip.reports.Report;

public class ReportSet {
    private Set<Report> reportSet;

    public ReportSet() {
        reportSet = new HashSet<>();
    }

    public void addReport(Report report) {
        reportSet.add(report);
    }

    public void removeReport(Report report) {
        reportSet.remove(report);
    }

    public Report getReport() {
        //gets the first report in the set
        return reportSet.iterator().next();
    }

    public Set<Report> getReportSet() {
        return reportSet;
    }

    public void add(Report report) {
        reportSet.add(report);
    }

}
