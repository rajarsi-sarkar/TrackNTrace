package com.hz.infrastructure.util;

import com.hazelcast.config.EventJournalConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;

public class ConfigSetters {

    public static EventJournalConfig getEnabledEventJournalConfig()
    {
        EventJournalConfig ejc = new EventJournalConfig()
                .setEnabled(true)
                .setCapacity(5000)
                .setTimeToLiveSeconds(20);

        return ejc;
//            cfg.getMapConfig(TWEETS).setEventJournalConfig(ejmc);
    }

    public static NetworkConfig getTCPEnabledNetworkConfig()
    {
        NetworkConfig nc = new NetworkConfig();
        nc.setPublicAddress("127.0.0.1").setPortAutoIncrement(true);

        JoinConfig join = nc.getJoin();
        join.getMulticastConfig().setEnabled(false);
        join.getTcpIpConfig().setEnabled(true)
                .addMember("127.0.0.1");

        return nc;
    }

}
