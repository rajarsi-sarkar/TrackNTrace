package com.hz.producers;

import com.dv.framework.channel.kafka.events.publisher.EventsPublisher;
import com.hz.events.CheckinEv;
import com.hz.infra.util.AppLogger;
import com.hz.infra.util.Constants;

import java.util.Properties;

public class CheckinPublisher
{
    private EventsPublisher<CheckinEv> mPub;

    public CheckinPublisher(Properties aKafkaProps) throws Exception
    {
        mPub = new EventsPublisher<>(Constants.CHECKIN_TOPIC,aKafkaProps);
    }

//    public void publish(String aFileName) throws Exception
//    {
//        InputStream is = this.getClass().getResourceAsStream(aFileName);
//        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//        while(reader.ready())
//        {
//            String line = reader.readLine();
//            String[] splits = line.split(",");
//            String flightId = splits[0];
//            String ticketId = splits[1];
//            String[] tags = splits[2].split(":");
//            String[] routes = splits[3].split(":");
//
//            CheckinEv checkinEv = new CheckinEv(flightId,ticketId,tags,routes);
//            mPub.send(ticketId, checkinEv);
//
//            Thread.sleep(1000);
//        }
//
//
//    }

    public void publish(CheckinEv aCheckInEv) throws Exception
    {
        mPub.send(aCheckInEv.getTicketId(), aCheckInEv);
        AppLogger.logInfo("Posted " + aCheckInEv.toString());

    }

}
