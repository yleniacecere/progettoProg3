package it.ylenia.telefonia;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Casi d'uso indipendenti da Swing; importi sempre rappresentati in centesimi interi. */
public final class Service {
    private final Repository repository;
    public Service(Repository repository) { this.repository = repository; }
    public List<Subscriber> subscribers() { return repository.all(); }
    public Subscriber active(String phone) {
        return subscribers().stream().filter(s -> s.phone().equals(phone) && s.active()).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Abbonamento non attivo o inesistente."));
    }
    public void register(String phone, String name, String surname, String address, boolean prepaid) {
        phone = phone.trim(); name = name.trim(); surname = surname.trim(); address = address.trim();
        if (!phone.matches("[0-9]{8,15}")) throw new IllegalArgumentException("Il numero deve contenere da 8 a 15 cifre.");
        if (name.isBlank() || surname.isBlank() || address.isBlank()) throw new IllegalArgumentException("Compilare tutti i dati anagrafici.");
        final String number = phone;
        if (subscribers().stream().anyMatch(s -> s.phone().equals(number))) throw new IllegalArgumentException("Numero già registrato, anche tra le utenze disdette.");
        repository.add(new Subscriber(phone, name, surname, address, prepaid, 0, "", true));
    }
    public void changePlan(String phone, Set<Plan.Usage> options) {
        active(phone);
        repository.options(phone, options.stream().sorted().map(Enum::name).collect(Collectors.joining(",")));
    }
    public void cancel(String phone) { active(phone); repository.cancel(phone); }
    public long communicate(String phone, Plan.Usage usage, int quantity, String recipient) {
        Subscriber sender = active(phone);
        if (quantity <= 0 || quantity > 1_000_000) throw new IllegalArgumentException("Quantità consentita: da 1 a 1.000.000.");
        if (usage != Plan.Usage.DATI) {
            active(recipient);
            if (phone.equals(recipient)) throw new IllegalArgumentException("Selezionare un altro abbonato come destinatario.");
        }
        long cost = Plan.fromOptions(sender.options()).cost(usage, quantity);
        String unit = switch (usage) { case CHIAMATA -> "secondi"; case SMS -> "messaggi"; case DATI -> "MB"; };
        repository.movement(phone, -cost, usage + " " + quantity + " " + unit + (usage == Plan.Usage.DATI ? "" : " verso " + recipient));
        return cost;
    }
    public void topUp(String phone, String amount, Payment payment) {
        Subscriber subscriber = active(phone);
        if (!subscriber.prepaid()) throw new IllegalArgumentException("Le ricariche sono disponibili solo per i conti ricaricabili.");
        final long cents;
        try { cents = new BigDecimal(amount.trim().replace(',', '.')).movePointRight(2).longValueExact(); }
        catch (ArithmeticException | NumberFormatException e) { throw new IllegalArgumentException("Inserire un importo con massimo due decimali."); }
        String receipt = payment.authorize(cents);
        repository.movement(phone, cents, "Ricarica — " + receipt);
    }
    public List<String> history(String phone) { return repository.history(phone); }
    public static String euros(long cents) { return BigDecimal.valueOf(cents, 2).toPlainString() + " €"; }
}
