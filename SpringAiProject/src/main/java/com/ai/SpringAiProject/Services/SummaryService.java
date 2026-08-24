package com.ai.SpringAiProject.Services;

import com.ai.SpringAiProject.Exceptions.AudioProcessingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class SummaryService {

    private final RestTemplate restTemplate;

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url:https://api.groq.com/openai}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.model:openai/gpt-oss-120b}")
    private String model;

    public SummaryService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String summarize(String transcript) {
        if (transcript == null || transcript.isBlank()) {
            throw new AudioProcessingException("Transcript is empty, so no summary can be generated.");
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new AudioProcessingException("OpenAI/Groq API key is not configured.");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content", "You are an expert meeting assistant. Create a clear, professional summary in plain English. Include: 1) a concise overview, 2) major discussion points, 3) decisions made, 4) action items, 5) next steps and owners if mentioned."
                            ),
                            Map.of(
                                    "role", "user",
                                    "content", "Summarize this transcript carefully and keep the summary actionable:\n\n" + transcript
                            )
                    ),
                    "temperature", 0.7
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/v1/chat/completions",
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || responseBody.get("choices") == null) {
                throw new AudioProcessingException("AI summary service returned no valid response.");
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new AudioProcessingException("AI summary service returned an empty response.");
            }

            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            if (message == null || message.get("content") == null) {
                throw new AudioProcessingException("AI summary service did not return summary content.");
            }

            return String.valueOf(message.get("content"));
        } catch (HttpStatusCodeException exception) {
            String providerMessage = exception.getResponseBodyAsString();
            throw new AudioProcessingException(
                    "Summary provider rejected the request: " + providerMessage,
                    exception
            );
        } catch (RestClientException exception) {
            throw new AudioProcessingException("Failed to generate meeting summary.", exception);
        }
    }
}
