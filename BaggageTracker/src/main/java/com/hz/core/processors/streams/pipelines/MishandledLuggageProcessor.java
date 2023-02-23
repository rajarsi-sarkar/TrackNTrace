package com.hz.core.processors.streams.pipelines;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.Traversers;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.datamodel.Tuple2;
import com.hazelcast.jet.kafka.KafkaSinks;
import com.hazelcast.jet.kafka.KafkaSources;
import com.hazelcast.jet.pipeline.*;
import com.hazelcast.map.IMap;
import com.hz.core.models.cache.MishandledLuggage;
import com.hz.core.models.cache.MishandledLuggageList;
import com.hz.events.in.DisruptedEv;
import com.hz.events.out.OutEv;
import com.hz.infrastructure.logs.AppLogger;
import com.hz.infrastructure.util.Constants;
import com.hz.infrastructure.util.KafkaProps;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;

@Component
public class MishandledLuggageProcessor {

    private final Properties m_subKafkaProps;
    private final Properties m_pubKafkaProps;

    private final Job m_job;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final IMap<String, DisruptedEv> m_analyticsStore;

    @Autowired

    public MishandledLuggageProcessor(HazelcastInstance aInstance, KafkaProps aKafkaProps)
    {
        Properties kafkaProps = aKafkaProps.getProps();
        m_subKafkaProps = (Properties)kafkaProps.clone();
        m_subKafkaProps.setProperty("key.deserializer", StringDeserializer.class.getCanonicalName());
        m_subKafkaProps.setProperty("value.deserializer", ByteArrayDeserializer.class.getCanonicalName());

        m_pubKafkaProps = (Properties)kafkaProps.clone();
        m_pubKafkaProps.setProperty("key.serializer", StringSerializer.class.getCanonicalName());
        m_pubKafkaProps.setProperty("value.serializer", StringSerializer.class.getCanonicalName());

        m_analyticsStore = aInstance.getMap(Constants.ANALYTICS_STORE);

        m_job = aInstance.getJet().newJob(makePipeline(Pipeline.create()), new JobConfig().addClass(this.getClass()));
        AppLogger.logInfo(this.getClass().getSimpleName() + ": UP!");
    }

    public void join()
    {
        m_job.join();
    }

