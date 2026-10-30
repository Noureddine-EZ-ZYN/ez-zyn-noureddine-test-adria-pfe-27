package ma.adria.bank.account;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AccountApiTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    private String createAccount(double balance) throws Exception {
        String body = mvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerReference\":\"CUST-1\",\"currency\":\"eur\",\"initialBalance\":" + balance + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andReturn().getResponse().getContentAsString();
        JsonNode json = mapper.readTree(body);
        return json.get("id").asText();
    }

    @Test
    void createAndGetAccount() throws Exception {
        String id = createAccount(100);
        mvc.perform(get("/api/accounts/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100.0));
    }

    @Test
    void debitUpdatesBalance() throws Exception {
        String id = createAccount(100);
        mvc.perform(post("/api/accounts/" + id + "/debit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":40}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(60.0));
    }

    @Test
    void debitRejectedWhenInsufficientFunds() throws Exception {
        String id = createAccount(10);
        mvc.perform(post("/api/accounts/" + id + "/debit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":50}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void blockedAccountRejectsOperations() throws Exception {
        String id = createAccount(100);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/accounts/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"BLOCKED\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/accounts/" + id + "/credit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":5}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void unknownAccountReturnsNotFound() throws Exception {
        mvc.perform(get("/api/accounts/" + java.util.UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
