package com.hz.core.models.state;

import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;

@Data
public class InstanceLuggageState implements Serializable
{
    private String ticketId;
    private HashSet<String> tagList = new HashSet<>();
}
