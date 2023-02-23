package com.hz.events.in;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RFIDStream implements Serializable {
    private String tagId;
    private String status;// (Ld, UnLd)
    private String currentLoc;
    private long timestamp;

    private String ticketId;
    private String flightId;
    private int tagCount;

    public enum status {
        LOAD,
        UNLOAD
    }

}
