package it.ylenia.telefonia;

import java.util.List;

/** Astrazione della persistenza usata dal servizio applicativo. */
public interface Repository extends AutoCloseable {
    List<Subscriber> all();
    void add(Subscriber subscriber);
    void options(String phone, String options);
    void cancel(String phone);
    void movement(String phone, long delta, String description);
    List<String> history(String phone);
    @Override void close();
}
