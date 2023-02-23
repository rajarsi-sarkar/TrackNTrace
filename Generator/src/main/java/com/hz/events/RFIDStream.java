package com.hz.events;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RFIDStream {
    private String tagId;
    private String status;// (Ld, UnLd)
    private String currentLoc;
    private long timestamp;

    public enum status {
        LOAD,
        UNLOAD
    }

    @Override
    public String toString()
    {
        return "tagId:" + tagId + "; " +
                "status:" + status + ";"
                ;
    }

}
