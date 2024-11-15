package domain;

import org.junit.jupiter.api.*;

public class TransactionEngineTest {
    private TransactionEngine transactionEngine;

    @BeforeEach
    public void setUp() {
        transactionEngine = new TransactionEngine();
    }

    public static Transaction constructTransaction(int transactionId, int accountId, int amount, boolean isDebit) {
        Transaction txn = new Transaction();
        txn.setTransactionId(transactionId);
        txn.setAccountId(accountId);
        txn.setAmount(amount);
        txn.setDebit(isDebit);
        return txn;
    }

    // getAverageTransactionAmountByAccount

    @Test
    @DisplayName("Test average transaction amount with no transactions")
    public void testAverageAmountNoTransactions() {
        Assertions.assertEquals(0, transactionEngine.getAverageTransactionAmountByAccount(1));
    }

    @Test
    @DisplayName("Test average transaction amount with one transaction")
    public void testAverageAmountOneTransaction() {
        Transaction txn = constructTransaction(0, 1, 500, true);
        transactionEngine.transactionHistory.add(txn);
        Assertions.assertEquals(500, transactionEngine.getAverageTransactionAmountByAccount(1));
    }

    @Test
    @DisplayName("Test average transaction amount with multiple transactions")
    public void testAverageAmountMultipleTransactions() {
        Transaction txn1 = constructTransaction(0, 1, 500, true);
        Transaction txn2 = constructTransaction(1, 1, 1500, true);
        transactionEngine.transactionHistory.add(txn1);
        transactionEngine.transactionHistory.add(txn2);
        Assertions.assertEquals(1000, transactionEngine.getAverageTransactionAmountByAccount(1));
    }

    @Test
    @DisplayName("Test average transaction amount for unknown account")
    public void testAverageAmountUnknownAccount() {
        Transaction txn = constructTransaction(0, 1, 500, true);
        transactionEngine.transactionHistory.add(txn);
        Assertions.assertEquals(0, transactionEngine.getAverageTransactionAmountByAccount(2));
    }

    // getTransactionPatternAboveThreshold

    @Test
    @DisplayName("Test transaction pattern with no transactions")
    public void testTransactionPatternNoTransactions() {
        Assertions.assertEquals(0, transactionEngine.getTransactionPatternAboveThreshold(1000));
    }

    @Test
    @DisplayName("Test transaction pattern with linear increasing amounts above threshold")
    public void testTransactionPatternWithPattern() {
        Transaction txn1 = constructTransaction(0, 1, 1200, true);
        Transaction txn2 = constructTransaction(1, 1, 1800, true);
        Transaction txn3 = constructTransaction(2, 1, 2400, true);
        transactionEngine.transactionHistory.add(txn1);
        transactionEngine.transactionHistory.add(txn2);
        transactionEngine.transactionHistory.add(txn3);
        Assertions.assertEquals(600, transactionEngine.getTransactionPatternAboveThreshold(1000));
    }

    @Test
    @DisplayName("Test transaction pattern without pattern above threshold")
    public void testTransactionPatternWithoutPattern() {
        Transaction txn1 = constructTransaction(0, 1, 1200, true);
        Transaction txn2 = constructTransaction(1, 1, 1700, true);
        Transaction txn3 = constructTransaction(2, 1, 2500, true);
        transactionEngine.transactionHistory.add(txn1);
        transactionEngine.transactionHistory.add(txn2);
        transactionEngine.transactionHistory.add(txn3);
        Assertions.assertEquals(0, transactionEngine.getTransactionPatternAboveThreshold(1000));
    }

    /*@Test
    @DisplayName("Test transaction pattern with mixed amounts above and below threshold")
    public void testTransactionPatternMixedAmounts() {
        Transaction txn1 = constructTransaction(0, 1, 800, true);
        Transaction txn2 = constructTransaction(1, 1, 1200, true);
        Transaction txn3 = constructTransaction(2, 1, 1800, true);
        transactionEngine.transactionHistory.add(txn1);
        transactionEngine.transactionHistory.add(txn2);
        transactionEngine.transactionHistory.add(txn3);
        Assertions.assertEquals(600, transactionEngine.getTransactionPatternAboveThreshold(1000));
    }*/

    // detectFraudulentTransaction

    @Test
    @DisplayName("Test fraudulent transaction detection for large debit amount")
    public void testFraudulentLargeDebit() {
        Transaction txn1 = constructTransaction(0, 1, 500, true);
        Transaction txn2 = constructTransaction(1, 1, 2000, true);
        transactionEngine.transactionHistory.add(txn1);
        Assertions.assertEquals(1000, transactionEngine.detectFraudulentTransaction(txn2));
    }


    @Test
    @DisplayName("Test fraudulent transaction detection for amount within average range")
    public void testFraudulentWithinRange() {
        Transaction txn1 = constructTransaction(0, 1, 500, true);
        Transaction txn2 = constructTransaction(1, 1, 900, true);
        transactionEngine.transactionHistory.add(txn1);
        Assertions.assertEquals(0, transactionEngine.detectFraudulentTransaction(txn2));
    }

    @Test
    @DisplayName("Test fraudulent transaction detection for non-debit transaction")
    public void testFraudulentNonDebit() {
        Transaction txn1 = constructTransaction(0, 1, 500, false);
        Transaction txn2 = constructTransaction(1, 1, 2000, false);
        transactionEngine.transactionHistory.add(txn1);
        Assertions.assertEquals(0, transactionEngine.detectFraudulentTransaction(txn2));
    }

    // addTransactionAndDetectFraud

    @Test
    @DisplayName("Test adding already existing transaction")
    public void testAddingExistingTransaction() {
        Transaction txn = constructTransaction(0, 1, 500, true);
        transactionEngine.transactionHistory.add(txn);
        Assertions.assertEquals(0, transactionEngine.addTransactionAndDetectFraud(txn));
    }

    @Test
    @DisplayName("Test adding transaction and detecting fraud (if fraud score > 0)")
    public void testAddTransactionAndDetectFraudulent() {
        Transaction txn1 = constructTransaction(0, 1, 500, true);
        Transaction txn2 = constructTransaction(1, 1, 2000, true);
        transactionEngine.transactionHistory.add(txn1);
        Assertions.assertEquals(1000, transactionEngine.addTransactionAndDetectFraud(txn2));
        Assertions.assertTrue(transactionEngine.transactionHistory.contains(txn2));
    }

    @Test
    @DisplayName("Test adding transaction and detecting pattern if fraud score == 0")
    public void testAddTransactionAndDetectPattern() {
        Transaction txn1 = constructTransaction(0, 1, 1200, true);
        Transaction txn2 = constructTransaction(1, 1, 1800, true);
        Transaction txn3 = constructTransaction(2, 1, 2400, true);
        transactionEngine.transactionHistory.add(txn1);
        transactionEngine.transactionHistory.add(txn2);
        Assertions.assertEquals(600, transactionEngine.addTransactionAndDetectFraud(txn3));
        Assertions.assertTrue(transactionEngine.transactionHistory.contains(txn3));
    }


}
