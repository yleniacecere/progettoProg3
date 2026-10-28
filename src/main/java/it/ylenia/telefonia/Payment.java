package it.ylenia.telefonia;

import java.time.YearMonth;

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
        private final String number, holder, cvv, expiry;
        public CreditCard(String number, String holder, String cvv, String expiry) {
            this.number = number == null ? "" : number.trim();
            this.holder = holder == null ? "" : holder.trim();
            this.cvv = cvv == null ? "" : cvv;
            this.expiry = expiry == null ? "" : expiry.trim();
        }
        @Override public String authorize(long cents) {
            Payment.validate(cents);
            if (!number.matches("[0-9]{16}")) throw new IllegalArgumentException("Il numero della carta deve contenere 16 cifre.");
            if (!holder.matches("[\\p{L}][\\p{L} '\u2019-]*") || holder.codePoints().filter(Character::isLetter).count() < 2)
                throw new IllegalArgumentException("Inserire un intestatario valido, usando lettere, spazi, apostrofi o trattini.");
            if (!cvv.matches("[0-9]{3}")) throw new IllegalArgumentException("Il CVV deve contenere 3 cifre.");
            if (!expiry.matches("(0[1-9]|1[0-2])/[0-9]{2}")) throw new IllegalArgumentException("La scadenza deve avere formato MM/AA.");
            YearMonth date = YearMonth.of(2000 + Integer.parseInt(expiry.substring(3)), Integer.parseInt(expiry.substring(0, 2)));
            if (date.isBefore(YearMonth.now())) throw new IllegalArgumentException("La carta è scaduta.");
            return "Carta di credito (autorizzazione simulata)";
        }
        @Override public String toString() { return "Carta di credito"; }
    }
    final class DebitCard implements Payment {
        private final String pin;
        public DebitCard(String pin) { this.pin = pin == null ? "" : pin; }
        @Override public String authorize(long cents) {
            Payment.validate(cents);
            if (!pin.matches("[0-9]{5}")) throw new IllegalArgumentException("Il PIN bancomat deve contenere 5 cifre.");
            return "Bancomat (autorizzazione simulata)";
        }
        @Override public String toString() { return "Bancomat"; }
    }
}
