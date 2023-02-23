package com.hz.producers;

import com.dv.framework.channel.kafka.events.publisher.EventsPublisher;
import com.hz.events.DisruptedEv;
import com.hz.infra.util.AppLogger;
import com.hz.infra.util.Constants;

import java.util.Properties;

public class DisruptorPublisher
{
    private EventsPublisher<DisruptedEv> mPub;

    public DisruptorPublisher(Properties aKafkaProps) throws Exception
    {
        mPub = new EventsPublisher<>(Constants.DISRUPTOR_TOPIC,aKafkaProps);
    }

    public void publish(DisruptedEv aDisruptedEv) throws Exception
    {
        mPub.send(aDisruptedEv.getFlightId(), aDisruptedEv);
        AppLogger.logInfo("Posted " + aDisruptedEv.toString());
    }

}
