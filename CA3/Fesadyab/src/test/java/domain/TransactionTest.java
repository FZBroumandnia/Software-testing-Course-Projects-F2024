package domain;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class TransactionTest {
    private Transaction transaction;

    @BeforeEach
    public void setUp() { transaction = new Transaction() {{ setTransactionId(0);}};}

    @Test
    public void transactionInitialization_When_setAndGet() {
        Transaction txn = new Transaction();
        txn.setTransactionId(1);
        txn.setAccountId(1);
        txn.setAmount(100);
        txn.setDebit(true);
        assertEquals(1, txn.getTransactionId());
        assertEquals(1, txn.getAccountId());
        assertEquals(100, txn.getAmount());
        assertTrue(txn.isDebit());
    }

    @Test
    public void equals_When_ComparesTransactionIds()
    {
        Transaction anotherTransaction = new Transaction() {{ setTransactionId(0);}};
        assertEquals(transaction, anotherTransaction);
    }

    @Test
    public void equals_When_DifferentTransactionIds()
    {
        Transaction newTransaction = new Transaction() {{ setTransactionId(1);}};
        assertNotEquals(transaction, newTransaction);
    }

    @Test
    public void equals_When_InvalidObject()
    {
        Object object = new Object();
        assertNotEquals(transaction, object);
    }
}
