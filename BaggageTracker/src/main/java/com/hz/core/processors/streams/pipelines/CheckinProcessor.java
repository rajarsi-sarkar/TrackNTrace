package com.hz.core.processors.streams.pipelines;//package com.hz.processorstream.pipelines;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.Traversers;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.kafka.KafkaSources;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.StreamStage;
import com.hazelcast.map.IMap;
import com.hz.core.models.cache.Luggage;
import com.hz.core.models.cache.LuggageState;
import com.hz.events.in.CheckinEv;
import com.hz.infrastructure.logs.AppLogger;
import com.hz.infrastructure.util.Constants;
import com.hz.infrastructure.util.KafkaProps;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Properties;

@Component
public class CheckinProcessor {

    private final Properties m_kafkaProps;

    private final Job m_job;

    private final IMap<String, LuggageState> m_luggageStateStore;
    private final IMap<String, Luggage> m_luggageStore;
    private static final ObjectMapper objectMapper = new ObjectMapper();


    @Autowired
    public CheckinProcessor(HazelcastInstance aInstance, KafkaProps aKafkaProps) {
        m_kafkaProps = aKafkaProps.getProps();
        m_kafkaProps.setProperty("key.deserializer", StringDeserializer.class.getCanonicalName());
        m_kafkaProps.setProperty("value.deserializer", ByteArrayDeserializer.class.getCanonicalName());

        m_luggageStateStore = aInstance.getMap(Constants.LUGGAGESTATE_STORE);
        m_luggageStore = aInstance.getMap(Constants.LUGGAGE_STORE);

        m_job = aInstance.getJet().newJob(makePipeline(Pipeline.create()), new JobConfig().addClass(this.getClass()).addClass(KafkaSources.class));
        AppLogger.logInfo(this.getClass().getSimpleName() + ": UP!");
    }

    public void join()
    {
        m_job.join();
    }

    private Pipeline makePipeline(Pipeline aPipeline) {

        StreamStage<CheckinEv> checkInEvs =
                aPipeline
                .readFrom(KafkaSources.<String, byte[]>kafka(m_kafkaProps, Constants.CHECKIN_TOPIC))
                .withoutTimestamps()
                .map(Map.Entry::getValue)
                .map(bytes -> objectMapper.readValue(bytes, CheckinEv.class))
                ;

        StreamStage<Luggage> luggages = checkInEvs
            .flatMap(checkInEv -> Traversers
                    .traverseArray(checkInEv.getTagIds())
                    .map(tagId ->
                        new Luggage(
                            tagId,
                            checkInEv.getTicketId(),
                            checkInEv.getFlightId(),
                            null,
                            System.currentTimeMillis()
                        )
                    )
            );

        luggages.writeTo(
            Sinks.mapWithUpdating
            (
                m_luggageStore,
                Luggage::getTagId,
                (cachedValue, luggage) -> luggage
            )
        );

        checkInEvs.writeTo(
                Sinks.mapWithUpdating(
                        m_luggageStateStore,
                        CheckinEv::getTicketId,
                        (cachedValue, checkInEv) -> {
                            LuggageState l = new LuggageState(
                                    checkInEv.getFlightId(),
                                    checkInEv.getTicketId(),
                                    checkInEv.getRoutes(),
                                    checkInEv.getTagIds(),
                                    null,
                                    "CHECK-IN"
                            );
                            return l;
                        }
                )
        );

//        luggages.map(ev -> {
//            return
//
//                            "Rajarsi CheckedIn: " +
//                                    ev.getTicketId() + "," +
//                                    ev.getTagId() + "," +
//                                    ev.getFlightId()
//                    ;
//        })
//                .writeTo(Sinks.logger());

//        checkInEvs.writeTo(Sinks.logger());

        return aPipeline;
    }
}
