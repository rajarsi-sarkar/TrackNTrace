package com.hz.core.processors.streams.pipelines;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.kafka.KafkaSinks;
import com.hazelcast.jet.kafka.KafkaSources;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.StreamStage;
import com.hazelcast.map.IMap;
import com.hz.core.models.cache.Luggage;
import com.hz.core.models.cache.LuggageState;
import com.hz.core.models.state.InstanceLuggageState;
import com.hz.events.in.RFIDStream;
import com.hz.events.internal.StatusEv;
import com.hz.infrastructure.logs.AppLogger;
import com.hz.infrastructure.util.Constants;
import com.hz.infrastructure.util.KafkaProps;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

@Component
public class RFIDProcessor {

    private final Properties m_subKafkaProps;
    private final Properties m_pubKafkaProps;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Job m_job;

    private final IMap<String, LuggageState> m_luggageStateStore;
    private final IMap<String, Luggage> m_luggageStore;

    @Autowired
    public RFIDProcessor(HazelcastInstance aInstance, KafkaProps aKafkaProps)
    {
        Properties kafkaProps = aKafkaProps.getProps();
        m_subKafkaProps = (Properties)kafkaProps.clone();
        m_subKafkaProps.setProperty("key.deserializer", StringDeserializer.class.getCanonicalName());
        m_subKafkaProps.setProperty("value.deserializer", ByteArrayDeserializer.class.getCanonicalName());

        m_pubKafkaProps = (Properties)kafkaProps.clone();
        m_pubKafkaProps.setProperty("key.serializer", StringSerializer.class.getCanonicalName());
        m_pubKafkaProps.setProperty("value.serializer", StringSerializer.class.getCanonicalName());

        m_luggageStateStore = aInstance.getMap(Constants.LUGGAGESTATE_STORE);
        m_luggageStore = aInstance.getMap(Constants.LUGGAGE_STORE);

        m_job = aInstance.getJet().newJob(makePipeline(Pipeline.create()), new JobConfig().addClass(this.getClass()).addClass(KafkaSources.class));
        AppLogger.logInfo(this.getClass().getSimpleName() + ": UP!");
    }

    private Pipeline makePipeline(Pipeline aPipeline) {

        StreamStage<RFIDStream> rfids = aPipeline
            .readFrom(KafkaSources.<String, byte[]>kafka(m_subKafkaProps, Constants.RFID_TOPIC))
            .withIngestionTimestamps()
            .map(Map.Entry::getValue)
            .map(bytes -> {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(bytes, RFIDStream.class);
            })

            .groupingKey(RFIDStream::getTagId)
            .mapUsingIMap( //enrich with ticketId
                m_luggageStore,
                (rfidEv, luggage) -> {
                    rfidEv.setTicketId(luggage.getTicketId());
                    rfidEv.setFlightId(luggage.getFlightId());
                    return rfidEv;
                }
            )
                .mapUsingIMap( // enrich with total tag count
                m_luggageStateStore,
                RFIDStream::getTicketId,
                (rfidEv, luggageState) -> {
                    rfidEv.setTagCount(luggageState.getTagCount());
                    return rfidEv;
                }
            );

        rfids
                .groupingKey(RFIDStream::getTicketId)
                .mapStateful( // stateful processing. (# of tags = tag count)
//                    MINUTES.toMillis(2),    // timeout
                    InstanceLuggageState::new,      // state type
                    RFIDProcessor::updateState
//                    RFIDProcessor::onTimeout
                )
                .writeTo
                        (
                                Sinks.mapWithUpdating
                                        (
                                                m_luggageStateStore,
                                                StatusEv::getTicketId,
                                                (aLuggageState, aStatusEv) -> onStatusReceived(aStatusEv, aLuggageState)
                                        )
                        )
        ;

        rfids
                .map
                        (
                                rfidEv -> Map.entry(rfidEv.getFlightId(), objectMapper.writeValueAsString(rfidEv))
                        )
                .writeTo
                        (
                                KafkaSinks.kafka
                                        (
                                                m_pubKafkaProps,
                                                Constants.ENRICHED_RFID_TOPIC_OUT
                                        )
                        );

        return aPipeline;
    }

    public static StatusEv updateState(InstanceLuggageState aState, String aTicketId, RFIDStream aRfidEv)
    {
        if(aState.getTicketId()==null)
            aState.setTicketId(aTicketId);

        aState.getTagList().add(aRfidEv.getTagId());

        if(aState.getTagList().size() == aRfidEv.getTagCount())
        {
            return new StatusEv(aTicketId, StatusEv.COMPLETE, new ArrayList<>(aState.getTagList()));
        }
        else return new StatusEv(aTicketId, StatusEv.PARTIAL, new ArrayList<>(aState.getTagList()));
    }

//    public static StatusEv onTimeout(InstanceLuggageState aState, String aTicketId, long aCurrentWatermark)
//    {
//        return new StatusEv(aTicketId, StatusEv.TIMEOUT, aState.getTagList());
//    }

    public static LuggageState onStatusReceived(StatusEv aStatusEv, LuggageState aLuggageState)
    {
        if(aStatusEv.getStatus().equalsIgnoreCase(StatusEv.COMPLETE)) {
            aLuggageState.setInRedStatus(false);
            aLuggageState.cleanUpProcessedTags();
        }else if(aStatusEv.getStatus().equalsIgnoreCase(StatusEv.PARTIAL))
        {
            aLuggageState.setProcessedTags(aStatusEv.getTagList().toArray(new String[0]));
        }
//        else if(aStatusEv.getStatus().equalsIgnoreCase(StatusEv.TIMEOUT))
//        {
//            aLuggageState.setProcessedTags(aStatusEv.getTagList().toArray(new String[0]));
//            System.out.println("Rajarsi: Timeout");
//        }
        return aLuggageState;
    }
}
