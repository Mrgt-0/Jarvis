package com.jarvis.Service;
import com.fasterxml.jackson.databind.*;
import com.jarvis.Model.DTO.CodeProblemDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.time.Duration;
import java.util.*;

@Slf4j
@Component
public class NlpClient {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public NlpClient(@Value("${nlp.url:http://localhost:8000}") String nlpUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(300));
        this.restClient = RestClient.builder()
                .baseUrl(nlpUrl)
                .requestFactory(factory)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public List<String> getExplanations(List<CodeProblemDTO> problems) {
        try {
            List<Map<String, String>> violations = problems.stream()
                    .map(p -> Map.of(
                            "fileName", p.getFileName() != null ? p.getFileName() : "",
                            "message", p.getMessage() != null ? p.getMessage() : "",
                            "severity", p.getSeverity() != null ? p.getSeverity() : "MEDIUM",
                            "ruleId", p.getRuleId() != null ? p.getRuleId() : "",
                            "snippet", p.getSnippet() != null ? p.getSnippet() : ""
                    ))
                    .toList();
            if (violations.isEmpty())
                return Collections.emptyList();

            Map<String, Object> request = Map.of(
                    "violations", violations,
                    "language", "ru"
            );
            log.info("Отправка в NLP: {} нарушений", violations.size());
            String rawResponse = restClient.post()
                    .uri("/explain")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.ALL)
                    .body(request)
                    .retrieve()
                    .body(String.class);
            JsonNode root = objectMapper.readTree(rawResponse);
            List<String> explanations = new ArrayList<>();

            if (root.has("explanations")) {
                JsonNode arr = root.get("explanations");
                for (JsonNode node : arr)
                    explanations.add(node.asText());
            }
            return explanations;
        } catch (Exception e) {
            log.warn("NLP сервис недоступен: {} ({})", e.getMessage(), e.getClass().getSimpleName());
            return Collections.emptyList();
        }
    }
}