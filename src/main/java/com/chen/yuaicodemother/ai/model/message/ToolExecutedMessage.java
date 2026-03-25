package com.chen.yuaicodemother.ai.model.message;


import dev.langchain4j.service.tool.ToolExecution;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ToolExecutedMessage extends StreamMessage{
    private String id;

    private String name;

    private String arguments;

    private String result;

    public ToolExecuteMessage(ToolExecution toolExecution) {
        super(StreamMessageTypeEnum.TOOL_EXECUTED.getText());
        this.id=toolExecution.request().id();
        this.name=toolExecution.request().name();
        this.arguments=toolExecution.request().arguments();
        this.result=toolExecution.result();
    }
}
