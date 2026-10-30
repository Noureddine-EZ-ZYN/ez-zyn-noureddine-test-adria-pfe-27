package ma.adria.bank.assistant.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatClient agent;

    public ChatService(ChatClient bankAgent) {
        this.agent = bankAgent;
    }

    public String chat(String conversationId, String message) {
        try {
            return agent.prompt()
                    .user(message)
                    .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .call()
                    .content();
        } catch (RuntimeException ex) {
            throw new AgentException(ex.getMessage(), ex);
        }
    }
}
