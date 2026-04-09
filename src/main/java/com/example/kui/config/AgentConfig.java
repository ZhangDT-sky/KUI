package com.example.kui.config;
import com.example.kui.memory.RedisChatMemoryStore;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.source.ClassPathSource;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
public class AgentConfig {

    @Autowired
    private RedisChatMemoryStore redisChatMemoryStore;

    @Autowired
    private ResourcePatternResolver resourcePatternResolver;

    @Autowired
    private EmbeddingModel embeddingModel;

    TavilyWebSearchEngine tavilyWebSearchEngine;

    // PgVector 配置属性
    @Value("${kui.pgvector.host:localhost}")
    private String pgHost;
    @Value("${kui.pgvector.port:5432}")
    private int pgPort;
    @Value("${kui.pgvector.database:embedding}")
    private String pgDatabase;
    @Value("${kui.pgvector.user:postgres}")
    private String pgUser;
    @Value("${kui.pgvector.password:root}")
    private String pgPassword;
    @Value("${kui.pgvector.table:embedding_store}")
    private String pgTable;

    // 内容配置属性
    @Value("${kui.content.auto-load-on-startup:false}")
    private boolean autoLoadOnStartup;
    @Value("${kui.content.base-path:content}")
    private String contentBasePath;

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();
    }

    @Bean
    public ChatMemoryProvider chatMemoryProvider(){
        return threadId -> MessageWindowChatMemory.builder()
                .id(threadId)
                .maxMessages(20)
                .chatMemoryStore(redisChatMemoryStore)
                .build();
    }

    @Bean
    public PgVectorEmbeddingStore  pgVectorEmbeddingStore() {
        return PgVectorEmbeddingStore.builder()
                .host(pgHost)
                .port(pgPort)
                .database(pgDatabase)
                .user(pgUser)
                .password(pgPassword)
                .table(pgTable)
                .dimension(1024)
                .build();
    }

    @Bean
    public EmbeddingStoreIngestor embeddingStoreIngestor(PgVectorEmbeddingStore pgVectorEmbeddingStore) {
        return EmbeddingStoreIngestor.builder()
                .embeddingStore(pgVectorEmbeddingStore)
                .embeddingModel(embeddingModel)
                // 配置文档分割器：每个chunk最大5000字符，重叠50字符，确保小文档不会被过度分割
                .documentSplitter(DocumentSplitters.recursive(5000, 50))
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(
            PgVectorEmbeddingStore pgVectorEmbeddingStore,
            EmbeddingStoreIngestor embeddingStoreIngestor
    ) {
        if(autoLoadOnStartup){
            List<Document> documents = loadAllDocumentsRecursively(contentBasePath);
            if (documents != null && !documents.isEmpty()) {
                embeddingStoreIngestor.ingest(documents);
                log.info("Successfully loaded {} documents from content directory", documents.size());
            } else {
                log.warn("No documents found in content directory. Skipping document ingestion.");
            }
        }
        else{
            log.info("Document auto-loading is disabled. Set 'kui.content.auto-load-on-startup=true' to enable.");
        }
        return pgVectorEmbeddingStore;
    }

    private List<Document> loadAllDocumentsRecursively(String basePath){
        List<Document> documents = new ArrayList<>();
        try{
            Resource[] resources = resourcePatternResolver.getResources(
                    "classpath:" + basePath + "/**/*"
            );

            for(Resource resource : resources){
                if(resource.exists() && resource.isReadable() && !resource.getURL().toString().endsWith("/")){
                    try{
                        // 获取 classpath 相对路径
                        String classpathPath = null;

                        // 方法1: 如果是 ClassPathResource，直接获取路径
                        if(resource instanceof ClassPathResource){
                            ClassPathResource classPathResource = (ClassPathResource) resource;
                            classpathPath = classPathResource.getPath();
                        } else {
                            // 方法2: 从 URL 中提取路径
                            String urlString = resource.getURL().toString();
                            // 处理 jar:file: 格式
                            if(urlString.contains(".jar!")){
                                int contentIndex = urlString.indexOf(basePath);
                                if(contentIndex != -1){
                                    classpathPath = urlString.substring(contentIndex);
                                    // 移除可能的查询参数
                                    int questionMarkIndex = classpathPath.indexOf('?');
                                    if(questionMarkIndex != -1){
                                        classpathPath = classpathPath.substring(0, questionMarkIndex);
                                    }
                                }
                            } else {
                                // 处理 file: 格式
                                int contentIndex = urlString.indexOf(basePath);
                                if(contentIndex != -1){
                                    classpathPath = urlString.substring(contentIndex);
                                    // 移除 file: 前缀（如果有）
                                    classpathPath = classpathPath.replaceFirst("^file:/", "");
                                    // 在 Windows 上处理路径分隔符
                                    classpathPath = classpathPath.replace("\\", "/");
                                }
                            }
                        }

                        // 跳过目录和无法识别的路径
                        if(classpathPath == null || classpathPath.isEmpty()){
                            continue;
                        }
                        Document document = ClassPathDocumentLoader.loadDocument(classpathPath);
                        // 为文档添加metadata，标识文档来源，这样分割后的segment也能保留这个信息
                        document.metadata().put("source", classpathPath);
                        document.metadata().put("filename", resource.getFilename());
                        documents.add(document);
                        log.info("Loaded document: {}", classpathPath);
                    }catch (Exception e){
                        log.warn("Skipped: {} - {}", resource.getFilename(), e.getMessage());
                        // 调试信息
                        if(e.getMessage() != null && e.getMessage().contains("not found")){
                            log.debug("  Resource URL: {}", resource.getURL());
                        }
                    }
                }
            }
        }catch (Exception e){
            log.error("Error loading documents: {}", e.getMessage(), e);
        }
        return documents;
    }

    @Bean
    public ContentRetriever contentRetriever(PgVectorEmbeddingStore pgVectorEmbeddingStore) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(pgVectorEmbeddingStore)
                .embeddingModel(embeddingModel)
                .minScore(0.5)
                .maxResults(10)
                .build();
    }
}
