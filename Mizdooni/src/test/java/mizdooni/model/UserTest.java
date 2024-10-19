package mizdooni.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;
public class UserTest {
    private User user;
    private Address address;
    @Mock
    Restaurant restaurant;
    private Reservation reservation1;
    private Reservation reservation2;

    @BeforeEach
    public void set_up() {
        address = new Address("Iran", "Isfahan", "Charbagh");
        User manager = new User("testManager", "123", "test@example.com", address, User.Role.manager);
       restaurant = new Restaurant("Test Restaurant",manager, "Foodcourt", LocalTime.now(),LocalTime.now().plusHours(6),
                "description", address,"Link");
        user = new User("testUser", "1234", "test@example.com", address, User.Role.client);
//        Table table = new Table(3, 2, 100);

        reservation1 = new Reservation(user, restaurant, new Table(1,2,3), LocalDateTime.now().minusDays(1));
        reservation2 = new Reservation(user, restaurant, new Table(1,2,3),LocalDateTime.now().plusDays(1) );
    }

    @Test
    public void add_reservation_should_add_reservation() {
        user.addReservation(reservation1);
        List<Reservation> reservations = user.getReservations();
        assertEquals(1, reservations.size());
        assertEquals(reservation1, reservations.get(0));
    }

    @Test
    public void check_reserved_should_return_true_for_existing_reservation() {
        user.addReservation(reservation1);

        assertTrue(user.checkReserved(restaurant));
    }

    @Test
    public void check_reserved_should_return_false_for_cancelled_reservation() {
        reservation1.cancel();
        user.addReservation(reservation1);

        assertFalse(user.checkReserved(restaurant));
    }

    @Test
    public void get_reservation_should_return_existing_reservation() {
        user.addReservation(reservation1);

        Reservation found = user.getReservation(0);
        assertNotNull(found);
        assertEquals(reservation1, found);
    }

    @Test
    public void get_reservation_should_return_null_if_reservation_is_cancelled() {
        reservation1.cancel();
        user.addReservation(reservation1);

        assertNull(user.getReservation(0));
    }

    @Test
    public void check_password_should_return_true_for_correct_password() {
        assertTrue(user.checkPassword("password123"));
    }

    @Test
    public void check_password_should_return_false_for_incorrect_password() {
        assertFalse(user.checkPassword("wrongpassword"));
    }

    @ParameterizedTest
    @CsvSource({
            "password123, true",
            "wrongpassword, false"
    })
    public void check_password_parametrized(String inputPassword, boolean expectedResult) {
        assertEquals(expectedResult, user.checkPassword(inputPassword));
    }

    @ParameterizedTest
    @ValueSource(strings = {"client", "manager"})
    public void role_assignment_should_assign_role_correctly(String role) {
        User.Role expectedRole = User.Role.valueOf(role);
        User newUser = new User("newUser", "newPassword", "new@example.com", address, expectedRole);
        assertEquals(expectedRole, newUser.getRole());
    }

    @Test
    public void unique_ids_should_be_assigned_to_each_user() {
        User user2 = new User("anotherUser", "password456", "another@example.com", address, User.Role.client);
        assertNotEquals(user.getId(), user2.getId());
    }

}
