package com.oddiya.video.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqsService {
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${aws.sqs.queue-url}")
    private String queueUrl;

    public void publishVideoJob(Long jobId, Long userId, String[] photoUrls, String template) {
        try {
            Map<String, Object> message = Map.of(
                    "jobId", jobId,
                    "userId", userId,
                    "photoUrls", photoUrls,
                    "template", template
            );

            String messageBody = objectMapper.writeValueAsString(message);

            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .build();

            sqsClient.sendMessage(request);
            log.info("Published video job {} to SQS", jobId);
        } catch (Exception e) {
            log.error("Failed to publish video job to SQS", e);
            throw new RuntimeException("Failed to publish video job", e);
        }
    }
}

