package com.hz.infrastructure.channels.kafka.consumers;//package com.hz.infrastructure.channel.kafka;

import com.dv.framework.channel.kafka.events.consumer.EventsConsumer;
import com.dv.framework.channel.kafka.events.consumer.IRecordProcessor;
import com.dv.framework.exception.ChannelException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hz.infrastructure.exceptions.SourceException;

import java.util.Properties;

public class GenericEventsConsumer<T>
{
    public GenericEventsConsumer(String aTopic, Properties aProperties, TypeReference<T> typeReference, IRecordProcessor<T> aProcessor) throws SourceException
    {
        try {
            new EventsConsumer<>(aTopic, aProperties, typeReference, aProcessor);
        }catch(ChannelException che){
            throw new SourceException(che);
        }
    }
}
