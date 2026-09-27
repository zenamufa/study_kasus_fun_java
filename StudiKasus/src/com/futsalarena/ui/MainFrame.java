package com.futsalarena.ui;

import com.futsalarena.model.JadwalSewa;
import com.futsalarena.repository.JadwalRepository;
import com.futsalarena.service.TarifService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Antarmuka GUI Utama Aplikasi Reservasi "Futsal & Mini Soccer Arena Malang"
 */
public class MainFrame extends JFrame {

    private final JadwalRepository repository;
    private final TarifService tarifService;

    // Komponen Form Input
    private JTextField txtIdBooking;
    private JTextField txtNamaTim;
    private JTextField txtNoHp;
    private JComboBox<String> cmbLapangan;
    private JTextField txtTanggal;
    private LocalDate selectedDate;
    private JComboBox<String> cmbJamMulai;
    private JComboBox<String> cmbDurasi;
    private JLabel lblJamSelesai;

    // Komponen Rincian Tarif & DP
    private JLabel lblRincianTarif;
    private JLabel lblTotalTarif;
    private JLabel lblMinimalDP;
    private JTextField txtNominalDP;
    private JLabel lblStatusDP;
    private JButton btnQuickDP50;
    private JButton btnQuickDPLunas;

    // Tab & Komponen Tabel
    private JTabbedPane tabbedPane;
    private JTable tblJadwal;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField txtSearch;
    private JComboBox<String> cmbFilterStatus;

    // Timeline Visual Panel
    private JPanel pnlTimelineSlots;
    private JLabel lblTimelineDate;

    // Header Stats
    private JLabel lblStatTotal;
    private JLabel lblStatToday;
    private JLabel lblStatLunas;

    // Palette Warna Modern
    public static final Color COLOR_PRIMARY = new Color(27, 67, 50);       // Hijau Hutan Gelap
    public static final Color COLOR_PRIMARY_LIGHT = new Color(45, 106, 79); // Hijau Medium
    public static final Color COLOR_ACCENT = new Color(64, 145, 108);      // Hijau Emerald
    public static final Color COLOR_BG = new Color(243, 246, 244);         // Abu-abu hijau lembut
    public static final Color COLOR_CARD = Color.WHITE;
    public static final Color COLOR_TEXT_MAIN = new Color(33, 37, 41);
    public static final Color COLOR_TEXT_MUTED = new Color(108, 117, 125);
    public static final Color COLOR_RED_ALERT = new Color(220, 53, 69);
    public static final Color COLOR_GREEN_SUCCESS = new Color(25, 135, 84);
    public static final Color COLOR_NIGHT_SLOT = new Color(30, 41, 59);

    public MainFrame() {
        this.repository = new JadwalRepository();
        this.tarifService = new TarifService();
        this.selectedDate = LocalDate.now();

        initFrameSettings();
        buildUI();
        refreshAllData();
    }

    private void initFrameSettings() {
        setTitle("Futsal & Mini Soccer Arena Malang — Sistem Reservasi Anti-Bentrok");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1260, 800);
        setMinimumSize(new Dimension(1050, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG);
        setLayout(new BorderLayout());
    }

    private void buildUI() {
        add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel pnlCenter = new JPanel(new BorderLayout(15, 15));
        pnlCenter.setBackground(COLOR_BG);
        pnlCenter.setBorder(new EmptyBorder(12, 15, 15, 15));

        // Form Reservasi di Kiri
        JPanel pnlLeft = createFormBookingPanel();
        pnlLeft.setPreferredSize(new Dimension(440, 0));
        pnlCenter.add(pnlLeft, BorderLayout.WEST);

        // Tab Timeline dan Tabel di Kanan
        pnlCenter.add(createTabbedPanel(), BorderLayout.CENTER);

        add(pnlCenter, BorderLayout.CENTER);
    }

