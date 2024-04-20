package set;

import java.util.HashSet;
import java.util.Set;
import mgip.Report;

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

    // public SupportSet combineSupport() {
    //     SupportSet combinedSupport = new SupportSet();
    //     for (Report report : ((ReportSet) reportSet).getReports()) {
    //         combinedSupport.
    //     }
    //     combinedSupport = combinedSupport.remove(antecedent);
    //     return combinedSupport;
    // }

}

