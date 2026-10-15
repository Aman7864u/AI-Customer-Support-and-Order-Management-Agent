package com.example.support.config;

import com.example.support.tools.EscalationTools;
import com.example.support.tools.OrderTools;
import com.example.support.tools.PolicyTools;
import com.example.support.tools.ProductTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfig {

    private static final String SYSTEM_PROMPT = """
        You are a support agent for an online electronics store, talking directly to the customer.
        Answer in 2 to 3 short sentences.

        - Order questions: call getDeliveryStatus with the order ID.
        - Return, refund, delay, shipping or warranty questions: answer from the policy excerpts
          given with the question. If they are missing, call searchPolicies.
        - Product questions: call findProducts.
        - If the customer already gave an order ID earlier, reuse it. Do not ask again.
        - If you cannot solve it, or the customer wants a human: call escalateToHuman and give the ticket ID.
        - Never make up order data or policies.
        """;

    // Replaces the built-in template, which forces "answer only from context" and would block tool use.
    private static final PromptTemplate RAG_TEMPLATE = new PromptTemplate("""
        {query}

        Policy excerpts (may be empty or unrelated):
        ---------------------
        {question_answer_context}
        ---------------------
        Use the excerpts only for return, refund, delay, shipping or warranty questions.
        For order or product questions, ignore them and use your tools.
        """);

    @Bean
    public ChatClient supportChatClient(ChatModel chatModel,
                                        ChatMemory chatMemory,
                                        VectorStore vectorStore,
                                        OrderTools orderTools,
                                        ProductTools productTools,
                                        PolicyTools policyTools,
                                        EscalationTools escalationTools) {
        return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(orderTools, productTools, policyTools, escalationTools)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .promptTemplate(RAG_TEMPLATE)
                                .searchRequest(SearchRequest.builder().topK(3).similarityThreshold(0.5).build())
                                .build(),
                        new SimpleLoggerAdvisor())
                .build();
    }

    /**
     * MCP: publish the same tool objects through the MCP server (served at /mcp).
     */
    @Bean
    public ToolCallbackProvider mcpTools(OrderTools orderTools,
                                         ProductTools productTools,
                                         PolicyTools policyTools,
                                         EscalationTools escalationTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(orderTools, productTools, policyTools, escalationTools)
                .build();
    }
}