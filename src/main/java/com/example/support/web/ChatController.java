package com.example.support.web;

import com.example.support.service.PolicyIngestionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatClient chatClient;
    private final PolicyIngestionService ingestion;

    public ChatController(ChatClient chatClient, PolicyIngestionService ingestion) {
        this.chatClient = chatClient;
        this.ingestion = ingestion;
    }

    public record ChatRequest(@NotBlank String message, String conversationId) {}

    public record ChatResponse(String conversationId, String reply) {}

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        String conversationId = (request.conversationId() == null || request.conversationId().isBlank())
                ? UUID.randomUUID().toString()
                : request.conversationId();

        String reply = chatClient.prompt()
                .user(request.message())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        return new ChatResponse(conversationId, reply);
    }

    @PostMapping("/admin/reindex")
    public Map<String, Object> reindex() throws Exception {
        return Map.of("chunksIndexed", ingestion.ingest());
    }
}
