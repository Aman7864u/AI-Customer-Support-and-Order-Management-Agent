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

    @Tool(description = "Search store policies for return windows, refunds, delivery delays, or warranty.")
    public String searchPolicies(@ToolParam(description = "Policy question keywords, e.g. 'return window'", required = false) String question) {
        if (question == null || question.isBlank()) {
            return "No question provided to search policies.";
        }

        try {
            List<Document> docs = vectorStore.similaritySearch(
                    SearchRequest.builder().query(question.trim()).topK(3).similarityThreshold(0.4).build());

            if (docs == null || docs.isEmpty()) {
                return "No relevant policy found. Escalate to a human agent if the customer needs a policy decision.";
            }
            return docs.stream()
                    .map(d -> "[" + d.getMetadata().getOrDefault("source", "policy") + "]\n" + d.getText())
                    .collect(Collectors.joining("\n\n---\n\n"));
        } catch (Exception e) {
            return "Unable to search policies at this moment: " + e.getMessage();
        }
    }
}
