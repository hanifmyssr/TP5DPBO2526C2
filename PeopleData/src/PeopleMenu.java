import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;

public class PeopleMenu extends JFrame {
    // ===== komponen (nama harus sama dengan "binding" di PeopleMenu.form) =====
    private JPanel mainPanel;
    private JPanel formPanel;
    private JPanel buttonPanel;
    private JLabel titleLabel;
    private JTextField idField;
    private JTextField namaField;
    private JTextField tahunLahirField;
    private JComboBox<String> kategoriComboBox;
    private JComboBox<String> pangkatComboBox;
    private JButton addUpdateButton;
    private JButton cancelButton;
    private JButton deleteButton;
    private JTable peopleTable;
    private JScrollPane tableScrollPane;

    // ===== database =====
    private Database database;
    private Connection conn;

    // ID baris yang sedang dipilih; null = mode tambah (tidak ada baris dipilih)
    private String selectedId = null;

    private static final int USIA_MINIMAL = 18;

    // wadah sederhana untuk hasil bacaan form
    private static class FormData {
        String id, nama, kategori, pangkat;
        int tahunLahir;
    }

    public static void main(String[] args) {
        PeopleMenu menu = new PeopleMenu();
        menu.setSize(700, 540);
        menu.setLocationRelativeTo(null);
        menu.setContentPane(menu.mainPanel);
        menu.getContentPane().setBackground(java.awt.Color.WHITE);
        menu.setVisible(true);
        menu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // constructor
    public PeopleMenu() {
        setTitle("Data Anggota Polri");

        // buka koneksi database; kalau gagal, tampilkan pesan lalu keluar
        try {
            database = new Database();
            conn = database.getConnection();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(null,
                    ex.getMessage() + "\n\nPastikan MySQL sudah berjalan dan db_people sudah di-import.",
                    "Error Koneksi", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        peopleTable.setModel(setTable());
        tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        titleLabel.setFont(titleLabel.getFont().deriveFont(java.awt.Font.BOLD, 20f));

        // isi pilihan satuan (kategori)
        String[] satuan = {"-- Pilih Satuan --", "Bareskrim", "Korlantas", "Baintelkam",
                "Korsabhara", "Korbrimob", "Korpolairud", "Densus 88 AT", "Div Propam"};
        kategoriComboBox.setModel(new DefaultComboBoxModel<>(satuan));

        // isi pilihan pangkat (22 pangkat Polri, dari terendah sampai tertinggi)
        String[] pangkat = {"-- Pilih Pangkat --",
                "Bharada", "Bharatu", "Bharaka", "Abripda", "Abriptu", "Abrip",
                "Bripda", "Briptu", "Brigpol", "Bripka", "Aipda", "Aiptu",
                "Ipda", "Iptu", "AKP", "Kompol", "AKBP", "Kombes Pol",
                "Brigjen Pol", "Irjen Pol", "Komjen Pol", "Jenderal Pol"};
        pangkatComboBox.setModel(new DefaultComboBoxModel<>(pangkat));

        deleteButton.setVisible(false);

        // tombol Add / Update
        addUpdateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedId == null) {
                    insertData();
                } else {
                    updateData();
                }
            }
        });

