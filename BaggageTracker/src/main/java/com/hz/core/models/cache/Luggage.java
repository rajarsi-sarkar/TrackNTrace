package com.hz.core.models.cache;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Luggage implements Serializable {
    private String tagId;
    private String ticketId;
    private String flightId;
    private String location;
    private long timeStamp;

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb
                .append("FlightId:").append(flightId).append(";")
                .append("TicketId:").append(ticketId).append(";")
                .append("Tagid:").append(tagId).append(";")
        ;
        return sb.toString();
    }

}
