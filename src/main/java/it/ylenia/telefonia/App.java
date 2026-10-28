package it.ylenia.telefonia;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;

/** Interfaccia standalone con accessi separati e sessione abbonato. */
public final class App {
    private final Service service;
    private final JFrame frame = new JFrame("Gestore telefonico — Ylenia Cecere");
    private final AdminAccess adminAccess = new AdminAccess();
    private final JPanel pages = new JPanel(new CardLayout());
    private boolean administrator;
    private String loggedPhone;
    private final JCheckBox archive = new JCheckBox("Archivio disdette");
    private final JComboBox<Subscriber> subscribers = UiTheme.comboBox(new Subscriber[0]);
    private static final Color BACKGROUND = UiTheme.BACKGROUND;
    private static final Color INK = UiTheme.INK;
    private static final Color MUTED = UiTheme.MUTED;
    private static final Color ACCENT = UiTheme.ACCENT;
    private final JLabel person = label("Benvenuta", 27, INK, true);
    private final JLabel address = label("", 13, MUTED, false);
    private final JLabel balance = label("—", 30, ACCENT, true);
    private final JLabel account = label("Il tuo conto", 13, MUTED, false);
    private final JLabel status = label("—", 16, INK, true);
    private final JLabel phone = label("—", 14, MUTED, false);
    private final JLabel options = label("Nessuna opzione", 13, MUTED, false);
    private final JLabel activity = label("Movimenti recenti", 18, INK, true);
    private final DefaultTableModel history = new DefaultTableModel(new String[]{"Data e ora", "Operazione", "Importo"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

    private static JLabel label(String text, int size, Color color, boolean bold) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(Font.SANS_SERIF, bold ? Font.BOLD : Font.PLAIN, size));
        label.setForeground(color);
        return label;
    }

