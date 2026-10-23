package com.example.demo.service;

import java.util.List;

import com.example.demo.entity.Stream;

public interface StreamService {

    Stream saveStream(Stream stream);

    List<Stream> getAllStreams();

    Stream getStreamById(Integer id);

    Stream updateStream(Integer id, Stream stream);

    void deleteStream(Integer id);
}