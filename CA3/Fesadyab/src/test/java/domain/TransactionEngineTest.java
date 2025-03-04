package domain;

import org.junit.jupiter.api.*;

public class TransactionEngineTest {
    private TransactionEngine transactionEngine;

    @BeforeEach
    public void setUp() {
        transactionEngine = new TransactionEngine();
    }

    public static Transaction makeTransaction(int transactionId, int accountId, int amount, boolean isDebit)
    {
        Transaction txn = new Transaction();
        txn.setTransactionId(transactionId);
        txn.setAccountId(accountId);
        txn.setAmount(amount);
        txn.setDebit(isDebit);
        return txn;
    }

    @Test
    public void averageAmount_When_NoTransactions()
    {
        Assertions.assertEquals(0, transactionEngine.getAverageTransactionAmountByAccount(1));
    }

    @Test
    public void averageAmount_When_OneTransaction()
    {
        Transaction txn = makeTransaction(0, 1, 500, true);
        transactionEngine.transactionHistory.add(txn);
        Assertions.assertEquals(500, transactionEngine.getAverageTransactionAmountByAccount(1));
    }

    @Test
    public void averageAmount_When_MultipleTransactions()
    {
        Transaction txn1 = makeTransaction(0, 1, 500, true);
        Transaction txn2 = makeTransaction(1, 1, 1500, true);
        transactionEngine.transactionHistory.add(txn1);
        transactionEngine.transactionHistory.add(txn2);
        Assertions.assertEquals(1000, transactionEngine.getAverageTransactionAmountByAccount(1));
    }

    @Test
    public void averageAmount_When_UnknownAccount()
    {
        Transaction txn = makeTransaction(0, 1, 500, true);
        transactionEngine.transactionHistory.add(txn);
        Assertions.assertEquals(0, transactionEngine.getAverageTransactionAmountByAccount(2));
    }

    @Test
    public void transactionPattern_When_NoTransactions()
    {
        Assertions.assertEquals(0, transactionEngine.getTransactionPatternAboveThreshold(1000));
    }

    @Test
    public void transactionPattern_When_PatternExists()
    {
        Transaction txn1 = makeTransaction(0, 1, 1200, true);
        Transaction txn2 = makeTransaction(1, 1, 1800, true);
        Transaction txn3 = makeTransaction(2, 1, 2400, true);
        transactionEngine.transactionHistory.add(txn1);
        transactionEngine.transactionHistory.add(txn2);
        transactionEngine.transactionHistory.add(txn3);
        Assertions.assertEquals(600, transactionEngine.getTransactionPatternAboveThreshold(1000));
    }

    @Test
    public void transactionPattern_When_NoPattern()
    {
        Transaction txn1 = makeTransaction(0, 1, 1200, true);
        Transaction txn2 = makeTransaction(1, 1, 1700, true);
        Transaction txn3 = makeTransaction(2, 1, 2500, true);
        transactionEngine.transactionHistory.add(txn1);
        transactionEngine.transactionHistory.add(txn2);
        transactionEngine.transactionHistory.add(txn3);
        Assertions.assertEquals(0, transactionEngine.getTransactionPatternAboveThreshold(1000));
    }

    @Test
    public void transactionPattern_When_TransactionUnderThreshold()
    {
        Transaction txn1 = makeTransaction(0, 1, 1200, true);
        Transaction txn2 = makeTransaction(1, 1, 800, true);
        transactionEngine.transactionHistory.add(txn1);
        transactionEngine.transactionHistory.add(txn2);
        Assertions.assertEquals(0, transactionEngine.getTransactionPatternAboveThreshold(1000));
    }

    @Test
    public void transactionPattern_When_TransactionUnderAndEqualThreshold()
    {
        Transaction txn1 = makeTransaction(1, 1, 800, true);
        Transaction txn2 = makeTransaction(0, 1, 1000, true);
        transactionEngine.transactionHistory.add(txn1);
        transactionEngine.transactionHistory.add(txn2);
        Assertions.assertEquals(0, transactionEngine.getTransactionPatternAboveThreshold(1000));
    }

    @Test
    public void detectFraudulentTransaction_When_LargeDebit()
    {
        Transaction txn1 = makeTransaction(0, 1, 500, true);
        Transaction txn2 = makeTransaction(1, 1, 2000, true);
        transactionEngine.transactionHistory.add(txn1);
        Assertions.assertEquals(1000, transactionEngine.detectFraudulentTransaction(txn2));
    }


    @Test
    public void testFraudulent_When_WithinRange()
    {
        Transaction txn1 = makeTransaction(0, 1, 500, true);
        Transaction txn2 = makeTransaction(1, 1, 900, true);
        transactionEngine.transactionHistory.add(txn1);
        Assertions.assertEquals(0, transactionEngine.detectFraudulentTransaction(txn2));
    }

    @Test
    public void detectFraudulentTransaction_When_NonDebit()
    {
        Transaction txn1 = makeTransaction(0, 1, 500, false);
        Transaction txn2 = makeTransaction(1, 1, 2000, false);
        transactionEngine.transactionHistory.add(txn1);
        Assertions.assertEquals(0, transactionEngine.detectFraudulentTransaction(txn2));
    }

    @Test
    public void addTransactionAndDetectFraudulent_When_accepted()
    {
        Transaction txn1 = makeTransaction(0, 1, 500, true);
        Transaction txn2 = makeTransaction(1, 1, 2000, true);
        transactionEngine.transactionHistory.add(txn1);
        Assertions.assertEquals(1000, transactionEngine.addTransactionAndDetectFraud(txn2));
        Assertions.assertTrue(transactionEngine.transactionHistory.contains(txn2));
    }

    @Test
    public void addTransactionAndDetectFraudulent_When_repeated()
    {
        Transaction txn1 = makeTransaction(0, 1, 500, true);
        transactionEngine.transactionHistory.add(txn1);
        Assertions.assertEquals(transactionEngine.addTransactionAndDetectFraud(txn1), 0);
    }

    @Test
    public void addTransactionAndDetectPatternTest()
    {
        Transaction txn1 = makeTransaction(0, 1, 1200, true);
        Transaction txn2 = makeTransaction(1, 1, 1800, true);
        Transaction txn3 = makeTransaction(2, 1, 2400, true);
        transactionEngine.transactionHistory.add(txn1);
        transactionEngine.transactionHistory.add(txn2);
        Assertions.assertEquals(600, transactionEngine.addTransactionAndDetectFraud(txn3));
        Assertions.assertTrue(transactionEngine.transactionHistory.contains(txn3));
    }
}
