package com.example.demo.service;

import java.io.IOException;
import java.nio.file.*;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UploadStorageService {
    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Set<String> MIME_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "application/pdf");
    private final Path root;

    public UploadStorageService(@Value("${app.upload-dir:./uploads}") String directory) {
        this.root = Paths.get(directory).toAbsolutePath().normalize();
    }

    public StoredFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        if (file.getSize() > MAX_BYTES) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Proof files must be 5 MB or smaller.");
        String mime = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        String original = file.getOriginalFilename() == null ? "proof" : Paths.get(file.getOriginalFilename()).getFileName().toString();
        String ext = extension(original);
        if (!MIME_TYPES.contains(mime) || !extensionMatches(mime, ext))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Upload a JPG, PNG, WebP image or PDF file.");
        try {
            byte[] bytes = file.getBytes();
            if (!signatureMatches(mime, bytes)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The file contents do not match the declared file type.");
            Files.createDirectories(root);
            String storedName = UUID.randomUUID() + "." + ext;
            Path target = root.resolve(storedName).normalize();
            if (!target.startsWith(root)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name.");
            Files.write(target, bytes, StandardOpenOption.CREATE_NEW);
            String safeDisplayName = original.replaceAll("[^A-Za-z0-9._ -]", "_");
            return new StoredFile(safeDisplayName.length() > 180 ? safeDisplayName.substring(0, 180) : safeDisplayName, storedName, mime);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to store the uploaded file.");
        }
    }

    public Path resolve(String storedName) {
        if (storedName == null || !storedName.matches("[0-9a-fA-F-]{36}\\.(jpg|jpeg|png|webp|pdf)"))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found.");
        Path path = root.resolve(storedName).normalize();
        if (!path.startsWith(root) || !Files.isRegularFile(path)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found.");
        return path;
    }

    private boolean signatureMatches(String mime, byte[] b) {
        if (mime.equals("image/jpeg")) return b.length > 3 && (b[0] & 255) == 255 && (b[1] & 255) == 216 && (b[2] & 255) == 255;
        if (mime.equals("image/png")) return b.length > 8 && (b[0] & 255) == 137 && b[1] == 80 && b[2] == 78 && b[3] == 71;
        if (mime.equals("image/webp")) return b.length > 12 && new String(b, 0, 4).equals("RIFF") && new String(b, 8, 4).equals("WEBP");
        return b.length > 4 && b[0] == '%' && b[1] == 'P' && b[2] == 'D' && b[3] == 'F' && b[4] == '-';
    }
    private boolean extensionMatches(String mime, String ext) {
        return switch (mime) {
            case "image/jpeg" -> ext.equals("jpg") || ext.equals("jpeg");
            case "image/png" -> ext.equals("png");
            case "image/webp" -> ext.equals("webp");
            case "application/pdf" -> ext.equals("pdf");
            default -> false;
        };
    }
    private String extension(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
    public record StoredFile(String displayName, String storedName, String mimeType) {}
}
