package com.example.support.tools;

import com.example.support.model.SupportTicket;
import com.example.support.model.SupportTicket.Priority;
import com.example.support.model.SupportTicket.TicketStatus;
import com.example.support.repository.TicketRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
public class EscalationTools {

    private final TicketRepository ticketRepository;

    public EscalationTools(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Tool(description = "Escalate the conversation to a human support agent by creating a support ticket. "
            + "Use when you cannot resolve the issue, the customer is upset or asks for a human, "
            + "an order is missing/lost, a refund needs approval, or policy does not cover the case.")
    public Map<String, Object> escalateToHuman(
            @ToolParam(description = "Order ID if known, otherwise null", required = false) Long orderId,
            @ToolParam(description = "Customer email or phone if the customer provided it", required = false) String customerContact,
            @ToolParam(description = "Short summary of the problem and what has already been tried") String summary,
            @ToolParam(description = "One of LOW, MEDIUM, HIGH, URGENT") String priority) {

        Priority p;
        try {
            p = Priority.valueOf(priority.trim().toUpperCase());
        } catch (Exception e) {
            p = Priority.MEDIUM;
        }

        SupportTicket ticket = ticketRepository.save(new SupportTicket(
                null, orderId, customerContact, summary, p, TicketStatus.OPEN, Instant.now()));

        return Map.of(
                "ticketId", ticket.ticketId(),
                "status", ticket.status().name(),
                "message", "A human agent will contact the customer within 24 hours."
        );
    }
}
