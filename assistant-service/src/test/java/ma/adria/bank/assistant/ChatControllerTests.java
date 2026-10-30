package ma.adria.bank.assistant;

import ma.adria.bank.assistant.service.AgentException;
import ma.adria.bank.assistant.service.ChatService;
import ma.adria.bank.assistant.web.rest.ChatController;
import ma.adria.bank.assistant.web.rest.RestExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@Import(RestExceptionHandler.class)
class ChatControllerTests {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ChatService service;

    @Test
    void returnsAgentReplyAndKeepsConversationId() throws Exception {
        given(service.chat(eq("conv-1"), eq("Quel est le solde ?"))).willReturn("Le solde est de 500 EUR.");

        mvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Quel est le solde ?\",\"conversationId\":\"conv-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value("conv-1"))
                .andExpect(jsonPath("$.reply").value("Le solde est de 500 EUR."));
    }

    @Test
    void generatesConversationIdWhenMissing() throws Exception {
        given(service.chat(any(), eq("Bonjour"))).willReturn("Bonjour !");

        mvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Bonjour\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").isNotEmpty());
    }

    @Test
    void blankMessageIsRejected() throws Exception {
        mvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void llmFailureReturnsBadGateway() throws Exception {
        given(service.chat(any(), any())).willThrow(new AgentException("clé API invalide", null));

        mvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Bonjour\"}"))
                .andExpect(status().isBadGateway());
    }
}