    /** Superficie arrotondata usata per raggruppare le informazioni del cruscotto. */
    private static JPanel card() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D canvas = (Graphics2D) g.create();
                canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                canvas.setColor(new Color(24, 38, 57, 8));
                canvas.fillRoundRect(0, 3, getWidth(), getHeight() - 3, 26, 26);
                canvas.setColor(Color.WHITE);
                canvas.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 4, 26, 26);
                canvas.setColor(new Color(228, 234, 240));
                canvas.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 4, 26, 26);
                canvas.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(22, 24, 22, 24));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private static void compactField(JTextField field, int width) {
        Dimension size = new Dimension(width, 42);
        field.setPreferredSize(size);
        field.setMaximumSize(size);
    }

    private static void addLine(JPanel panel, JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(component);
        panel.add(Box.createVerticalStrut(10));
    }

    private App(Repository repository) {
        service = new Service(repository);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) { repository.close(); }
        });
        JPanel root = new JPanel(new BorderLayout(0, 24));
        root.setBackground(BACKGROUND);
        root.setBorder(new EmptyBorder(28, 32, 24, 32));
        frame.setContentPane(pages);
        pages.add(root, "dashboard");

        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setOpaque(false);
        JPanel brand = new JPanel(new GridLayout(2, 1, 0, 6));
        brand.setOpaque(false);
        brand.add(label("linea.", 32, ACCENT, true));
        brand.add(label("Il tuo gestore telefonico", 13, MUTED, false));
        header.add(brand, BorderLayout.WEST);
        JPanel selectors = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        selectors.setOpaque(false);
        selectors.add(selector("ABBONATO", subscribers, 310));
        archive.setOpaque(false);
        selectors.add(archive);
        JButton logout = UiTheme.button("Esci", false);
        logout.addActionListener(e -> showLogin());
        selectors.add(logout);
        header.add(selectors, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 22));
        content.setOpaque(false);
        JPanel overview = new JPanel(new BorderLayout(0, 20));
        overview.setOpaque(false);
        JPanel identity = new JPanel(new GridLayout(2, 1, 0, 6));
        identity.setOpaque(false);
        identity.add(person); identity.add(address);
        overview.add(identity, BorderLayout.NORTH);
        JPanel cards = new JPanel(new GridLayout(1, 3, 18, 0));
        cards.setOpaque(false);
        JPanel credit = card();
        addLine(credit, label("01 / IL TUO SALDO", 11, ACCENT, true));
        addLine(credit, account); addLine(credit, balance);
        addLine(credit, label("Il saldo si aggiorna a ogni operazione", 12, MUTED, false));
        JPanel subscription = card();
        addLine(subscription, label("02 / ABBONAMENTO", 11, ACCENT, true));
        addLine(subscription, status); addLine(subscription, phone);
        JPanel plan = card();
        addLine(plan, label("03 / PIANO TARIFFARIO", 11, ACCENT, true));
        addLine(plan, label("Base", 22, INK, true));
        addLine(plan, label("0,20 €/min · 0,10 €/SMS · 0,02 €/MB", 12, MUTED, false));
        addLine(plan, options);
        cards.add(credit); cards.add(subscription); cards.add(plan);
        overview.add(cards, BorderLayout.CENTER);
        actions.setOpaque(false);
        overview.add(actions, BorderLayout.SOUTH);
        content.add(overview, BorderLayout.NORTH);

        JPanel movements = card();
        movements.setLayout(new BorderLayout(0, 16));
        movements.add(activity, BorderLayout.NORTH);
        JTable table = new JTable(history);
        table.setRowHeight(46);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.setForeground(INK);
        table.setSelectionBackground(new Color(218, 239, 234));
        table.setSelectionForeground(INK);
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        table.getTableHeader().setPreferredSize(new Dimension(0, 34));
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(170);
        table.getColumnModel().getColumn(1).setPreferredWidth(570);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable owner, Object value, boolean selected, boolean focus, int row, int column) {
                super.getTableCellRendererComponent(owner, value, selected, focus, row, column);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                setHorizontalAlignment(column == 2 ? SwingConstants.RIGHT : SwingConstants.LEFT);
                setBackground(selected ? owner.getSelectionBackground() : row % 2 == 0 ? Color.WHITE : new Color(247, 250, 251));
                setForeground(column == 2 ? (String.valueOf(value).startsWith("-") ? new Color(172, 70, 72) : ACCENT) : column == 0 ? MUTED : INK);
                setFont(new Font(Font.SANS_SERIF, column == 2 ? Font.BOLD : Font.PLAIN, 13));
                return this;
            }
        });
        JScrollPane scroll = new JScrollPane(table);
        UiTheme.styleScrollBar(scroll.getVerticalScrollBar());
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        movements.add(scroll, BorderLayout.CENTER);
        content.add(movements, BorderLayout.CENTER);
        root.add(content, BorderLayout.CENTER);
        archive.addActionListener(e -> refresh());
        subscribers.addActionListener(e -> showDetails());
        pages.add(loginPage(true), "adminLogin");
        pages.add(loginPage(false), "subscriberLogin");
        showLogin();
        frame.setMinimumSize(new Dimension(1050, 720));
        frame.setSize(1180, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void page(String name) { ((CardLayout) pages.getLayout()).show(pages, name); }

    private void showLogin() {
        administrator = false;
        loggedPhone = null;
        subscribers.removeAllItems();
        history.setRowCount(0);
        page("subscriberLogin");
    }

    private JPanel loginPage(boolean admin) {
        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(BACKGROUND);
        JPanel panel = card();
        panel.setPreferredSize(new Dimension(490, 490));
        panel.setBorder(new EmptyBorder(38, 36, 30, 36));
        addLine(panel, label(admin ? "AREA GESTIONE" : "LA TUA AREA PERSONALE", 11, ACCENT, true));
        panel.add(Box.createVerticalStrut(18));
        addLine(panel, label(admin ? "Accesso amministratore" : "Accesso abbonato", 23, INK, true));
        JLabel hint = label(admin ? "Inserisci le credenziali amministratore." : "Usa il numero registrato dall'amministratore.", 13, MUTED, false);
        addLine(panel, hint);
        JTextField username = new JTextField(24);
        addLine(panel, new JLabel(admin ? "Username" : "Numero di telefono"));
        compactField(username, 380);
        addLine(panel, username);
        JPasswordField password = new JPasswordField(24);
        password.setEchoChar('*');
        compactField(password, 380);
        if (admin) {
            addLine(panel, new JLabel("Password"));
            addLine(panel, password);

        }
        panel.add(Box.createVerticalStrut(10));
        JButton enter = UiTheme.button("Accedi alla tua area  →", true);
        enter.addActionListener(e -> {
            char[] secret = password.getPassword();
            try {
                if (admin) {
                    adminAccess.login(username.getText(), secret);
                    administrator = true;
                    loggedPhone = null;
                } else {
                    Subscriber subscriber = service.loginSubscriber(username.getText());
                    administrator = false;
                    loggedPhone = subscriber.phone();
                }
                archive.setSelected(false);
                archive.setVisible(administrator);
                subscribers.setEnabled(administrator);
                refresh();
                page("dashboard");
                username.setText("");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Accesso non riuscito", JOptionPane.ERROR_MESSAGE);
            } finally {
                java.util.Arrays.fill(secret, '\0');
                password.setText("");
            }
        });
        addLine(panel, enter);
        username.addActionListener(e -> enter.doClick());
        password.addActionListener(e -> enter.doClick());
        panel.add(Box.createVerticalGlue());
        JButton switchPage = UiTheme.button(admin ? "Sei un abbonato? Accedi qui" : "Accesso amministratore", false);
        switchPage.addActionListener(e -> {
            username.setText(""); password.setText("");
            page(admin ? "subscriberLogin" : "adminLogin");
        });
        addLine(panel, switchPage);
        JPanel composition = new JPanel(new BorderLayout(20, 0));
        composition.setOpaque(false);
        composition.add(UiTheme.welcome(), BorderLayout.WEST);
        composition.add(panel, BorderLayout.CENTER);
        background.add(composition);
        return background;
    }

    private static JPanel selector(String title, JComponent field, int width) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.add(label(title, 11, MUTED, true), BorderLayout.NORTH);
        field.setPreferredSize(new Dimension(width, 38));
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }
    private Subscriber selected() {
        Subscriber s = (Subscriber) subscribers.getSelectedItem();
        if (s == null) throw new IllegalArgumentException("Selezionare un abbonato. L'amministratore può registrarne uno.");
        return s;
    }
    private void button(String label, Runnable action) {
        JButton button = UiTheme.button(label, actions.getComponentCount() == 0 || label.equals("Ricarica"));
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        button.setMargin(new Insets(11, 18, 11, 18));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (label.equals("Disdici")) button.setForeground(new Color(180, 45, 65));
        Subscriber current = (Subscriber) subscribers.getSelectedItem();
        button.setEnabled(label.equals("Registra abbonato") || (current != null && current.active()
            && (!label.equals("Ricarica") || current.prepaid())));
        button.addActionListener(e -> {
            try { action.run(); refresh(); }
            catch (IllegalArgumentException | IllegalStateException ex) { JOptionPane.showMessageDialog(frame, ex.getMessage(), "Operazione non completata", JOptionPane.ERROR_MESSAGE); }
        });
        actions.add(button);
    }
    private void refresh() {
        Subscriber previous = (Subscriber) subscribers.getSelectedItem();
        subscribers.removeAllItems();
        boolean admin = administrator;
        for (Subscriber s : service.subscribers()) {
            if (admin ? (archive.isSelected() ? !s.active() : s.active()) : s.active() && s.phone().equals(loggedPhone)) {
                subscribers.addItem(s);
                if (previous != null && previous.phone().equals(s.phone())) subscribers.setSelectedItem(s);
            }
        }
        actions.removeAll();
        if (admin) {
            button("Registra abbonato", this::register);
            button("Modifica piano", this::plan);
            button("Disdici", () -> {
                Subscriber s = selected();
                if (JOptionPane.showConfirmDialog(frame, "Disdire l'abbonamento " + s.phone() + "? Lo storico sarà conservato.", "Disdetta", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) service.cancel(s.phone());
            });
        } else {
            button("Chiamata", () -> communicate(Plan.Usage.CHIAMATA));
            button("SMS", () -> communicate(Plan.Usage.SMS));
            button("Connessione dati", () -> communicate(Plan.Usage.DATI));
            button("Ricarica", this::topUp);
        }
        actions.revalidate(); actions.repaint(); showDetails();
    }
    private void showDetails() {
        Subscriber s = (Subscriber) subscribers.getSelectedItem();
        for (Component component : actions.getComponents()) {
            JButton button = (JButton) component;
            button.setEnabled(button.getText().equals("Registra abbonato") || (s != null && s.active()
                && (!button.getText().equals("Ricarica") || s.prepaid())));
        }
        history.setRowCount(0);
        if (s == null) {
            person.setText("Tutto inizia da una nuova linea.");
            address.setText("Seleziona la modalità Amministratore e registra il primo abbonato.");
            account.setText("IL TUO CONTO"); balance.setText("—"); status.setText("Nessuna linea selezionata");
            phone.setText("—"); options.setText("Nessuna opzione");
            activity.setText("Nessun movimento da mostrare");
            return;
        }
        person.setText(s.name() + " " + s.surname());
        address.setText(s.address());
        account.setText(s.prepaid() ? "CREDITO DISPONIBILE" : "ADDEBITI · CONTO FISSO");
        balance.setText(Service.euros(s.prepaid() ? s.balance() : -s.balance()));
        status.setText(s.active() ? "●  Linea attiva" : "●  Linea disdetta");
        status.setForeground(s.active() ? new Color(24, 125, 99) : MUTED);
        phone.setText(s.phone() + " · " + (s.prepaid() ? "Ricaricabile" : "Fisso"));
        String discounts = s.options().replace("CHIAMATA", "Chiamate").replace("DATI", "Dati").replace(",", ", ");
        options.setText(discounts.isEmpty() ? "Nessuna opzione attiva" : "−50%: " + discounts);
        options.setToolTipText("Opzioni senza canone. Chiamate conteggiate per minuto iniziato.");
        for (String movement : service.history(s.phone())) {
            String[] parts = movement.split(" \\| ", 3);
            if (parts.length == 3) {
                String date = LocalDateTime.parse(parts[0]).format(DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm", java.util.Locale.ITALIAN));
                history.addRow(new Object[]{date, parts[1], parts[2]});
            }
        }
        activity.setText(history.getRowCount() == 0 ? "Movimenti · nessuna operazione ancora" : "Movimenti recenti · " + history.getRowCount());
    }
    private boolean form(String title, Object... labelsAndFields) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 8));
        panel.setBorder(new EmptyBorder(10, 12, 10, 12));
        for (int i = 0; i < labelsAndFields.length; i += 2) {
            panel.add(new JLabel((String) labelsAndFields[i])); panel.add((Component) labelsAndFields[i + 1]);
        }
        return JOptionPane.showConfirmDialog(frame, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }
    private void register() {
        JTextField name = new JTextField(20), surname = new JTextField(20), address = new JTextField(20), phone = new JTextField(20);
        JComboBox<String> account = UiTheme.comboBox(new String[]{"Ricaricabile", "Fisso"});
        if (form("Nuovo abbonato — piano base", "Nome", name, "Cognome", surname, "Residenza", address, "Telefono", phone, "Conto", account))
            service.register(phone.getText(), name.getText(), surname.getText(), address.getText(), account.getSelectedIndex() == 0);
    }
    private void plan() {
        Subscriber s = service.active(selected().phone());
        JCheckBox calls = new JCheckBox("Sconto 50%", s.options().contains("CHIAMATA"));
        JCheckBox sms = new JCheckBox("Sconto 50%", s.options().contains("SMS"));
        JCheckBox data = new JCheckBox("Sconto 50%", s.options().contains("DATI"));
        if (form("Opzioni del piano base", "Chiamate", calls, "SMS", sms, "Dati", data)) {
            EnumSet<Plan.Usage> options = EnumSet.noneOf(Plan.Usage.class);
            if (calls.isSelected()) options.add(Plan.Usage.CHIAMATA);
            if (sms.isSelected()) options.add(Plan.Usage.SMS);
            if (data.isSelected()) options.add(Plan.Usage.DATI);
            service.changePlan(s.phone(), options);
        }
    }
    private void communicate(Plan.Usage usage) {
        Subscriber s = selected();
        JComboBox<Subscriber> recipient = UiTheme.comboBox(new Subscriber[0]);
        service.subscribers().stream().filter(a -> a.active() && !a.phone().equals(s.phone())).forEach(recipient::addItem);
        JTextField quantity = new JTextField("1");
        String unit = switch (usage) { case CHIAMATA -> "Durata simulata (secondi)"; case SMS -> "Numero SMS"; case DATI -> "Quantità (MB interi)"; };
        if (usage != Plan.Usage.DATI && recipient.getItemCount() == 0) throw new IllegalArgumentException("Serve un altro abbonato attivo come destinatario.");
        boolean ok = usage == Plan.Usage.DATI ? form("Connessione dati", unit, quantity) : form(usage.toString(), "Destinatario", recipient, unit, quantity);
        if (ok) {
            int value;
            try { value = Integer.parseInt(quantity.getText().trim()); }
            catch (NumberFormatException e) { throw new IllegalArgumentException("Inserire una quantità intera valida."); }
            long cost = service.communicate(s.phone(), usage, value, usage == Plan.Usage.DATI ? "" : ((Subscriber) recipient.getSelectedItem()).phone());
            JOptionPane.showMessageDialog(frame, "Operazione simulata completata. Costo: " + Service.euros(cost));
        }
    }
    private void topUp() {
        Subscriber s = selected();
        JTextField amount = new JTextField("10,00");
        JComboBox<String> payment = UiTheme.comboBox(new String[]{"Contanti", "Carta di credito", "Bancomat"});
        JTextField number = new JTextField(), holder = new JTextField(), expiry = new JTextField();
        JPasswordField cvv = new JPasswordField(), pin = new JPasswordField();
        cvv.setEchoChar('*'); pin.setEchoChar('*');
        JPanel details = new JPanel(new CardLayout());
        JPanel cash = new JPanel(); cash.add(new JLabel("Pagamento in contanti simulato."));
        JPanel credit = new JPanel(new GridLayout(0, 1, 0, 6));
        credit.add(new JLabel("Numero carta fittizio (16 cifre)")); credit.add(number);
        credit.add(new JLabel("Intestatario")); credit.add(holder);
        credit.add(new JLabel("CVV (3 cifre)")); credit.add(cvv);
        credit.add(new JLabel("Scadenza (MM/AA)")); credit.add(expiry);
        JPanel debit = new JPanel();
        debit.setLayout(new BoxLayout(debit, BoxLayout.Y_AXIS));
        compactField(pin, 150);
        addLine(debit, new JLabel("PIN fittizio (5 cifre)"));
        addLine(debit, pin);
        details.add(cash, "Contanti"); details.add(credit, "Carta di credito"); details.add(debit, "Bancomat");
        payment.addActionListener(e -> ((CardLayout) details.getLayout()).show(details, (String) payment.getSelectedItem()));
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        JPanel fields = new JPanel(new GridLayout(0, 1, 0, 6));
        fields.add(new JLabel("Importo (€)")); fields.add(amount);
        fields.add(new JLabel("Metodo di pagamento — usare dati fittizi")); fields.add(payment);
        panel.add(fields, BorderLayout.NORTH); panel.add(details, BorderLayout.CENTER);
        try {
            while (JOptionPane.showConfirmDialog(frame, panel, "Ricarica", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                char[] cvvValue = cvv.getPassword(), pinValue = pin.getPassword();
                try {
                    Payment strategy = switch (payment.getSelectedIndex()) {
                        case 1 -> new Payment.CreditCard(number.getText(), holder.getText(), new String(cvvValue), expiry.getText());
                        case 2 -> new Payment.DebitCard(new String(pinValue));
                        default -> new Payment.Cash();
                    };
                    service.topUp(s.phone(), amount.getText(), strategy);
                    JOptionPane.showMessageDialog(frame, "Ricarica completata.");
                    return;
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage(), "Ricarica non completata", JOptionPane.ERROR_MESSAGE);
                } finally {
                    java.util.Arrays.fill(cvvValue, '\0'); java.util.Arrays.fill(pinValue, '\0');
                }
            }
        } finally { number.setText(""); holder.setText(""); expiry.setText(""); cvv.setText(""); pin.setText(""); }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo look : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(look.getName())) { UIManager.setLookAndFeel(look.getClassName()); break; }
                }
                UIManager.put("defaultFont", new Font(Font.SANS_SERIF, Font.PLAIN, 13));
                UIManager.put("control", BACKGROUND);
                UIManager.put("nimbusBase", ACCENT);
                UIManager.put("nimbusSelectionBackground", ACCENT);
                UIManager.put("text", INK);
                UIManager.put("nimbusFocus", new Color(89, 166, 157));
                UIManager.put("nimbusLightBackground", Color.WHITE);
                UIManager.put("TextField.contentMargins", new Insets(9, 12, 9, 12));
                UIManager.put("PasswordField.contentMargins", new Insets(9, 12, 9, 12));
                UIManager.put("ComboBox.contentMargins", new Insets(6, 10, 6, 10));
                UIManager.put("TableHeader.background", new Color(243, 247, 249));
                UIManager.put("TableHeader.foreground", MUTED);
                UIManager.put("OptionPane.okButtonText", "Conferma");
                UIManager.put("OptionPane.cancelButtonText", "Annulla");
                UIManager.put("OptionPane.yesButtonText", "Sì");
                UIManager.put("OptionPane.noButtonText", "No");
                Files.createDirectories(Path.of("data"));
                new App(new SqliteRepository("jdbc:sqlite:data/telefonia.db"));
            } catch (Exception e) { JOptionPane.showMessageDialog(null, "Avvio non riuscito: " + e.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE); }
        });
    }
}
