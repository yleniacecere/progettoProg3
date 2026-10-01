package it.ylenia.telefonia;

/** Tariffa espressa in centesimi; le chiamate sono conteggiate per minuto iniziato. */
public interface Plan {
    long cost(Usage usage, int quantity);
    enum Usage { CHIAMATA, SMS, DATI }

    /** Piano assegnato a ogni nuova utenza. */
    final class Base implements Plan {
        @Override public long cost(Usage usage, int quantity) {
            if (quantity <= 0) throw new IllegalArgumentException("La quantità deve essere positiva.");
            return switch (usage) {
                case CHIAMATA -> ((quantity + 59L) / 60) * 20;
                case SMS -> quantity * 10L;
                case DATI -> quantity * 2L;
            };
        }
    }

    /** Decorator: dimezza il costo della categoria scelta, arrotondando al centesimo superiore. */
    final class Discount implements Plan {
        private final Plan wrapped;
        private final Usage category;
        public Discount(Plan wrapped, Usage category) { this.wrapped = wrapped; this.category = category; }
        @Override public long cost(Usage usage, int quantity) {
            long price = wrapped.cost(usage, quantity);
            return usage == category ? (price + 1) / 2 : price;
        }
    }

    static Plan fromOptions(String options) {
        Plan plan = new Base();
        for (Usage usage : Usage.values()) {
            if (java.util.Arrays.asList(options.split(",")).contains(usage.name()))
                plan = new Discount(plan, usage);
        }
        return plan;
    }
}
