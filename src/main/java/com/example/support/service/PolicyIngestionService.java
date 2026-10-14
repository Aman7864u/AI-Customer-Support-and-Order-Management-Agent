package com.example.support.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * RAG ingestion: reads policy files from classpath:/policies, splits them into chunks,
 * embeds them and stores them in the vector store. Skipped if the store already has data.
 * Call POST /api/admin/reindex to force a re-ingest after editing the policy files.
 */
@Service
public class PolicyIngestionService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PolicyIngestionService.class);

    private final VectorStore vectorStore;

    public PolicyIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        boolean empty = vectorStore.similaritySearch(
                SearchRequest.builder().query("refund policy").topK(1).similarityThreshold(0.0).build()).isEmpty();
        if (empty) {
            ingest();
        } else {
            log.info("Policy vectors already present, skipping ingestion.");
        }
    }

    public int ingest() throws Exception {
        Resource[] files = new PathMatchingResourcePatternResolver().getResources("classpath:/policies/*.md");
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> chunks = new ArrayList<>();

        for (Resource file : files) {
            List<Document> docs = new TextReader(file).get();
            docs.forEach(d -> d.getMetadata().put("source", file.getFilename()));
            chunks.addAll(splitter.apply(docs));
        }

        vectorStore.add(chunks);
        log.info("Ingested {} policy chunks from {} files.", chunks.size(), files.length);
        return chunks.size();
    }
}
