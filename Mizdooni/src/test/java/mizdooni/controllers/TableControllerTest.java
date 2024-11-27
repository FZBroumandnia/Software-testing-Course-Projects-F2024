package mizdooni.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TableControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetTables() throws Exception {
        mockMvc.perform(get("/tables/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("tables listed"));
    }

    @Test
    void testAddTable() throws Exception {
        mockMvc.perform(post("/tables/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seatNumber\":\"4\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("table added"));
    }

}
