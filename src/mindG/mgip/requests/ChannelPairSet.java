package mindG.mgip.requests;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javafx.util.Pair;
import mindG.mgip.matching.Substitutions;
import mindG.network.NodeSet;

public class ChannelPairSet implements Iterable<Pair<Channel, NodeSet>> {
    private static Hashtable<ChannelType, Set<Pair<Channel, NodeSet>>> channelPairs;

    public ChannelPairSet() {
        channelPairs = new Hashtable<ChannelType, Set<Pair<Channel, NodeSet>>>();
        channelPairs.put(ChannelType.MATCHED, new HashSet<Pair<Channel, NodeSet>>());
        channelPairs.put(ChannelType.AntRule, new HashSet<Pair<Channel, NodeSet>>());
        channelPairs.put(ChannelType.RuleCons, new HashSet<Pair<Channel, NodeSet>>());
    }

    public boolean addChannelPair(Pair<Channel, NodeSet> channelPair) {
        Channel currentChannel = channelPair.getKey();
        // String channelContextName = currentChannel.getContextName();
        // int channelAttitudeId = currentChannel.getAttitudeID();
        // Substitutions filterSubs = currentChannel.getFilterSubstitutions();
        // Substitutions switchSubs = currentChannel.getSwitcherSubstitutions();
        ChannelType channelType = currentChannel.getChannelType(currentChannel);
        Set<Pair<Channel, NodeSet>> targetSet = channelPairs.get(channelType);

        boolean added = targetSet.add(channelPair);
        // channelPairs.put(channelType, added);
        return added;
    }

    public Pair<Channel, NodeSet> removeChannelPair(Pair<Channel, NodeSet> channelPair) {
        return channelPair;

    }

    /***
     * Method acting as a filter for quick HashSet filtering applied on channels
     * based on request processing status.
     * 
     * @param processedRequest boolean expressing filter criteria
     * @return newly created ChannelSet
     */
    public ChannelPairSet getFilteredChannels(boolean processedChannel) {
        return null;

    }

    public Collection<Channel> getChannels() {
        return null;

    }

    public boolean contains(Pair<Channel, NodeSet> newChannelPair) {
        return false;
    }

    public Pair<Channel, NodeSet> getChannelPair(Pair<Channel, NodeSet> newChannelPair) {
        return newChannelPair;

    }

    public Collection<Pair<Channel, NodeSet>> getAntRuleChannels() {
        return null;
    }

    public Collection<Pair<Channel, NodeSet>> getRuleConsChannels() {
        return null;
    }

    public Collection<Pair<Channel, NodeSet>> getMatchChannels() {
        return null;
    }

    @Override
    public Iterator<Pair<Channel, NodeSet>> iterator() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'iterator'");
    }

    public static void printContents() {
        for (ChannelType type : channelPairs.keySet()) {
            System.out.println("ChannelType: " + type);
            Set<Pair<Channel, NodeSet>> pairs = channelPairs.get(type);
            for (Pair<Channel, NodeSet> pair : pairs) {
                System.out.println("  Channel: " + pair.getKey().getChannelType(pair.getKey()));
                System.out.println("  NodeSet: " + pair.getValue());
            }
        }
    }

    public static void main(String[] args) {
        Channel matchChannel1 = new MatchChannel();
        Channel channel2 = new AntecedentToRuleChannel();
        Channel channel3Channel = new RuleToConsequentChannel();
        Channel matchChannel2 = new MatchChannel();
        Channel matchChannel3 = new MatchChannel();
        Channel channelAnt = new AntecedentToRuleChannel();
        Channel matchChannel4 = new MatchChannel();
        Channel ruletocons = new RuleToConsequentChannel();
        // System.out.println(matchChannel1.getChannelType(matchChannel1));
        // System.out.println(channel2.getChannelType(channel2));
        // System.out.println(channel3Channel.getChannelType(channel3Channel));
        // System.out.println(matchChannel2.getChannelType(matchChannel2));

        Pair<Channel, NodeSet> match1 = new Pair<Channel, NodeSet>(matchChannel1, new NodeSet());
        // System.out.println(match1.getPair().getKey().getChannelType(match1.getPair().getKey()));
        Pair<Channel, NodeSet> ant = new Pair<Channel, NodeSet>(channel2, new NodeSet());
        // System.out.println(ant.getPair().getKey().getChannelType(ant.getPair().getKey()));

        Pair<Channel, NodeSet> rule = new Pair<Channel, NodeSet>(channel3Channel, new NodeSet());
        // System.out.println(rule.getPair().getKey().getChannelType(rule.getPair().getKey()));

        Pair<Channel, NodeSet> match2 = new Pair<Channel, NodeSet>(matchChannel2, new NodeSet());
        // System.out.println(match2.getPair().getKey().getChannelType(match2.getPair().getKey()));
        Pair<Channel, NodeSet> ruleToCons = new Pair<Channel, NodeSet>(ruletocons, new NodeSet());
        Pair<Channel, NodeSet> match3 = new Pair<Channel, NodeSet>(matchChannel3, new NodeSet());
        Pair<Channel, NodeSet> ant2 = new Pair<Channel, NodeSet>(channelAnt, new NodeSet());
        Pair<Channel, NodeSet> match4 = new Pair<Channel, NodeSet>(matchChannel4, new NodeSet());
        ChannelPairSet channelSet = new ChannelPairSet();

        // Set<ChannelPair> channelSetMatch =
        // channelPairs.getOrDefault(ChannelType.MATCHED, new HashSet<>());
        // Set<ChannelPair> channelSetAnt =
        // channelPairs.getOrDefault(ChannelType.AntRule, new HashSet<>());
        // Set<ChannelPair> channelSetRule =
        // channelPairs.getOrDefault(ChannelType.RuleCons, new HashSet<>());
        channelSet.addChannelPair(match1);
        channelSet.addChannelPair(ant);
        channelSet.addChannelPair(rule);
        channelSet.addChannelPair(match2);
        // channelSetMatch.add(match1);
        // channelSetMatch.add(match2);
        // channelSetAnt.add(ant);
        // channelSetRule.add(rule);
        // channelPairs.put(ChannelType.MATCHED, channelSetMatch);
        // channelPairs.put(ChannelType.RuleCons, channelSetRule);
        // printContents();

        // System.out.println("yayyyyyyyyy");

        channelSet.addChannelPair(ruleToCons);
        channelSet.addChannelPair(ant2);
        channelSet.addChannelPair(match3);
        channelSet.addChannelPair(match4);

        printContents();
    }

}
