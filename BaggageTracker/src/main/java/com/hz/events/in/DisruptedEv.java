package com.hz.events.in;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisruptedEv implements Serializable {
    private String flightId;
    private List<String> tagIds = new ArrayList<>();
    private boolean isInsert;
    @Override
    public String toString()
    {
        return "flightId:" + flightId + "; " +
                "tagIds:" + Arrays.toString(tagIds.toArray()) + ";"
                ;
    }

}
