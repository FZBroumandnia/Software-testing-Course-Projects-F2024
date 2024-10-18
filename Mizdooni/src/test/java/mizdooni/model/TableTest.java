package mizdooni.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TableTest {
    Table table;

    @Mock
    User dummyUser;

    @Mock
    Restaurant restaurant;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(restaurant.getId()).thenReturn(1);

        table = new Table(1, restaurant.getId(), 10);
        List<Reservation> reservations = Arrays.asList(
                new Reservation(dummyUser, restaurant, table, make_localDateTime(2024, 5, 1, 13, 30)),
                new Reservation(dummyUser, restaurant, table, make_localDateTime(2024, 5, 1, 19, 30)),
                new Reservation(dummyUser, restaurant, table, make_localDateTime(2024, 5, 2, 13, 30)),
                new Reservation(dummyUser, restaurant, table, make_localDateTime(2024, 5, 2, 19, 30))
        );
        reservations.forEach(r -> table.addReservation(r));
    }

    private LocalDateTime make_localDateTime(int year, int month, int day, int hour, int minute) {
        return LocalDateTime.of(LocalDate.of(year, month, day), LocalTime.of(hour, minute));
    }

    @Test
    public void isReserved_When_Reserved_Then_True() {
        LocalDateTime date_time = make_localDateTime(2024, 2, 2, 12, 20);
        Reservation r = new Reservation(dummyUser, restaurant, table, date_time);
        table.addReservation(r);
        assertTrue(table.isReserved(date_time)); // Changed to assertTrue
    }
}