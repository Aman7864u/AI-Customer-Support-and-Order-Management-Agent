package com.example.support.config;

import com.example.support.tools.EscalationTools;
import com.example.support.tools.OrderTools;
import com.example.support.tools.PolicyTools;
import com.example.support.tools.ProductTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfig {

    private static final String SYSTEM_PROMPT = """
            You are a friendly, concise customer support agent for an online electronics store.

            Rules:
            1. For anything about a specific order, ALWAYS call the order tools. Never guess order data.
               If the customer gives no order ID and you need one, ask for it.
            2. For delivery delays, refunds, returns or shipping rules, call searchPolicies and base your
               answer ONLY on what it returns. Never invent policy.
            3. For product questions, call findProducts.
            4. If you cannot resolve the issue (lost package, policy gap, angry customer, customer asks for
               a human, refund needs approval), call escalateToHuman, then tell the customer the ticket ID
               and that a human will follow up.
            5. Never reveal one customer's data to another. Do not share email addresses or tracking details
               unless the customer asked for them.
            6. Keep answers short, polite and in plain language. Mention concrete facts (status, dates, ticket ID).
            """;

    @Bean
    public ChatClient supportChatClient(ChatModel chatModel,
                                        ChatMemory chatMemory,
                                        OrderTools orderTools,
                                        ProductTools productTools,
                                        PolicyTools policyTools,
                                        EscalationTools escalationTools) {
        return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(orderTools, productTools, policyTools, escalationTools)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new SimpleLoggerAdvisor())
                .build();
    }

    /**
     * MCP: publish the same tool objects through the MCP server (served at /mcp).
     * Any MCP client (Claude Desktop, MCP Inspector, another Spring AI app) can now call them.
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
