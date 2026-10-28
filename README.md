# Gestore telefonico 1

Progetto universitario di **Programmazione III** per la gestione e la simulazione dei servizi di un gestore telefonico.

| Dato | Informazione |
| --- | --- |
| Studente | Ylenia Cecere |
| Matricola | 0124002397 |
| Gruppo | x3z0871zba |
| Componenti | 1 |

## Obiettivo

L'applicazione permette di amministrare abbonamenti telefonici e simulare chiamate, SMS, traffico dati e ricariche. Ogni abbonato possiede nome, cognome, residenza, numero di telefono univoco, piano tariffario e un conto fisso oppure ricaricabile.

La soluzione è un'applicazione desktop standalone sviluppata in Java, con interfaccia Swing e database locale SQLite. Anagrafiche, opzioni, saldi e movimenti vengono conservati tra un'esecuzione e la successiva.

## Funzionalità

### Area amministratore

L'amministratore accede con le credenziali predefinite **username `admin` e password `admin`**, senza registrazione, e può:

- registrare abbonati indicando i dati anagrafici, il numero e il tipo di conto;
- consultare dati dell'utenza, saldo, piano e storico dei movimenti;
- attivare e disattivare le opzioni tariffarie;
- disdire abbonamenti;
- consultare le utenze disdette tramite **Archivio disdette**.

La registrazione richiede tutti i dati anagrafici e un numero composto da 8 a 15 cifre. Il numero deve essere univoco anche rispetto alle utenze già disdette.

### Area abbonato

L'abbonato accede con il proprio numero, precedentemente registrato e ancora attivo. Visualizza esclusivamente la propria utenza e può:

- simulare chiamate verso un altro abbonato attivo;
- simulare l'invio di uno o più SMS a un altro abbonato attivo;
- simulare traffico dati indicando i MB utilizzati;
- ricaricare il conto ricaricabile scegliendo il metodo di pagamento;
- consultare credito o addebiti, opzioni attive e storico, dal movimento più recente.

L'accesso tramite numero identifica l'utenza nella simulazione e non prevede una password personale. Numeri inesistenti o disdetti non consentono l'accesso.

## Regole della simulazione

### Tariffe e opzioni

Ogni nuova utenza riceve il piano base senza opzioni attive.

| Servizio | Tariffa base | Quantità inserita |
| --- | --- | --- |
| Chiamate | 0,20 € per minuto iniziato | Durata in secondi |
| SMS | 0,10 € per messaggio | Numero di messaggi |
| Traffico dati | 0,02 € per MB | Numero di MB interi |

Sono disponibili tre opzioni indipendenti e combinabili: **sconto del 50% sulle chiamate**, **sconto del 50% sugli SMS** e **sconto del 50% sui dati**. Non sono previsti canoni; il costo scontato è arrotondato al centesimo superiore.

Ad esempio, una chiamata di 61 secondi costa 0,40 € con il piano base e 0,20 € con l'opzione chiamate.

Chiamate e SMS richiedono un destinatario attivo dello stesso gestore, diverso dal mittente. Paga soltanto il mittente; la ricezione non comporta addebiti. Per il traffico dati non è previsto un destinatario. Le quantità ammesse vanno da 1 a 1.000.000 per operazione.

Le operazioni sono simulate immediatamente: una chiamata non richiede di attendere la durata indicata e gli SMS non vengono inviati realmente. Data e ora sono registrate, ma le tariffe non dipendono dalla fascia oraria.

### Tipi di conto

| Conto | Comportamento |
| --- | --- |
| Ricaricabile | Credito iniziale zero; le operazioni richiedono credito sufficiente e non è consentito lo scoperto. |
| Fisso | Accumula addebiti senza richiedere ricariche; l'interfaccia mostra il totale degli addebiti. |

Il conto fisso è rappresentato internamente da un saldo negativo corrispondente ai consumi. Fatturazione periodica e pagamento delle fatture non rientrano nella simulazione.

### Ricariche e pagamenti

Le ricariche sono disponibili soltanto per i conti ricaricabili, con importi da **0,01 € a 1.000,00 €**. Gli importi sono espressi in centesimi; nell'inserimento sono accettati punto e virgola come separatore decimale.

| Metodo | Dati richiesti |
| --- | --- |
| Contanti | Nessun dato aggiuntivo |
| Carta di credito | Numero di 16 cifre, intestatario valido, CVV di 3 cifre e scadenza `MM/AA` non passata |
| Bancomat | PIN di 5 cifre |

I pagamenti sono simulati e utilizzano dati fittizi, senza collegamenti a servizi bancari. Per la carta vengono verificati formato e scadenza, considerando valido anche il mese corrente; non viene applicato l'algoritmo di Luhn. CVV e PIN sono mascherati nell'interfaccia. I dettagli del pagamento non vengono conservati nel database o nello storico.

### Disdetta

La disdetta è immediata: l'utenza viene rimossa dall'elenco operativo e rimane consultabile nell'archivio amministrativo insieme allo storico. Le operazioni sull'utenza sono disabilitate e il numero non può essere riutilizzato. Non sono previsti rimborsi o saldi finali.

## Soluzione progettuale

