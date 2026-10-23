package com.example.demo.dto.request;

public class StreamRequest {

    private String streamName;
    private String description;

    public StreamRequest() {
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}