    private Pipeline makePipeline(Pipeline aPipeline) {

        StreamStage<DisruptedEv> disruptedEvs =
                aPipeline
                        .readFrom(KafkaSources.<String, byte[]>kafka(m_subKafkaProps, Constants.DISRUPTOR_TOPIC))
                        .withoutTimestamps()
                        .map(Map.Entry::getValue)
                        .map(bytes -> {
                            return objectMapper.readValue(bytes, DisruptedEv.class);
                        })
                ;

        StreamStage<MishandledLuggageList> mishandledLuggageEntries =
                aPipeline.
                        readFrom
                                (
                                        Sources.<String, MishandledLuggageList>mapJournal
                                                (
                                                        Constants.MISHANDLED_LUGGAGE_STORE,
                                                        JournalInitialPosition.START_FROM_CURRENT
                                                )
                                ).withoutTimestamps()
                        .map(Map.Entry::getValue)
                        ;

        disruptedEvs.map(e -> {
            System.out.println("MishandledLuggageProcessor: DisruptedTagIds:" + e.getFlightId() + ";" + Arrays.toString(e.getTagIds().toArray(new String[0])));
            return e;
        })
        .writeTo(
                Sinks.mapWithUpdating(
                        m_analyticsStore,
                        DisruptedEv::getFlightId,
                        (cached, newEv) -> {
                            if(cached == null)
                                return newEv;
                            analyze(cached, newEv);
                            return cached;
                        })
        );

//        StreamStage<Envelope> envelope = mishandledLuggageEntries.map(e ->  new Envelope(null,null, null));

        StreamStage<Envelope> envelope = mishandledLuggageEntries
                .mapUsingIMap( // enrich
                        m_analyticsStore,
                        MishandledLuggageList::getFlightId,
                        Tuple2::tuple2 // => Tuple2.tuple2(sensorDTO, controlDTO)
                )
                .map(
                        tuple2 -> {
                            DisruptedEv d = tuple2.f1();
                            MishandledLuggageList m = tuple2.f0();

                            if(d == null)
                            {
                                List<String> tagList = new ArrayList<>();
                                for(MishandledLuggage ml: m.getMishandledList())
                                {
                                    tagList.addAll(Arrays.asList(ml.getTagIds()));
                                }

                                DisruptedEv dev = new DisruptedEv(
                                        m.getFlightId(),
                                        tagList,
                                        true
                                );
                                return new Envelope(
                                        dev,
                                        m,
                                        null
                                );
                            }else {
                                List<String> removedTags = new ArrayList<>(d.getTagIds());
                                List<MishandledLuggage> mls = m.getMishandledList();
                                for (MishandledLuggage ml: mls)
                                {
                                    for(String tagId : ml.getTagIds())
                                    {
                                        removedTags.remove(tagId);
                                    }
                                }
                                if(removedTags.size()==0)
                                {
                                    return new Envelope(d,m,"MATCHED");
                                }else {
                                    m.setMissedTags(removedTags.toArray(new String[0]));
                                    return new Envelope(d,m,"NOT-MATCHED");
                                }
                            }
                        }
                )
        ;

        envelope.filter(e-> e.disruptedEv.isInsert())
                .map(e -> e.getDisruptedEv())
                        .writeTo(
                                Sinks.mapWithUpdating
                                        (
                                                m_analyticsStore,
                                                DisruptedEv::getFlightId,
                                                (disruptedEv, d) -> d
                                        )
                        );


        envelope
                .filter(e-> !(e.disruptedEv.isInsert()))
                .filter(e -> e.getStatus().equalsIgnoreCase("MATCHED"))
                .flatMap(e -> Traversers
                        .traverseArray(e.getMishandledLuggages().getMishandledList().toArray(new MishandledLuggage[0]))
                )
                .map(m -> new OutEv(m.getFlightId(), m.getTagIds(), "MATCHED"))
                .map(e -> {
                    System.out.println("MishandledLuggageProcessor: " + e.toString());
                    return e;
                })
                .map(outEv -> {
                    Map.Entry<String, String> e = Map.entry(outEv.getFlightId(), objectMapper.writeValueAsString(outEv));
                    return e;
                })
                .writeTo(KafkaSinks.kafka(m_pubKafkaProps, Constants.NOTIFICATION_TOPIC));

        envelope
                .filter(e-> !(e.disruptedEv.isInsert()))
                .filter(e -> !e.getStatus().equalsIgnoreCase("MATCHED"))
                .map(e -> new OutEv(e.getMishandledLuggages().getFlightId(), e.getMishandledLuggages().getMissedTags(), "NOT-MATCHED"))
                .map(e -> {
                    System.out.println("MishandledLuggageProcessor:NOT-MATCHED: " + e.toString());
                    return e;
                })
//                .writeTo(Sinks.logger());
                .map(outEv -> {
                    Map.Entry<String, String> e = Map.entry(outEv.getFlightId(), objectMapper.writeValueAsString(outEv));
                    return e;
                })
                .writeTo(KafkaSinks.kafka(m_pubKafkaProps, Constants.NOTIFICATION_TOPIC));

        return aPipeline;
    }

    private static void analyze(DisruptedEv aCached, DisruptedEv aNew)
    {
        if(aCached.getFlightId().equalsIgnoreCase(aNew.getFlightId()))
        {
            aCached.getTagIds().removeAll(aNew.getTagIds());
            if(aCached.getTagIds().size()==0)
                System.out.println("MishandledLuggageProcessor: analyse(): MATCHED ");
            else
                System.out.println("MishandledLuggageProcessor: analyse(): NOT-MATCHED ");
        }
    }

    @Data
    @AllArgsConstructor
    private static class Envelope implements Serializable {
        DisruptedEv disruptedEv;
        MishandledLuggageList mishandledLuggages;
        String status;
    }
}
