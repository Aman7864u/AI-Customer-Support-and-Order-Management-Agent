# AI Customer Support Agent (Spring Boot + Spring AI + RAG + MCP + MongoDB)

An AI agent that answers customer questions and uses business tools:

| Capability | How it is built |
|---|---|
| Understand request, decide which tool to call | Spring AI `ChatClient` with tool calling |
| Look up orders, delivery status, products | Java `@Tool` methods backed by MongoDB repositories |
| Answer policy questions (refunds, delays, warranty) | RAG: policy files are chunked, embedded and stored in MongoDB Atlas Vector Search; `searchPolicies` tool retrieves the right chunks |
| Escalate unresolved issues | `escalateToHuman` tool saves a ticket in MongoDB |
| Conversation memory | `MessageChatMemoryAdvisor` keyed by `conversationId` |
| MCP | The same tool objects are published by the Spring AI MCP server at `/mcp` |

## Architecture

```
Customer -> POST /api/chat -> ChatClient (system prompt + memory)
                                 |
          LLM decides which tool to call (tool calling loop)
                                 |
    getOrder / getDeliveryStatus -> OrderRepository -> MongoDB (orders)
    findProducts                 -> ProductRepository -> MongoDB (products)
    searchPolicies               -> VectorStore (Atlas Vector Search) <- policies/*.md
    escalateToHuman              -> TicketRepository -> MongoDB (tickets)
                                 |
                        Final answer to the customer

Other MCP clients -> /mcp -> same four tool classes
```

## Prerequisites
- Java 21 and Maven
- Docker (for local MongoDB with vector search) or a free MongoDB Atlas cluster
- An OpenAI API key (or change the model starter in `pom.xml` and `application.yml`)

## Run

```bash
docker compose up -d                 # MongoDB Atlas Local (supports $vectorSearch)
export OPENAI_API_KEY=sk-...
# optional, for Atlas cloud:
# export MONGODB_URI="mongodb+srv://user:pass@cluster.mongodb.net/support"
mvn spring-boot:run
```

On first start the app seeds demo orders/products and indexes `src/main/resources/policies/*.md`.
Note: a plain `mongo` image does not support vector search; use the Atlas Local image or Atlas cloud.

## Try it

```bash
# Delayed order (1025): tool lookup + delay policy via RAG + escalation if needed
curl -s localhost:8080/api/chat -H 'Content-Type: application/json' \
  -d '{"message":"My order #1025 has not arrived. Can you check its status?"}'

# Follow up in the same conversation (use the conversationId from the first reply)
curl -s localhost:8080/api/chat -H 'Content-Type: application/json' \
  -d '{"conversationId":"<id>","message":"I want a refund instead."}'

# Delivered order, refund policy question
curl -s localhost:8080/api/chat -H 'Content-Type: application/json' \
  -d '{"message":"Order 1026 arrived 6 days ago. Can I still return the watch?"}'

# Product info
curl -s localhost:8080/api/chat -H 'Content-Type: application/json' \
  -d '{"message":"Do you have the USB-C charger in stock?"}'

# Missing order, should say not found and offer help or escalate
curl -s localhost:8080/api/chat -H 'Content-Type: application/json' \
  -d '{"message":"Where is order 9999?"}'
```

Check created tickets in MongoDB: `db.tickets.find()`.

## Test the MCP server

```bash
npx @modelcontextprotocol/inspector
# Transport: Streamable HTTP, URL: http://localhost:8080/mcp
```

You should see `getOrder`, `getDeliveryStatus`, `findProducts`, `searchPolicies`, `escalateToHuman`.

## Project layout

```
src/main/java/com/example/support
  config/    AgentConfig (ChatClient, MCP tool provider), SeedDataRunner
  model/     Order, Product, SupportTicket
  repository/ Mongo repositories
  tools/     OrderTools, ProductTools, PolicyTools (RAG), EscalationTools
  service/   PolicyIngestionService (RAG ingestion)
  web/       ChatController
src/main/resources
  application.yml
  policies/  refund, delivery, warranty markdown (edit these, then POST /api/admin/reindex)
```

## Notes and next steps for a stronger submission
- **Security**: add authentication and verify the order belongs to the logged-in customer (e.g. match email) before returning order data. Protect `/mcp` and `/api/admin/*`.
- **Reindexing**: `/api/admin/reindex` adds chunks again; clear the `policy_vectors` collection first if you edit documents.
- **Evaluation**: write a small test set of questions with expected tool calls and check them (good for the report).
- **Guardrails**: limit refund amounts the agent may mention, log every tool call (the `SimpleLoggerAdvisor` already logs prompts and responses).
- **Persistent memory**: swap the default in-memory chat memory for a Mongo-backed `ChatMemoryRepository`.
- **Version note**: built for Spring Boot 3.5.x with Spring AI 1.1.x. Spring AI 2.x targets Spring Boot 4; if you move to it, a few package names and properties change.
