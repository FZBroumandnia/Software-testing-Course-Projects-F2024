package mizdooni.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import mizdooni.model.Restaurant;
import mizdooni.model.Table;
import mizdooni.model.User;
import mizdooni.model.Address;
import mizdooni.service.RestaurantService;
import mizdooni.service.TableService;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.http.MediaType;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static mizdooni.controllers.ControllerUtils.PARAMS_BAD_TYPE;
import static mizdooni.controllers.ControllerUtils.PARAMS_MISSING;
import static org.apache.logging.log4j.ThreadContext.isEmpty;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
public class TableControllerTest {


    final private String RESTAURANT_NOT_FOUND = "restaurant not found";

    final private String TABLE_LIST = "tables listed";

    @MockBean
    private RestaurantService restaurantService;

    @MockBean
    private TableService tableService;


    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        Mockito.reset(restaurantService, tableService);
    }

    private void stubRestaurantServiceForavalidRestaurant(int validRestaurantId)
    {
        when(restaurantService.getRestaurant(validRestaurantId)).thenReturn(createRestaurant());
    }

    private Restaurant createRestaurant() {
        Address address =  Mockito.mock(Address.class);
        User manager = Mockito.mock(User.class);
        return new Restaurant("name", manager, "Fast Food", LocalTime.of(9, 0), LocalTime.of(22, 0), "Test description", address, "/placeholder.jpg");
    }

    private Table createTable(int tableNumber, int restaurantId, int seatsNumber) {
        return new Table(tableNumber, restaurantId, seatsNumber);
    }

    @Test
    public void getTables_When_NoTable_Then_Empty() throws Exception {
        int validRestaurantId = 1;
        stubRestaurantServiceForavalidRestaurant(validRestaurantId);
        when(tableService.getTables(validRestaurantId)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/tables/{restaurantId}", validRestaurantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(TABLE_LIST))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    public void getTables_When_validSituation_Then_Successful() throws Exception {
        int validRestaurantId = 1;
        List<Table> tables = List.of( createTable(1, validRestaurantId, 4), createTable(2, validRestaurantId, 6));
        stubRestaurantServiceForavalidRestaurant(validRestaurantId);
        when(tableService.getTables(validRestaurantId)).thenReturn(tables);

        mockMvc.perform(get("/tables/{restaurantId}", validRestaurantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(TABLE_LIST))
                .andExpect(jsonPath("$.data.length()").value(tables.size()))
                .andExpect(jsonPath("$.data[0].tableNumber").value(tables.get(0).getTableNumber()))
                .andExpect(jsonPath("$.data[0].seatsNumber").value(tables.get(0).getSeatsNumber()))
                .andExpect(jsonPath("$.data[1].tableNumber").value(tables.get(1).getTableNumber()))
                .andExpect(jsonPath("$.data[1].seatsNumber").value(tables.get(1).getSeatsNumber()));
    }

    @Test
    public void getTables_When_nonExistingRestaurant_Then_restaurantNotFound() throws Exception {
        int restaurantId = 999;
        when(restaurantService.getRestaurant(restaurantId)).thenReturn(null);

        mockMvc.perform(get("/tables/{restaurantId}", restaurantId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(RESTAURANT_NOT_FOUND));
    }
}
