package com.movelog.domain.gpt.application;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GptService {

    private final WebClient gptWebClient;  // @Qualifier 제거하고 빈 이름에 맞는 생성자 주입
    @Value("${chatgpt.api-key}")
    private String apiKey;
    @Value("${chatgpt.model}")
    private String model;
    @Value("${chatgpt.max-tokens}")
    private Integer maxTokens;
    @Value("${chatgpt.temperature}")
    private Double temperature;
    @Value("${chatgpt.top-p}")
    private Double topP;

    public Mono<JsonNode> callChatGpt(String prompt) {
        return gptWebClient.post()
                .uri("/chat/completions")  // 경로에 대한 수정
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(buildRequestBody(prompt))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnSuccess(response -> log.info("GPT API 응답 성공: {}", response))
                .doOnError(error -> log.error("GPT API 호출 중 오류 발생: {}", error.getMessage()))
                .onErrorResume(error -> {
                    log.error("GPT API 호출 중 예외 처리 발생: {}", error.getMessage());
                    return Mono.error(new RuntimeException("GPT API 호출 실패", error));
                });
    }

    private Map<String, Object> buildRequestBody(String prompt) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("model", model);
        bodyMap.put("max_tokens", maxTokens);
        bodyMap.put("temperature", temperature);
        bodyMap.put("top_p", topP);
        bodyMap.put("messages", List.of(
                Map.of(
                        "role", "user",
                        "content", prompt
                )
        ));
        return bodyMap;
    }
}
