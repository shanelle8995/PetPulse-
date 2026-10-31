/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package petpulse;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class UITheme {
    // Modern Tech Clinical Color Palette
    public static final Color DARK_SLATE = Color.decode("#0F172A");     // Sidebar BG
    public static final Color DEEP_TEAL = Color.decode("#0D9488");      // Active Nav
    public static final Color COOL_OFF_WHITE = Color.decode("#F1F5F9"); // Screen BG
    public static final Color BRIGHT_TEAL = Color.decode("#14B8A6");    // Primary Add Button
    public static final Color STEEL_BLUE = Color.decode("#475569");     // Update / Secondary
    public static final Color ARCHIVE_RED = Color.decode("#DC2626");     // Archive Button
    public static final Color NIGHT_NAVY = Color.decode("#020617");     // Primary Text
    public static final Color CARD_BG = Color.WHITE;

    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);

    public static JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(32);
        table.setGridColor(Color.decode("#E2E8F0"));
        table.setSelectionBackground(Color.decode("#CCFBF1"));
        table.setSelectionForeground(NIGHT_NAVY);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(DARK_SLATE);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.LEFT);
        table.setDefaultRenderer(Object.class, renderer);
    }
}