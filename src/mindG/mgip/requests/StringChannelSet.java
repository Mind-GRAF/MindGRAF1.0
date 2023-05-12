package mindG.mgip.requests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

public class StringChannelSet implements Iterable<String> {
    private static Hashtable<ChannelType, ArrayList<String>> channelsIDs;

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

    public static ArrayList<String> getChannelsIDS() {
        ArrayList<String> allMergedChannelIDS = new ArrayList<String>();
        Collection<ArrayList<String>> collectionOfSets = channelsIDs.values();
        for (ArrayList<String> set : collectionOfSets)
            allMergedChannelIDS.addAll(set);
        return allMergedChannelIDS;

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

}
