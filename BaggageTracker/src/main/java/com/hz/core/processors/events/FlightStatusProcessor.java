package com.hz.core.processors.events;//package com.hz.processorstream.pipelines;

import com.dv.framework.channel.kafka.events.consumer.IRecordProcessor;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import com.hz.core.models.cache.LuggageState;
import com.hz.core.models.cache.MishandledLuggage;
import com.hz.core.models.cache.MishandledLuggageList;
import com.hz.events.in.FlightStatusEv;
import com.hz.infrastructure.logs.AppLogger;
import com.hz.infrastructure.util.Constants;
import com.hz.infrastructure.util.KafkaProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class FlightStatusProcessor implements IRecordProcessor<FlightStatusEv> {

    private final Properties m_kafkaProps;

    private final IMap<String, LuggageState> m_luggageStateStore;
    private final IMap<String, MishandledLuggageList> m_mishandledLuggageStore;
    private final Predicate m_isRedStatusP;

    @Autowired
    public FlightStatusProcessor(HazelcastInstance aInstance, KafkaProps aKafkaProps) {
        m_kafkaProps = aKafkaProps.getProps();

        m_luggageStateStore = aInstance.getMap(Constants.LUGGAGESTATE_STORE);
        m_mishandledLuggageStore = aInstance.getMap(Constants.MISHANDLED_LUGGAGE_STORE);

        m_isRedStatusP = Predicates.equal("inRedStatus", true);

        AppLogger.logInfo(this.getClass().getSimpleName() + ": UP!");
    }

    @Override
    public void process(FlightStatusEv flightStatusEv) {

        if(flightStatusEv.getFlightStatus().equalsIgnoreCase(FlightStatusEv.status.DEPARTED.name()))
        {
            Predicate flightIdP = Predicates.equal("flightId", flightStatusEv.getFlightId());
            Predicate q = Predicates.and(flightIdP,m_isRedStatusP);
            Collection<LuggageState> lStates = m_luggageStateStore.values( q );
            MishandledLuggageList list = new MishandledLuggageList();
            for(LuggageState ls : lStates) {
                list.setFlightId(ls.getFlightId());
                Set<String> set1 = Stream.of(ls.getTagIds()).collect(Collectors.toSet());
                if(ls.getProcessedTags()!= null)
                {
                    Set<String> set2 = Stream.of(ls.getProcessedTags()).collect(Collectors.toSet());
                    set1.removeAll(set2);
                }
                if(set1.size()>0)
                {
                    MishandledLuggage mb = new MishandledLuggage(ls.getFlightId(), ls.getTicketId(), set1.toArray(new String[0]));
                    list.addLuggage(mb);
                }
            }
            if(list.getMishandledList().size()>0) {
                for(int i=0; i<list.getMishandledList().size(); i++)
                    System.out.println("FlightStatusProcessor:" + list.getFlightId() + ";" + Arrays.toString(list.getMishandledList().get(i).getTagIds()));

                m_mishandledLuggageStore.put(list.getFlightId(), list);
            }else
            {
                System.out.println("FlightStatusProcessor:" + list.getFlightId() + "; None mishandled");
            }
        }

    }
}
