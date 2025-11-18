package com.example.kui.config;

import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebSearchConfig {

    @Value("${langchain4j.web-search-engine.tavily.api-key}")
    private String tavilyApiKey;

    @Value("${langchain4j.web-search-engine.tavily.max-results:3}")
    private Integer maxResults;

    @Bean
    public WebSearchEngine webSearchEngine() {
        return TavilyWebSearchEngine.builder()
                .apiKey(tavilyApiKey)
                .build();
    }

    @Bean
    public ContentRetriever webContentRetriever(WebSearchEngine webSearchEngine) {
        return WebSearchContentRetriever.builder()
                .webSearchEngine(webSearchEngine)
                .maxResults(maxResults)
                .build();
    }





}
