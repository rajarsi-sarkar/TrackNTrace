package com.hz;

import com.hz.controller.AirlineWorkflow;
import com.hz.infra.config.KafkaProperties;
import com.hz.infra.util.PropertyFileReader;

import java.util.Properties;

public class Main {
    public static void main(String[] args) throws Exception {
//        CheckinPublisher checkinPublisher = new CheckinPublisher();
//        checkinPublisher.publish("/data/Checkin.txt");

        Properties prodProps = KafkaProperties.getProducerProps();

        Properties airlineProps = PropertyFileReader.getProps("Airlines.txt");

        Object[] airlines = airlineProps.keySet().toArray();

        while (true)
        {
            for(int i=0; i<airlines.length; i++)
            {
                String airline = airlines[i].toString();
                int checkIns = Integer.parseInt(airlineProps.get(airline).toString());
                long rnd = String.valueOf(System.currentTimeMillis()).hashCode();
                String flightId = airline + rnd;
                AirlineWorkflow wf = new AirlineWorkflow(flightId, checkIns, prodProps);
                wf.start();
//            new AirlineWorkflow_copy(airline,prodProps);
            }
            try {
                Thread.sleep(20000);
            }catch(Exception e){}
        }
    }
}
