package it.ylenia.telefonia;

import org.junit.jupiter.api.*;
import java.nio.file.Path;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class ServiceTest {
    private Repository repository;
    private Service service;
    @BeforeEach void setup() {
        repository = new SqliteRepository("jdbc:sqlite::memory:"); service = new Service(repository);
        service.register("3331111111", "Ylenia", "Cecere", "Napoli", true);
        service.register("3332222222", "Anna", "Rossi", "Roma", false);
    }
    @AfterEach void cleanup() { repository.close(); }
    @Test void billingRoundsStartedMinutesAndCombinesOptions() {
        Plan plan = Plan.fromOptions("CHIAMATA,DATI");
        assertEquals(10, plan.cost(Plan.Usage.CHIAMATA, 60));
        assertEquals(20, plan.cost(Plan.Usage.CHIAMATA, 61));
        assertEquals(10, plan.cost(Plan.Usage.SMS, 1));
        assertEquals(5, plan.cost(Plan.Usage.DATI, 5));
    }
    @Test void insufficientCreditDoesNotWriteHistory() {
        assertThrows(IllegalArgumentException.class, () -> service.communicate("3331111111", Plan.Usage.SMS, 1, "3332222222"));
        assertEquals(0, service.active("3331111111").balance());
        assertTrue(service.history("3331111111").isEmpty());
        service.topUp("3331111111", "0,10", new Payment.Cash());
        service.communicate("3331111111", Plan.Usage.SMS, 1, "3332222222");
        assertEquals(0, service.active("3331111111").balance());
        assertEquals(2, service.history("3331111111").size());
    }
    @Test void fixedAccountAccumulatesCharges() {
        assertEquals(40, service.communicate("3332222222", Plan.Usage.CHIAMATA, 61, "3331111111"));
        assertEquals(-40, service.active("3332222222").balance());
        assertThrows(IllegalArgumentException.class, () -> service.topUp("3332222222", "10", new Payment.Cash()));
    }
    @Test void allPaymentStrategiesAndInvalidAmounts() {
        for (Payment p : new Payment[]{new Payment.Cash(), new Payment.CreditCard(), new Payment.DebitCard()}) service.topUp("3331111111", "10", p);
        assertEquals(3000, service.active("3331111111").balance());
        for (String amount : new String[]{"0", "-1", "1.001", "abc", "1000.01"})
            assertThrows(IllegalArgumentException.class, () -> service.topUp("3331111111", amount, new Payment.Cash()));
        assertEquals(3, service.history("3331111111").size());
    }
    @Test void cancellationAndValidation() {
        assertThrows(IllegalArgumentException.class, () -> service.register("3331111111", "A", "B", "C", true));
        assertThrows(IllegalArgumentException.class, () -> service.communicate("3332222222", Plan.Usage.SMS, 1, "3332222222"));
        service.cancel("3331111111");
        assertThrows(IllegalArgumentException.class, () -> service.topUp("3331111111", "10", new Payment.Cash()));
        assertThrows(IllegalArgumentException.class, () -> service.communicate("3332222222", Plan.Usage.SMS, 1, "3331111111"));
        assertEquals(2, service.subscribers().size());
    }
    @Test void dataOptionsAffectCharges() {
        service.changePlan("3332222222", Set.of(Plan.Usage.DATI));
        assertEquals(7, service.communicate("3332222222", Plan.Usage.DATI, 7, ""));
    }
    @Test void persistsAcrossReopening(@org.junit.jupiter.api.io.TempDir Path directory) {
        String url = "jdbc:sqlite:" + directory.resolve("test.db");
        try (Repository first = new SqliteRepository(url)) {
            Service s = new Service(first);
            s.register("3334444444", "Nome", "Cognome", "Roma", true);
            s.topUp("3334444444", "12.50", new Payment.Cash());
        }
        try (Repository second = new SqliteRepository(url)) {
            assertEquals(1250, second.all().get(0).balance());
            assertEquals(1, second.history("3334444444").size());
        }
    }
}
