package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import com.example.demo.dto.request.StreamRequest;
import com.example.demo.dto.response.StreamResponse;
import com.example.demo.entity.Stream;
import com.example.demo.service.StreamService;

@RestController
@RequestMapping("/api/streams")
public class StreamController {

    private final StreamService streamService;

    public StreamController(StreamService streamService) {
        this.streamService = streamService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public StreamResponse createStream(@RequestBody StreamRequest request) {

        Stream stream = convertToEntity(request);

        Stream saved = streamService.saveStream(stream);

        return convertToResponse(saved);
    }

    @GetMapping
    public List<StreamResponse> getAllStreams() {

        return streamService.getAllStreams()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public StreamResponse getStreamById(
            @PathVariable("id") Integer id) {

        Stream stream = streamService.getStreamById(id);

        return convertToResponse(stream);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public StreamResponse updateStream(
            @PathVariable("id") Integer id,
            @RequestBody StreamRequest request) {

        Stream stream = convertToEntity(request);

        Stream updated = streamService.updateStream(id, stream);

        return convertToResponse(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteStream(
            @PathVariable("id") Integer id) {

        streamService.deleteStream(id);

        return "Stream deleted successfully";
    }

    private Stream convertToEntity(StreamRequest request) {

        Stream stream = new Stream();

        stream.setStreamName(request.getStreamName());
        stream.setDescription(request.getDescription());

        return stream;
    }

    private StreamResponse convertToResponse(Stream stream) {

        if (stream == null) {
            return null;
        }

        StreamResponse response = new StreamResponse();

        response.setStreamId(stream.getStreamId());
        response.setStreamName(stream.getStreamName());
        response.setDescription(stream.getDescription());

        return response;
    }
}
