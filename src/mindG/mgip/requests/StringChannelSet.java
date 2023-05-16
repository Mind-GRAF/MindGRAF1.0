package mindG.mgip.requests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import mindG.network.Network;
import mindG.network.Node;

public class StringChannelSet implements Iterable<String> {
    public static Hashtable<ChannelType, ArrayList<String>> channelsIDs;

    public StringChannelSet() {
        channelsIDs = new Hashtable<ChannelType, ArrayList<String>>();
        channelsIDs.put(ChannelType.MATCHED, new ArrayList<String>());
        channelsIDs.put(ChannelType.AntRule, new ArrayList<String>());
        channelsIDs.put(ChannelType.RuleCons, new ArrayList<String>());
    }

    public boolean addChannel(Channel currentChannel) {
        String currentChannelID = currentChannel.stringifyChannelID();
        ChannelType channelType = currentChannel.getChannelType();
        ArrayList<String> targetSet = channelsIDs.remove(channelType);
        if (findStringInListcheck(currentChannelID, targetSet)) {
            channelsIDs.put(channelType, targetSet);
            return false;
        }
        boolean added = targetSet.add(currentChannelID);
        channelsIDs.put(channelType, targetSet);
        return added;
    }

    public Channel removeChannelID(Channel channel) {
        String currentChannelID = channel.stringifyChannelID();
        ChannelType channelType = channel.getChannelType();
        ArrayList<String> targetSet = channelsIDs.remove(channelType);
        targetSet.remove(currentChannelID);
        channelsIDs.put(channelType, targetSet);
        return channel;
    }

    public ArrayList<String> getChannelsIDS() {
        ArrayList<String> allMergedChannelIDS = new ArrayList<String>();
        Collection<ArrayList<String>> collectionOfSets = channelsIDs.values();
        for (ArrayList<String> set : collectionOfSets)
            allMergedChannelIDS.addAll(set);
        return allMergedChannelIDS;

    }

    public void printArrayList(ArrayList<String> list) {
        for (String item : list) {
            System.out.println(item);
        }
    }

    public static String findStringInList(String searchStr, ArrayList<String> stringList) {
        for (String str : stringList) {
            if (str.equals(searchStr)) {
                return str;
            }
        }
        // String not found
        return null;
    }

    public static boolean findStringInListcheck(String searchStr, ArrayList<String> stringList) {
        for (String str : stringList) {
            if (str.equals(searchStr)) {
                return true;
            }
        }
        // String not found
        return false;
    }

    public String getChannelID(Channel channel) {
        ChannelType channelType = channel.getChannelType();
        String channelId = channel.stringifyChannelID();
        ArrayList<String> set = channelsIDs.get(channelType);
        return findStringInList(channelId, set);
    }

    public ArrayList<String> getAntRuleChannels() {
        ArrayList<String> channelsList = channelsIDs.get(ChannelType.AntRule);
        return channelsList;
    }

    public ArrayList<String> getRuleConsChannels() {
        ArrayList<String> channelsList = channelsIDs.get(ChannelType.RuleCons);
        return channelsList;
    }

    public ArrayList<String> getMatchChannels() {
        ArrayList<String> channelsList = channelsIDs.get(ChannelType.MATCHED);
        return channelsList;
    }

    @Override
    public Iterator<String> iterator() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'iterator'");
    }

    public static void printHashtableContents(Hashtable<ChannelType, ArrayList<String>> hashtable) {
        Enumeration<ChannelType> keys = hashtable.keys();
        while (keys.hasMoreElements()) {
            ChannelType key = keys.nextElement();
            ArrayList<String> arrayList = hashtable.get(key);
            System.out.println(key + ":");
            for (String str : arrayList) {
                System.out.println("\t" + str);
            }
        }
    }

    public static void main(String[] args) {
        Network network = new Network();

        Node base1 = Network.createNode("N1", "propositionnode"); // Proposition Node
        Channel matchChannel = new AntecedentToRuleChannel(null, null, "reality", 2, base1);
        // Channel channel2 = new AntecedentToRuleChannel(null, null, "fiction", 2,
        // null);
        // Channel channel3Channel = new RuleToConsequentChannel(null, null, "myth", 3,
        // null);
        Channel matchChannel2 = new RuleToConsequentChannel(null, null, "reality", 2, base1);
        // Channel matchChannel3 = new MatchChannel(null, null, "dcbjdsv", 3, 1, null);
        // Channel channelAnt = new AntecedentToRuleChannel(null, null, "bfdbfdb", 2,
        // null);
        // Channel matchChannel4 = new MatchChannel(null, null, "hfghgfh", 3, 1, null);
        // Channel ruletocons = new RuleToConsequentChannel(null, null, "mythology", 3,
        // null);
        // Channel fdegfdg = new RuleToConsequentChannel(null, null, "fdgdfgdfgdf", 5,
        // null);

        String match1 = matchChannel.stringifyChannelID();
        // String ant = channel2.stringifyChannelID();

        // String rule = channel3Channel.stringifyChannelID();

        String match2 = matchChannel2.stringifyChannelID();
        // String ruleToCons = matchChannel3.stringifyChannelID();
        // String match3 = channelAnt.stringifyChannelID();
        // String ant2 = matchChannel4.stringifyChannelID();
        // String match4 = ruletocons.stringifyChannelID();
        StringChannelSet stringSet = new StringChannelSet();

        stringSet.addChannel(matchChannel);
        // stringSet.addChannel(channel2);
        // stringSet.addChannel(channel3Channel);
        stringSet.addChannel(matchChannel2);

        // stringSet.addChannel(matchChannel3);
        // stringSet.addChannel(channelAnt);
        // stringSet.addChannel(matchChannel4);
        // stringSet.addChannel(ruletocons);
        printHashtableContents(channelsIDs);

        System.out.println(stringSet.getChannelsIDS());
        // System.out.println(stringSet.getChannelID(fdegfdg));
    }

}
