package com.hz.controller;

import com.hz.events.CheckinEv;
import com.hz.events.FlightStatusEv;
import com.hz.events.RFIDStream;
import com.hz.producers.CheckinPublisher;
import com.hz.producers.FlightStatusPublisher;
import com.hz.producers.RFIDPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class AirlineWorkflow_copy
{
    private String mFlightId;
    private ArrayList<String> mluggages = new ArrayList<>();
    Properties mKafkaProps;

    public AirlineWorkflow_copy(String aFlightId, Properties aKafkaProps) throws Exception
    {
        mKafkaProps = aKafkaProps;
        mFlightId = aFlightId;
        checkIn();
//        pause(2000);
//        flightArrives();
//        pause(2000);
//        flightDeparts();
//        flightInAir();
//        transitStop();
//        flightDeparts();
    }

    private void checkIn() throws Exception
    {
        CheckinPublisher checkinPublisher = new CheckinPublisher(mKafkaProps);
        Random checkInRandom = new Random(1000000);
        Random tagsRandom = new Random();
        int low = 1;
        int high = 3;

        for (int i=0; i<=100; i++)
        {
            String ticketId = String.valueOf(Math.abs(checkInRandom.nextInt()));
            String tagPrefix = String.valueOf(ticketId.hashCode());

            List<String> tags = new ArrayList<>();
            int tagCnt = tagsRandom.nextInt(high-low) + low;
            for(int j=0; j<tagCnt; j++)
            {
                tags.add("T" + tagPrefix + j );
            }

            CheckinEv checkinEv = new CheckinEv(
                    mFlightId,
                    ticketId,
                    tags.toArray(new String[0]),
                    null
            );

            mluggages.addAll(tags);

            checkinPublisher.publish(checkinEv);
        }
    }

    private void flightArrives() throws Exception
    {
        RFIDPublisher publisher = new RFIDPublisher(mKafkaProps);

        for(String tag : mluggages)
        {
            RFIDStream rfid = new RFIDStream(
                    tag,
                    RFIDStream.status.LOAD.name(),
                    null,
                    System.currentTimeMillis()
            );
            publisher.publish(rfid);
        }
    }

    private void flightDeparts() throws Exception
    {
        FlightStatusPublisher pub = new FlightStatusPublisher(mKafkaProps);
        FlightStatusEv flightStatusEv = new FlightStatusEv(mFlightId, FlightStatusEv.status.DEPARTED.name(),null);
        pub.publish(flightStatusEv);

    }

    private void flightInAir()
    {
        pause(30000);
    }

    private void pause(long interval)
    {
        try{
            Thread.sleep(interval);
        }catch(Exception e){}
    }
}
