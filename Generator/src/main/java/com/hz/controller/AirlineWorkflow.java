package com.hz.controller;

import com.hz.events.CheckinEv;
import com.hz.events.DisruptedEv;
import com.hz.events.FlightStatusEv;
import com.hz.events.RFIDStream;
import com.hz.producers.CheckinPublisher;
import com.hz.producers.DisruptorPublisher;
import com.hz.producers.FlightStatusPublisher;
import com.hz.producers.RFIDPublisher;

import java.util.*;

public class AirlineWorkflow
{
    private String m_flightId;
    private int m_checkIns;
    private List<String> mluggageTags;
    Properties mKafkaProps;
    CheckinPublisher checkinPublisher;
    RFIDPublisher publisher;
    FlightStatusPublisher pub;
    DisruptorPublisher disruptorPublisher;

    public AirlineWorkflow(String aAirline, int aCheckins, Properties aKafkaProps) throws Exception
    {
        mKafkaProps = aKafkaProps;
        m_flightId = aAirline;
        m_checkIns = aCheckins;
        mluggageTags = new ArrayList<>();

        checkinPublisher = new CheckinPublisher(mKafkaProps);
        publisher = new RFIDPublisher(mKafkaProps);
        pub = new FlightStatusPublisher(mKafkaProps);
        disruptorPublisher = new DisruptorPublisher(mKafkaProps);

    }

    public void start() throws Exception {
        pause(5000);
        checkIn();
        pause(5000);
        disrupt2();
        pause(1000);
        loadLuggages();
        pause(5000);
        flightDeparts();
    }

    private void checkIn() throws Exception
    {
        Random tagsRandom = new Random();
        int low = 1;
        int high = 3;

        for (int i=0; i<m_checkIns; i++)
        {
            String ticketId = String.valueOf((m_flightId + String.valueOf(i)).hashCode());
            String tagPrefix = m_flightId + ticketId ;

            List<String> tags = new ArrayList<>();
            int tagCnt = tagsRandom.nextInt(high-low) + low;
            for(int j=0; j<tagCnt; j++)
            {
                tags.add(tagPrefix + j );
            }

            CheckinEv checkinEv = new CheckinEv(
                    m_flightId,
                    ticketId,
                    tags.toArray(new String[0]),
                    null
            );

            mluggageTags.addAll(tags);

            checkinPublisher.publish(checkinEv);
        }
    }

    private void loadLuggages() throws Exception
    {
        for(String tag : mluggageTags)
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
        FlightStatusEv flightStatusEv = new FlightStatusEv(m_flightId, FlightStatusEv.status.DEPARTED.name(),null);
        pub.publish(flightStatusEv);
    }

    private void disrupt() throws Exception {
        int tagsSize = mluggageTags.size();
        System.out.println("*** TagSize:" + tagsSize);
        Random randRemove = new Random();
        int half = (int)Math.floor(tagsSize/2);
        System.out.println("*** Half:" + half);
        int removalNum = 0;
        while(half>1 && removalNum==0)
            removalNum = randRemove.nextInt(half);

        System.out.println("*** to remove:" + removalNum);
        List<String> newTagsAfterRemoval = new ArrayList<>();

        int [] indexesToRemove = new int[removalNum];
        Random randIndex = new Random();
        for(int i=0; i<removalNum; i++)
        {
            indexesToRemove[i] = randIndex.nextInt(tagsSize);
        }

        for(int i=0; i<indexesToRemove.length; i++)
        {
            indexesToRemove[i] = randIndex.nextInt(tagsSize);
        }


        System.out.println("*** indexesToRemove:" + Arrays.toString(indexesToRemove));

        DisruptedEv disruptedEvent = new DisruptedEv();
        disruptedEvent.setFlightId(m_flightId);
        for(int i=0; i<tagsSize; i++)
        {
            boolean isSkip = false;
            for(int j=0; j<indexesToRemove.length; j++)
            {
                if(indexesToRemove[j] == i)
                {
                    isSkip = true;
                    disruptedEvent.addTag(mluggageTags.get(i));
                    break;
                }
            }
            if(!isSkip)
                newTagsAfterRemoval.add(mluggageTags.get(i));
        }

        mluggageTags = newTagsAfterRemoval;
        if(disruptedEvent.getTagIds().size()>0)
        {
            disruptorPublisher.publish(disruptedEvent);
            System.out.println(disruptedEvent.toString());
        }else {
            System.out.println("*** No Disruption ***");
        }
    }

    private void disrupt2() throws Exception {
        int tagsSize = mluggageTags.size();
        Random randRemove = new Random();
        int half = (int)Math.floor(tagsSize/2);
        int removalNum = 0;
        while(half>1 && removalNum==0)
            removalNum = randRemove.nextInt(half);

        List<String> originalsCopy = new ArrayList<>(mluggageTags);

        int [] indexesToRemove = new int[removalNum];
        Random randIndex = new Random();
        for(int i=0; i<removalNum; i++)
            indexesToRemove[i] = randIndex.nextInt(tagsSize);

        List<String> tagsToRemove = new ArrayList<>();
        for(int i=0; i<indexesToRemove.length; i++)
            tagsToRemove.add(originalsCopy.get(indexesToRemove[i]));

        for(int i=0; i<indexesToRemove.length; i++)
            originalsCopy.remove(tagsToRemove.get(i));

        DisruptedEv disruptedEvent = new DisruptedEv();
        disruptedEvent.setFlightId(m_flightId);
        disruptedEvent.addTags(tagsToRemove);

        System.out.println("List before_removal: " + Arrays.toString(mluggageTags.toArray(new String[0])));
        System.out.println("items removed: " + Arrays.toString(tagsToRemove.toArray(new String[0])));
        System.out.println("list after_removal: " + Arrays.toString(originalsCopy.toArray(new String[0])));
        mluggageTags = originalsCopy;

        if(disruptedEvent.getTagIds().size()>0)
        {
            disruptorPublisher.publish(disruptedEvent);
            System.out.println(disruptedEvent.toString());
        }else {
            System.out.println("*** No Disruption ***");
        }
    }

    private void pause(long interval)
    {
        try{
            Thread.sleep(interval);
        }catch(Exception e){}
    }
}
