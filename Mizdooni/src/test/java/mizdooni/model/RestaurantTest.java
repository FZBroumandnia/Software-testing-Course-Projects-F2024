package mizdooni.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class RestaurantTest {

    Restaurant restaurant;
    @Mock
    User dummyUser;
    @Mock
    Address address;
    Review randomReview;
    @Mock
    Rating rating;
    LocalTime startTime = LocalTime.of(12,0);
    LocalTime endTime = LocalTime.of(23,0);
    @BeforeEach
    void setup()
    {
        MockitoAnnotations.openMocks(this);
        restaurant = new Restaurant("name", dummyUser, "type", startTime, endTime, "description", address, "imageLink");
        randomReview = new Review(dummyUser,rating,"", LocalDateTime.now());
    }

    private Table make_random_table()
    {
        return new Table(1, restaurant.getId(), 10);
    }

    private Table make_random_table(int seatsNumber)
    {
        return new Table(1, restaurant.getId(), seatsNumber);
    }

    @Test
    public void addTable_When_TableAdded_Then_Exist()
    {
        Table table = make_random_table();
        restaurant.addTable(table);
        List<Table> tables = restaurant.getTables();
        assertTrue(tables.stream().anyMatch(t-> t.getTableNumber()==table.getTableNumber()));
    }

    @Test
    public void addTable_When_NoTableAdded_Then_Empty()
    {
        List<Table> tables = restaurant.getTables();
        assertTrue(tables.isEmpty());
    }

    @Test
    public void getTable_When_TableAdded_Then_Find()
    {
        Table table = make_random_table();
        restaurant.addTable(table);
        Table t = restaurant.getTable(table.getTableNumber());
        assertNotNull(t);
        assertEquals(t.getTableNumber(), table.getTableNumber());
    }

    @Test
    public void getTable_When_TableNotAdded_Then_NULL()
    {
        Table table = make_random_table();
        assertNull(restaurant.getTable(table.getTableNumber()));
    }

    @Test
    public void addReview_When_ReviewAdded_Then_Exist()
    {
        restaurant.addReview(randomReview);
        List<Review> reviews = restaurant.getReviews();
        assertTrue(reviews.stream().anyMatch(r-> r.equals(randomReview)));
    }

    @Test
    public void addReview_When_NoReviewAdded_Then_Empty()
    {
        List<Review> reviews = restaurant.getReviews();
        assertTrue(reviews.isEmpty());
    }

    @Test
    public void getMaxSeatsNumber_When_NoTable_Then_Zero()
    {
        assertEquals(0, restaurant.getMaxSeatsNumber());
    }

    static Stream<Arguments> generateTableSeatsNumber() {
        return Stream.of(
                Arguments.of(Arrays.asList(1, 7, 2, 5), 7),
                Arguments.of(Arrays.asList(8, 8, 8), 8)
        );
    }

    @ParameterizedTest
    @MethodSource("generateTableSeatsNumber")
    public void getMaxSeatsNumber_When_MoreThanOneTable_Then_Max(List<Integer> seatsNumbers, int max)
    {
        for (int s : seatsNumbers)
        {
            restaurant.addTable(make_random_table(s));
        }
        assertEquals(max, restaurant.getMaxSeatsNumber());
    }

    @Test
    public void getMaxSeatsNumber_When_OneTable_Then_TableSeatsNumber()
    {
        Table t = make_random_table();
        restaurant.addTable(t);
        assertEquals(t.getSeatsNumber(), restaurant.getMaxSeatsNumber());
    }
}