    // ==========================================
    // 1. HEADER PANEL
    // ==========================================
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_PRIMARY);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Judul & Subtitle
        JPanel pnlTitles = new JPanel(new GridLayout(2, 1, 2, 2));
        pnlTitles.setOpaque(false);

        JLabel lblTitle = new JLabel("⚽ FUTSAL & MINI SOCCER ARENA MALANG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("Sistem Manajemen Jadwal & Reservasi Lapangan Otomatis Anti-Bentrok");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(216, 243, 220));

        pnlTitles.add(lblTitle);
        pnlTitles.add(lblSubtitle);
        header.add(pnlTitles, BorderLayout.WEST);

        // Stats Badges di Kanan
        JPanel pnlStats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlStats.setOpaque(false);

        lblStatTotal = createStatBadge("Total Reservasi", "0", new Color(45, 106, 79));
        lblStatToday = createStatBadge("Jadwal Hari Ini", "0", new Color(64, 145, 108));
        lblStatLunas = createStatBadge("Sudah Lunas", "0", new Color(82, 183, 136));

        pnlStats.add(lblStatTotal);
        pnlStats.add(lblStatToday);
        pnlStats.add(lblStatLunas);
        header.add(pnlStats, BorderLayout.EAST);

        return header;
    }

    private JLabel createStatBadge(String title, String value, Color bg) {
        JLabel lbl = new JLabel(String.format("<html><center><small>%s</small><br><b>%s</b></center></html>", title, value));
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(Color.WHITE);
        lbl.setBackground(bg);
        lbl.setOpaque(true);
        lbl.setBorder(new EmptyBorder(4, 12, 4, 12));
        return lbl;
    }

    // ==========================================
    // 2. FORM BOOKING PANEL (KIRI)
    // ==========================================
    private JPanel createFormBookingPanel() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 225, 230), 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        // Header Form
        JPanel pnlFormHeader = new JPanel(new BorderLayout());
        pnlFormHeader.setOpaque(false);
        JLabel lblHeader = new JLabel("📝 Formulir Reservasi Baru");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblHeader.setForeground(COLOR_PRIMARY);
        pnlFormHeader.add(lblHeader, BorderLayout.WEST);

        JButton btnReset = new JButton("🔄 Reset");
        btnReset.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnReset.setFocusPainted(false);
        btnReset.addActionListener(e -> resetForm());
        pnlFormHeader.add(btnReset, BorderLayout.EAST);

        card.add(pnlFormHeader, BorderLayout.NORTH);

        // Body Form (Grid / Scrollable)
        JPanel pnlBody = new JPanel();
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
        pnlBody.setOpaque(false);
        pnlBody.setBorder(new EmptyBorder(10, 0, 10, 0));

        // 1. ID Booking
        txtIdBooking = new JTextField();
        txtIdBooking.setEditable(false);
        txtIdBooking.setFont(new Font("Consolas", Font.BOLD, 13));
        txtIdBooking.setBackground(new Color(245, 247, 248));
        pnlBody.add(createFormRow("ID Reservasi (Otomatis):", txtIdBooking));

        // 2. Nama Tim / Fakultas / Angkatan
        txtNamaTim = new JTextField();
        txtNamaTim.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pnlBody.add(createFormRow("Nama Tim / Fakultas / Angkatan:*", txtNamaTim));

        // 3. No HP / WhatsApp
        txtNoHp = new JTextField();
        txtNoHp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pnlBody.add(createFormRow("Nomor HP / WhatsApp Pemesan:*", txtNoHp));

        // 4. Jenis Lapangan
        cmbLapangan = new JComboBox<>(new String[]{
                "Futsal Vinyl (Indoor)",
                "Mini Soccer Rumput Sintetis (Outdoor)"
        });
        cmbLapangan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbLapangan.addActionListener(e -> kalkulasiTarifOtomatis());
        pnlBody.add(createFormRow("Pilihan Lapangan Olahraga:", cmbLapangan));

        // 5. Pemilihan Tanggal Kalender
        JPanel pnlTanggalRow = new JPanel(new BorderLayout(5, 0));
        pnlTanggalRow.setOpaque(false);

        txtTanggal = new JTextField();
        txtTanggal.setEditable(false);
        txtTanggal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtTanggal.setBackground(Color.WHITE);

        JButton btnCalendar = new JButton("📅 Kalender");
        btnCalendar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCalendar.setBackground(COLOR_PRIMARY_LIGHT);
        btnCalendar.setForeground(Color.WHITE);
        btnCalendar.setFocusPainted(false);
        btnCalendar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCalendar.addActionListener(e -> {
            LocalDate picked = DatePickerDialog.showDialog(this, selectedDate);
            if (picked != null) {
                setSelectedDate(picked);
            }
        });

        pnlTanggalRow.add(txtTanggal, BorderLayout.CENTER);
        pnlTanggalRow.add(btnCalendar, BorderLayout.EAST);
        pnlBody.add(createFormRow("Tanggal Main (Kalender):*", pnlTanggalRow));

        // Pintasan Hari Ini / Besok
        JPanel pnlQuickDate = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        pnlQuickDate.setOpaque(false);
        JButton btnToday = new JButton("Hari Ini");
        btnToday.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnToday.addActionListener(e -> setSelectedDate(LocalDate.now()));

        JButton btnTomorrow = new JButton("Besok");
        btnTomorrow.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnTomorrow.addActionListener(e -> setSelectedDate(LocalDate.now().plusDays(1)));

        pnlQuickDate.add(btnToday);
        pnlQuickDate.add(btnTomorrow);
        pnlBody.add(pnlQuickDate);

        // 6. Slot Jam Mulai (08.00 s.d. 23.00) & Durasi
        JPanel pnlTimeRow = new JPanel(new GridLayout(1, 2, 8, 0));
        pnlTimeRow.setOpaque(false);

        DefaultComboBoxModel<String> jamModel = new DefaultComboBoxModel<>();
        for (int h = 8; h <= 23; h++) {
            String ket = (h >= 18) ? " (🌙 Malam+Lampu)" : " (☀️ Siang)";
            jamModel.addElement(String.format("%02d:00%s", h, ket));
        }
        cmbJamMulai = new JComboBox<>(jamModel);
        cmbJamMulai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbJamMulai.addActionListener(e -> {
            updateJamSelesaiDanTarif();
        });

        cmbDurasi = new JComboBox<>(new String[]{"1 Jam", "2 Jam", "3 Jam", "4 Jam"});
        cmbDurasi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbDurasi.addActionListener(e -> {
            updateJamSelesaiDanTarif();
        });

        JPanel pnlJamWrap = new JPanel(new BorderLayout(0, 3));
        pnlJamWrap.setOpaque(false);
        JLabel lblJamMulaiTitle = new JLabel("Jam Mulai (08:00 - 23:00):");
        lblJamMulaiTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        pnlJamWrap.add(lblJamMulaiTitle, BorderLayout.NORTH);
        pnlJamWrap.add(cmbJamMulai, BorderLayout.CENTER);

        JPanel pnlDurasiWrap = new JPanel(new BorderLayout(0, 3));
        pnlDurasiWrap.setOpaque(false);
        JLabel lblDurasiTitle = new JLabel("Durasi Sewa:");
        lblDurasiTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        pnlDurasiWrap.add(lblDurasiTitle, BorderLayout.NORTH);
        pnlDurasiWrap.add(cmbDurasi, BorderLayout.CENTER);

        pnlTimeRow.add(pnlJamWrap);
        pnlTimeRow.add(pnlDurasiWrap);
        pnlBody.add(pnlTimeRow);

        lblJamSelesai = new JLabel("Jam Selesai: 09:00 WIB", SwingConstants.RIGHT);
        lblJamSelesai.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblJamSelesai.setForeground(COLOR_TEXT_MUTED);
        pnlBody.add(lblJamSelesai);

        pnlBody.add(Box.createVerticalStrut(8));

        // 7. CARD TARIF OTOMATIS (SIANG VS MALAM + LAMPU)
        JPanel pnlTarifBox = new JPanel();
        pnlTarifBox.setLayout(new BoxLayout(pnlTarifBox, BoxLayout.Y_AXIS));
        pnlTarifBox.setBackground(new Color(245, 250, 246));
        pnlTarifBox.setBorder(new CompoundBorder(
                new LineBorder(new Color(180, 220, 195), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        lblRincianTarif = new JLabel("Rincian: -");
        lblRincianTarif.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblRincianTarif.setForeground(new Color(50, 80, 60));

        lblTotalTarif = new JLabel("Total Tarif: Rp 0");
        lblTotalTarif.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalTarif.setForeground(COLOR_PRIMARY);

        lblMinimalDP = new JLabel("Minimal DP 50%: Rp 0");
        lblMinimalDP.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblMinimalDP.setForeground(new Color(180, 90, 20));

        pnlTarifBox.add(lblRincianTarif);
        pnlTarifBox.add(Box.createVerticalStrut(4));
        pnlTarifBox.add(lblTotalTarif);
        pnlTarifBox.add(Box.createVerticalStrut(2));
        pnlTarifBox.add(lblMinimalDP);

        pnlBody.add(pnlTarifBox);
        pnlBody.add(Box.createVerticalStrut(8));

        // 8. INPUT UANG MUKA (DP) MINIMAL 50%
        JPanel pnlDpHeader = new JPanel(new BorderLayout());
        pnlDpHeader.setOpaque(false);
        JLabel lblDpTitle = new JLabel("Nominal Uang Muka (DP minimal 50%):*");
        lblDpTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDpTitle.setForeground(COLOR_TEXT_MAIN);
        pnlDpHeader.add(lblDpTitle, BorderLayout.WEST);

        JPanel pnlDpButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pnlDpButtons.setOpaque(false);
        btnQuickDP50 = new JButton("50% DP");
        btnQuickDP50.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnQuickDP50.setMargin(new Insets(2, 6, 2, 6));

        btnQuickDPLunas = new JButton("100% Lunas");
        btnQuickDPLunas.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnQuickDPLunas.setMargin(new Insets(2, 6, 2, 6));

        pnlDpButtons.add(btnQuickDP50);
        pnlDpButtons.add(btnQuickDPLunas);
        pnlDpHeader.add(pnlDpButtons, BorderLayout.EAST);
        pnlBody.add(pnlDpHeader);

        txtNominalDP = new JTextField();
        txtNominalDP.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtNominalDP.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { validasiStatusDPRealtime(); }
            public void removeUpdate(DocumentEvent e) { validasiStatusDPRealtime(); }
            public void changedUpdate(DocumentEvent e) { validasiStatusDPRealtime(); }
        });
        pnlBody.add(txtNominalDP);

        lblStatusDP = new JLabel("Status DP: Menunggu input...");
        lblStatusDP.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStatusDP.setForeground(COLOR_TEXT_MUTED);
        pnlBody.add(lblStatusDP);

        // Quick buttons action
        btnQuickDP50.addActionListener(e -> setQuickDP(0.50));
        btnQuickDPLunas.addActionListener(e -> setQuickDP(1.00));

        JScrollPane scrollForm = new JScrollPane(pnlBody);
        scrollForm.setBorder(null);
        scrollForm.setOpaque(false);
        scrollForm.getViewport().setOpaque(false);
        scrollForm.getVerticalScrollBar().setUnitIncrement(14);
        card.add(scrollForm, BorderLayout.CENTER);

        // Footer Action (Tombol Simpan)
        JPanel pnlFooter = new JPanel(new BorderLayout(0, 5));
        pnlFooter.setOpaque(false);
        pnlFooter.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton btnSimpan = new JButton("💾 Simpan Reservasi & Validasi Bentrok");
        btnSimpan.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSimpan.setBackground(COLOR_GREEN_SUCCESS);
        btnSimpan.setForeground(Color.WHITE);
        btnSimpan.setFocusPainted(false);
        btnSimpan.setPreferredSize(new Dimension(0, 42));
        btnSimpan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSimpan.addActionListener(e -> simpanReservasi());

        pnlFooter.add(btnSimpan, BorderLayout.CENTER);
        card.add(pnlFooter, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createFormRow(String labelText, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(3, 0, 5, 0));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(COLOR_TEXT_MAIN);

        panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    // ==========================================
    // 3. TABBED PANEL (TIMELINE & TABEL)
    // ==========================================
    private JTabbedPane createTabbedPanel() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabbedPane.addTab("🕒 Timeline Ketersediaan Slot Jam", createTimelinePanel());
        tabbedPane.addTab("📋 Data Reservasi (CSV)", createTablePanel());

        return tabbedPane;
    }

    // --- Tab 1: Timeline Slots (08.00 - 23.00) ---
    private JPanel createTimelinePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(COLOR_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 225, 230), 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));

        // Header Timeline
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);

        JPanel pnlTitleDate = new JPanel(new GridLayout(2, 1));
        pnlTitleDate.setOpaque(false);

        JLabel lblTitle = new JLabel("Matriks Ketersediaan Slot Jam Lapangan (Anti-Bentrok)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(COLOR_PRIMARY);

        lblTimelineDate = new JLabel("Tanggal: - | Lapangan: -");
        lblTimelineDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTimelineDate.setForeground(COLOR_TEXT_MUTED);

        pnlTitleDate.add(lblTitle);
        pnlTitleDate.add(lblTimelineDate);
        pnlHeader.add(pnlTitleDate, BorderLayout.WEST);

        // Keterangan Legend
        JPanel pnlLegend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlLegend.setOpaque(false);
        pnlLegend.add(createLegendItem("Tersedia (Bebas)", new Color(220, 245, 225), COLOR_GREEN_SUCCESS));
        pnlLegend.add(createLegendItem("Dipesan (Bentrok)", new Color(255, 225, 225), COLOR_RED_ALERT));
        pnlLegend.add(createLegendItem("Malam + Lampu", new Color(230, 235, 245), COLOR_NIGHT_SLOT));
        pnlHeader.add(pnlLegend, BorderLayout.EAST);

        panel.add(pnlHeader, BorderLayout.NORTH);

        // Slots Grid (08:00 - 23:00 -> 16 slot jam)
        pnlTimelineSlots = new JPanel(new GridLayout(4, 4, 10, 10));
        pnlTimelineSlots.setOpaque(false);
        pnlTimelineSlots.setBorder(new EmptyBorder(10, 0, 10, 0));

        JScrollPane scrollSlots = new JScrollPane(pnlTimelineSlots);
        scrollSlots.setBorder(null);
        scrollSlots.setOpaque(false);
        scrollSlots.getViewport().setOpaque(false);

        panel.add(scrollSlots, BorderLayout.CENTER);

        // Hint Footer
        JLabel lblHint = new JLabel("💡 Tips: Klik pada slot jam berwarna HIJAU untuk langsung mengisi jam mulai pada form sewa!", SwingConstants.CENTER);
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHint.setForeground(new Color(60, 100, 80));
        lblHint.setBorder(new EmptyBorder(6, 0, 0, 0));
        panel.add(lblHint, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLegendItem(String label, Color bg, Color border) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JLabel box = new JLabel("   ");
        box.setOpaque(true);
        box.setBackground(bg);
        box.setBorder(new LineBorder(border, 1));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        p.add(box);
        p.add(lbl);
        return p;
    }

    // --- Tab 2: Tabel Reservasi ---
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(COLOR_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 225, 230), 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        // Filter / Search Toolbar
        JPanel pnlToolbar = new JPanel(new BorderLayout(8, 0));
        pnlToolbar.setOpaque(false);

        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlSearch.setOpaque(false);
        pnlSearch.add(new JLabel("🔍 Cari:"));
        txtSearch = new JTextField(15);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });
        pnlSearch.add(txtSearch);

        pnlSearch.add(new JLabel("  Status:"));
        cmbFilterStatus = new JComboBox<>(new String[]{"Semua Status", "LUNAS", "DP (Belum Lunas)"});
        cmbFilterStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbFilterStatus.addActionListener(e -> filterTable());
        pnlSearch.add(cmbFilterStatus);

        pnlToolbar.add(pnlSearch, BorderLayout.WEST);

        // Action Buttons Toolbar
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlActions.setOpaque(false);

        JButton btnStruk = new JButton("📄 Cetak Struk");
        btnStruk.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnStruk.setBackground(COLOR_PRIMARY_LIGHT);
        btnStruk.setForeground(Color.WHITE);
        btnStruk.setFocusPainted(false);
        btnStruk.addActionListener(e -> cetakStrukTerpilih());

        JButton btnLunasi = new JButton("💰 Pelunasan Sisa");
        btnLunasi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLunasi.addActionListener(e -> lunasiReservasiTerpilih());

        JButton btnHapus = new JButton("🗑️ Batalkan");
        btnHapus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnHapus.setForeground(COLOR_RED_ALERT);
        btnHapus.addActionListener(e -> batalkanReservasiTerpilih());

        JButton btnOpenCsv = new JButton("📂 Buka CSV");
        btnOpenCsv.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnOpenCsv.addActionListener(e -> bukaBerkasCsv());

        pnlActions.add(btnStruk);
        pnlActions.add(btnLunasi);
        pnlActions.add(btnHapus);
        pnlActions.add(btnOpenCsv);

        pnlToolbar.add(pnlActions, BorderLayout.EAST);
        panel.add(pnlToolbar, BorderLayout.NORTH);

        // Table
        String[] columnNames = {
                "ID Booking", "Nama Tim", "No HP", "Tanggal",
                "Jam Main", "Durasi", "Lapangan", "Total Tarif",
                "Uang Muka (DP)", "Status DP"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblJadwal = new JTable(tableModel);
        tblJadwal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblJadwal.setRowHeight(28);
        tblJadwal.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblJadwal.getTableHeader().setBackground(new Color(240, 244, 242));
        tblJadwal.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom Cell Renderer untuk Status DP
        tblJadwal.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                if (!isSelected) {
                    String str = (value != null) ? value.toString() : "";
                    if (str.toUpperCase().contains("LUNAS")) {
                        lbl.setForeground(COLOR_GREEN_SUCCESS);
                    } else {
                        lbl.setForeground(new Color(210, 105, 30));
                    }
                }
                return lbl;
            }
        });

        rowSorter = new TableRowSorter<>(tableModel);
        tblJadwal.setRowSorter(rowSorter);

        JScrollPane scrollTable = new JScrollPane(tblJadwal);
        scrollTable.setBorder(BorderFactory.createLineBorder(new Color(230, 235, 238), 1));
        panel.add(scrollTable, BorderLayout.CENTER);

        return panel;
    }

    // ==========================================
    // LOGIKA PERHITUNGAN, TANGGAL & FORM
    // ==========================================

    private void setSelectedDate(LocalDate date) {
        this.selectedDate = date;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd (EEEE)", Locale.forLanguageTag("id-ID"));
        txtTanggal.setText(date.format(dtf));
        txtIdBooking.setText(repository.generateNextIdBooking(date));
        kalkulasiTarifOtomatis();
        updateTimelineGrid();
    }

    private int getSelectedJamMulai() {
        int idx = cmbJamMulai.getSelectedIndex();
        return 8 + idx; // slot mulai dari 08:00
    }

    private int getSelectedDurasi() {
        return cmbDurasi.getSelectedIndex() + 1; // 1 s.d. 4 jam
    }

    private void updateJamSelesaiDanTarif() {
        int start = getSelectedJamMulai();
        int durasi = getSelectedDurasi();
        int selesai = start + durasi;

        if (selesai > 24) {
            lblJamSelesai.setText(String.format("⚠️ Selesai: %02d:00 (Melebihi batas jam 24:00!)", selesai));
            lblJamSelesai.setForeground(COLOR_RED_ALERT);
        } else {
            lblJamSelesai.setText(String.format("Jam Selesai: %02d:00 WIB", selesai));
            lblJamSelesai.setForeground(COLOR_TEXT_MUTED);
        }

        kalkulasiTarifOtomatis();
    }

    private void kalkulasiTarifOtomatis() {
        if (cmbLapangan == null || cmbJamMulai == null || cmbDurasi == null) {
            return;
        }
        String lapangan = (String) cmbLapangan.getSelectedItem();
        int start = getSelectedJamMulai();
        int durasi = getSelectedDurasi();

        TarifService.RincianTarif rincian = tarifService.hitung(lapangan, start, durasi);

        lblRincianTarif.setText("<html>Rincian: " + rincian.getRincianTeks() + "</html>");
        lblTotalTarif.setText("Total Tarif: " + rincian.formatRupiah(rincian.getTotalTarif()));
        lblMinimalDP.setText("Minimal DP 50%: " + rincian.formatRupiah(rincian.getMinimalDP()));

        // Jika input DP kosong atau baru, defaultkan ke 50%
        if (txtNominalDP.getText().trim().isEmpty()) {
            setQuickDP(0.50);
        } else {
            validasiStatusDPRealtime();
        }
    }

    private void setQuickDP(double persentase) {
        String lapangan = (String) cmbLapangan.getSelectedItem();
        int start = getSelectedJamMulai();
        int durasi = getSelectedDurasi();
        TarifService.RincianTarif rincian = tarifService.hitung(lapangan, start, durasi);

        long nominal = Math.round(rincian.getTotalTarif() * persentase);
        txtNominalDP.setText(String.valueOf(nominal));
    }

    private void validasiStatusDPRealtime() {
        String txt = txtNominalDP.getText().trim().replaceAll("[^0-9]", "");
        if (txt.isEmpty()) {
            lblStatusDP.setText("Status DP: Menunggu input DP minimal 50%...");
            lblStatusDP.setForeground(COLOR_TEXT_MUTED);
            return;
        }

        try {
            double nominalDP = Double.parseDouble(txt);
            String lapangan = (String) cmbLapangan.getSelectedItem();
            int start = getSelectedJamMulai();
            int durasi = getSelectedDurasi();
            TarifService.RincianTarif rincian = tarifService.hitung(lapangan, start, durasi);
            double totalTarif = rincian.getTotalTarif();

            if (nominalDP >= totalTarif) {
                lblStatusDP.setText("Status DP: LUNAS 100% (Pembayaran Penuh)");
                lblStatusDP.setForeground(COLOR_GREEN_SUCCESS);
            } else if (tarifService.isValidDP(totalTarif, nominalDP)) {
                double persen = (nominalDP / totalTarif) * 100.0;
                lblStatusDP.setText(String.format(Locale.US, "Status DP: DP Diterima (%.1f%%) — Sisa: %s",
                        persen, rincian.formatRupiah(totalTarif - nominalDP)));
                lblStatusDP.setForeground(new Color(40, 116, 166));
            } else {
                double minDP = rincian.getMinimalDP();
                lblStatusDP.setText(String.format("Status DP: ⚠️ KURANG DARI 50%%! (Minimal: %s)", rincian.formatRupiah(minDP)));
                lblStatusDP.setForeground(COLOR_RED_ALERT);
            }
        } catch (NumberFormatException ignored) {
            lblStatusDP.setText("Status DP: Format angka tidak valid");
            lblStatusDP.setForeground(COLOR_RED_ALERT);
        }
    }

    // ==========================================
    // LOGIKA PENYIMPANAN & VALIDASI BENTROK
    // ==========================================

    private void simpanReservasi() {
        String namaTim = txtNamaTim.getText().trim();
        String noHp = txtNoHp.getText().trim();
        String lapangan = (String) cmbLapangan.getSelectedItem();
        int jamMulai = getSelectedJamMulai();
        int durasi = getSelectedDurasi();
        int jamSelesai = jamMulai + durasi;

        // 1. Validasi Input Dasar
        if (namaTim.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama Tim / Fakultas wajib diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            txtNamaTim.requestFocus();
            return;
        }

        if (noHp.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nomor WhatsApp / HP wajib diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            txtNoHp.requestFocus();
            return;
        }

        if (jamSelesai > 24) {
            JOptionPane.showMessageDialog(this, "Jam sewa melebihi batas operasional 24:00 WIB! Kurangi durasi sewa.",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Validasi DP
        String dpStr = txtNominalDP.getText().trim().replaceAll("[^0-9]", "");
        if (dpStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Uang Muka (DP) wajib diisi minimal 50% dari total tarif!",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            txtNominalDP.requestFocus();
            return;
        }

        double nominalDP = Double.parseDouble(dpStr);
        TarifService.RincianTarif rincian = tarifService.hitung(lapangan, jamMulai, durasi);
        double totalTarif = rincian.getTotalTarif();

        if (!tarifService.isValidDP(totalTarif, nominalDP)) {
            NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));
            JOptionPane.showMessageDialog(this,
                    String.format("Uang Muka (DP) TIDAK MEMENUHI SYARAT!\n\n" +
                                  "Total Tarif: %s\n" +
                                  "Minimal DP (50%%): %s\n" +
                                  "Nominal yang diinput: %s\n\n" +
                                  "Silakan sesuaikan nominal DP minimal 50%%.",
                            nf.format(totalTarif), nf.format(rincian.getMinimalDP()), nf.format(nominalDP)),
                    "Validasi DP Gagal", JOptionPane.ERROR_MESSAGE);
            txtNominalDP.requestFocus();
            return;
        }

        String statusDP = tarifService.tentukanStatusDP(totalTarif, nominalDP);
        String idBooking = txtIdBooking.getText();

        JadwalSewa jadwalBaru = new JadwalSewa(
                idBooking, namaTim, noHp, selectedDate,
                jamMulai, durasi, statusDP, totalTarif,
                nominalDP, lapangan
        );

        // 3. Simpan dengan Validasi Anti-Bentrok
        try {
            repository.tambahJadwal(jadwalBaru);
            refreshAllData();

            int pilihan = JOptionPane.showConfirmDialog(this,
                    String.format("Reservasi Tim [%s] BERHASIL disimpan ke berkas jadwal_futsal.csv!\n" +
                                  "ID Booking: %s\n" +
                                  "Jadwal: %s (%02d:00 - %02d:00)\n\n" +
                                  "Apakah Anda ingin melihat dan mencetak Bukti Struk Reservasi sekarang?",
                            namaTim, idBooking, selectedDate, jamMulai, jamSelesai),
                    "Reservasi Berhasil", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

            if (pilihan == JOptionPane.YES_OPTION) {
                StrukDialog dialog = new StrukDialog(this, jadwalBaru);
                dialog.setVisible(true);
            }

            resetForm();
        } catch (IllegalArgumentException exBentrok) {
            // Error bentrok jadwal
            JOptionPane.showMessageDialog(this, exBentrok.getMessage(),
                    "BENTROK JADWAL SEWA TERDETEKSI", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan jadwal: " + ex.getMessage(),
                    "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetForm() {
        txtNamaTim.setText("");
        txtNoHp.setText("");
        cmbLapangan.setSelectedIndex(0);
        cmbJamMulai.setSelectedIndex(0);
        cmbDurasi.setSelectedIndex(0);
        setSelectedDate(LocalDate.now());
        txtNamaTim.requestFocus();
    }

    // ==========================================
    // REFRESH DATA, STATS & TIMELINE MATRIX
    // ==========================================

    private void refreshAllData() {
        updateTableData();
        updateTimelineGrid();
        updateHeaderStats();
    }

    private void updateHeaderStats() {
        List<JadwalSewa> all = repository.getAllJadwal();
        lblStatTotal.setText(String.format("<html><center><small>Total Reservasi</small><br><b>%d</b></center></html>", all.size()));

        long todayCount = all.stream().filter(j -> j.getTanggalMain().equals(LocalDate.now())).count();
        lblStatToday.setText(String.format("<html><center><small>Jadwal Hari Ini</small><br><b>%d</b></center></html>", todayCount));

        long lunasCount = all.stream().filter(j -> j.getStatusDP().toUpperCase().contains("LUNAS")).count();
        lblStatLunas.setText(String.format("<html><center><small>Sudah Lunas</small><br><b>%d</b></center></html>", lunasCount));
    }

    private void updateTableData() {
        tableModel.setRowCount(0);
        List<JadwalSewa> list = repository.getAllJadwal();
        for (JadwalSewa j : list) {
            tableModel.addRow(new Object[]{
                    j.getIdBooking(),
                    j.getNamaTim(),
                    j.getNoHp(),
                    j.getTanggalMain().toString(),
                    j.getFormatJam(),
                    j.getDurasiJam() + " Jam",
                    j.getJenisLapangan(),
                    j.getTotalTarifRupiah(),
                    j.getNominalDPRupiah(),
                    j.getStatusDP()
            });
        }
    }

    private void updateTimelineGrid() {
        if (pnlTimelineSlots == null) return;

        String lapangan = (cmbLapangan != null) ? (String) cmbLapangan.getSelectedItem() : "Futsal Vinyl (Indoor)";
        lblTimelineDate.setText(String.format("Tanggal: %s | Lapangan: %s",
                selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))),
                lapangan));

        pnlTimelineSlots.removeAll();

        List<JadwalSewa> bookingsToday = repository.getJadwalByTanggal(selectedDate, lapangan);

        for (int h = 8; h <= 23; h++) {
            final int slotHour = h;
            boolean isMalam = (slotHour >= 18);

            // Cek apakah slot jam ini terisi oleh booking
            JadwalSewa bookedBy = null;
            for (JadwalSewa b : bookingsToday) {
                if (slotHour >= b.getJamMulai() && slotHour < b.getJamSelesai()) {
                    bookedBy = b;
                    break;
                }
            }

            JPanel slotCard = new JPanel(new BorderLayout(4, 4));
            slotCard.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(bookedBy != null ? COLOR_RED_ALERT : (isMalam ? COLOR_NIGHT_SLOT : COLOR_ACCENT), 1, true),
                    new EmptyBorder(8, 8, 8, 8)
            ));

            String iconTime = isMalam ? "🌙 [Lampu Nyala]" : "☀️ [Siang]";
            JLabel lblHour = new JLabel(String.format("%02d:00 - %02d:00", slotHour, slotHour + 1));
            lblHour.setFont(new Font("Segoe UI", Font.BOLD, 13));

            JLabel lblBadge = new JLabel(iconTime);
            lblBadge.setFont(new Font("Segoe UI", Font.PLAIN, 10));

            JPanel pnlTop = new JPanel(new BorderLayout());
            pnlTop.setOpaque(false);
            pnlTop.add(lblHour, BorderLayout.WEST);
            pnlTop.add(lblBadge, BorderLayout.EAST);
            slotCard.add(pnlTop, BorderLayout.NORTH);

            if (bookedBy != null) {
                slotCard.setBackground(new Color(254, 242, 242));
                lblHour.setForeground(COLOR_RED_ALERT);
                lblBadge.setForeground(COLOR_RED_ALERT);

                JLabel lblStatus = new JLabel("<html><b>DIPESAN (BENTROK):</b><br>" +
                        bookedBy.getNamaTim() + "<br>" +
                        "<small>" + bookedBy.getIdBooking() + "</small></html>");
                lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                lblStatus.setForeground(new Color(150, 30, 30));
                slotCard.add(lblStatus, BorderLayout.CENTER);
            } else {
                slotCard.setBackground(isMalam ? new Color(241, 245, 249) : new Color(240, 253, 244));
                lblHour.setForeground(isMalam ? COLOR_NIGHT_SLOT : COLOR_PRIMARY);
                lblBadge.setForeground(isMalam ? new Color(100, 116, 139) : COLOR_ACCENT);

                JLabel lblStatus = new JLabel("<html><font color='#198754'><b>✓ TERSEDIA</b></font><br><small>Klik untuk pilih slot ini</small></html>");
                lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                slotCard.add(lblStatus, BorderLayout.CENTER);

                slotCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
                slotCard.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        cmbJamMulai.setSelectedIndex(slotHour - 8);
                        tabbedPane.setSelectedIndex(0);
                    }
                });
            }

            pnlTimelineSlots.add(slotCard);
        }

        pnlTimelineSlots.revalidate();
        pnlTimelineSlots.repaint();
    }

    private void filterTable() {
        String text = txtSearch.getText().trim();
        String status = (String) cmbFilterStatus.getSelectedItem();

        RowFilter<DefaultTableModel, Object> rf = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ?> entry) {
                // Filter text
                boolean matchesText = true;
                if (!text.isEmpty()) {
                    boolean found = false;
                    for (int i = 0; i < entry.getValueCount(); i++) {
                        if (entry.getStringValue(i).toLowerCase().contains(text.toLowerCase())) {
                            found = true;
                            break;
                        }
                    }
                    matchesText = found;
                }

                // Filter status
                boolean matchesStatus = true;
                if (status != null && !status.equalsIgnoreCase("Semua Status")) {
                    String rowStatus = entry.getStringValue(9);
                    if (status.equalsIgnoreCase("LUNAS")) {
                        matchesStatus = rowStatus.toUpperCase().contains("LUNAS");
                    } else if (status.contains("DP")) {
                        matchesStatus = !rowStatus.toUpperCase().contains("LUNAS 100%");
                    }
                }

                return matchesText && matchesStatus;
            }
        };

        rowSorter.setRowFilter(rf);
    }

    // ==========================================
    // ACTION HANDLERS
    // ==========================================

    private JadwalSewa getSelectedJadwalFromTable() {
        int selectedRow = tblJadwal.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Silakan pilih salah satu jadwal pada tabel terlebih dahulu!",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int modelRow = tblJadwal.convertRowIndexToModel(selectedRow);
        String idBooking = (String) tableModel.getValueAt(modelRow, 0);

        for (JadwalSewa j : repository.getAllJadwal()) {
            if (j.getIdBooking().equalsIgnoreCase(idBooking)) {
                return j;
            }
        }
        return null;
    }

    private void cetakStrukTerpilih() {
        JadwalSewa j = getSelectedJadwalFromTable();
        if (j != null) {
            StrukDialog dialog = new StrukDialog(this, j);
            dialog.setVisible(true);
        }
    }

    private void lunasiReservasiTerpilih() {
        JadwalSewa j = getSelectedJadwalFromTable();
        if (j == null) return;

        double sisa = j.getSisaPembayaran();
        if (sisa <= 0) {
            JOptionPane.showMessageDialog(this, "Jadwal booking ini sudah LUNAS 100%!", "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String input = JOptionPane.showInputDialog(this,
                String.format("Pelunasan Sisa Pembayaran\n" +
                              "• Tim: %s (ID: %s)\n" +
                              "• Total Tarif: %s\n" +
                              "• DP Terbayar: %s\n" +
                              "• Sisa Tagihan: %s\n\n" +
                              "Masukkan nominal pembayaran sisa (Rp):",
                        j.getNamaTim(), j.getIdBooking(), j.getTotalTarifRupiah(), j.getNominalDPRupiah(), j.getSisaPembayaranRupiah()),
                Math.round(sisa));

        if (input != null && !input.trim().isEmpty()) {
            try {
                double bayar = Double.parseDouble(input.trim().replaceAll("[^0-9]", ""));
                if (bayar <= 0) {
                    JOptionPane.showMessageDialog(this, "Nominal pembayaran harus lebih besar dari 0!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                repository.updatePelunasan(j.getIdBooking(), bayar);
                refreshAllData();
                JOptionPane.showMessageDialog(this, "Pelunasan berhasil disimpan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Input tidak valid: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void batalkanReservasiTerpilih() {
        JadwalSewa j = getSelectedJadwalFromTable();
        if (j == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Apakah Anda yakin ingin membatalkan/menghapus reservasi berikut?\n" +
                              "• ID: %s\n" +
                              "• Tim: %s\n" +
                              "• Tanggal: %s Jam %s\n\n" +
                              "Tindakan ini akan mengosongkan kembali slot jam di berkas CSV!",
                        j.getIdBooking(), j.getNamaTim(), j.getTanggalMain(), j.getFormatJam()),
                "Konfirmasi Pembatalan Booking", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                repository.hapusJadwal(j.getIdBooking());
                refreshAllData();
                JOptionPane.showMessageDialog(this, "Jadwal booking berhasil dibatalkan dan dihapus dari CSV!",
                        "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Gagal membatalkan jadwal: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void bukaBerkasCsv() {
        try {
            File f = repository.getFileCsv();
            if (f.exists()) {
                Desktop.getDesktop().open(f);
            } else {
                JOptionPane.showMessageDialog(this, "Berkas CSV belum terbentuk.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Tidak dapat membuka file CSV secara otomatis: " + ex.getMessage() +
                    "\nLokasi file: " + repository.getFileCsv().getAbsolutePath(), "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
