package mizdooni.controllers;

import mizdooni.model.*;
import mizdooni.service.*;
import mizdooni.response.*;
import mizdooni.exceptions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;

import static mizdooni.controllers.ControllerUtils.DATE_FORMATTER;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class ReservationControllerTest {

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReservationController reservationController;

    @Mock
    private Restaurant ExistingRestaurant;

    @Mock
    private Reservation mockReservation;


    int existing_restaurant_id() {
        return 1;
    }

    int non_existing_restaurant_id() {
        return 9;
    }

    int existing_table_id() { return 2; }

    int non_existing_table_id() { return 10; }

    int existing_customer_id() { return 2; }

    int non_existing_customer_id() { return 1; }

    String a_valid_date() { return "2020-10-10"; }

    void stub_set_up_existing_restaurant() {
        lenient().when(ExistingRestaurant.getName()).thenReturn("existing restaurant");
        lenient().when(ExistingRestaurant.getId()).thenReturn(existing_restaurant_id());
        when(restaurantService.getRestaurant(existing_restaurant_id())).thenReturn(ExistingRestaurant);
    }

    @Test
    void getReservations_When_NonExistingRestaurant_Then_NotFound() {
        ResponseException exception = assertThrows(ResponseException.class, () -> {
            reservationController.getReservations(non_existing_restaurant_id(), non_existing_table_id(), "");
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("restaurant not found", exception.getMessage());
        verify(restaurantService).getRestaurant(non_existing_restaurant_id());
        verifyNoInteractions(reservationService);
    }

    @Test
    void getReservations_When_NonExistingTable_Then_TableNotFound() {
        try{
            stub_set_up_existing_restaurant();
            when(reservationService.getReservations(anyInt(),anyInt(),any())).thenThrow(new TableNotFound());
            reservationController.getReservations(existing_restaurant_id(), non_existing_table_id(), a_valid_date());
        }
        catch (Throwable e)
        {
            assertTrue(e.getClass()==ResponseException.class);
            assertTrue(e.getMessage().equals( "Table not found." ));
        }
    }

    @Test
    void getReservations_When_NotManagerUser_Then_UserNotManager() {
        try{
            stub_set_up_existing_restaurant();
            when(reservationService.getReservations(anyInt(),anyInt(),any())).thenThrow(new UserNotManager());
            reservationController.getReservations(existing_restaurant_id(), existing_table_id(), a_valid_date());
        }
        catch (Throwable e)
        {
            assertTrue(e.getClass()==ResponseException.class);
            assertTrue(e.getMessage().equals( "User is not a manager." ));
        }
    }

    @Test
    void getReservations_When_InvalidManager_Then_InvalidManagerRestaurant() {
        try{
            stub_set_up_existing_restaurant();
            when(reservationService.getReservations(anyInt(),anyInt(),any())).thenThrow(new InvalidManagerRestaurant());
            reservationController.getReservations(existing_restaurant_id(), existing_table_id(), a_valid_date());
        }
        catch (Throwable e)
        {
            assertTrue(e.getClass()==ResponseException.class);
            assertTrue(e.getMessage().equals( "The manager is not valid for this restaurant." ));
        }
    }

    @Test
    void getReservations_When_InValidDate_Then_ParameterBadType()
    {
        stub_set_up_existing_restaurant();
        ResponseException exception = assertThrows(ResponseException.class, () -> {
            reservationController.getReservations(existing_restaurant_id(), existing_table_id(), "invalid date");
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("bad parameter type", exception.getMessage());
    }

    @Test
    void getReservations_When_ValidReservation_Then_success() {
        try {
            stub_set_up_existing_restaurant();
            List<Reservation> reservations = new ArrayList<>(List.of(mockReservation));
            when(reservationService.getReservations(anyInt(),anyInt(),any())).thenReturn(reservations);
            Response response = reservationController.getReservations(existing_restaurant_id(), existing_table_id(), a_valid_date());

            assertEquals(HttpStatus.OK, response.getStatus());
            assertEquals("restaurant table reservations", response.getMessage());
            assertTrue(response.isSuccess());
            assertEquals(reservations, response.getData());
            verify(restaurantService).getRestaurant(existing_restaurant_id());
            verify(reservationService).getReservations(existing_restaurant_id(), existing_table_id(), LocalDate.parse(a_valid_date(), DATE_FORMATTER));
        } catch (Throwable e) {
            fail();
        }
    }

    @Test
    void getCustomerReservations_When_ValidCustomer_Then_success()
    {
        try{
            List<Reservation> reservations = new ArrayList<>(List.of(mockReservation));
            when(reservationService.getCustomerReservations(existing_customer_id())).thenReturn(reservations);
            Response result =reservationController.getCustomerReservations(existing_customer_id());
            assertEquals(HttpStatus.OK, result.getStatus());
            assertEquals("user reservations", result.getMessage());
            assertTrue(result.isSuccess());
            assertEquals(reservations, result.getData());
            verify(reservationService).getCustomerReservations(existing_customer_id());
        }
        catch (Throwable e)
        {
            fail();
        }
    }
}
