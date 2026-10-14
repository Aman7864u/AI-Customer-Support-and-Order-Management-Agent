package com.example.support.repository;

import com.example.support.model.SupportTicket;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TicketRepository extends MongoRepository<SupportTicket, String> {
}
