package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stream")
public class Stream {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stream_id")
    private Integer streamId;

    @Column(name = "stream_name", nullable = false, unique = true, length = 100)
    private String streamName;

    @Column(name = "description", length = 200)
    private String description;

    public Stream() {
    }

    public Integer getStreamId() {
        return streamId;
    }

    public void setStreamId(Integer streamId) {
        this.streamId = streamId;
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