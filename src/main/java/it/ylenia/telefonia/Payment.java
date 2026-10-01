package it.ylenia.telefonia;

/** Strategy di pagamento: tutte le autorizzazioni sono simulate, senza dati bancari reali. */
public interface Payment {
    String authorize(long cents);
    static void validate(long cents) {
        if (cents <= 0 || cents > 100_000) throw new IllegalArgumentException("Ricarica consentita: da 0,01 a 1.000 euro.");
    }
    final class Cash implements Payment {
        @Override public String authorize(long cents) { Payment.validate(cents); return "Contanti (simulato)"; }
        @Override public String toString() { return "Contanti"; }
    }
    final class CreditCard implements Payment {
        @Override public String authorize(long cents) { Payment.validate(cents); return "Carta di credito (autorizzazione simulata)"; }
        @Override public String toString() { return "Carta di credito"; }
    }
    final class DebitCard implements Payment {
        @Override public String authorize(long cents) { Payment.validate(cents); return "Bancomat (autorizzazione simulata)"; }
        @Override public String toString() { return "Bancomat"; }
    }
}
