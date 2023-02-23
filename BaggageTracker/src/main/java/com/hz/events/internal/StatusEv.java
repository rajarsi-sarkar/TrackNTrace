package com.hz.events.internal;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;

@Data
@AllArgsConstructor
public class StatusEv implements Serializable {
    public static final String COMPLETE = "COMPLETE";
    public static final String TIMEOUT = "TIMED-OUT";
    public static final String PARTIAL = "PARTIAL";

    private String ticketId;
    private String status; // Complete, Timeout
    private ArrayList<String> tagList;
}
