package com.hz.infrastructure.channels.kafka.consumers;//package com.hz.consumers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hz.core.processors.events.FlightStatusProcessor;
import com.hz.events.in.FlightStatusEv;
import com.hz.infrastructure.exceptions.SourceException;
import com.hz.infrastructure.util.Constants;
import com.hz.infrastructure.util.KafkaProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FlightStatusConsumer {

    @Autowired
    public FlightStatusConsumer(FlightStatusProcessor aFlightStatusProcessor, KafkaProps aKafkaProperties) throws SourceException {
//        Properties props = (Properties)aKafkaProperties.getProps().clone();
        new GenericEventsConsumer<FlightStatusEv>(Constants.FLIGHTSTATUS_TOPIC, aKafkaProperties.getProps(), new TypeReference<FlightStatusEv>() {}, aFlightStatusProcessor);
    }
}
