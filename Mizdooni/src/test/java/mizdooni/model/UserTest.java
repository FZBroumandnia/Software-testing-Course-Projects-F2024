package mizdooni.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

public class UserTest {
    private User user;
    private Address address;
    @Mock
    Restaurant restaurant;
    private Reservation reservation;

    @BeforeEach
    public void set_up() {
        address = new Address("Iran", "Isfahan", "Charbagh");
        User manager = new User("testManager", "123", "test@example.com", address, User.Role.manager);
        restaurant = new Restaurant("Test Restaurant", manager, "Foodcourt", LocalTime.now(), LocalTime.now().plusHours(6),
                "description", address, "Link");
        user = new User("testUser", "1234", "test@example.com", address, User.Role.client);
        reservation = new Reservation(user, restaurant, new Table(1, 2, 3), LocalDateTime.now().minusDays(1));
    }

    @Test
    public void addReservation_When_TableAdded_Then_Exist() {
        user.addReservation(reservation);
        List<Reservation> reservations = user.getReservations();
        assertEquals(1, reservations.size());
        assertEquals(reservation, reservations.get(0));
    }

    @Test
    public void checkReserved_When_ExistingReservation_Then_True() {
        user.addReservation(reservation);

        assertTrue(user.checkReserved(restaurant));
    }

    @Test
    public void checkReserved_When_ExistingReservatioIsCanceled_Then_False() {
        reservation.cancel();
        user.addReservation(reservation);

        assertFalse(user.checkReserved(restaurant));
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> provideReservations() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(true, true),
                org.junit.jupiter.params.provider.Arguments.of(false, false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideReservations")
    public void checkReserved_When_Parametrized_Then_Equal(boolean hasReservation, boolean expectedResult) {
        if (hasReservation) {
            user.addReservation(reservation);
        }
        assertEquals(expectedResult, user.checkReserved(restaurant));
    }


    @Test
    public void getReservation_When_ReservationIsAdded_Then_Exist() {
        user.addReservation(reservation);

        Reservation found = user.getReservation(0);
        assertNotNull(found);
        assertEquals(reservation, found);
    }

    @Test
    public void getReservation_When_ReservationIsCancelled_Then_Null() {
        reservation.cancel();
        user.addReservation(reservation);

        assertNull(user.getReservation(0));
    }

    @Test
    public void getReservation_When_ReservationIsNotAdded_Then_Null() {
        assertNull(user.getReservation(3));
    }

    @Test
    public void checkPassword_When_CorrectPassword_Then_True() {
        assertTrue(user.checkPassword("1234"));
    }

    @Test
    public void checkPassword_When_IncorrectPassword_Then_False() {
        assertFalse(user.checkPassword("wrongpassword"));
    }

    @ParameterizedTest
    @CsvSource({
            "1234, true",
            "wrongpassword, false"
    })
    public void check_password_When_Parametrized_Then_Equal(String inputPassword, boolean expectedResult) {
        assertEquals(expectedResult, user.checkPassword(inputPassword));
    }

    @ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"client", "manager"})
    public void role_assignment_should_assign_role_correctly(String role) {
        User.Role expectedRole = User.Role.valueOf(role);
        User newUser = new User("newUser", "newPassword", "new@example.com", address, expectedRole);
        assertEquals(expectedRole, newUser.getRole());
    }

}
