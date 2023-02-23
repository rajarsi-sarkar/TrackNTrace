package com.hz.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlightStatusEv {
    private String flightId;
    private String flightStatus;
    private String location;

    public enum status {
        LANDED,
        DEPARTED,
        TAXI,
        DOCKED
    }

    @Override
    public String toString()
    {
        return "flightId:" + flightId + "; " +
                "flightStatus:" + flightStatus + "; "
                ;
    }
}
