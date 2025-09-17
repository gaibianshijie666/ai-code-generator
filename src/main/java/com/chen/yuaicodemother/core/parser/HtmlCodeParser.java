package com.chen.yuaicodemother.core.parser;

import com.chen.yuaicodemother.ai.model.HtmlCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HtmlCodeParser implements CodeParser<HtmlCodeResult> {

    private static final Pattern HTML_CODE_PATTERN
            = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    @Override
    public HtmlCodeResult parseCode(String codeContent) {
        HtmlCodeResult result = new HtmlCodeResult();

//        提取html代码
        String htmlCode=extractHtmlCode(codeContent);
        if(htmlCode!=null&&!htmlCode.trim().isEmpty()){
            result.setHtmlCode(htmlCode);
        }else {
//            如果没有找到代码块，那么将整个内容作为HTML
            result.setHtmlCode(codeContent.trim());
        }
        return result;
    }

    private String extractHtmlCode(String content) {
        Matcher matcher = HTML_CODE_PATTERN.matcher(content);
        if(matcher.find()){
            return matcher.group(1);
        }
        return null;
    }
}
