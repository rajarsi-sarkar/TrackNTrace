package com.hz.infrastructure.config;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.pipeline.ServiceFactories;
import com.hazelcast.jet.pipeline.ServiceFactory;
import com.hazelcast.sql.SqlService;
import com.hz.infrastructure.exceptions.ConfigException;
import com.hz.infrastructure.util.ConfigSetters;
import com.hz.infrastructure.util.Constants;
import com.hz.infrastructure.util.KafkaProps;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class Startup
{

    @Bean
    public HazelcastInstance getHazelcastInstance()
    {
        return Hazelcast.newHazelcastInstance(getConfig());
    }

    private Config getConfig()
    {
        Config cfg = new Config();
        cfg.getJetConfig().setEnabled(true);
        cfg.getJetConfig().setResourceUploadEnabled(true);
        cfg.setNetworkConfig(ConfigSetters.getTCPEnabledNetworkConfig());
        cfg.setClusterName("TNT");


        cfg.getMapConfig(Constants.LUGGAGESTATE_STORE).setEventJournalConfig(ConfigSetters.getEnabledEventJournalConfig());
        cfg.getMapConfig(Constants.LUGGAGE_STORE).setEventJournalConfig(ConfigSetters.getEnabledEventJournalConfig());
        cfg.getMapConfig(Constants.MISHANDLED_LUGGAGE_STORE).setEventJournalConfig(ConfigSetters.getEnabledEventJournalConfig());

        cfg.getMapConfig(Constants.LUGGAGESTATE_STORE).setTimeToLiveSeconds(300);
        cfg.getMapConfig(Constants.LUGGAGE_STORE).setTimeToLiveSeconds(300);
        cfg.getMapConfig(Constants.MISHANDLED_LUGGAGE_STORE).setTimeToLiveSeconds(300);
        cfg.getMapConfig(Constants.ANALYTICS_STORE).setTimeToLiveSeconds(300);

        return cfg;
    }

    @Bean
    public KafkaProps getKafkaProperties() throws ConfigException
    {
        Properties props = new Properties();

//        try
//        {
//            InputStream inputStream = this.getClass().getResourceAsStream("/kafka_consumer.properties");
//            props.load(inputStream);
//        }catch(Exception e) {throw new ConfigException(e);}

        props.setProperty("bootstrap.servers","localhost:9092");
        props.setProperty("group.id","TrackNTrace");
        props.setProperty("enable.auto.commit","false");
//        props.setProperty("auto.commit.interval.ms", "1000");
        props.setProperty("auto.offset.reset","latest");
//        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 480000);
//        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 100000);
//        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 900000);
//        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
//        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 600000);

        return new KafkaProps(props);
    }

    @Bean
    public ServiceFactory<?, SqlService> getSQLServiceFactory()
    {
        return ServiceFactories.sharedService(
                ctx -> ctx.hazelcastInstance().getSql()
        );
    }

//    @Bean
//    @Autowired
//    public MishandledLuggageProcessor getMishandledLuggageProcessor(HazelcastInstance aHazelcastInstance, KafkaProps aKafkaProps)
//    {
//        return new MishandledLuggageProcessor(aHazelcastInstance, aKafkaProps);
//    }
//
//    @Bean
//    @Autowired
//    public RFIDProcessor getRFIDProcessor(HazelcastInstance aHazelcastInstance, KafkaProps aKafkaProps)
//    {
//        return new RFIDProcessor(aHazelcastInstance, aKafkaProps);
//    }
}
