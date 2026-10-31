package petpulse;

import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Appointment Booking & Scheduling Module.
 *
 * Full self-service CRUD (Book, Update, Soft-Archive)
 * for both Client and Admin accounts.
 */
public class AppointmentsFrame extends BaseFrame {

    private final JTextField txtId = UITheme.field("(auto)");
    private final JTextField txtOwner = UITheme.field("");
    private final JTextField txtDate =
            UITheme.field(LocalDate.now().toString());
    private final JTextField txtTime = UITheme.field("09:00");
    private final JTextField txtSearch =
            UITheme.searchField("Search appointments...");

    private final JComboBox<String> cbPet = UITheme.combo();

    private final JComboBox<String> cbService = UITheme.combo(
            "Check-up",
            "Vaccination",
            "Grooming",
            "Surgery",
            "Dental Cleaning",
            "Deworming"
    );

    private final JComboBox<String> cbVet =
            UITheme.combo("Dr. Grey", "Dr. House");

    private final JComboBox<String> cbStatus =
            UITheme.combo("Scheduled", "Completed", "Cancelled");

    private final DefaultTableModel model = UITheme.model(
            "Appointment ID",
            "Pet Name",
            "Owner",
            "Date",
            "Time",
            "Service",
            "Veterinarian",
            "Status"
    );

    private final JTable table = new JTable(model);

