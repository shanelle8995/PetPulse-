/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package petpulse;

import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private JPanel sidebarPanel;
    private JButton activeNavButton = null;

    // Dashboard Metric Labels
    private JLabel lblTotalPetsValue, lblTodayAppointmentsValue, lblTotalPaymentsValue, lblPendingVaccinationsValue;

    public MainFrame() {
        setTitle("PetPulse - Pet Care & Information System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1340, 820);
        setLocationRelativeTo(null);

        initComponents();
        loadDashboardStats();
        DBConnection.logTransaction("System", "Login", "User admin logged in");
    }

    private void initComponents() {
        Container container = getContentPane();
        container.setLayout(new BorderLayout());

        // --- SIDEBAR ---
        sidebarPanel = new JPanel();
        sidebarPanel.setBackground(UITheme.DARK_SLATE);
        sidebarPanel.setPreferredSize(new Dimension(230, 820));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));

        JLabel lblBrand = new JLabel("🐾 PetPulse");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblBrand.setForeground(Color.WHITE);
        lblBrand.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        sidebarPanel.add(lblBrand);

        String[] menuItems = {
            "Dashboard", "Pet Information", "Appointments", 
            "Vaccinations", "Payments", "Transaction History", "Pet Records", "Archive"
        };

        for (String item : menuItems) {
            JButton btnNav = createNavButton(item);
            sidebarPanel.add(btnNav);
            sidebarPanel.add(Box.createRigidArea(new Dimension(0, 4)));
            if (item.equals("Dashboard")) setActiveNavButton(btnNav);
        }

        sidebarPanel.add(Box.createVerticalGlue());

        JButton btnLogout = UITheme.createButton("Logout", UITheme.ARCHIVE_RED);
        btnLogout.setMaximumSize(new Dimension(200, 38));
        btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                DBConnection.logTransaction("System", "Logout", "User admin logged out");
                System.exit(0);
            }
        });
        sidebarPanel.add(btnLogout);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        container.add(sidebarPanel, BorderLayout.WEST);

        // --- MAIN CONTENT AREA ---
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(UITheme.COOL_OFF_WHITE);

        mainContentPanel.add(createDashboardPanel(), "Dashboard");
        mainContentPanel.add(createPetInformationPanel(), "Pet Information");
        mainContentPanel.add(createAppointmentsPanel(), "Appointments");
        mainContentPanel.add(createVaccinationsPanel(), "Vaccinations");
        mainContentPanel.add(createPaymentsPanel(), "Payments");
        mainContentPanel.add(createTransactionHistoryPanel(), "Transaction History");
        mainContentPanel.add(createPetRecordsPanel(), "Pet Records");
        mainContentPanel.add(createArchivePanel(), "Archive");

        container.add(mainContentPanel, BorderLayout.CENTER);
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(UITheme.DARK_SLATE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(210, 40));
        btn.setMargin(new Insets(0, 15, 0, 0));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            setActiveNavButton(btn);
            cardLayout.show(mainContentPanel, text);
            if (text.equals("Dashboard")) loadDashboardStats();
        });
        return btn;
    }

    private void setActiveNavButton(JButton btn) {
        if (activeNavButton != null) activeNavButton.setBackground(UITheme.DARK_SLATE);
        activeNavButton = btn;
        activeNavButton.setBackground(UITheme.DEEP_TEAL);
    }

    // ==========================================
    // 1. DASHBOARD MODULE
    // ==========================================
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UITheme.COOL_OFF_WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.COOL_OFF_WHITE);

        JLabel lblTitle = new JLabel("Dashboard Overview");
        lblTitle.setFont(UITheme.FONT_HEADER);
        lblTitle.setForeground(UITheme.NIGHT_NAVY);

        JLabel lblDate = new JLabel(new SimpleDateFormat("EEEE, MMMM d, yyyy").format(new Date()));
        lblDate.setFont(UITheme.FONT_BOLD);
        lblDate.setForeground(UITheme.STEEL_BLUE);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(lblDate, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        gridPanel.setBackground(UITheme.COOL_OFF_WHITE);

        lblTotalPetsValue = new JLabel("0", SwingConstants.CENTER);
        lblTodayAppointmentsValue = new JLabel("0", SwingConstants.CENTER);
        lblTotalPaymentsValue = new JLabel("PHP 0.00", SwingConstants.CENTER);
        lblPendingVaccinationsValue = new JLabel("0", SwingConstants.CENTER);

        gridPanel.add(createStatCard("Total Pets Registered", lblTotalPetsValue, UITheme.BRIGHT_TEAL));
        gridPanel.add(createStatCard("Today's Appointments", lblTodayAppointmentsValue, Color.decode("#3B82F6")));
        gridPanel.add(createStatCard("Total Payments Received", lblTotalPaymentsValue, Color.decode("#F59E0B")));
        gridPanel.add(createStatCard("Pending Vaccinations", lblPendingVaccinationsValue, UITheme.ARCHIVE_RED));

        panel.add(gridPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#E2E8F0"), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JPanel topBar = new JPanel();
        topBar.setBackground(accentColor);
        topBar.setPreferredSize(new Dimension(card.getWidth(), 4));
        card.add(topBar, BorderLayout.NORTH);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_SUBHEADER);
        lblTitle.setForeground(UITheme.STEEL_BLUE);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(UITheme.NIGHT_NAVY);

        card.add(lblTitle, BorderLayout.CENTER);
        card.add(valueLabel, BorderLayout.SOUTH);
        return card;
    }

    private void loadDashboardStats() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM pets");
            if (rs.next()) lblTotalPetsValue.setText(String.valueOf(rs.getInt(1)));

            rs = stmt.executeQuery("SELECT COUNT(*) FROM appointments WHERE appointment_date = CURRENT_DATE()");
            if (rs.next()) lblTodayAppointmentsValue.setText(String.valueOf(rs.getInt(1)));

            rs = stmt.executeQuery("SELECT IFNULL(SUM(amount), 0) FROM payments WHERE status = 'Paid'");
            if (rs.next()) lblTotalPaymentsValue.setText(String.format("PHP %.2f", rs.getDouble(1)));

            rs = stmt.executeQuery("SELECT COUNT(*) FROM vaccinations WHERE status = 'Pending'");
            if (rs.next()) lblPendingVaccinationsValue.setText(String.valueOf(rs.getInt(1)));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // 2. PET INFORMATION MODULE
    // ==========================================
    private JPanel createPetInformationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.COOL_OFF_WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Pet Information Details");
        lblTitle.setFont(UITheme.FONT_HEADER);
        panel.add(lblTitle, BorderLayout.NORTH);

        JTextField txtPetId = new JTextField("(auto)"); txtPetId.setEditable(false);
        JTextField txtPetName = new JTextField();
        JComboBox<String> cbSpecies = new JComboBox<>(new String[]{"Dog", "Cat", "Bird", "Other"});
        JTextField txtBreed = new JTextField();
        JTextField txtAge = new JTextField();
        JComboBox<String> cbGender = new JComboBox<>(new String[]{"Male", "Female"});
        JTextField txtOwnerName = new JTextField();
        JTextField txtContact = new JTextField();
        JComboBox<String> cbHealth = new JComboBox<>(new String[]{"Healthy", "Under Treatment", "Critical"});
        JComboBox<String> cbVaccStatus = new JComboBox<>(new String[]{"Vaccinated", "Partially Vaccinated", "Not Vaccinated"});

        JPanel formPanel = new JPanel(new GridLayout(5, 4, 10, 8));
        formPanel.setBackground(UITheme.CARD_BG);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Pet Profile Details"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        formPanel.add(new JLabel("Pet ID:")); formPanel.add(txtPetId);
        formPanel.add(new JLabel("Gender:")); formPanel.add(cbGender);
        formPanel.add(new JLabel("Pet Name:")); formPanel.add(txtPetName);
        formPanel.add(new JLabel("Owner Name:")); formPanel.add(txtOwnerName);
        formPanel.add(new JLabel("Species:")); formPanel.add(cbSpecies);
        formPanel.add(new JLabel("Contact Number:")); formPanel.add(txtContact);
        formPanel.add(new JLabel("Breed:")); formPanel.add(txtBreed);
        formPanel.add(new JLabel("Health Status:")); formPanel.add(cbHealth);
        formPanel.add(new JLabel("Age:")); formPanel.add(txtAge);
        formPanel.add(new JLabel("Vaccination Status:")); formPanel.add(cbVaccStatus);

        JButton btnAdd = UITheme.createButton("Add", UITheme.BRIGHT_TEAL);
        JButton btnUpdate = UITheme.createButton("Update", UITheme.STEEL_BLUE);
        JButton btnArchive = UITheme.createButton("Archive", UITheme.ARCHIVE_RED);
        JButton btnClear = UITheme.createButton("Clear", UITheme.STEEL_BLUE);

        JPanel btnPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        btnPanel.setBackground(UITheme.COOL_OFF_WHITE);
        btnPanel.add(btnAdd); btnPanel.add(btnUpdate); btnPanel.add(btnArchive); btnPanel.add(btnClear);

        JPanel topContainer = new JPanel(new BorderLayout(10, 10));
        topContainer.setBackground(UITheme.COOL_OFF_WHITE);
        topContainer.add(formPanel, BorderLayout.CENTER);
        topContainer.add(btnPanel, BorderLayout.EAST);

        String[] cols = {"Pet ID", "Pet Name", "Species", "Breed", "Age", "Gender", "Owner Name", "Contact", "Health", "Vaccination"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        UITheme.styleTable(table);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JTextField txtSearch = new JTextField();
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.setBackground(UITheme.COOL_OFF_WHITE);
        searchPanel.add(new JLabel("Search: "), BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
            private void filter() {
                String text = txtSearch.getText();
                sorter.setRowFilter(text.trim().isEmpty() ? null : RowFilter.regexFilter("(?i)" + text));
            }
        });

        Runnable loadData = () -> {
            model.setRowCount(0);
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) return;
                ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM pets");
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("pet_id"), rs.getString("pet_name"), rs.getString("species"),
                        rs.getString("breed"), rs.getInt("age"), rs.getString("gender"),
                        rs.getString("owner_name"), rs.getString("contact_number"),
                        rs.getString("health_status"), rs.getString("vaccination_status")
                    });
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        };
        loadData.run();

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int mRow = table.convertRowIndexToModel(row);
                txtPetId.setText(model.getValueAt(mRow, 0).toString());
                txtPetName.setText(model.getValueAt(mRow, 1).toString());
                cbSpecies.setSelectedItem(model.getValueAt(mRow, 2).toString());
                txtBreed.setText(model.getValueAt(mRow, 3).toString());
                txtAge.setText(model.getValueAt(mRow, 4).toString());
                cbGender.setSelectedItem(model.getValueAt(mRow, 5).toString());
                txtOwnerName.setText(model.getValueAt(mRow, 6).toString());
                txtContact.setText(model.getValueAt(mRow, 7).toString());
                cbHealth.setSelectedItem(model.getValueAt(mRow, 8).toString());
                cbVaccStatus.setSelectedItem(model.getValueAt(mRow, 9).toString());
            }
        });

        btnClear.addActionListener(e -> {
            txtPetId.setText("(auto)"); txtPetName.setText(""); txtBreed.setText("");
            txtAge.setText(""); txtOwnerName.setText(""); txtContact.setText("");
            table.clearSelection();
        });

        btnAdd.addActionListener(e -> {
            if (txtPetName.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Pet Name required!"); return; }
            try (Connection conn = DBConnection.getConnection()) {
                ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM pets");
                rs.next();
                String newId = String.format("PET-%03d", rs.getInt(1) + 1);

                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO pets VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                pstmt.setString(1, newId);
                pstmt.setString(2, txtPetName.getText());
                pstmt.setString(3, cbSpecies.getSelectedItem().toString());
                pstmt.setString(4, txtBreed.getText());
                pstmt.setInt(5, Integer.parseInt(txtAge.getText().isEmpty() ? "0" : txtAge.getText()));
                pstmt.setString(6, cbGender.getSelectedItem().toString());
                pstmt.setString(7, txtOwnerName.getText());
                pstmt.setString(8, txtContact.getText());
                pstmt.setString(9, cbHealth.getSelectedItem().toString());
                pstmt.setString(10, cbVaccStatus.getSelectedItem().toString());
                pstmt.executeUpdate();

                DBConnection.logTransaction("Pet Information", "Add", newId + " - " + txtPetName.getText());
                JOptionPane.showMessageDialog(this, "Pet Record Created!");
                btnClear.doClick(); loadData.run();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnUpdate.addActionListener(e -> {
            if (txtPetId.getText().equals("(auto)")) { JOptionPane.showMessageDialog(this, "Select a pet to update!"); return; }
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement pstmt = conn.prepareStatement("UPDATE pets SET pet_name=?, species=?, breed=?, age=?, gender=?, owner_name=?, contact_number=?, health_status=?, vaccination_status=? WHERE pet_id=?");
                pstmt.setString(1, txtPetName.getText());
                pstmt.setString(2, cbSpecies.getSelectedItem().toString());
                pstmt.setString(3, txtBreed.getText());
                pstmt.setInt(4, Integer.parseInt(txtAge.getText()));
                pstmt.setString(5, cbGender.getSelectedItem().toString());
                pstmt.setString(6, txtOwnerName.getText());
                pstmt.setString(7, txtContact.getText());
                pstmt.setString(8, cbHealth.getSelectedItem().toString());
                pstmt.setString(9, cbVaccStatus.getSelectedItem().toString());
                pstmt.setString(10, txtPetId.getText());
                pstmt.executeUpdate();

                DBConnection.logTransaction("Pet Information", "Update", txtPetId.getText() + " Updated");
                JOptionPane.showMessageDialog(this, "Pet Record Updated!");
                btnClear.doClick(); loadData.run();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnArchive.addActionListener(e -> {
            if (txtPetId.getText().equals("(auto)")) { JOptionPane.showMessageDialog(this, "Select a pet to archive!"); return; }
            int confirm = JOptionPane.showConfirmDialog(this, "Move record " + txtPetId.getText() + " to Archive?", "Confirm Archival", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DBConnection.getConnection()) {
                    PreparedStatement apstmt = conn.prepareStatement("INSERT INTO archive (module_name, record_id, summary, full_data) VALUES ('Pet Information', ?, ?, ?)");
                    apstmt.setString(1, txtPetId.getText());
                    apstmt.setString(2, txtPetName.getText() + " (" + cbSpecies.getSelectedItem() + ")");
                    apstmt.setString(3, String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", 
                        txtPetId.getText(), txtPetName.getText(), cbSpecies.getSelectedItem(), txtBreed.getText(),
                        txtAge.getText(), cbGender.getSelectedItem(), txtOwnerName.getText(), txtContact.getText(),
                        cbHealth.getSelectedItem(), cbVaccStatus.getSelectedItem()));
                    apstmt.executeUpdate();

                    PreparedStatement dpstmt = conn.prepareStatement("DELETE FROM pets WHERE pet_id=?");
                    dpstmt.setString(1, txtPetId.getText());
                    dpstmt.executeUpdate();

                    DBConnection.logTransaction("Pet Information", "Archive", txtPetId.getText() + " Archived");
                    JOptionPane.showMessageDialog(this, "Record Moved to Archive Vault!");
                    btnClear.doClick(); loadData.run();
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
        });

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBackground(UITheme.COOL_OFF_WHITE);
        bottomPanel.add(searchPanel, BorderLayout.NORTH);
        bottomPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        panel.add(topContainer, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ==========================================
    // 3. APPOINTMENTS MODULE
    // ==========================================
    private JPanel createAppointmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.COOL_OFF_WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Appointments Details");
        lblTitle.setFont(UITheme.FONT_HEADER);
        panel.add(lblTitle, BorderLayout.NORTH);

        JTextField txtAptId = new JTextField("(auto)"); txtAptId.setEditable(false);
        JTextField txtPetName = new JTextField();
        JTextField txtOwnerName = new JTextField();
        JTextField txtDate = new JTextField("2026-10-15");
        JTextField txtTime = new JTextField("09:00");
        JComboBox<String> cbService = new JComboBox<>(new String[]{"Check-up", "Vaccination", "Grooming", "Surgery"});
        JComboBox<String> cbVet = new JComboBox<>(new String[]{"Dr. Santos", "Dr. Reyes"});
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Scheduled", "Completed", "Cancelled"});

        JPanel formPanel = new JPanel(new GridLayout(4, 4, 10, 8));
        formPanel.setBackground(UITheme.CARD_BG);
        formPanel.setBorder(BorderFactory.createTitledBorder("Schedule Appointment"));

        formPanel.add(new JLabel("Appointment ID:")); formPanel.add(txtAptId);
        formPanel.add(new JLabel("Service:")); formPanel.add(cbService);
        formPanel.add(new JLabel("Pet Name:")); formPanel.add(txtPetName);
        formPanel.add(new JLabel("Veterinarian:")); formPanel.add(cbVet);
        formPanel.add(new JLabel("Owner Name:")); formPanel.add(txtOwnerName);
        formPanel.add(new JLabel("Status:")); formPanel.add(cbStatus);
        formPanel.add(new JLabel("Date (YYYY-MM-DD):")); formPanel.add(txtDate);
        formPanel.add(new JLabel("Time (HH:MM):")); formPanel.add(txtTime);

        JButton btnAdd = UITheme.createButton("Add", UITheme.BRIGHT_TEAL);
        JButton btnUpdate = UITheme.createButton("Update", UITheme.STEEL_BLUE);
        JButton btnArchive = UITheme.createButton("Archive", UITheme.ARCHIVE_RED);
        JButton btnClear = UITheme.createButton("Clear", UITheme.STEEL_BLUE);

        JPanel btnPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        btnPanel.add(btnAdd); btnPanel.add(btnUpdate); btnPanel.add(btnArchive); btnPanel.add(btnClear);

        JPanel topContainer = new JPanel(new BorderLayout(10, 10));
        topContainer.setBackground(UITheme.COOL_OFF_WHITE);
        topContainer.add(formPanel, BorderLayout.CENTER);
        topContainer.add(btnPanel, BorderLayout.EAST);

        String[] cols = {"Appointment ID", "Pet Name", "Owner Name", "Date", "Time", "Service", "Veterinarian", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        UITheme.styleTable(table);

        Runnable loadData = () -> {
            model.setRowCount(0);
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) return;
                ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM appointments");
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("appointment_id"), rs.getString("pet_name"), rs.getString("owner_name"),
                        rs.getString("appointment_date"), rs.getString("appointment_time"),
                        rs.getString("service"), rs.getString("veterinarian"), rs.getString("status")
                    });
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        };
        loadData.run();

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                txtAptId.setText(model.getValueAt(row, 0).toString());
                txtPetName.setText(model.getValueAt(row, 1).toString());
                txtOwnerName.setText(model.getValueAt(row, 2).toString());
                txtDate.setText(model.getValueAt(row, 3).toString());
                txtTime.setText(model.getValueAt(row, 4).toString());
                cbService.setSelectedItem(model.getValueAt(row, 5).toString());
                cbVet.setSelectedItem(model.getValueAt(row, 6).toString());
                cbStatus.setSelectedItem(model.getValueAt(row, 7).toString());
            }
        });

        btnClear.addActionListener(e -> {
            txtAptId.setText("(auto)"); txtPetName.setText(""); txtOwnerName.setText("");
            table.clearSelection();
        });

        btnAdd.addActionListener(e -> {
            try (Connection conn = DBConnection.getConnection()) {
                ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM appointments");
                rs.next();
                String newId = String.format("APT-%03d", rs.getInt(1) + 1);

                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO appointments VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                pstmt.setString(1, newId);
                pstmt.setString(2, txtPetName.getText());
                pstmt.setString(3, txtOwnerName.getText());
                pstmt.setString(4, txtDate.getText());
                pstmt.setString(5, txtTime.getText());
                pstmt.setString(6, cbService.getSelectedItem().toString());
                pstmt.setString(7, cbVet.getSelectedItem().toString());
                pstmt.setString(8, cbStatus.getSelectedItem().toString());
                pstmt.executeUpdate();

                DBConnection.logTransaction("Appointments", "Add", newId + " - " + txtPetName.getText());
                JOptionPane.showMessageDialog(this, "Appointment Created!");
                btnClear.doClick(); loadData.run();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnUpdate.addActionListener(e -> {
            if (txtAptId.getText().equals("(auto)")) return;
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement pstmt = conn.prepareStatement("UPDATE appointments SET pet_name=?, owner_name=?, appointment_date=?, appointment_time=?, service=?, veterinarian=?, status=? WHERE appointment_id=?");
                pstmt.setString(1, txtPetName.getText());
                pstmt.setString(2, txtOwnerName.getText());
                pstmt.setString(3, txtDate.getText());
                pstmt.setString(4, txtTime.getText());
                pstmt.setString(5, cbService.getSelectedItem().toString());
                pstmt.setString(6, cbVet.getSelectedItem().toString());
                pstmt.setString(7, cbStatus.getSelectedItem().toString());
                pstmt.setString(8, txtAptId.getText());
                pstmt.executeUpdate();

                DBConnection.logTransaction("Appointments", "Update", txtAptId.getText() + " Updated");
                JOptionPane.showMessageDialog(this, "Appointment Updated!");
                btnClear.doClick(); loadData.run();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnArchive.addActionListener(e -> {
            if (txtAptId.getText().equals("(auto)")) return;
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement apstmt = conn.prepareStatement("INSERT INTO archive (module_name, record_id, summary, full_data) VALUES ('Appointments', ?, ?, ?)");
                apstmt.setString(1, txtAptId.getText());
                apstmt.setString(2, txtPetName.getText() + " (" + cbService.getSelectedItem() + ")");
                apstmt.setString(3, String.format("%s,%s,%s,%s,%s,%s,%s,%s", txtAptId.getText(), txtPetName.getText(), txtOwnerName.getText(), txtDate.getText(), txtTime.getText(), cbService.getSelectedItem(), cbVet.getSelectedItem(), cbStatus.getSelectedItem()));
                apstmt.executeUpdate();

                PreparedStatement dpstmt = conn.prepareStatement("DELETE FROM appointments WHERE appointment_id=?");
                dpstmt.setString(1, txtAptId.getText());
                dpstmt.executeUpdate();

                DBConnection.logTransaction("Appointments", "Archive", txtAptId.getText() + " Archived");
                JOptionPane.showMessageDialog(this, "Appointment Moved to Archive!");
                btnClear.doClick(); loadData.run();
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        panel.add(topContainer, BorderLayout.CENTER);
        panel.add(new JScrollPane(table), BorderLayout.SOUTH);
        return panel;
    }

    // ==========================================
    // 4. VACCINATIONS MODULE (Matching Screenshot 116 Layout)
    // ==========================================
    private JPanel createVaccinationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.COOL_OFF_WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Vaccinations Details");
        lblTitle.setFont(UITheme.FONT_HEADER);
        panel.add(lblTitle, BorderLayout.NORTH);

        JTextField txtVacId = new JTextField("(auto)"); txtVacId.setEditable(false);
        JTextField txtPetName = new JTextField();
        JComboBox<String> cbVaccine = new JComboBox<>(new String[]{"Rabies", "DHPP", "Bordetella", "FVRCP"});
        JTextField txtDateGiven = new JTextField("2026-10-05");
        JTextField txtNextDueDate = new JTextField("2027-10-05");
        JTextField txtDosage = new JTextField("1 ml");
        JComboBox<String> cbVet = new JComboBox<>(new String[]{"Dr. Santos", "Dr. Reyes"});
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Done", "Pending"});

        JPanel formPanel = new JPanel(new GridLayout(5, 4, 10, 8));
        formPanel.setBackground(UITheme.CARD_BG);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Vaccination Record Details"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        formPanel.add(new JLabel("Vaccination ID:")); formPanel.add(txtVacId);
        formPanel.add(new JLabel("Dosage:")); formPanel.add(txtDosage);
        formPanel.add(new JLabel("Pet Name:")); formPanel.add(txtPetName);
        formPanel.add(new JLabel("Veterinarian:")); formPanel.add(cbVet);
        formPanel.add(new JLabel("Vaccine Name:")); formPanel.add(cbVaccine);
        formPanel.add(new JLabel("Status:")); formPanel.add(cbStatus);
        formPanel.add(new JLabel("Date Given:")); formPanel.add(txtDateGiven);
        formPanel.add(new JLabel("")); formPanel.add(new JLabel(""));
        formPanel.add(new JLabel("Next Due Date:")); formPanel.add(txtNextDueDate);
        formPanel.add(new JLabel("")); formPanel.add(new JLabel(""));

        JButton btnAdd = UITheme.createButton("Add", UITheme.BRIGHT_TEAL);
        JButton btnUpdate = UITheme.createButton("Update", UITheme.STEEL_BLUE);
        JButton btnArchive = UITheme.createButton("Archive", UITheme.ARCHIVE_RED);
        JButton btnClear = UITheme.createButton("Clear", UITheme.STEEL_BLUE);

        JPanel btnPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        btnPanel.add(btnAdd); btnPanel.add(btnUpdate); btnPanel.add(btnArchive); btnPanel.add(btnClear);

        JPanel topContainer = new JPanel(new BorderLayout(10, 10));
        topContainer.setBackground(UITheme.COOL_OFF_WHITE);
        topContainer.add(formPanel, BorderLayout.CENTER);
        topContainer.add(btnPanel, BorderLayout.EAST);

        String[] cols = {"Vaccination ID", "Pet Name", "Vaccine Name", "Date Given", "Next Due Date", "Dosage", "Veterinarian", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        UITheme.styleTable(table);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JTextField txtSearch = new JTextField();
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.setBackground(UITheme.COOL_OFF_WHITE);
        searchPanel.add(new JLabel("Search: "), BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
            private void filter() {
                String text = txtSearch.getText();
                sorter.setRowFilter(text.trim().isEmpty() ? null : RowFilter.regexFilter("(?i)" + text));
            }
        });

        Runnable loadData = () -> {
            model.setRowCount(0);
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) return;
                ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM vaccinations");
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("vaccination_id"), rs.getString("pet_name"), rs.getString("vaccine_name"),
                        rs.getString("date_given"), rs.getString("next_due_date"), rs.getString("dosage"),
                        rs.getString("veterinarian"), rs.getString("status")
                    });
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        };
        loadData.run();

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int mRow = table.convertRowIndexToModel(row);
                txtVacId.setText(model.getValueAt(mRow, 0).toString());
                txtPetName.setText(model.getValueAt(mRow, 1).toString());
                cbVaccine.setSelectedItem(model.getValueAt(mRow, 2).toString());
                txtDateGiven.setText(model.getValueAt(mRow, 3).toString());
                txtNextDueDate.setText(model.getValueAt(mRow, 4).toString());
                txtDosage.setText(model.getValueAt(mRow, 5).toString());
                cbVet.setSelectedItem(model.getValueAt(mRow, 6).toString());
                cbStatus.setSelectedItem(model.getValueAt(mRow, 7).toString());
            }
        });

        btnClear.addActionListener(e -> {
            txtVacId.setText("(auto)"); txtPetName.setText(""); txtDosage.setText("");
            table.clearSelection();
        });

        btnAdd.addActionListener(e -> {
            try (Connection conn = DBConnection.getConnection()) {
                ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM vaccinations");
                rs.next();
                String newId = String.format("VAC-%03d", rs.getInt(1) + 1);

                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO vaccinations VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                pstmt.setString(1, newId);
                pstmt.setString(2, txtPetName.getText());
                pstmt.setString(3, cbVaccine.getSelectedItem().toString());
                pstmt.setString(4, txtDateGiven.getText());
                pstmt.setString(5, txtNextDueDate.getText());
                pstmt.setString(6, txtDosage.getText());
                pstmt.setString(7, cbVet.getSelectedItem().toString());
                pstmt.setString(8, cbStatus.getSelectedItem().toString());
                pstmt.executeUpdate();

                DBConnection.logTransaction("Vaccinations", "Add", newId + " - " + txtPetName.getText());
                JOptionPane.showMessageDialog(this, "Vaccination Record Added!");
                btnClear.doClick(); loadData.run();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnUpdate.addActionListener(e -> {
            if (txtVacId.getText().equals("(auto)")) return;
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement pstmt = conn.prepareStatement("UPDATE vaccinations SET pet_name=?, vaccine_name=?, date_given=?, next_due_date=?, dosage=?, veterinarian=?, status=? WHERE vaccination_id=?");
                pstmt.setString(1, txtPetName.getText());
                pstmt.setString(2, cbVaccine.getSelectedItem().toString());
                pstmt.setString(3, txtDateGiven.getText());
                pstmt.setString(4, txtNextDueDate.getText());
                pstmt.setString(5, txtDosage.getText());
                pstmt.setString(6, cbVet.getSelectedItem().toString());
                pstmt.setString(7, cbStatus.getSelectedItem().toString());
                pstmt.setString(8, txtVacId.getText());
                pstmt.executeUpdate();

                DBConnection.logTransaction("Vaccinations", "Update", txtVacId.getText() + " Updated");
                JOptionPane.showMessageDialog(this, "Vaccination Record Updated!");
                btnClear.doClick(); loadData.run();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnArchive.addActionListener(e -> {
            if (txtVacId.getText().equals("(auto)")) return;
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement apstmt = conn.prepareStatement("INSERT INTO archive (module_name, record_id, summary, full_data) VALUES ('Vaccinations', ?, ?, ?)");
                apstmt.setString(1, txtVacId.getText());
                apstmt.setString(2, txtPetName.getText() + " (" + cbVaccine.getSelectedItem() + ")");
                apstmt.setString(3, String.format("%s,%s,%s,%s,%s,%s,%s,%s", txtVacId.getText(), txtPetName.getText(), cbVaccine.getSelectedItem(), txtDateGiven.getText(), txtNextDueDate.getText(), txtDosage.getText(), cbVet.getSelectedItem(), cbStatus.getSelectedItem()));
                apstmt.executeUpdate();

                PreparedStatement dpstmt = conn.prepareStatement("DELETE FROM vaccinations WHERE vaccination_id=?");
                dpstmt.setString(1, txtVacId.getText());
                dpstmt.executeUpdate();

                DBConnection.logTransaction("Vaccinations", "Archive", txtVacId.getText() + " Archived");
                JOptionPane.showMessageDialog(this, "Vaccination Moved to Archive!");
                btnClear.doClick(); loadData.run();
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBackground(UITheme.COOL_OFF_WHITE);
        bottomPanel.add(searchPanel, BorderLayout.NORTH);
        bottomPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        panel.add(topContainer, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ==========================================
    // 5. PAYMENTS MODULE (Matching Screenshot 117 Compact Horizontal Layout)
    // ==========================================
    private JPanel createPaymentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.COOL_OFF_WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Payments Details");
        lblTitle.setFont(UITheme.FONT_HEADER);
        panel.add(lblTitle, BorderLayout.NORTH);

        JTextField txtPayId = new JTextField("(auto)"); txtPayId.setEditable(false);
        JTextField txtPetName = new JTextField();
        JTextField txtOwnerName = new JTextField();
        JComboBox<String> cbService = new JComboBox<>(new String[]{"Check-up", "Grooming", "Vaccination", "Surgery"});
        JTextField txtAmount = new JTextField("500.00");
        JComboBox<String> cbMethod = new JComboBox<>(new String[]{"Cash", "GCash", "Card"});
        JTextField txtPayDate = new JTextField("2026-10-05");
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Paid", "Pending"});

        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        formPanel.setBackground(UITheme.CARD_BG);
        formPanel.setBorder(BorderFactory.createTitledBorder("Payments Entry Row"));

        txtPayId.setPreferredSize(new Dimension(70, 26));
        txtPetName.setPreferredSize(new Dimension(90, 26));
        txtOwnerName.setPreferredSize(new Dimension(110, 26));
        txtAmount.setPreferredSize(new Dimension(80, 26));
        txtPayDate.setPreferredSize(new Dimension(90, 26));

        formPanel.add(new JLabel("Payment ID:")); formPanel.add(txtPayId);
        formPanel.add(new JLabel("Pet Name:")); formPanel.add(txtPetName);
        formPanel.add(new JLabel("Owner:")); formPanel.add(txtOwnerName);
        formPanel.add(new JLabel("Service:")); formPanel.add(cbService);
        formPanel.add(new JLabel("Amount:")); formPanel.add(txtAmount);
        formPanel.add(new JLabel("Method:")); formPanel.add(cbMethod);
        formPanel.add(new JLabel("Date:")); formPanel.add(txtPayDate);
        formPanel.add(new JLabel("Status:")); formPanel.add(cbStatus);

        JButton btnAdd = UITheme.createButton("Add", UITheme.BRIGHT_TEAL);
        JButton btnUpdate = UITheme.createButton("Update", UITheme.STEEL_BLUE);
        JButton btnArchive = UITheme.createButton("Archive", UITheme.ARCHIVE_RED);
        JButton btnClear = UITheme.createButton("Clear", UITheme.STEEL_BLUE);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        btnRow.setBackground(UITheme.COOL_OFF_WHITE);
        btnRow.add(btnAdd); btnRow.add(btnUpdate); btnRow.add(btnArchive); btnRow.add(btnClear);

        String[] cols = {"Payment ID", "Pet Name", "Owner Name", "Service", "Amount", "Payment Method", "Payment Date", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        UITheme.styleTable(table);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JTextField txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(180, 26));
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(UITheme.COOL_OFF_WHITE);
        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(txtSearch);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
            private void filter() {
                String text = txtSearch.getText();
                sorter.setRowFilter(text.trim().isEmpty() ? null : RowFilter.regexFilter("(?i)" + text));
            }
        });

        Runnable loadData = () -> {
            model.setRowCount(0);
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) return;
                ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM payments");
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("payment_id"), rs.getString("pet_name"), rs.getString("owner_name"),
                        rs.getString("service"), rs.getDouble("amount"), rs.getString("payment_method"),
                        rs.getString("payment_date"), rs.getString("status")
                    });
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        };
        loadData.run();

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int mRow = table.convertRowIndexToModel(row);
                txtPayId.setText(model.getValueAt(mRow, 0).toString());
                txtPetName.setText(model.getValueAt(mRow, 1).toString());
                txtOwnerName.setText(model.getValueAt(mRow, 2).toString());
                cbService.setSelectedItem(model.getValueAt(mRow, 3).toString());
                txtAmount.setText(model.getValueAt(mRow, 4).toString());
                cbMethod.setSelectedItem(model.getValueAt(mRow, 5).toString());
                txtPayDate.setText(model.getValueAt(mRow, 6).toString());
                cbStatus.setSelectedItem(model.getValueAt(mRow, 7).toString());
            }
        });

        btnClear.addActionListener(e -> {
            txtPayId.setText("(auto)"); txtPetName.setText(""); txtOwnerName.setText(""); txtAmount.setText("0.00");
            table.clearSelection();
        });

        btnAdd.addActionListener(e -> {
            try (Connection conn = DBConnection.getConnection()) {
                ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM payments");
                rs.next();
                String newId = String.format("PAY-%03d", rs.getInt(1) + 1);

                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO payments VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                pstmt.setString(1, newId);
                pstmt.setString(2, txtPetName.getText());
                pstmt.setString(3, txtOwnerName.getText());
                pstmt.setString(4, cbService.getSelectedItem().toString());
                pstmt.setDouble(5, Double.parseDouble(txtAmount.getText().isEmpty() ? "0" : txtAmount.getText()));
                pstmt.setString(6, cbMethod.getSelectedItem().toString());
                pstmt.setString(7, txtPayDate.getText());
                pstmt.setString(8, cbStatus.getSelectedItem().toString());
                pstmt.executeUpdate();

                DBConnection.logTransaction("Payments", "Add", newId + " - " + txtPetName.getText());
                JOptionPane.showMessageDialog(this, "Payment Entry Saved!");
                btnClear.doClick(); loadData.run();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnUpdate.addActionListener(e -> {
            if (txtPayId.getText().equals("(auto)")) return;
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement pstmt = conn.prepareStatement("UPDATE payments SET pet_name=?, owner_name=?, service=?, amount=?, payment_method=?, payment_date=?, status=? WHERE payment_id=?");
                pstmt.setString(1, txtPetName.getText());
                pstmt.setString(2, txtOwnerName.getText());
                pstmt.setString(3, cbService.getSelectedItem().toString());
                pstmt.setDouble(4, Double.parseDouble(txtAmount.getText()));
                pstmt.setString(5, cbMethod.getSelectedItem().toString());
                pstmt.setString(6, txtPayDate.getText());
                pstmt.setString(7, cbStatus.getSelectedItem().toString());
                pstmt.setString(8, txtPayId.getText());
                pstmt.executeUpdate();

                DBConnection.logTransaction("Payments", "Update", txtPayId.getText() + " Updated");
                JOptionPane.showMessageDialog(this, "Payment Updated!");
                btnClear.doClick(); loadData.run();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        btnArchive.addActionListener(e -> {
            if (txtPayId.getText().equals("(auto)")) return;
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement apstmt = conn.prepareStatement("INSERT INTO archive (module_name, record_id, summary, full_data) VALUES ('Payments', ?, ?, ?)");
                apstmt.setString(1, txtPayId.getText());
                apstmt.setString(2, txtPetName.getText() + " (PHP " + txtAmount.getText() + ")");
                apstmt.setString(3, String.format("%s,%s,%s,%s,%s,%s,%s,%s", txtPayId.getText(), txtPetName.getText(), txtOwnerName.getText(), cbService.getSelectedItem(), txtAmount.getText(), cbMethod.getSelectedItem(), txtPayDate.getText(), cbStatus.getSelectedItem()));
                apstmt.executeUpdate();

                PreparedStatement dpstmt = conn.prepareStatement("DELETE FROM payments WHERE payment_id=?");
                dpstmt.setString(1, txtPayId.getText());
                dpstmt.executeUpdate();

                DBConnection.logTransaction("Payments", "Archive", txtPayId.getText() + " Archived");
                JOptionPane.showMessageDialog(this, "Payment Record Archived!");
                btnClear.doClick(); loadData.run();
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(UITheme.COOL_OFF_WHITE);
        topContainer.add(formPanel, BorderLayout.NORTH);

        JPanel controlsBar = new JPanel(new BorderLayout());
        controlsBar.setBackground(UITheme.COOL_OFF_WHITE);
        controlsBar.add(btnRow, BorderLayout.WEST);
        controlsBar.add(searchPanel, BorderLayout.EAST);

        topContainer.add(controlsBar, BorderLayout.SOUTH);

        panel.add(topContainer, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // ==========================================
    // 6. TRANSACTION HISTORY MODULE
    // ==========================================
    private JPanel createTransactionHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.COOL_OFF_WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Transaction Audit Trail Log");
        lblTitle.setFont(UITheme.FONT_HEADER);
        panel.add(lblTitle, BorderLayout.NORTH);

        String[] cols = {"Date & Time", "Module", "Action", "Details"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        UITheme.styleTable(table);

        Runnable loadLogs = () -> {
            model.setRowCount(0);
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) return;
                ResultSet rs = conn.createStatement().executeQuery("SELECT timestamp, module_name, action_type, details FROM transaction_history ORDER BY timestamp DESC");
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)});
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        };
        loadLogs.run();

        JButton btnClear = UITheme.createButton("Clear History", UITheme.ARCHIVE_RED);
        btnClear.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Clear all transaction logs?", "Clear Audit History", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DBConnection.getConnection()) {
                    conn.createStatement().executeUpdate("TRUNCATE TABLE transaction_history");
                    loadLogs.run();
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setBackground(UITheme.COOL_OFF_WHITE);
        bottom.add(btnClear);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    // ==========================================
    // 7. PET RECORDS MODULE (View Only)
    // ==========================================
    private JPanel createPetRecordsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.COOL_OFF_WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Consolidated View-Only Pet Records");
        lblTitle.setFont(UITheme.FONT_HEADER);
        panel.add(lblTitle, BorderLayout.NORTH);

        String[] cols = {"Pet ID", "Pet Name", "Species", "Owner Name"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        UITheme.styleTable(table);

        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                ResultSet rs = conn.createStatement().executeQuery("SELECT pet_id, pet_name, species, owner_name FROM pets");
                while (rs.next()) model.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)});
            }
        } catch (SQLException ex) { ex.printStackTrace(); }

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // ==========================================
    // 8. ARCHIVE MODULE
    // ==========================================
    private JPanel createArchivePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UITheme.COOL_OFF_WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Archived Records Repository");
        lblTitle.setFont(UITheme.FONT_HEADER);
        panel.add(lblTitle, BorderLayout.NORTH);

        String[] cols = {"Archive ID", "Module", "Record ID", "Summary", "Archived At", "Full Data"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        UITheme.styleTable(table);

        Runnable loadArchive = () -> {
            model.setRowCount(0);
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) return;
                ResultSet rs = conn.createStatement().executeQuery("SELECT archive_id, module_name, record_id, summary, archived_at, full_data FROM archive");
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6)});
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        };
        loadArchive.run();

        JButton btnRestore = UITheme.createButton("Restore", UITheme.BRIGHT_TEAL);
        JButton btnDeletePerm = UITheme.createButton("Delete Permanently", UITheme.ARCHIVE_RED);
        JButton btnClearArchive = UITheme.createButton("Clear Archive", UITheme.STEEL_BLUE);

        btnRestore.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Select an item to restore!"); return; }
            int archiveId = (int) model.getValueAt(row, 0);
            String module = model.getValueAt(row, 1).toString();
            String fullData = model.getValueAt(row, 5).toString();
            String[] val = fullData.split(",");

            try (Connection conn = DBConnection.getConnection()) {
                if (module.equals("Pet Information")) {
                    PreparedStatement pstmt = conn.prepareStatement("INSERT INTO pets VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    for (int i = 0; i < 10; i++) {
                        if (i == 4) pstmt.setInt(5, Integer.parseInt(val[i]));
                        else pstmt.setString(i + 1, val[i]);
                    }
                    pstmt.executeUpdate();
                } else if (module.equals("Appointments")) {
                    PreparedStatement pstmt = conn.prepareStatement("INSERT INTO appointments VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                    for (int i = 0; i < 8; i++) pstmt.setString(i + 1, val[i]);
                    pstmt.executeUpdate();
                } else if (module.equals("Vaccinations")) {
                    PreparedStatement pstmt = conn.prepareStatement("INSERT INTO vaccinations VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                    for (int i = 0; i < 8; i++) pstmt.setString(i + 1, val[i]);
                    pstmt.executeUpdate();
                } else if (module.equals("Payments")) {
                    PreparedStatement pstmt = conn.prepareStatement("INSERT INTO payments VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                    pstmt.setString(1, val[0]); pstmt.setString(2, val[1]); pstmt.setString(3, val[2]);
                    pstmt.setString(4, val[3]); pstmt.setDouble(5, Double.parseDouble(val[4]));
                    pstmt.setString(6, val[5]); pstmt.setString(7, val[6]); pstmt.setString(8, val[7]);
                    pstmt.executeUpdate();
                }

                PreparedStatement dpstmt = conn.prepareStatement("DELETE FROM archive WHERE archive_id=?");
                dpstmt.setInt(1, archiveId);
                dpstmt.executeUpdate();

                DBConnection.logTransaction("Archive", "Restore", module + " - " + model.getValueAt(row, 2));
                JOptionPane.showMessageDialog(this, "Record Restored Successfully!");
                loadArchive.run();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error Restoring: " + ex.getMessage()); }
        });

        btnDeletePerm.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Select an item to delete permanently!"); return; }
            int archiveId = (int) model.getValueAt(row, 0);
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM archive WHERE archive_id=?");
                pstmt.setInt(1, archiveId);
                pstmt.executeUpdate();

                DBConnection.logTransaction("Archive", "Delete", "Permanently deleted archive ID " + archiveId);
                loadArchive.run();
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        btnClearArchive.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Permanently delete all archived records?", "Clear Archive", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DBConnection.getConnection()) {
                    conn.createStatement().executeUpdate("TRUNCATE TABLE archive");
                    loadArchive.run();
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
        });

        JPanel bottomControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bottomControls.setBackground(UITheme.COOL_OFF_WHITE);
        bottomControls.add(btnRestore);
        bottomControls.add(btnDeletePerm);
        bottomControls.add(btnClearArchive);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(bottomControls, BorderLayout.SOUTH);
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}