package mizdooni.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import mizdooni.service.RestaurantService;
import mizdooni.service.TableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TableControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private TableService tableService;

    private String baseUrl;
    private RestTemplate restTemplate;

    @BeforeEach
    public void setUp() {
        this.baseUrl = "http://localhost:" + port + "/tables";
        this.restTemplate = new RestTemplate();
    }

    @Test
    @Sql(scripts = "/testdata/insert_restaurant.sql") // Insert a sample restaurant for testing
    public void testGetTables_ValidRestaurantId() {
        int validRestaurantId = 1; // Matches the ID inserted by the test SQL script
        String url = baseUrl + "/" + validRestaurantId;

        var response = restTemplate.getForEntity(url, String.class);
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).contains("tables listed");
    }

    @Test
    public void testGetTables_InvalidRestaurantId() {
        int invalidRestaurantId = 999;
        String url = baseUrl + "/" + invalidRestaurantId;

        var response = restTemplate.getForEntity(url, String.class);
        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(response.getBody()).contains("restaurant not found");
    }

    @Test
    @Sql(scripts = "/testdata/insert_restaurant.sql")
    public void testAddTable_ValidInput() throws Exception {
        int validRestaurantId = 1;
        String url = baseUrl + "/" + validRestaurantId;

        Map<String, String> requestBody = Map.of("seatsNumber", "4");
        String requestJson = objectMapper.writeValueAsString(requestBody);

        var response = restTemplate.postForEntity(url, requestJson, String.class);
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).contains("table added");
    }

    @Test
    public void testAddTable_InvalidSeatsNumber() throws Exception {
        int validRestaurantId = 1; // Assume this is a valid ID
        String url = baseUrl + "/" + validRestaurantId;

        Map<String, String> requestBody = Map.of("seatsNumber", "invalid");
        String requestJson = objectMapper.writeValueAsString(requestBody);

        var response = restTemplate.postForEntity(url, requestJson, String.class);
        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(response.getBody()).contains("params bad type");
    }

    @Test
    public void testAddTable_MissingSeatsNumber() throws Exception {
        int validRestaurantId = 1; // Assume this is a valid ID
        String url = baseUrl + "/" + validRestaurantId;

        Map<String, String> requestBody = Map.of(); // Missing seatsNumber
        String requestJson = objectMapper.writeValueAsString(requestBody);

        var response = restTemplate.postForEntity(url, requestJson, String.class);
        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(response.getBody()).contains("params missing");
    }
}