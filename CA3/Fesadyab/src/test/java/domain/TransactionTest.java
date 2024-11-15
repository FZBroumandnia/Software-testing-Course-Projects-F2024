package domain;

import org.junit.jupiter.api.*;

public class TransactionTest {
    private Transaction transaction;

    @BeforeEach
    public void setUp() { transaction = new Transaction() {{ setTransactionId(0);}};}

    @Test
    public void equals_When_ComparesTransactionIds()
    {
        Transaction anotherTransaction = new Transaction() {{ setTransactionId(0);}};
        Assertions.assertTrue(transaction.equals(anotherTransaction));
    }

    @Test
    public void equals_When_DifferentTransactionIds()
    {
        Transaction newTransaction = new Transaction() {{ setTransactionId(1);}};
        Assertions.assertFalse(transaction.equals(newTransaction));
    }

    @Test
    public void equals_When_InvalidObject()
    {
        Object object = new Object();
        Assertions.assertFalse(transaction.equals(object));
    }
}
