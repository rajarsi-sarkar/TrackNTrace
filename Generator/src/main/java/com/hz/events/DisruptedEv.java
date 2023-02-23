package com.hz.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisruptedEv {
    private String flightId;
    private List<String> tagIds = new ArrayList<>();

    public void addTag(String aTag)
    {
        tagIds.add(aTag);
    }
    public void addTags(List<String> aTagList)
    {
        tagIds.addAll(aTagList);
    }

    @Override
    public String toString()
    {
        return "flightId:" + flightId + "; " +
                "tagIds:" + Arrays.toString(tagIds.toArray()) + ";"
                ;
    }

}
