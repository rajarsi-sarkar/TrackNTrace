package com.hz.producers;

import com.dv.framework.channel.kafka.events.publisher.EventsPublisher;
import com.hz.events.FlightStatusEv;
import com.hz.infra.util.AppLogger;
import com.hz.infra.util.Constants;

import java.util.Properties;

public class FlightStatusPublisher
{
    private EventsPublisher<FlightStatusEv> mPub;

    public FlightStatusPublisher(Properties aKafkaProps) throws Exception {
        mPub = new EventsPublisher<>(Constants.FLIGHTSTATUS_TOPIC,aKafkaProps);
    }

    public void publish(FlightStatusEv aFlightStatusEv) throws Exception
    {
        mPub.send(aFlightStatusEv.getFlightId(), aFlightStatusEv);
        AppLogger.logInfo("Posted " + aFlightStatusEv.toString());
    }

}
