package domain;

import org.junit.jupiter.api.*;

public class TransactionTest {
    private Transaction transaction;

    @BeforeEach
    public void setUp() {
        transaction = new Transaction() {{
            setTransactionId(0);
        }};
    }

    @Test
    @DisplayName("Test transaction.equals should only compare transaction IDs")
    public void testEqualsComparesTransactionIds() {
        Transaction transaction = new Transaction() {{
            setTransactionId(0);
        }};
        Transaction anotherTransaction = new Transaction() {{
            setTransactionId(0);
        }};
        Assertions.assertTrue(transaction.equals(anotherTransaction));
    }

    @Test
    @DisplayName("Test transaction.equals with different transaction IDs")
    public void testEqualsDifferentTransactionIds() {
        Transaction newTransaction = new Transaction() {{
            setTransactionId(1);
        }};
        Assertions.assertFalse(transaction.equals(newTransaction));
    }

    @Test
    @DisplayName("Test transaction.equals with object of another type")
    public void testEqualsWrongObject() {
        Object object = new Object();
        Assertions.assertFalse(transaction.equals(object));
    }

    @Test
    @DisplayName("Test transaction getter and setters")
    public void testTransactionGetterSetters() {
        transaction.setTransactionId(1);
        transaction.setAccountId(2);
        transaction.setAmount(300);
        transaction.setDebit(true);
        Assertions.assertEquals(1, transaction.getTransactionId());
        Assertions.assertEquals(2, transaction.getAccountId());
        Assertions.assertEquals(300, transaction.getAmount());
        Assertions.assertTrue(transaction.isDebit());
    }
}
