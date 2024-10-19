package mizdooni.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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

}
