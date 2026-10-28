package it.ylenia.telefonia;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Componenti grafici condivisi, realizzati esclusivamente con Swing e Java2D. */
final class UiTheme {
    static final Color BACKGROUND = new Color(243, 246, 250);
    static final Color INK = new Color(24, 38, 57);
    static final Color MUTED = new Color(107, 122, 142);
    static final Color ACCENT = new Color(0, 113, 109);
    private UiTheme() { }

    static void styleScrollBar(JScrollBar bar) {
        bar.setPreferredSize(new Dimension(10, 10));
        bar.setUnitIncrement(24);
        bar.setOpaque(false);
        bar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            private JButton hiddenButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                button.setFocusable(false);
                return button;
            }
            @Override protected JButton createDecreaseButton(int direction) { return hiddenButton(); }
            @Override protected JButton createIncreaseButton(int direction) { return hiddenButton(); }
            @Override protected void paintTrack(Graphics graphics, JComponent component, Rectangle bounds) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(243, 247, 246));
                g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);
                g.dispose();
            }
            @Override protected void paintThumb(Graphics graphics, JComponent component, Rectangle bounds) {
                if (bounds.isEmpty() || !component.isEnabled()) return;
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(isDragging || isThumbRollover() ? ACCENT : new Color(139, 179, 171));
                g.fillRoundRect(bounds.x + 2, bounds.y + 2, Math.max(1, bounds.width - 4), Math.max(1, bounds.height - 4), 8, 8);
                g.dispose();
            }
            @Override protected Dimension getMinimumThumbSize() { return new Dimension(10, 36); }
        });
    }

    static <T> JComboBox<T> comboBox(T[] values) {
        JComboBox<T> combo = new JComboBox<>(values);
        // Il renderer esplicito evita i colori di selezione imposti da Nimbus.
        combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton arrow = new javax.swing.plaf.basic.BasicArrowButton(
                    SwingConstants.SOUTH, Color.WHITE, Color.WHITE, ACCENT, Color.WHITE);
                arrow.setBorder(new EmptyBorder(0, 6, 0, 6));
                return arrow;
            }
        });
        combo.setBackground(Color.WHITE);
        combo.setForeground(INK);
        combo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(204, 215, 223)), new EmptyBorder(2, 2, 2, 2)));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                setOpaque(true);
                setBackground(selected && index >= 0 ? new Color(218, 239, 234) : Color.WHITE);
                setForeground(combo.isEnabled() ? INK : MUTED);
                setBorder(new EmptyBorder(8, 10, 8, 10));
                return this;
            }
        });
        combo.setMaximumRowCount(8);
        return combo;
    }

    static JButton button(String text, boolean primary) {
        JButton button = new JButton(text) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = primary ? ACCENT : Color.WHITE;
                if (!isEnabled()) fill = new Color(232, 237, 242);
                else if (getModel().isPressed()) fill = primary ? ACCENT.darker() : new Color(219, 235, 233);
                else if (getModel().isRollover()) fill = primary ? new Color(0, 132, 125) : new Color(234, 244, 242);
                g.setColor(fill);
                g.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 16, 16);
                if (hasFocus()) {
                    g.setColor(new Color(100, 177, 170));
                    g.setStroke(new BasicStroke(2));
                    g.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 16, 16);
                }
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setContentAreaFilled(false);
        button.setBorder(new EmptyBorder(12, 20, 12, 20));
        button.setFocusPainted(false);
        button.setRolloverEnabled(true);
        button.setForeground(primary ? Color.WHITE : INK);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    static JPanel welcome() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setPaint(new GradientPaint(0, 0, new Color(15, 47, 60), getWidth(), getHeight(), ACCENT));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g.setStroke(new BasicStroke(1.4f));
                for (int radius = 100; radius < 650; radius += 65) {
                    g.setColor(new Color(255, 255, 255, 20));
                    g.drawOval(getWidth() - radius / 2, getHeight() - radius / 2, radius, radius);
                }
                g.setColor(new Color(178, 229, 206));
                g.fillOval(getWidth() - 90, 65, 16, 16);
                g.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(42, 38, 42, 38));
        panel.setPreferredSize(new Dimension(390, 490));
        panel.add(text("linea.", 42, Color.WHITE, true));
        panel.add(Box.createVerticalStrut(70));
        panel.add(text("Più vicini.", 34, Color.WHITE, true));
        panel.add(text("Ogni giorno.", 34, Color.WHITE, true));
        panel.add(Box.createVerticalStrut(22));
        panel.add(text("Il tuo mondo, in una sola linea.", 14, new Color(195, 225, 223), false));
        panel.add(Box.createVerticalGlue());
        panel.add(text("CHIAMATE   /   MESSAGGI   /   DATI", 11, new Color(195, 225, 223), true));
        return panel;
    }

    private static JLabel text(String value, int size, Color color, boolean bold) {
        JLabel label = new JLabel(value);
        label.setFont(new Font(Font.SANS_SERIF, bold ? Font.BOLD : Font.PLAIN, size));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
}
