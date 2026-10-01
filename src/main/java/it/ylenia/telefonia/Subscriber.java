package it.ylenia.telefonia;

/** Dati anagrafici e conto: saldo in centesimi, negativo per gli addebiti del conto fisso. */
public record Subscriber(String phone, String name, String surname, String address,
                         boolean prepaid, long balance, String options, boolean active) {
    @Override public String toString() { return phone + " — " + name + " " + surname + (active ? "" : " (disdetto)"); }
}
