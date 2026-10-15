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

    @Tool(description = "Escalate unresolved issues to human support by creating a ticket.")
    public Map<String, Object> escalateToHuman(
            @ToolParam(description = "Order ID if known, otherwise null", required = false) Long orderId,
            @ToolParam(description = "Customer email or phone if the customer provided it", required = false) String customerContact,
            @ToolParam(description = "Short summary of the problem and what has already been tried") String summary,
            @ToolParam(description = "One of LOW, MEDIUM, HIGH, URGENT", required = false) String priority) {

        Priority p;
        try {
            p = (priority != null) ? Priority.valueOf(priority.trim().toUpperCase()) : Priority.MEDIUM;
        } catch (Exception e) {
            p = Priority.MEDIUM;
        }

        SupportTicket ticket = ticketRepository.save(new SupportTicket(
                null, orderId, customerContact, summary, p, TicketStatus.OPEN, Instant.now()));

        String ticketId = (ticket != null && ticket.ticketId() != null) ? ticket.ticketId() : "TICKET-" + System.currentTimeMillis();

        return Map.of(
                "ticketId", ticketId,
                "status", ticket != null ? ticket.status().name() : "OPEN",
                "message", "A human agent will contact the customer within 24 hours."
        );
    }
}
