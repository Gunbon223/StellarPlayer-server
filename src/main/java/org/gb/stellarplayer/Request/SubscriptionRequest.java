package org.gb.stellarplayer.Request;

import ch.qos.logback.core.joran.spi.DefaultClass;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class SubscriptionRequest {
    private String name;
    private String dateType;
    private String offer;
    private String description;
    private List<String> features;
    private Double price;
}
