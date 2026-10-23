package com.example.demo.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entity.Stream;
import com.example.demo.repository.StreamRepository;
import com.example.demo.service.StreamService;

@Service
public class StreamServiceImpl implements StreamService {

    private final StreamRepository streamRepository;

    public StreamServiceImpl(StreamRepository streamRepository) {
        this.streamRepository = streamRepository;
    }

    @Override
    public Stream saveStream(Stream stream) {
        return streamRepository.save(stream);
    }

    @Override
    public List<Stream> getAllStreams() {
        return streamRepository.findAll();
    }

    @Override
    public Stream getStreamById(Integer id) {
        return streamRepository.findById(id)
                .orElse(null);
    }

    @Override
    public Stream updateStream(Integer id, Stream stream) {

        Stream existing = streamRepository.findById(id)
                .orElse(null);

        if (existing != null) {

            existing.setStreamName(stream.getStreamName());
            existing.setDescription(stream.getDescription());

            return streamRepository.save(existing);
        }

        return null;
    }

    @Override
    public void deleteStream(Integer id) {
        streamRepository.deleteById(id);
    }
}