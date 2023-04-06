package Server.Model;

import com.google.gson.annotations.Expose;

import java.util.*;
import java.lang.*;

public class ChatRoom {
    @Expose
    private final Stack<ChatMessage> flow;

    public ChatRoom() {
        this.flow = new Stack<ChatMessage>();
    }

    public synchronized void addMessage(ChatMessage message){
        this.flow.add(message);
    }

    public synchronized List<String> getHistory(int last){
        List<String> history = new ArrayList<String>();
        for(ChatMessage message : flow) history.add(message.toString());
        return history;
    }

    public Stack<ChatMessage> getFlow() {
        return flow;
    }
}
