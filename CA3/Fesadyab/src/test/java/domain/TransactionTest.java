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
    public void testEqualsDifferentTransactionIds() {
        Transaction newTransaction = new Transaction() {{
            setTransactionId(1);
        }};
        Assertions.assertFalse(transaction.equals(newTransaction));
    }

    @Test
    public void testEqualsWrongObject() {
        Object object = new Object();
        Assertions.assertFalse(transaction.equals(object));
    }

    @Test
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
