package com.hz.infra.config;

import java.util.Properties;

public class KafkaProperties {
    public static Properties getProducerProps()
    {
        Properties producerProps = new Properties();
        producerProps.setProperty("bootstrap.servers","localhost:9092");
        producerProps.setProperty("acks","1");
        producerProps.setProperty("retries","3");
        producerProps.setProperty("linger.ms","10");

        return producerProps;
    }
}
