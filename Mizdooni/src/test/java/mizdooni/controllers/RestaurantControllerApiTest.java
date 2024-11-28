package mizdooni.controllers;

import mizdooni.model.Address;
import mizdooni.model.Restaurant;
import mizdooni.model.User;
import mizdooni.service.RestaurantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static mizdooni.controllers.ControllerUtils.PARAMS_BAD_TYPE;
import static mizdooni.controllers.ControllerUtils.PARAMS_MISSING;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
public class RestaurantControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestaurantService restaurantService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        Mockito.reset(restaurantService);
    }

    private Restaurant createMockRestaurant(int restaurantId) {
        Address restaurantAddress = new Address("SampleCountry", "SampleCity", "SampleStreet");
        User restaurantManager = new User("sampleManager", "securePassword", "manager@sample.com", restaurantAddress, User.Role.manager);
        return new Restaurant(
                "Sample Restaurant",
                restaurantManager,
                "Sample Cuisine",
                LocalTime.of(9, 0),
                LocalTime.of(21, 0),
                "Sample Description",
                restaurantAddress,
                "/sample-image.jpg"
        );
    }

    @Test
    @DisplayName("Test getRestaurant() with valid restaurantId")
    public void testGetRestaurantWithValidId() throws Exception {
        int restaurantId = 1;
        Restaurant restaurant = createMockRestaurant(restaurantId);
        when(restaurantService.getRestaurant(restaurantId)).thenReturn(restaurant);

        mockMvc.perform(get("/restaurants/{restaurantId}", restaurantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("restaurant found"))
                .andExpect(jsonPath("$.data.name").value(restaurant.getName()));
    }

    @Test
    @DisplayName("Test getRestaurant() with invalid restaurantId")
    public void testGetRestaurantWithInvalidId() throws Exception {
        int restaurantId = 999;
        when(restaurantService.getRestaurant(restaurantId)).thenReturn(null);

        mockMvc.perform(get("/restaurants/{restaurantId}", restaurantId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("restaurant not found"));
    }

    @Test
    @DisplayName("Test addRestaurant() with valid input")
    public void testAddRestaurantWithValidInput() throws Exception {
        Map<String, Object> validRestaurantData = createValidRestaurantData();
        when(restaurantService.addRestaurant(
                validRestaurantData.get("name").toString(),
                validRestaurantData.get("type").toString(),
                LocalTime.parse(validRestaurantData.get("startTime").toString()),
                LocalTime.parse(validRestaurantData.get("endTime").toString()),
                validRestaurantData.get("description").toString(),
                createAddressFromData(validRestaurantData),
                validRestaurantData.get("image").toString()
        )).thenReturn(1);

        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRestaurantData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("restaurant added"));
    }

    @Test
    @DisplayName("Test addRestaurant() with missing required fields")
    public void testAddRestaurantWithMissingRequiredFields() throws Exception {
        Map<String, Object> incompleteRestaurantData = createValidRestaurantData();
        incompleteRestaurantData.remove("name");

        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incompleteRestaurantData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(PARAMS_MISSING));
    }

    @Test
    @DisplayName("Test addRestaurant() with invalid time format")
    public void testAddRestaurantWithInvalidTimeFormat() throws Exception {
        Map<String, Object> invalidTimeRestaurantData = createValidRestaurantData();
        invalidTimeRestaurantData.put("startTime", "25:00");

        mockMvc.perform(post("/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTimeRestaurantData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(PARAMS_BAD_TYPE));
    }

    @Test
    @DisplayName("Test validateRestaurantName() with existing name")
    public void testValidateRestaurantNameWithExistingName() throws Exception {
        String restaurantName = "Existing Restaurant";
        when(restaurantService.restaurantExists(restaurantName)).thenReturn(true);

        mockMvc.perform(get("/validate/restaurant-name")
                        .param("data", restaurantName))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("restaurant name is taken"));
    }

    @Test
    @DisplayName("Test validateRestaurantName() with unique name")
    public void testValidateRestaurantNameWithUniqueName() throws Exception {
        String restaurantName = "Unique Restaurant";
        when(restaurantService.restaurantExists(restaurantName)).thenReturn(false);

        mockMvc.perform(get("/validate/restaurant-name")
                        .param("data", restaurantName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("restaurant name is available"));
    }

    private Map<String, Object> createValidRestaurantData() {
        Map<String, Object> restaurantData = new HashMap<>();
        restaurantData.put("name", "Sample Restaurant");
        restaurantData.put("type", "Mediterranean");
        restaurantData.put("startTime", "09:00");
        restaurantData.put("endTime", "21:00");
        restaurantData.put("description", "A delightful place to dine");
        restaurantData.put("image", "/default-image.jpg");

        Map<String, String> addressData = new HashMap<>();
        addressData.put("country", "SampleLand");
        addressData.put("city", "SampleTown");
        addressData.put("street", "123 Dining Ave");
        restaurantData.put("address", addressData);

        return restaurantData;
    }

    private Address createAddressFromData(Map<String, Object> restaurantData) {
        Map<String, String> addressData = (Map<String, String>) restaurantData.get("address");
        return new Address(
                addressData.get("country"),
                addressData.get("city"),
                addressData.get("street")
        );
    }
}
