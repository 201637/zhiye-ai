package com.example.zhiyeaiagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class FileBasedChatMemory implements ChatMemory {
    //指定文件存储路径
    private final String Base_DIR;

    private static final Kryo kryo=new Kryo();

    static {
        //kryo有两种注册，一个手动（麻烦但高效），一个自动（简单但性能不如手动）
        //取消手动注册
        kryo.setRegistrationRequired(false);
        //设置实例化策略
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }
    //构造对象时，指定文件保存目录
    public FileBasedChatMemory(String dir) {
        Base_DIR = dir;
        //判断文件是否存在
        File file = new File(dir);
        //如果不存在，创建一个
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 添加一条消息
     * @param conversationId
     * @param message
     */
    @Override
    public void add(String conversationId, Message message) {
        // 直接调用列表版本的add方法，避免覆盖历史消息
        add(conversationId, List.of(message));
    }

    /**
     * 添加多条消息
     * @param conversationId
     * @param messages
     */
    @Override
    public void add(String conversationId, List messages) {
        List messagesList = getOrCreateConversation(conversationId);
        messagesList.addAll(messages);
        saveConversation(conversationId, messagesList);
    }

    /**
     * 获取会话消息的最后几条 几为lastN
     * @param conversationId
     * @param lastN
     * @return
     */
    @Override
    public List<Message> get(String conversationId, int lastN) {
        //先获取往期所有消息
        List<Message> messageList = getOrCreateConversation(conversationId);
        return messageList.stream()
                //总消息数-最后几条，就是要跳过的消息数
                .skip(Math.max(0,messageList.size() - lastN))
                .toList();
    }

    /**
     * 通过id清空该会话消息
     * @param conversationId
     */
    @Override
    public void clear(String conversationId) {
        File file = getConversationFile(conversationId);
        if(file.exists()){
            file.delete();
        }
    }


    /**
     * 获取或创建会话消息的列表
     */
    public List<Message> getOrCreateConversation(String conversationId) {
        File file = getConversationFile(conversationId);
        List<Message> messages = new ArrayList<>();
        if(file.exists()){
            try(Input input= new Input(new FileInputStream( file))) {
                messages = kryo.readObject(input, ArrayList.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return messages;
    }

    /**
     * 保存会话消息
     */
    public void saveConversation(String conversationId, List<Message> messages) {
        File file = getConversationFile(conversationId);
        if (!file.exists()){
            try(Output output = new Output(new FileOutputStream( file))) {
                kryo.writeObject(output, messages);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 每个会话文件单独保存
     */
    public File getConversationFile(String conversationId) {
        return new File(Base_DIR ,conversationId + ".kryo");
    }
}
