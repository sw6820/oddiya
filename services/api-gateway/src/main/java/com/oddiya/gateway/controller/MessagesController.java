package com.oddiya.gateway.controller;

import com.oddiya.gateway.config.UIMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Messages API Controller
 * Serves UI strings dynamically to prevent hardcoding in HTML
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessagesController {

    private final UIMessages uiMessages;

    /**
     * Get all Korean UI messages
     * Used by mobile web app to load localized strings
     */
    @GetMapping("/ko")
    public Mono<Map<String, String>> getKoreanMessages() {
        return Mono.just(uiMessages.getAllKorean());
    }

    /**
     * Get specific message by key
     */
    @GetMapping("/ko/{key}")
    public Mono<String> getMessage(String key) {
        return Mono.just(uiMessages.get(key));
    }
}
