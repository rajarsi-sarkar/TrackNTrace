package com.hz.core.models.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MishandledLuggageList implements Serializable {
    String flightId;
    List<MishandledLuggage> mishandledList = new ArrayList<>();
    String[] missedTags;

    public void addLuggage(MishandledLuggage mishandledLuggage)
    {
        mishandledList.add(mishandledLuggage);
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        for (MishandledLuggage ml:mishandledList) {
            sb
                    .append("TicketId:").append(ml.getTicketId()).append(";")
                    .append("Tagid[]:").append(Arrays.toString(ml.getTagIds())).append(";")
            ;
        }
        return sb.toString();
    }

}
