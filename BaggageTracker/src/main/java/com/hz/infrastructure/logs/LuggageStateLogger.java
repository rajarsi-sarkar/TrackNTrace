package com.hz.infrastructure.logs;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.pipeline.JournalInitialPosition;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.Sources;
import com.hazelcast.map.IMap;
import com.hz.core.models.cache.LuggageState;
import com.hz.infrastructure.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LuggageStateLogger {
    private final Job m_job;
    private final IMap<String, LuggageState> m_luggageStateStore;

    @Autowired
    public LuggageStateLogger(HazelcastInstance aHazelcastInstance)
    {
        AppLogger.logInfo("ENTER: LuggageState-Logger");
        m_luggageStateStore = aHazelcastInstance.getMap(Constants.LUGGAGESTATE_STORE);

        m_job = aHazelcastInstance.getJet().newJob(makePipeline(Pipeline.create()), new JobConfig().addClass(this.getClass()));
    }

    private Pipeline makePipeline(Pipeline aPipeline) {

        aPipeline.readFrom(
                        Sources.<String, LuggageState>mapJournal
                                (
                                        Constants.LUGGAGESTATE_STORE,
                                        JournalInitialPosition.START_FROM_CURRENT
                                )
                ).withoutTimestamps()
                .map(e -> e.getValue().toString())
                .writeTo(Sinks.logger());

        return aPipeline;
    }



}
