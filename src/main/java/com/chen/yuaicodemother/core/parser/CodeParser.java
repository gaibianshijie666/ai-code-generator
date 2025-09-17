package com.chen.yuaicodemother.core.parser;

public interface CodeParser<T> {

    /**
     * 解析代码内容
     * @param codeContent
     * @return
     */
    T parseCode(String codeContent);
}
