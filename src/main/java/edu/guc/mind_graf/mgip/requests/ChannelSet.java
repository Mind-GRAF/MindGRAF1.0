package edu.guc.mind_graf.mgip.requests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

public class ChannelSet implements Iterable<Channel> {
    public Hashtable<ChannelType, Hashtable<String, Channel>> channels;

    public ChannelSet() {
        channels = new Hashtable<ChannelType, Hashtable<String, Channel>>();
        channels.put(ChannelType.Matched, new Hashtable<String, Channel>());
        channels.put(ChannelType.RuleCons, new Hashtable<String, Channel>());
        channels.put(ChannelType.AntRule, new Hashtable<String, Channel>());
        channels.put(ChannelType.Act, new Hashtable<String, Channel>());
        channels.put(ChannelType.IfRule, new Hashtable<String, Channel>());
        channels.put(ChannelType.WhenRule, new Hashtable<String, Channel>());
    }

    public Channel addChannel(Channel channel) {
        ChannelType channelType = channel.getChannelType();
        Hashtable<String, Channel> targetSet = channels.remove(channelType);
        String channelId = channel.stringifyChannelID();
        Channel added = targetSet.put(channelId, channel);
        channels.put(channelType, targetSet);
        return added;
    }

    @Override
    public Iterator<Channel> iterator() {
        /*
         * add ruletoantecedent channels fel akher 3alashan a serve el reports el gaya
         * menhom ba3d ma aserve el reports el tanya men channels tanya -- RuleNode
         * proceessReport
         */
        Collection<Channel> toBeAddedLater = new ArrayList<Channel>();
        Collection<Channel> allMergedChannels = new ArrayList<Channel>();
        Collection<Hashtable<String, Channel>> collectionOfSets = channels.values();
        for (Hashtable<String, Channel> set : collectionOfSets) {
            for (Channel channel : set.values()) {
                boolean ruleAntChannel = channel instanceof AntecedentToRuleChannel;
                if (ruleAntChannel)
                    toBeAddedLater.add(channel);
                else
                    allMergedChannels.add(channel);
            }
        }
        for (Channel ruleAntChannel : toBeAddedLater) {
            allMergedChannels.add(ruleAntChannel);
        }
        return allMergedChannels.iterator();
    }

    public Collection<Channel> getChannels() {
        Collection<Channel> allMergedChannels = new ArrayList<Channel>();
        Collection<Hashtable<String, Channel>> collectionOfSets = channels.values();
        for (Hashtable<String, Channel> set : collectionOfSets)
            allMergedChannels.addAll(set.values());
        return allMergedChannels;
    }

    public Collection<Channel> getAntRuleChannels() {
        Hashtable<String, Channel> channelsHash = channels.get(ChannelType.AntRule);
        return channelsHash.values();
    }

    public Collection<Channel> getRuleConsChannels() {
        Hashtable<String, Channel> channelsHash = channels.get(ChannelType.RuleCons);
        return channelsHash.values();
    }

    public Collection<Channel> getMatchChannels() {
        Hashtable<String, Channel> channelsHash = channels.get(ChannelType.Matched);
        return channelsHash.values();
    }

    public boolean contains(Channel newChannel) {
        return getChannel(newChannel) != null;
    }

    public Channel getChannel(Channel newChannel) {
        ChannelType channelType = newChannel.getChannelType();
        String channelId = newChannel.stringifyChannelID();
        Hashtable<String, Channel> set = channels.get(channelType);
        return set.get(channelId);
    }

    public Channel getChannel(String newChannel) {
        Collection<Channel> mergedChannels = getChannels();
        for (Channel channel : mergedChannels) {
            if (newChannel == null) {
                return null;
            } else if (newChannel.equals(channel.stringifyChannelID())) {
                return channel;
            }
        }
        return null;
    }

    public void printChannels(Collection<Channel> channelCollection) {
        for (Channel channel : channelCollection) {
            System.out.println(channel.stringifyChannelID());
        }
    }

}
