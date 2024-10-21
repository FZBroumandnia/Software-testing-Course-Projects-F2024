package mizdooni.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TableTest {
    Table table;
    @Mock
    User dummyUser;
    @Mock
    Restaurant restaurant;
    List<Reservation> reservations;
    Reservation canceled_reservation;


    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(restaurant.getId()).thenReturn(1);

        table = new Table(1, restaurant.getId(), 10);
        canceled_reservation = new Reservation(dummyUser, restaurant, table, make_localDateTime(2024, 5, 2, 19, 30));
        canceled_reservation.cancel();
        reservations = Arrays.asList(
                new Reservation(dummyUser, restaurant, table, make_localDateTime(2024, 5, 1, 13, 30)),
                new Reservation(dummyUser, restaurant, table, make_localDateTime(2024, 5, 1, 19, 30)),
                new Reservation(dummyUser, restaurant, table, make_localDateTime(2024, 5, 2, 13, 30)),
                canceled_reservation
        );
        reservations.forEach(r -> table.addReservation(r));
    }

    private LocalDateTime make_localDateTime(int year, int month, int day, int hour, int minute) {
        return LocalDateTime.of(LocalDate.of(year, month, day), LocalTime.of(hour, minute));
    }

    private LocalDate give_unreserved_date() {
        return LocalDate.of(2020, 7, 1);
    }

    private LocalTime give_unreserved_time() {
        return LocalTime.of(17, 20);
    }

    @Test
    public void addReservation_When_add_then_True() {
        LocalDateTime date_time = make_localDateTime(2024, 2, 2, 12, 20);
        Reservation r = new Reservation(dummyUser, restaurant, table, date_time);
        table.addReservation(r);
        List<Reservation> reservations = table.getReservations();
        assertTrue(reservations.stream().anyMatch(res -> res.getDateTime().equals(date_time)));
    }

    @Test
    public void isReserved_When_Reserved_Then_True() {
        LocalDateTime date_time = reservations.getFirst().getDateTime();
        assertTrue(table.isReserved(date_time));
    }

    @Test
    public void isReserved_When_ReserveDateDifferent_Then_False() {
        LocalDateTime reserved_date_time = reservations.getFirst().getDateTime();
        LocalDateTime unreserved_date_time = LocalDateTime.of(give_unreserved_date(), reserved_date_time.toLocalTime());
        assertFalse(table.isReserved(unreserved_date_time));
    }

    @Test
    public void isReserved_When_ReserveTimeDifferent_Then_False() {
        LocalDateTime reserved_date_time = reservations.getFirst().getDateTime();
        LocalDateTime unreserved_date_time = LocalDateTime.of(reserved_date_time.toLocalDate(), give_unreserved_time());
        assertFalse(table.isReserved(unreserved_date_time));
    }

    @Test
    public void isReserved_When_ReservationCanceled_Then_False() {
        assertFalse(table.isReserved(canceled_reservation.getDateTime()));
    }
}