    public AppointmentsFrame() {

        super(
                "Appointments",
                "Appointments",
                "Schedule clinic visits and keep track of every booking"
        );

        boolean admin = Session.isAdmin();

        UITheme.lock(txtId);
        UITheme.lock(txtOwner);

        Db.bindPets(cbPet, txtOwner);

        JPanel form = UITheme.formCard(4);

        form.add(UITheme.box("Appointment ID", txtId));
        form.add(UITheme.box("Pet Name / ID", cbPet));
        form.add(UITheme.box("Owner Name", txtOwner));
        form.add(UITheme.box("Service", cbService));
        form.add(UITheme.box("Date (YYYY-MM-DD)", txtDate));
        form.add(UITheme.box("Time 24h (HH:MM)", txtTime));
        form.add(UITheme.box("Veterinarian", cbVet));
        form.add(UITheme.box("Status", cbStatus));

        JButton btnAdd = UITheme.createBtn(
                admin ? "Add" : "Book Appointment",
                UITheme.BRIGHT_TEAL
        );

        JButton btnUpd = UITheme.createBtn(
                "Update",
                UITheme.STEEL_BLUE
        );

        JButton btnArc = UITheme.createBtn(
                "Archive",
                UITheme.ARCHIVE_RED
        );

        JButton btnClr = UITheme.createBtn(
                "Clear",
                UITheme.STEEL_BLUE
        );

        JPanel btns = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0)
        );

        btns.setOpaque(false);

        btns.add(btnAdd);
        btns.add(btnUpd);
        btns.add(btnArc);
        btns.add(btnClr);

        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);

        bar.add(btns, BorderLayout.WEST);
        bar.add(txtSearch, BorderLayout.EAST);

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);

        top.add(form, BorderLayout.NORTH);
        top.add(bar, BorderLayout.SOUTH);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setOpaque(false);

        root.add(top, BorderLayout.NORTH);
        root.add(UITheme.tableCard(table), BorderLayout.CENTER);

        setBody(root);

        UITheme.attachSearch(txtSearch, table);

        if (!admin) {
            cbStatus.setEnabled(false);
        }

        table.getSelectionModel().addListSelectionListener(e -> {

            if (e.getValueIsAdjusting()) {
                return;
            }

            int r = table.getSelectedRow();

            if (r < 0 || r >= table.getRowCount()) {
                return;
            }

            try {

                r = table.convertRowIndexToModel(r);

                txtId.setText(v(r, 0));
                Db.selectPet(cbPet, v(r, 1));
                txtOwner.setText(v(r, 2));
                txtDate.setText(v(r, 3));
                txtTime.setText(v(r, 4));

                cbService.setSelectedItem(v(r, 5));
                cbVet.setSelectedItem(v(r, 6));
                cbStatus.setSelectedItem(v(r, 7));

            } catch (Exception ex) {
            }
        });

        btnClr.addActionListener(e -> clearForm());
        btnAdd.addActionListener(e -> addAppointment());
        btnUpd.addActionListener(e -> updateAppointment());
        btnArc.addActionListener(e -> archiveAppointment());

        loadData();
        clearForm();
    }

    private String v(int r, int c) {

        Object val = model.getValueAt(r, c);

        return val == null ? "" : String.valueOf(val);
    }

    private void clearForm() {

        txtId.setText("(auto)");
        txtDate.setText(LocalDate.now().toString());
        txtTime.setText("09:00");

        cbService.setSelectedIndex(0);
        cbVet.setSelectedIndex(0);
        cbStatus.setSelectedIndex(0);

        Db.resetPetCombo(cbPet);

        if (!Session.isAdmin()) {
            txtOwner.setText(Session.getFullName());
        } else {
            txtOwner.setText("");
        }

        table.clearSelection();
    }

    private String extractPetName(Object item) {

        if (item == null) {
            return "";
        }

        String s = item.toString().trim();

        return s.contains(" - ")
                ? s.split(" - ", 2)[1].trim()
                : s;
    }

    private String check() {

        Object selectedPet = cbPet.getSelectedItem();

        if (selectedPet == null
                || selectedPet.toString().trim().isEmpty()) {

            return "Please select or type a registered Pet Name / ID first.";
        }

        return Validation.firstError(
                Validation.date(txtDate.getText(), "Date"),
                Validation.time(txtTime.getText())
        );
    }

    private boolean conflict(Connection c, String id)
            throws SQLException {

        String vet = cbVet.getSelectedItem() != null
                ? cbVet.getSelectedItem().toString()
                : "";

        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM appointments "
                + "WHERE veterinarian=? "
                + "AND appointment_date=? "
                + "AND appointment_time=? "
                + "AND status='Scheduled' "
                + "AND appointment_id<>?"
        )) {

            ps.setString(1, vet);
            ps.setString(2, txtDate.getText().trim());
            ps.setString(3, txtTime.getText().trim());
            ps.setString(4, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private void addAppointment() {

        String err = check();

        if (err != null) {
            UITheme.toast(this, err, false);
            return;
        }

        String petName =
                extractPetName(cbPet.getSelectedItem());

        String ownerName = Session.isAdmin()
                ? txtOwner.getText().trim()
                : Session.getFullName();

        String service = cbService.getSelectedItem().toString();
        String vet = cbVet.getSelectedItem().toString();
        String status = cbStatus.getSelectedItem().toString();

        try (Connection c = DBConnection.getConnection()) {

            String id = Db.nextId(c, "Appointments", "APT-");

            if ("Scheduled".equals(status) && conflict(c, id)) {

                UITheme.toast(
                        this,
                        vet + " is already booked at that date and 24h time.",
                        false
                );

                return;
            }

            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO appointments "
                    + "(appointment_id, pet_name, owner_name, "
                    + "appointment_date, appointment_time, service, "
                    + "veterinarian, status) "
                    + "VALUES (?,?,?,?,?,?,?,?)"
            )) {

                ps.setString(1, id);
                ps.setString(2, petName);
                ps.setString(3, ownerName);
                ps.setString(4, txtDate.getText().trim());
                ps.setString(5, txtTime.getText().trim());
                ps.setString(6, service);
                ps.setString(7, vet);
                ps.setString(8, status);

                ps.executeUpdate();
            }

            Db.syncAllAppointmentsToPayments();

            DBConnection.log(
                    "Appointments",
                    "Add",
                    id + " - " + petName + " (" + ownerName + ")"
            );

            UITheme.toast(
                    this,
                    "Appointment scheduled & Payment invoice auto-generated!",
                    true
            );

            clearForm();
            loadData();

        } catch (Exception ex) {
            UITheme.showError(this, ex);
        }
    }

    private void updateAppointment() {

        if ("(auto)".equals(txtId.getText())) {

            UITheme.toast(
                    this,
                    "Select an appointment from the table first.",
                    false
            );

            return;
        }

        String err = check();

        if (err != null) {
            UITheme.toast(this, err, false);
            return;
        }

        String petName =
                extractPetName(cbPet.getSelectedItem());

        String ownerName = txtOwner.getText().trim();
        String service = cbService.getSelectedItem().toString();
        String vet = cbVet.getSelectedItem().toString();
        String status = cbStatus.getSelectedItem().toString();

        try (Connection c = DBConnection.getConnection()) {

            if ("Scheduled".equals(status)
                    && conflict(c, txtId.getText())) {

                UITheme.toast(
                        this,
                        vet + " is already booked at that date and 24h time.",
                        false
                );

                return;
            }

            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE appointments SET "
                    + "pet_name=?, owner_name=?, appointment_date=?, "
                    + "appointment_time=?, service=?, veterinarian=?, "
                    + "status=? WHERE appointment_id=?"
            )) {

                ps.setString(1, petName);
                ps.setString(2, ownerName);
                ps.setString(3, txtDate.getText().trim());
                ps.setString(4, txtTime.getText().trim());
                ps.setString(5, service);
                ps.setString(6, vet);
                ps.setString(7, status);
                ps.setString(8, txtId.getText());

                ps.executeUpdate();
            }

            Db.syncAllAppointmentsToPayments();

            DBConnection.log(
                    "Appointments",
                    "Update",
                    txtId.getText() + " updated"
            );

            UITheme.toast(
                    this,
                    "Appointment & Payment synced.",
                    true
            );

            clearForm();
            loadData();

        } catch (Exception ex) {
            UITheme.showError(this, ex);
        }
    }

    private void archiveAppointment() {

        if ("(auto)".equals(txtId.getText())) {

            UITheme.toast(
                    this,
                    "Select an appointment from the table first.",
                    false
            );

            return;
        }

        if (JOptionPane.showConfirmDialog(
                this,
                "Move " + txtId.getText() + " to the archive?",
                "Archive Appointment",
                JOptionPane.YES_NO_OPTION
        ) != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            Db.archiveRecord(
                    "Appointments",
                    txtId.getText(),
                    extractPetName(cbPet.getSelectedItem())
                    + " (" + cbService.getSelectedItem() + ")"
            );

            DBConnection.log(
                    "Appointments",
                    "Archive",
                    txtId.getText() + " archived"
            );

            UITheme.toast(
                    this,
                    "Appointment moved to archive.",
                    true
            );

            clearForm();
            loadData();

        } catch (Exception ex) {
            UITheme.showError(this, ex);
        }
    }

    private void loadData() {

        try {

            Db.syncAllAppointmentsToPayments();

            if (Session.isAdmin()) {

                Db.fill(
                        model,
                        "SELECT appointment_id, pet_name, owner_name, "
                        + "appointment_date, appointment_time, service, "
                        + "veterinarian, status FROM appointments "
                        + "ORDER BY appointment_date DESC, appointment_time",
                        8
                );

            } else {

                String owner = Session.getFullName();

                model.setRowCount(0);

                try (
                        Connection c = DBConnection.getConnection();
                        PreparedStatement ps = c.prepareStatement(
                                "SELECT appointment_id, pet_name, owner_name, "
                                + "appointment_date, appointment_time, "
                                + "service, veterinarian, status "
                                + "FROM appointments WHERE owner_name=? "
                                + "ORDER BY appointment_date DESC, appointment_time"
                        )
                ) {

                    ps.setString(1, owner);

                    try (ResultSet rs = ps.executeQuery()) {

                        while (rs.next()) {

                            Object[] row = new Object[8];

                            for (int i = 0; i < 8; i++) {

                                String val = rs.getString(i + 1);

                                row[i] = val == null ? "" : val;
                            }

                            model.addRow(row);
                        }
                    }
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            UITheme.toast(
                    this,
                    "Could not load appointments. Is MySQL running?",
                    false
            );
        }
    }
}


package petpulse;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Soft-delete vault: Restore, Delete Permanently, Clear Archive (Admin only). */
public class ArchiveFrame extends BaseFrame {

    private final DefaultTableModel model = UITheme.model(
            "Archive ID",
            "Module",
            "Record ID",
            "Summary",
            "Archived At",
            "Full Data"
    );

    private final JTable table = new JTable(model);

    private final JTextField txtSearch =
            UITheme.searchField("Search archive...");

    public ArchiveFrame() {

        super(
                "Archive",
                "Archive",
                "Archived records are safe here - restore them any time"
        );

        JButton btnRestore = UITheme.createBtn(
                "Restore",
                UITheme.BRIGHT_TEAL
        );

        JButton btnDel = UITheme.createBtn(
                "Delete Permanently",
                UITheme.ARCHIVE_RED
        );

        JButton btnClear = UITheme.createBtn(
                "Clear Archive",
                UITheme.STEEL_BLUE
        );

        JPanel btns = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0)
        );

        btns.setOpaque(false);

        btns.add(btnRestore);
        btns.add(btnDel);
        btns.add(btnClear);

        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);

        // Continued in Part 2...
    }
}