Il progetto separa interfaccia grafica, logica applicativa e persistenza, consentendo di verificare i casi d'uso indipendentemente da Swing.

| Componente | Responsabilità |
| --- | --- |
| `App` | Avvio, schermate Swing, sessioni di accesso e interazioni con l'utente |
| `UiTheme` | Stile e componenti grafici condivisi, realizzati con Swing e Java2D |
| `AdminAccess` | Verifica delle credenziali predefinite dell'amministratore |
| `Subscriber` | Record Java che rappresenta anagrafica, conto, opzioni e stato dell'utenza |
| `Service` | Registrazione, accesso abbonato, modifica piano, disdetta, consumi e ricariche |
| `Plan` | Calcolo dei costi del piano base e delle opzioni |
| `Payment` | Autorizzazione simulata dei metodi di pagamento |
| `Repository` | Contratto per le operazioni di persistenza |
| `SqliteRepository` | Implementazione del repository tramite JDBC e SQLite |

### Design pattern e principi SOLID

- **Decorator:** `Plan.Discount` avvolge un altro `Plan` e applica lo sconto alla categoria scelta. La composizione dei decorator permette di combinare le opzioni senza creare una classe per ogni combinazione tariffaria.
- **Strategy:** `Payment.Cash`, `Payment.CreditCard` e `Payment.DebitCard` implementano la stessa interfaccia. Il servizio di ricarica utilizza la strategia selezionata senza gestire direttamente i dettagli di ciascun metodo.
- **Repository e inversione delle dipendenze:** `Service` riceve un'astrazione `Repository`, separando i casi d'uso dall'implementazione SQLite.

La separazione delle responsabilità e le interfacce dedicate a tariffe e pagamenti applicano i principi SOLID. Nuove implementazioni di `Plan` e `Payment` possono essere introdotte attraverso i rispettivi contratti; nuove opzioni richiedono anche l'aggiornamento del catalogo e dell'interfaccia.

### Persistenza e gestione degli errori

Il database contiene due tabelle:

- `subscribers`: numero di telefono come chiave primaria, anagrafica, tipo di conto, saldo, opzioni e stato attivo/disdetto;
- `movements`: identificativo, riferimento all'abbonato, data e ora, descrizione e variazione del saldo.

Gli importi sono memorizzati come interi in centesimi, evitando errori di rappresentazione in virgola mobile. Le query parametrizzate utilizzano `PreparedStatement`; una chiave esterna collega ogni movimento al relativo abbonato.

Aggiornamento del saldo e inserimento del movimento avvengono nella stessa transazione. Il controllo del credito e dello stato attivo è incluso nell'aggiornamento: in caso di errore viene eseguito il rollback, evitando modifiche parziali.

Gli errori di validazione vengono segnalati tramite `IllegalArgumentException`, quelli di persistenza tramite `IllegalStateException`. L'interfaccia presenta all'utente un messaggio relativo all'operazione non completata.

## Tecnologie e struttura

- **Java 17**, Swing e Java2D per l'applicazione desktop;
- **Maven** per compilazione, test e creazione del JAR eseguibile;
- **SQLite e JDBC** per la persistenza locale;
- **JUnit 5** per i test automatici.

```text
.
├── pom.xml                         # Configurazione Maven
├── README.md                       # Descrizione e istruzioni d'uso
├── docs/
│   └── relazione.md                # Relazione e diagramma UML delle classi
├── src/main/java/it/ylenia/telefonia/
│   ├── App.java
│   ├── UiTheme.java
│   ├── AdminAccess.java
│   ├── Subscriber.java
│   ├── Service.java
│   ├── Plan.java
│   ├── Payment.java
│   ├── Repository.java
│   └── SqliteRepository.java
├── src/test/java/it/ylenia/telefonia/
│   └── ServiceTest.java            # Test della logica e della persistenza
├── data/                           # Dati locali dell'applicazione
└── target/                         # File prodotti dalla compilazione
```

## Compilazione e avvio

### Requisiti

- JDK 17 o successivo;
- Apache Maven;
- ambiente desktop per l'interfaccia Swing.

### Da terminale

Aprire un terminale nella cartella principale del progetto, contenente `pom.xml`, ed eseguire:

```sh
mvn clean package
```

Il comando compila il progetto, esegue i test e genera il JAR eseguibile con le dipendenze incluse. Avviare quindi l'applicazione con:

```sh
java -jar target/gestore-telefonico-1.0.0.jar
```

Il database `data/telefonia.db` e le relative tabelle vengono creati automaticamente se non presenti. Non occorre avviare un server database o eseguire script SQL.

Il percorso del database è relativo alla cartella da cui si esegue il programma: avviare l'applicazione dalla cartella principale del progetto per riutilizzare gli stessi dati. La cartella `data/` è esclusa dal controllo di versione.

## Documentazione e ambito

La [relazione di progetto](docs/relazione.md) approfondisce requisiti, diagramma UML, design pattern, principi SOLID e gestione della persistenza.

L'applicazione ha finalità didattiche: simula consumi e pagamenti e gestisce utenze appartenenti allo stesso gestore. Reti telefoniche reali, transazioni bancarie, verifica dell'identità dell'abbonato, fatturazione mensile e comunicazioni con altri gestori sono esterne all'ambito implementato.
