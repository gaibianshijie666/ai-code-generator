package com.chen.yuaicodemother.core;

import cn.hutool.json.JSONUtil;
import com.chen.yuaicodemother.ai.AgentChatService;
import com.chen.yuaicodemother.ai.AgentChatServiceFactory;
import com.chen.yuaicodemother.ai.model.message.AiResponseMessage;
import com.chen.yuaicodemother.ai.model.message.ToolExecutedMessage;
import com.chen.yuaicodemother.ai.model.message.ToolRequestMessage;
import com.chen.yuaicodemother.model.entity.Agent;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Agent 聊天门面类，提供动态 Agent 对话的统一入口
 */
@Service
@Slf4j
public class AgentChatFacade {

    @Resource
    private AgentChatServiceFactory agentChatServiceFactory;

    /**
     * Agent 对话入口，返回流式响应
     *
     * @param agent       Agent 实体
     * @param userId      用户 ID
     * @param userMessage 用户消息
     * @return Flux<String> 流式响应
     */
    public Flux<String> chat(Agent agent, Long userId, String userMessage) {
        AgentChatService chatService = agentChatServiceFactory.getAgentChatService(agent, userId);
        String memoryId = "agent_chat:" + agent.getId() + ":" + userId;
        TokenStream tokenStream = chatService.chat(memoryId, userMessage);

        return Flux.create(sink -> {
            tokenStream
                    .onPartialResponse((String partialResponse) -> {
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                    })
                    .onCompleteResponse((ChatResponse response) -> {
                        sink.complete();
                    })
                    .onError((Throwable error) -> {
                        log.error("Agent TokenStream 处理异常: {}", error.getMessage(), error);
                        sink.error(error);
                    })
                    .start();
        });
    }
}
