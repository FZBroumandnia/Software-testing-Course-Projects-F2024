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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    int existing_reservation_id() { return 1; }

    int non_existing_reservation_id() { return 2; }

    int a_valid_people_number() { return 10; }
    int a_invalid_people_number() { return -1; }

    String a_valid_date() { return "2020-10-10"; }

    String a_valid_passed_date() { return  a_valid_date(); }

    Map<String, String> valid_reservation_params()
    {
        return Map.of(
                "people", "1",
                "datetime", a_valid_date()+" 00:00"
            );
    }

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

    @Test
    void getCustomerReservations_When_NonExistingCustomer_Then_UserNotFound()
    {
        try{
            when(reservationService.getCustomerReservations(non_existing_customer_id())).thenThrow(new UserNotFound());
            reservationController.getCustomerReservations(non_existing_customer_id());
        }
        catch (Throwable e)
        {
            assertTrue(e.getClass()==ResponseException.class);
            assertTrue(e.getMessage().equals( "User not found." ));
        }
    }

    @Test
    void getCustomerReservations_When_NoAccessCustomer_Then_UserNoAccess()
    {
        try{
            when(reservationService.getCustomerReservations(existing_customer_id())).thenThrow(new UserNoAccess());
            reservationController.getCustomerReservations(existing_customer_id());
        }
        catch (Throwable e)
        {
            assertTrue(e.getClass()==ResponseException.class);
            assertTrue(e.getMessage().equals( "User has no access to this resource." ));
        }
    }

    @Test
    void cancelReservation_When_ValidReservation_Then_success()
    {
        try{
            doNothing().when(reservationService).cancelReservation(existing_reservation_id());
            Response result =reservationController.cancelReservation(existing_reservation_id());
            assertEquals(HttpStatus.OK, result.getStatus());
            assertEquals("reservation cancelled", result.getMessage());
            assertTrue(result.isSuccess());
            verify(reservationService).cancelReservation(existing_reservation_id());
        }
        catch (Throwable e)
        {
            fail();
        }
    }

    @Test
    void cancelReservation_When_NoSuchReservationForUser_Then_ReservationNotFound()
    {
        try{
            doThrow(new ReservationNotFound()).when(reservationService).cancelReservation(non_existing_reservation_id());
            reservationController.cancelReservation(non_existing_reservation_id());
        }
        catch (Throwable e)
        {
            assertTrue(e.getClass()==ResponseException.class);
            assertTrue(e.getMessage().equals( "Reservation not found." ));
        }
    }

    @Test
    void cancelReservation_When_ReservationPassed_Then_ReservationCannotBeCancelled()
    {
        try{
            doThrow(new ReservationCannotBeCancelled()).when(reservationService).cancelReservation(existing_reservation_id());
            reservationController.cancelReservation(existing_reservation_id());
        }
        catch (Throwable e)
        {
            assertTrue(e.getClass()==ResponseException.class);
            assertTrue(e.getMessage().equals( "Reservation cannot be cancelled." ));
        }
    }

    @Test
    void addReservation_When_NonExistingRestaurant_Then_NotFound()
    {
        ResponseException exception = assertThrows(ResponseException.class, () -> {
            reservationController.addReservation(non_existing_restaurant_id(), valid_reservation_params());
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("restaurant not found", exception.getMessage());
        verify(restaurantService).getRestaurant(non_existing_restaurant_id());
        verifyNoInteractions(reservationService);
    }

    @Test
    void addReservation_When_InValidPeopleNum_Then_ParameterBadType()
    {
        stub_set_up_existing_restaurant();
        Map <String, String> InvalidResParams = Map.of(
                "people", "1!",
                "datetime", a_valid_date()
        );
        ResponseException exception = assertThrows(ResponseException.class, () -> {
            reservationController.addReservation(existing_restaurant_id(),  InvalidResParams);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("bad parameter type", exception.getMessage());
    }

    @Test
    void addReservation_When_InValidReservationDate_Then_ParameterBadType()
    {
        stub_set_up_existing_restaurant();
        Map <String, String> InvalidResParams = Map.of(
                "people", "1",
                "datetime", "Invalid Date"
        );
        ResponseException exception = assertThrows(ResponseException.class, () -> {
            reservationController.addReservation(existing_restaurant_id(),  InvalidResParams);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("bad parameter type", exception.getMessage());
    }

    @Test
    void addReservation_When_ValidReservation_Then_success() {
        try {
            stub_set_up_existing_restaurant();
            when(reservationService.reserveTable(anyInt(),anyInt(),any())).thenReturn(mockReservation);
            Response response = reservationController.addReservation(existing_restaurant_id(), valid_reservation_params());

            assertEquals(HttpStatus.OK, response.getStatus());
            assertEquals("reservation done", response.getMessage());
            assertTrue(response.isSuccess());
            assertEquals(mockReservation, response.getData());
            verify(restaurantService).getRestaurant(existing_restaurant_id());
        } catch (Throwable e) {

            fail();
        }
    }

    @Test
    void addReservation_When_NonExistingTable_Then_TableNotFound() {
        try{
            stub_set_up_existing_restaurant();
            when(reservationService.reserveTable(anyInt(),anyInt(),any())).thenThrow(new TableNotFound());
            reservationController.addReservation(existing_restaurant_id(), valid_reservation_params());
        }
        catch (Throwable e)
        {
            assertTrue(e.getClass()==ResponseException.class);
            assertTrue(e.getMessage().equals( "Table not found." ));
        }
    }

    @Test
    void addReservation_When_ReservationTimePassed_Then_DateTimeInThePast() {
        try{
            stub_set_up_existing_restaurant();
            when(reservationService.reserveTable(anyInt(),anyInt(),any())).thenThrow(new DateTimeInThePast());
            reservationController.addReservation(existing_restaurant_id(), valid_reservation_params());
        }
        catch (Throwable e)
        {
            assertTrue(e.getClass()==ResponseException.class);
            assertTrue(e.getMessage().equals( "Date time is before current time." ));
        }
    }

    @Test
    void  addReservation_When_ReservationInCloseTime_Then_ReservationNotInOpenTimes(){
        try{
            stub_set_up_existing_restaurant();
            when(reservationService.reserveTable(anyInt(),anyInt(),any())).thenThrow(new ReservationNotInOpenTimes());
            reservationController.addReservation(existing_restaurant_id(), valid_reservation_params());
        }
        catch (Throwable e)
        {
            assertTrue(e.getClass()==ResponseException.class);
            assertTrue(e.getMessage().equals( "Reservation hour is not within restaurant's open times." ));
        }
    }

    @Test
    void addReservation_When_NotOnWorkingTimes_Then_InvalidWorkingTime() {
        try{
            stub_set_up_existing_restaurant();
            when(reservationService.reserveTable(anyInt(),anyInt(),any())).thenThrow(new InvalidWorkingTime());
            reservationController.addReservation(existing_restaurant_id(), valid_reservation_params());
        }
        catch (Throwable e)
        {
            assertTrue(e.getClass()==ResponseException.class);
            assertTrue(e.getMessage().equals( "Invalid working time." ));
        }
    }

    @Test
    void addReservation_When_ManagerIsReserving_Then_ManagerReservationNotAllowed() {
        try{
            stub_set_up_existing_restaurant();
            when(reservationService.reserveTable(anyInt(),anyInt(),any())).thenThrow(new ManagerReservationNotAllowed());
            reservationController.addReservation(existing_restaurant_id(), valid_reservation_params());
        }
        catch (Throwable e)
        {
            assertTrue(e.getClass()==ResponseException.class);
            assertTrue(e.getMessage().equals( "Manager cannot reserve tables." ));
        }
    }

    @Test
    void addReservation_When_NotLogeInAccount_Then_UserNotFound() {
        try{
            stub_set_up_existing_restaurant();
            when(reservationService.reserveTable(anyInt(),anyInt(),any())).thenThrow(new UserNotFound());
            reservationController.addReservation(existing_restaurant_id(), valid_reservation_params());
        }
        catch (Throwable e)
        {
            assertTrue(e.getClass()==ResponseException.class);
            assertTrue(e.getMessage().equals( "User not found." ));
        }
    }

    @Test
    void getAvailableTimes_When_NonExistingRestaurant_Then_NotFound()
    {
        ResponseException exception = assertThrows(ResponseException.class, () -> {
            reservationController.getAvailableTimes(non_existing_reservation_id(),  a_valid_people_number(), a_valid_date());
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("restaurant not found", exception.getMessage());
    }

    @Test
    void getAvailableTimes_When_InValidDate_Then_ParameterBadType()
    {
        stub_set_up_existing_restaurant();
        ResponseException exception = assertThrows(ResponseException.class, () -> {
            reservationController.getAvailableTimes(existing_reservation_id(),  a_valid_people_number(), "invalid date");
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("bad parameter type", exception.getMessage());
    }

    @Test
    void getAvailableTimes_When_ValidParameters_Then_success() {
        try {
            stub_set_up_existing_restaurant();
            List<LocalTime> happy_times = new ArrayList<>(List.of(LocalTime.now(), LocalTime.now()));
            when(reservationService.getAvailableTimes(anyInt(),anyInt(),any())).thenReturn(happy_times);
            Response response = reservationController.getAvailableTimes(existing_reservation_id(),  a_valid_people_number(), a_valid_date());

            assertEquals(HttpStatus.OK, response.getStatus());
            assertEquals("available times", response.getMessage());
            assertTrue(response.isSuccess());
            assertEquals(happy_times, response.getData());
            verify(restaurantService).getRestaurant(existing_restaurant_id());
        } catch (Throwable e) {

            fail();
        }
    }

    @Test
    void getAvailableTimes_When_DatePassed_Then_DateTimeInThePast() {
        try{
            stub_set_up_existing_restaurant();
            when(reservationService.getAvailableTimes(anyInt(),anyInt(),any())).thenThrow(new DateTimeInThePast());
            reservationController.getAvailableTimes(existing_reservation_id(),  a_valid_people_number(), a_valid_passed_date());
        }
        catch (Throwable e)
        {
            assertTrue(e.getClass()==ResponseException.class);
            assertTrue(e.getMessage().equals( "Date time is before current time." ));
        }
    }

    @Test
    void getAvailableTimes_When_InvalidPeopleNum_Then_BadPeopleNumber() {
        try{
            stub_set_up_existing_restaurant();
            when(reservationService.getAvailableTimes(anyInt(),anyInt(),any())).thenThrow(new DateTimeInThePast());
            reservationController.getAvailableTimes(existing_reservation_id(),  a_invalid_people_number(), a_valid_passed_date());
        }
        catch (Throwable e)
        {
            assertTrue(e.getClass()==ResponseException.class);
            assertTrue(e.getMessage().equals( "Date time is before current time." ));
        }
    }

}
