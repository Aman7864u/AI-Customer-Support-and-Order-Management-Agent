package com.example.support.tools;

import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG exposed as a tool: the agent decides when it needs company policy text
 * (refunds, returns, delivery delays, shipping) and this tool retrieves the most relevant chunks.
 */
@Component
public class PolicyTools {

    private final VectorStore vectorStore;

    public PolicyTools(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Tool(description = "Search the company's policy documents (refund, return, delivery, shipping and "
            + "delay policies) and return the most relevant passages. Use this before answering any "
            + "policy question such as refund eligibility, return windows or what happens when a delivery is late.")
    public String searchPolicies(@ToolParam(description = "A short natural-language question about a policy") String question) {
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder().query(question).topK(4).similarityThreshold(0.5).build());

        if (docs == null || docs.isEmpty()) {
            return "No relevant policy found. Escalate to a human agent if the customer needs a policy decision.";
        }
        return docs.stream()
                .map(d -> "[" + d.getMetadata().getOrDefault("source", "policy") + "]\n" + d.getText())
                .collect(Collectors.joining("\n\n---\n\n"));
    }
}
