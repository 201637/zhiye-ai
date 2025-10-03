package com.example.zhiyeaiagent.controller;

import com.example.zhiyeaiagent.agent.CommonManus;
import com.example.zhiyeaiagent.app.ZhiYeApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {
    @Resource
    private ZhiYeApp zhiYeApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    /**
     * 同步调用恋爱大师应用
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("/loveapp/chat/sync")
    public String doChatWithLoveAppSync(String message, String chatId) {
        return zhiYeApp.doChat(message, chatId);
    }

    /**
     * SSE流式调用恋爱大师应用
     * @param message
     * @param chatId
     * @return
     * 第一种，直接返回Flux<String>    必须设置响应类型为MediaType.TEXT_EVENT_STREAM_VALUE，文本流式返回
     */
    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(String message, String chatId) {
        return zhiYeApp.doChatByStream(message, chatId);
    }

    /**
     * SSE流式调用恋爱大师应用
     * @param message
     * @param chatId
     * @return
     * 第二种
     */
    @GetMapping(value = "/love_app/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppServerSentEvent(String message, String chatId) {
        return zhiYeApp.doChatByStream(message, chatId)
                .map(chunk->ServerSentEvent.<String>builder().data(chunk).build());
    }

    /**
     * SSE流式调用恋爱大师应用
     * @param message
     * @param chatId
     * @return
     * 第三种
     */
    @GetMapping(value = "/love_app/chat/sse_emitter")
    public SseEmitter doChatWithLoveAppSSEEmitter(String message, String chatId) {
        //初始化，设置3分钟超时
        SseEmitter sseEmitter = new SseEmitter(180000L);
        //获取 Flux 响应式数据流并且直接通过订阅推送给 SSEEmitter
        zhiYeApp.doChatByStream(message, chatId)
                .subscribe(chunk-> {
                    try {
                        sseEmitter.send(chunk);
                    } catch (IOException e) {
                        sseEmitter.completeWithError(e);
                    }
                },sseEmitter::completeWithError,sseEmitter::complete);
        return sseEmitter;
    }

    /**
     * 流式调用超级智能体
     * @param message
     * @return
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        //不同用户使用不同的智能体，否则会出现多个用户调用一个智能体时，会阻塞
        CommonManus commonManus = new CommonManus(allTools,dashscopeChatModel);
        return commonManus.runStream(message);
    }
}
