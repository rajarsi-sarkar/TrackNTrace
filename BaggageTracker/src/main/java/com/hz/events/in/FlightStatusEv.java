package com.hz.events.in;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlightStatusEv implements Serializable {
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