        // tombol Delete (konfirmasi ada di dalam deleteData)
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteData();
            }
        });

        // tombol Cancel
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
            }
        });

        // klik baris tabel -> isi form (nilai diambil langsung dari tabel)
        peopleTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = peopleTable.getSelectedRow();
                if (row < 0) return;

                TableModel model = peopleTable.getModel();
                String curId = model.getValueAt(row, 1).toString();
                String curNama = model.getValueAt(row, 2).toString();
                String curTahun = model.getValueAt(row, 3).toString();
                String curKategori = model.getValueAt(row, 4).toString();
                String curPangkat = model.getValueAt(row, 5).toString();

                selectedId = curId;
                idField.setText(curId);
                namaField.setText(curNama);
                tahunLahirField.setText(curTahun);
                kategoriComboBox.setSelectedItem(curKategori);
                pangkatComboBox.setSelectedItem(curPangkat);

                addUpdateButton.setText("Update");
                deleteButton.setVisible(true);
            }
        });
    }

    // READ: membuat model tabel dari isi tabel person di database
    public final DefaultTableModel setTable() {
        String[] columns = {"No", "ID Anggota", "Nama", "Tahun Lahir", "Satuan", "Pangkat"};
        DefaultTableModel model = new DefaultTableModel(null, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // tabel hanya untuk dilihat
            }
        };

        String sql = "SELECT id, nama, tahun_lahir, kategori, pangkat FROM person ORDER BY id";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int no = 1;
            while (rs.next()) {
                Object[] row = {no++, rs.getString("id"), rs.getString("nama"),
                        rs.getInt("tahun_lahir"), rs.getString("kategori"), rs.getString("pangkat")};
                model.addRow(row);
            }
        } catch (SQLException ex) {
            showDbError(ex);
        }
        return model;
    }

    // CREATE
    public void insertData() {
        FormData d = readForm();
        if (d == null) return;

        try {
            if (idExists(d.id)) {
                showError("ID \"" + d.id + "\" sudah dipakai! Gunakan ID lain.");
                return;
            }

            String sql = "INSERT INTO person (id, nama, tahun_lahir, kategori, pangkat) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, d.id);
                ps.setString(2, d.nama);
                ps.setInt(3, d.tahunLahir);
                ps.setString(4, d.kategori);
                ps.setString(5, d.pangkat);
                ps.executeUpdate();
            }

            peopleTable.setModel(setTable());
            clearForm();
            JOptionPane.showMessageDialog(this, "Data berhasil ditambahkan");
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    // UPDATE
    public void updateData() {
        FormData d = readForm();
        if (d == null) return;

        try {
            // kalau ID diubah, pastikan ID baru belum dipakai baris lain
            if (!d.id.equalsIgnoreCase(selectedId) && idExists(d.id)) {
                showError("ID \"" + d.id + "\" sudah dipakai! Gunakan ID lain.");
                return;
            }

            String sql = "UPDATE person SET id = ?, nama = ?, tahun_lahir = ?, kategori = ?, pangkat = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, d.id);
                ps.setString(2, d.nama);
                ps.setInt(3, d.tahunLahir);
                ps.setString(4, d.kategori);
                ps.setString(5, d.pangkat);
                ps.setString(6, selectedId);
                ps.executeUpdate();
            }

            peopleTable.setModel(setTable());
            clearForm();
            JOptionPane.showMessageDialog(this, "Data berhasil diubah");
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    // DELETE (dengan confirmation prompt)
    public void deleteData() {
        if (selectedId == null) return;

        int pilihan = JOptionPane.showConfirmDialog(
                this,
                "Apakah kamu yakin ingin menghapus data ini?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (pilihan != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM person WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, selectedId);
            ps.executeUpdate();

            peopleTable.setModel(setTable());
            clearForm();
            JOptionPane.showMessageDialog(this, "Data berhasil dihapus");
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    // reset form ke kondisi awal
    public void clearForm() {
        idField.setText("");
        namaField.setText("");
        tahunLahirField.setText("");
        kategoriComboBox.setSelectedIndex(0);
        pangkatComboBox.setSelectedIndex(0);

        addUpdateButton.setText("Add");
        deleteButton.setVisible(false);
        selectedId = null;
        peopleTable.clearSelection();
    }

    // cek apakah ID sudah ada di database
    private boolean idExists(String id) throws SQLException {
        String sql = "SELECT 1 FROM person WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // baca + validasi input form, return null jika tidak valid (dialog error ditampilkan)
    private FormData readForm() {
        String id = idField.getText().trim();
        String nama = namaField.getText().trim();
        String tahunStr = tahunLahirField.getText().trim();

        // kolom input kosong
        if (id.isEmpty() || nama.isEmpty() || tahunStr.isEmpty()
                || kategoriComboBox.getSelectedIndex() == 0
                || pangkatComboBox.getSelectedIndex() == 0) {
            showError("Semua kolom harus diisi!\n(ID, Nama, Tahun Lahir, Satuan, dan Pangkat)");
            return null;
        }

        int tahun;
        try {
            tahun = Integer.parseInt(tahunStr);
        } catch (NumberFormatException ex) {
            showError("Tahun lahir harus berupa angka!");
            return null;
        }

        // validasi logis: usia minimal 18 tahun
        if (Year.now().getValue() - tahun < USIA_MINIMAL) {
            showError("Anggota minimal berusia " + USIA_MINIMAL + " tahun!");
            return null;
        }

        FormData d = new FormData();
        d.id = id;
        d.nama = nama;
        d.tahunLahir = tahun;
        d.kategori = (String) kategoriComboBox.getSelectedItem();
        d.pangkat = (String) pangkatComboBox.getSelectedItem();
        return d;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showDbError(SQLException ex) {
        showError("Terjadi kesalahan database:\n" + ex.getMessage());
    }
}
