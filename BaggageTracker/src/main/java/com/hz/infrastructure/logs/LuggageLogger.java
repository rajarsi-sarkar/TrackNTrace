//package com.hz.infrastructure.logs;
//
//import com.hazelcast.core.HazelcastInstance;
//import com.hazelcast.jet.Job;
//import com.hazelcast.jet.config.JobConfig;
//import com.hazelcast.jet.pipeline.JournalInitialPosition;
//import com.hazelcast.jet.pipeline.Pipeline;
//import com.hazelcast.jet.pipeline.Sinks;
//import com.hazelcast.jet.pipeline.Sources;
//import com.hazelcast.map.IMap;
//import com.hz.core.models.cache.Luggage;
//import com.hz.infrastructure.util.Constants;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Component
//public class LuggageLogger {
//    private final Job m_job;
//    private final IMap<String, Luggage> m_luggageStore;
//
//    @Autowired
//    public LuggageLogger(HazelcastInstance aHazelcastInstance)
//    {
//        AppLogger.logInfo("ENTER: Luggage-Logger");
//        m_luggageStore = aHazelcastInstance.getMap(Constants.LUGGAGE_STORE);
//
//        m_job = aHazelcastInstance.getJet().newJob(makePipeline(Pipeline.create()), new JobConfig().addClass(this.getClass()));
//    }
//
//    private Pipeline makePipeline(Pipeline aPipeline) {
//
//        aPipeline.readFrom(
//                        Sources.<String, Luggage>mapJournal
//                                (
//                                        Constants.LUGGAGE_STORE,
//                                        JournalInitialPosition.START_FROM_CURRENT
//                                )
//                ).withoutTimestamps()
//                .map(e -> e.getValue().toString())
//                .writeTo(Sinks.logger());
//
//        return aPipeline;
//    }
//
//
//
//}
