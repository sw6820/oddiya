package com.oddiya.plan.service;

import com.oddiya.plan.dto.LlmRequest;
import com.oddiya.plan.dto.LlmResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class LlmAgentClient {
    @Value("${llm.agent.base-url}")
    private String baseUrl;

    public Mono<LlmResponse> generatePlan(LlmRequest request) {
        WebClient client = WebClient.builder()
                .baseUrl(baseUrl)
                .build();

        return client.post()
                .uri("/api/v1/plans/generate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(LlmResponse.class)
                .onErrorMap(throwable -> new RuntimeException("Failed to call LLM Agent: " + throwable.getMessage()));
    }
}

