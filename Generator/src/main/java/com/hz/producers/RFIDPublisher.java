package com.hz.producers;

import com.dv.framework.channel.kafka.events.publisher.EventsPublisher;
import com.hz.events.RFIDStream;
import com.hz.infra.util.AppLogger;
import com.hz.infra.util.Constants;

import java.util.List;
import java.util.Properties;

public class RFIDPublisher
{
    private static String mTagPrefix = "IA-";
    private List<Integer> mTagNums;
    private EventsPublisher<RFIDStream> mPub;

    public RFIDPublisher(Properties aKafkaProps) throws Exception
    {
        mPub = new EventsPublisher<>(Constants.RFID_TOPIC,aKafkaProps);
    }

    public void publish(RFIDStream aRFIDStream) throws Exception
    {
        mPub.send(aRFIDStream.getTagId(), aRFIDStream);
        AppLogger.logInfo("Posted " + aRFIDStream.toString());
    }
}
