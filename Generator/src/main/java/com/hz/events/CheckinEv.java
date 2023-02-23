package com.hz.events;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckinEv {
    private String flightId;
    private String ticketId;
    private String[] tagIds;
    private String[] routes;

    @Override
    public String toString()
    {
        return "flightId:" + flightId + "; " +
                "ticketId:" + ticketId + ";"
                ;
    }

}
