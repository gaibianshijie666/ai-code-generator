package com.chen.yuaicodemother.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface AgentChatService {
    TokenStream chat(@MemoryId String memoryId, @UserMessage String userMessage);
}
