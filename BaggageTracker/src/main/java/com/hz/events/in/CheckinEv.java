package com.hz.events.in;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckinEv implements Serializable {
    private String flightId;
    private String ticketId;
    private String[] tagIds;
    private String[] routes;
}
