package com.example.support.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "tickets")
public record SupportTicket(
        @Id String ticketId,
        Long orderId,
        String customerContact,
        String summary,
        Priority priority,
        TicketStatus status,
        Instant createdAt
) {
    public enum Priority { LOW, MEDIUM, HIGH, URGENT }
    public enum TicketStatus { OPEN, IN_PROGRESS, RESOLVED }
}
