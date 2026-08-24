package com.ai.SpringAiProject.Services;

import com.ai.SpringAiProject.Exceptions.AudioProcessingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class TranscriptionService {

    private final RestTemplate restTemplate;

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url:https://api.groq.com/openai}")
    private String baseUrl;

    @Value("${spring.ai.openai.audio.model:whisper-large-v3-turbo}")
    private String model;

    public TranscriptionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String transcribe(MultipartFile audioFile) {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new AudioProcessingException("Audio file is empty or missing.");
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new AudioProcessingException("OpenAI/Groq API key is not configured.");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResourceWithFilename(audioFile.getBytes(), audioFile.getOriginalFilename()));
            body.add("model", model);

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/v1/audio/transcriptions",
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || responseBody.get("text") == null) {
                throw new AudioProcessingException("AI transcription returned no usable text.");
            }

            return String.valueOf(responseBody.get("text"));
        } catch (RestClientException | IOException exception) {
            throw new AudioProcessingException("Failed to transcribe the uploaded audio file.", exception);
        }
    }

    private static final class ByteArrayResourceWithFilename extends ByteArrayResource {
        private final String filename;

        private ByteArrayResourceWithFilename(byte[] byteArray, String filename) {
            super(byteArray);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
}
