package com.quietjournal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quietjournal.exception.ImageUploadException;
import com.quietjournal.util.SupabaseProperties;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class SupabaseService {

    private final WebClient supabaseWebClient;;
    private final SupabaseProperties supabaseProps;
    private final ObjectMapper objectMapper = new ObjectMapper();
    public SupabaseService(WebClient supabaseWebClient,SupabaseProperties supabaseProps) {
        this.supabaseWebClient = supabaseWebClient;
        this.supabaseProps = supabaseProps;
    }

    // ----------------- UPLOAD -----------------
    public List<String> uploadFiles(String bucket, MultipartFile[] files) {
        List<String> uploadedPaths = new ArrayList<>();
        if (files != null ) {
            for (MultipartFile file : files) {
                uploadedPaths.add(uploadFile(bucket, file));
            }
        }
        return uploadedPaths;
    }

    public String uploadFile(String bucket, MultipartFile file) {
        String path = UUID.randomUUID() + "-" + file.getOriginalFilename();
        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", file.getResource())
                    .filename(Objects.requireNonNull(file.getOriginalFilename()))
                    .contentType(file.getContentType() != null
                            ? MediaType.parseMediaType(file.getContentType())
                            : MediaType.APPLICATION_OCTET_STREAM);

            supabaseWebClient.post()
                    .uri("/{bucket}/{path}", bucket, path)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(builder.build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return path;
        } catch (Exception ex) {
            throw new ImageUploadException("Failed to upload image " + file.getOriginalFilename(), ex);
        }
    }

    // ----------------- DELETE -----------------
    public void deleteFile(String bucket, String path) {
        supabaseWebClient.delete()
                .uri("/{bucket}/{path}", bucket, path)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    // ----------------- SIGNED URL -----------------
    public String generateSignedUrl(String bucket, String path, int expiresInSeconds) {
        try {
            String response = supabaseWebClient.post()
                    .uri("/sign/{bucket}/{path}", supabaseProps.getBucket(), path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"expiresIn\":3600}")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);

            String signedPath = jsonNode.get("signedURL").asText();

            return supabaseProps.getUrl() + "/storage/v1" + signedPath;
        } catch (Exception ex) {

            throw new ImageUploadException("Failed to generate signed URL for " + path, ex);
        }
    }
}
