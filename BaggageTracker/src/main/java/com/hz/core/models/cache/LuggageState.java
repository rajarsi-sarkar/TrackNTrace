package com.hz.core.models.cache;

import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;

@Data
public class LuggageState implements Serializable {
    private String ticketId; // id
    private String flightId;
    private String[] routes;
    private String[] tagIds;
    private String currentLocation;
    private boolean inRedStatus;
    private String[] processedTags;
    private String collaborator;

    public LuggageState(String aFlightId, String aTicketId, String[] aRoutes, String[] aTagIds, String aExpectedCurrentLocation, String aCollaborator)
    {
        collaborator = aCollaborator;
        flightId=aFlightId;
        ticketId=aTicketId;
        routes=aRoutes;
        tagIds=aTagIds;
        currentLocation =aExpectedCurrentLocation;
        inRedStatus = true;

//        String tagStr = "";
//        id = String.valueOf(Arrays.stream(tagIds).reduce(tagStr, (subtotal, element) -> subtotal + element).hashCode());

        if(currentLocation == null && routes!=null)
            currentLocation = routes[0];
    }

    public void cleanUpProcessedTags()
    {
        processedTags = null;
    }

    public int getCurrentTagCount()
    {
        int length = 0;
        if (processedTags == null || processedTags.length == 0)
        {
            length = tagIds.length;
        }else length = tagIds.length - processedTags.length ;

        if(length>0)
        length = length -1;

        return length;
    }

    public int getTagCount()
    {
        return tagIds.length;
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb
                .append("FlightId:").append(flightId).append(";")
                .append("TicketId:").append(ticketId).append(";")
                .append("InRedStatus:").append(inRedStatus).append(";")
                .append("Tags:").append(Arrays.toString(tagIds)).append(";")
                .append("ProcessedTags:").append(Arrays.toString(processedTags)).append(";")
                .append("collaborator:").append(collaborator).append(";")
        ;
        return sb.toString();
    }
}
