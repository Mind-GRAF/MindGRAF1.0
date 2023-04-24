package mindG.mgip;

import java.util.HashSet;
import java.util.Iterator;

import mindG.mgip.InferenceTypes;
import mindG.mgip.Report;

public class ReportSet implements Iterable<Report> {
    private HashSet<Report> reports;

    public ReportSet() {
        reports = new HashSet<Report>();
    }

    public void addReport(Report rport) {
        reports.add(rport);
    }

    public boolean contains(Report report) {
        return reports.contains(report);
    }

    public void clear() {
        reports = new HashSet<Report>();
    }

    public boolean removeReport(Report report) {
        return reports.remove(report);
    }

    public boolean isEmpty() {
        return reports.size() == 0;
    }

    public boolean hasForwardReports() {
        return false;

    }

    @Override
    public Iterator<Report> iterator() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'iterator'");
    }
}
