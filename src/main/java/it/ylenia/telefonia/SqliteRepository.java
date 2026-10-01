package it.ylenia.telefonia;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Repository JDBC. L'aggiornamento del saldo e lo storico condividono una transazione. */
public final class SqliteRepository implements Repository {
    private final Connection db;
    public SqliteRepository(String url) {
        try {
            db = DriverManager.getConnection(url);
            try (Statement s = db.createStatement()) {
                s.execute("PRAGMA foreign_keys=ON");
                s.execute("CREATE TABLE IF NOT EXISTS subscribers(phone TEXT PRIMARY KEY, name TEXT NOT NULL, surname TEXT NOT NULL, address TEXT NOT NULL, prepaid INTEGER NOT NULL, balance INTEGER NOT NULL DEFAULT 0, options TEXT NOT NULL DEFAULT '', active INTEGER NOT NULL DEFAULT 1)");
                s.execute("CREATE TABLE IF NOT EXISTS movements(id INTEGER PRIMARY KEY, phone TEXT NOT NULL REFERENCES subscribers(phone), time TEXT NOT NULL, description TEXT NOT NULL, delta INTEGER NOT NULL)");
            }
        } catch (SQLException e) { throw failure(e); }
    }
    private static IllegalStateException failure(SQLException e) { return new IllegalStateException("Errore nel database: " + e.getMessage(), e); }
    @Override public List<Subscriber> all() {
        List<Subscriber> result = new ArrayList<>();
        try (Statement s = db.createStatement(); ResultSet r = s.executeQuery("SELECT * FROM subscribers ORDER BY surname,name")) {
            while (r.next()) result.add(new Subscriber(r.getString("phone"), r.getString("name"), r.getString("surname"), r.getString("address"), r.getBoolean("prepaid"), r.getLong("balance"), r.getString("options"), r.getBoolean("active")));
            return result;
        } catch (SQLException e) { throw failure(e); }
    }
    @Override public void add(Subscriber a) {
        update("INSERT INTO subscribers(phone,name,surname,address,prepaid) VALUES(?,?,?,?,?)", a.phone(), a.name(), a.surname(), a.address(), a.prepaid());
    }
    private int update(String sql, Object... args) {
        try (PreparedStatement s = db.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) s.setObject(i + 1, args[i]);
            return s.executeUpdate();
        } catch (SQLException e) { throw failure(e); }
    }
    @Override public void options(String phone, String options) { update("UPDATE subscribers SET options=? WHERE phone=? AND active=1", options, phone); }
    @Override public void cancel(String phone) { update("UPDATE subscribers SET active=0 WHERE phone=?", phone); }
    @Override public void movement(String phone, long delta, String description) {
        try {
            db.setAutoCommit(false);
            int changed = update("UPDATE subscribers SET balance=balance+? WHERE phone=? AND active=1 AND (prepaid=0 OR balance+? >= 0)", delta, phone, delta);
            if (changed != 1) throw new IllegalArgumentException("Credito insufficiente o abbonamento non attivo.");
            update("INSERT INTO movements(phone,time,description,delta) VALUES(?,?,?,?)", phone, LocalDateTime.now().toString(), description, delta);
            db.commit();
        } catch (SQLException | RuntimeException e) {
            try { db.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
            if (e instanceof RuntimeException runtime) throw runtime;
            throw failure((SQLException) e);
        } finally {
            try { db.setAutoCommit(true); } catch (SQLException e) { throw failure(e); }
        }
    }
    @Override public List<String> history(String phone) {
        List<String> result = new ArrayList<>();
        try (PreparedStatement s = db.prepareStatement("SELECT time,description,delta FROM movements WHERE phone=? ORDER BY id DESC")) {
            s.setString(1, phone);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) result.add(r.getString(1) + " | " + r.getString(2) + " | " + Service.euros(r.getLong(3)));
            }
            return result;
        } catch (SQLException e) { throw failure(e); }
    }
    @Override public void close() { try { db.close(); } catch (SQLException e) { throw failure(e); } }
}
