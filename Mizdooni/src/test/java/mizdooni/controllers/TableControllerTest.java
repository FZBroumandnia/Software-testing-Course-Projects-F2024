package mizdooni.controllers;

import mizdooni.model.Table;
import mizdooni.service.RestaurantService;
import mizdooni.service.TableService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TableControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestaurantService restaurantService;

    @MockBean
    private TableService tableService;

    @Test
    void testGetTables() throws Exception {
        List<Table> mockTables = List.of(new Table(1, 4, 10), new Table(2, 6, 10));
        Mockito.doNothing().when(restaurantService).restaurantExists(anyString());
        Mockito.when(tableService.getTables(anyInt())).thenReturn(mockTables);

        mockMvc.perform(get("/tables/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("tables listed"))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].seatsNumber").value(4));
    }

    @Test
    void testAddTable() throws Exception {
        Mockito.doNothing().when(restaurantService).restaurantExists(anyString());
        Mockito.doNothing().when(tableService).addTable(anyInt(), anyInt());

        mockMvc.perform(post("/tables/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seatNumber\":\"4\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("table added"));
    }

    @Test
    void testAddTable_MissingSeatNumber() throws Exception {
        mockMvc.perform(post("/tables/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("PARAMS_MISSING"));
    }

    @Test
    void testAddTable_InvalidSeatNumber() throws Exception {
        mockMvc.perform(post("/tables/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seatNumber\":\"invalid\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("PARAMS_BAD_TYPE"));
    }
}