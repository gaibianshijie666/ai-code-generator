package com.chen.yuaicodemother.ai;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.chen.yuaicodemother.ai.tools.BaseTool;
import com.chen.yuaicodemother.ai.tools.ToolManager;
import com.chen.yuaicodemother.model.entity.Agent;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Configuration
@Slf4j
public class AgentChatServiceFactory {

    @Resource
    @Qualifier("openAiStreamingChatModel")
    private StreamingChatModel openAiStreamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ToolManager toolManager;

    /**
     * Agent AI 服务实例缓存
     * 缓存策略：
     * - 最大缓存 1000 个实例
     * - 写入后 30 分钟过期
     * - 访问后 10 分钟过期
     */
    private final Cache<String, AgentChatService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("Agent AI 服务实例被移除，缓存键: {}, 原因: {}", key, cause);
            })
            .build();

    /**
     * 根据 Agent 和用户ID获取聊天服务（带缓存）
     */
    public AgentChatService getAgentChatService(Agent agent, Long userId) {
        String cacheKey = "agent:" + agent.getId() + ":" + userId;
        return serviceCache.get(cacheKey, key -> createAgentChatService(agent, userId));
    }

    /**
     * 创建新的 Agent AI 服务实例
     */
    private AgentChatService createAgentChatService(Agent agent, Long userId) {
        String memoryId = "agent_chat:" + agent.getId() + ":" + userId;

        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .id(memoryId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(50)
                .build();

        Object[] selectedTools = filterTools(agent.getToolIds());

        AiServices.Builder<AgentChatService> builder = AiServices.builder(AgentChatService.class)
                .streamingChatModel(openAiStreamingChatModel)
                .chatMemoryProvider(memId -> chatMemory)
                .systemMessage(agent.getSystemPrompt());

        if (selectedTools.length > 0) {
            builder.tools(selectedTools);
        }

        return builder.build();
    }

    /**
     * 根据 toolIds JSON 过滤出启用的工具
     */
    private Object[] filterTools(String toolIdsJson) {
        if (StrUtil.isBlank(toolIdsJson)) {
            return new Object[0];
        }
        try {
            List<String> enabledToolNames = JSONUtil.toList(toolIdsJson, String.class);
            BaseTool[] allTools = toolManager.getAllTools();
            return Arrays.stream(allTools)
                    .filter(t -> enabledToolNames.contains(t.getToolName()))
                    .toArray();
        } catch (Exception e) {
            log.warn("解析工具ID列表失败: {}", toolIdsJson, e);
            return new Object[0];
        }
    }
}
