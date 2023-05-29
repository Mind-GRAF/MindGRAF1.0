package inferenceRules;

import java.util.ArrayList;
import java.util.Collection;

import requests.Channel;
import requests.Report;

public class RuleResponse {

	private Report report;
	private Collection<Channel> consequentChannels;

	public RuleResponse() {
		consequentChannels = new ArrayList<Channel>();
	}

	public Report getReport() {
		return report;
	}

	public void setReport(Report report) {
		this.report = report;
	}

	public void addReport(Report report) {
		this.report = report;
	}

	public Collection<Channel> getConsequentChannels() {
		return consequentChannels;
	}

	public void setConsequentChannels(Collection<Channel> consequentChannels) {
		this.consequentChannels = consequentChannels;
	}

	public void addChannel(Channel channel) {
		this.consequentChannels.add(channel);
	}
}