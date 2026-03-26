package com.chen.yuaicodemother.ai.model.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**（
 *Ai响应信息
 *
 */
@EqualsAndHashCode(callSuper=true)
@Data
@NoArgsConstructor
public class AiResponseMessage extends StreamMessage {
    private String data;

    public AiResponseMessage(String data){
        super(StreamMessageTypeEnum.AI_RESPONSE.getValue());
        this.data = data;
    }
}
