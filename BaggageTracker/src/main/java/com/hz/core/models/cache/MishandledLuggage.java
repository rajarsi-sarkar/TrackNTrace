package com.hz.core.models.cache;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;

@Data
@AllArgsConstructor
public class MishandledLuggage implements Serializable {
    String flightId;
    String ticketId; // id
    String[] tagIds; // id

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb
                .append("TicketId:").append(ticketId).append(";")
                .append("Tagid[]:").append(Arrays.toString(tagIds)).append(";")
        ;
        return sb.toString();
    }

}
