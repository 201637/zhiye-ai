package com.example.zhiyeaiagent.app;


import com.example.zhiyeaiagent.advisor.MyLoggerAdvisor;
import com.example.zhiyeaiagent.chatmemory.FileBasedChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class ZhiYeApp {
    private final ChatClient chatClient;
    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案";

    /**
     * 初始化 chatClient
     * @param dashcoscopeChatModel
     */
    public ZhiYeApp(ChatModel dashcoscopeChatModel) {
        //基于文件的会话记忆
        String dir = System.getProperty("user.dir") + "/tmp/chat-memory";
        ChatMemory chatMemory=new FileBasedChatMemory(dir);
//        //初始化基于内存的对话记忆
//        ChatMemory chatMemory=new InMemoryChatMemory();
        chatClient=ChatClient.builder(dashcoscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        //自定义日志拦截器Advisor，可按需开启
                        new MyLoggerAdvisor()
//                        //自定义推理增强 Advisor, 可按需开启
//                        ,new ReReadingAdvisor()
                ).build();
    }

    /**
     * Ai 基础对话(多轮记忆对话)
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient.prompt(message)
                .advisors(sepc -> sepc.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        //得到的结果输出成文本
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * Ai 基础对话(多轮记忆对话,SSE 流式传输)
     * @param message
     * @param chatId
     * @return
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient.prompt(message)
                .advisors(sepc -> sepc.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                //流式传输
                .stream()
                .content();
        //起到监听，打印输出（打字机效果）
//        contented.subscribe( content -> log.info("content: {}", content));
    }
}
