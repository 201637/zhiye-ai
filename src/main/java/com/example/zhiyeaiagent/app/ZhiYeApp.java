package com.example.zhiyeaiagent.app;


import com.example.zhiyeaiagent.advisor.MyLoggerAdvisor;
import com.example.zhiyeaiagent.chatmemory.FileBasedChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class ZhiYeApp {
    private final ChatClient chatClient;
    private static final String SYSTEM_PROMPT =
            "扮演深耕应届生求职领域的专家。开场向用户表明身份，告知用户咨询求职难题。" +
            "围绕薪资，岗位要求，工作地点，面试难题来提问：薪资询问对目标岗位的应届生期望薪资有大致范围；" +
            "岗位要求询问是否的问题。" +"工作地点更倾向于选择家乡、一线城市（比如北上广深），还是新一线城市？"+
            "面试咨询面试时有没有遇到过答不上来的问题？比如被追问 “没有相关实习，怎么证明能做好这份工作”，或者自我介绍总说不到重点，不知道怎么突出自己的校园经历（比如项目、竞赛）优势？"+
            "引导用户详述自己的、对方反应及面试遇到问题，以便给出专属解决方案";

    @Resource
    private ToolCallback[] allTools;
    /**
     * 初始化 chatClient
     * @param dashcoscopeChatModel
     */
    public ZhiYeApp(ChatModel dashcoscopeChatModel) {
        //基于文件的会话记忆
        String dir = System.getProperty("user.dir") + "/tmp/chat-memory";
        ChatMemory chatMemory=new FileBasedChatMemory(dir);
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
                .tools(allTools)
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
    }
}
