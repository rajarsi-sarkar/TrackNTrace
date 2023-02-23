package com.hz.events.out;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;

@Data
@AllArgsConstructor
public class OutEv implements Serializable {
    String flightId;
    String[] tagIds;
    String status;

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb
                .append("FlightId:").append(flightId).append(";")
                .append("Tagid[]:").append(Arrays.toString(tagIds)).append(";")
                .append("status:").append(status).append(";")
        ;
        return sb.toString();
    }
}
