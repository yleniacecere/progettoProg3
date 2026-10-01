# Gestore telefonico 1

Progetto standalone di **Ylenia Cecere**, matricola **0124002397**, gruppo **x3z0871zba** (un componente).

## Avvio

Richiede JDK 17 o successivo e Maven. La prima compilazione richiede Internet per scaricare le dipendenze.

```sh
mvn clean package
java -jar target/gestore-telefonico-1.0.0.jar
```

Il database SQLite viene creato in `data/telefonia.db`, rispetto alla cartella da cui si avvia il programma. Avviare sempre dalla stessa cartella per riutilizzare i dati. Per importare il progetto in un IDE, aprire `pom.xml` ed eseguire la classe `it.ylenia.telefonia.App`.

## Utilizzo

1. In modalità Amministratore registrare almeno due abbonati, indicando nome, cognome, residenza, telefono e tipo di conto.
2. Modificare eventualmente il piano selezionando le opzioni.
3. Passare alla modalità Abbonato e selezionare l'utenza. Ricaricare il conto ricaricabile prima di effettuare operazioni.
4. Simulare chiamate, SMS o traffico dati. Il pannello mostra saldo, piano e storico.
5. L'amministratore può disdire un'utenza: dati e storico rimangono consultabili.

L'accesso è una selezione di ruolo per simulazione, senza password: non è un sistema di autenticazione. I pagamenti sono simulati e non richiedono informazioni bancarie. Le chiamate non aspettano realmente il tempo indicato: si registra la durata dichiarata in secondi.

## Regole adottate

- Piano base: 20 centesimi per minuto iniziato, 10 centesimi per SMS, 2 centesimi per MB intero.
- Tre opzioni combinabili: sconto del 50% su chiamate, SMS o dati, senza canone. Arrotondamento del costo al centesimo superiore.
- Chiamate e SMS soltanto verso un altro abbonato attivo del gestore; paga solo il mittente. Nessun costo di ricezione.
- Conto ricaricabile: saldo iniziale zero, nessuno scoperto e ricariche da 0,01 a 1.000 euro. Conto fisso: accumula addebiti; fatturazione periodica e saldo fatture sono fuori da questa simulazione.
- Disdetta immediata senza cancellare lo storico; nessun rimborso o saldo finale simulato. I numeri disdetti non vengono riutilizzati.
- Data e ora dell'operazione sono registrate; le tariffe non dipendono dalla fascia oraria.

Queste sono scelte progettuali per i dettagli non specificati dalla traccia, da confermare con il docente se necessario.

## Struttura e verifica

`App` contiene Swing, `Service` i casi d'uso, `Repository` l'astrazione di persistenza e `SqliteRepository` l'implementazione JDBC. `Plan.Discount` implementa Decorator; le implementazioni di `Payment` realizzano Strategy.

```sh
mvn test
```

I test verificano tariffe, credito insufficiente, strategie di pagamento, conto fisso, opzioni, disdetta e persistenza dopo la riapertura del database. Per la verifica grafica manuale seguire il flusso di utilizzo e controllare anche annullamento dei dialoghi e riapertura dell'applicazione.

La bozza di relazione e il diagramma UML sono in `docs/relazione.md`. Per la consegna creare `Relazione_Cecere_Ylenia`, inserire la relazione esportata in PDF, una cartella con questi sorgenti e il PDF originale della traccia fornito dal docente. Non includere database con dati personali, cartella `target` o impostazioni dell'IDE.
