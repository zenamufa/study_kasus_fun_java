package com.futsalarena.ui;

import com.futsalarena.model.JadwalSewa;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Dialog Tanda Terima / Struk Reservasi resmi bergaya slip kasir modern.
 */
public class StrukDialog extends JDialog {

    private final JadwalSewa jadwal;
    private final JTextArea txtStruk;

    public StrukDialog(Frame parent, JadwalSewa jadwal) {
        super(parent, "Tanda Terima Reservasi - " + jadwal.getIdBooking(), true);
        this.jadwal = jadwal;

        setSize(460, 620);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 247, 248));

        // Header Title
        JPanel pnlHeader = new JPanel();
        pnlHeader.setBackground(new Color(27, 67, 50));
        pnlHeader.setBorder(new EmptyBorder(12, 16, 12, 16));
        JLabel lblTitle = new JLabel("📄 BUKTI RESERVASI LAPANGAN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle);
        add(pnlHeader, BorderLayout.NORTH);

        // Content Area (Struk)
        txtStruk = new JTextArea();
        txtStruk.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtStruk.setEditable(false);
        txtStruk.setBackground(Color.WHITE);
        txtStruk.setMargin(new Insets(16, 20, 16, 20));
        txtStruk.setText(generateStrukText());
        txtStruk.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(txtStruk);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(12, 16, 12, 16),
                BorderFactory.createLineBorder(new Color(210, 215, 220), 1)
        ));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Actions
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        pnlActions.setBackground(new Color(245, 247, 248));

        JButton btnCopy = new JButton("📋 Salin Teks");
        btnCopy.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCopy.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(txtStruk.getText()), null);
            JOptionPane.showMessageDialog(this, "Teks struk berhasil disalin ke clipboard!", "Informasi", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnSave = new JButton("💾 Simpan Struk (.txt)");
        btnSave.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnSave.addActionListener(e -> simpanStrukFile());

        JButton btnClose = new JButton("Tutup");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnClose.setBackground(new Color(45, 106, 79));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dispose());

        pnlActions.add(btnCopy);
        pnlActions.add(btnSave);
        pnlActions.add(btnClose);
        add(pnlActions, BorderLayout.SOUTH);
    }

    private String generateStrukText() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", Locale.forLanguageTag("id-ID"));
        DateTimeFormatter dateOnly = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.forLanguageTag("id-ID"));

        StringBuilder sb = new StringBuilder();
        sb.append("================================================\n");
        sb.append("       FUTSAL & MINI SOCCER ARENA MALANG        \n");
        sb.append("   Pusat Olahraga Futsal & Mini Soccer Modern   \n");
        sb.append("     Jl. Veteran No. 10, Lowokwaru, Malang      \n");
        sb.append("         WhatsApp / Hotline: 0812-3456-7890     \n");
        sb.append("================================================\n");
        sb.append(String.format("Waktu Cetak : %s\n", LocalDateTime.now().format(dtf)));
        sb.append(String.format("ID Booking  : %s\n", jadwal.getIdBooking()));
        sb.append("------------------------------------------------\n");
        sb.append("DATA PENYEWA:\n");
        sb.append(String.format("• Nama Tim/Fakultas : %s\n", jadwal.getNamaTim()));
        sb.append(String.format("• No. Handphone/WA  : %s\n", jadwal.getNoHp()));
        sb.append(String.format("• Jenis Lapangan    : %s\n", jadwal.getJenisLapangan()));
        sb.append("------------------------------------------------\n");
        sb.append("JADWAL MAIN:\n");
        sb.append(String.format("• Tanggal Main      : %s\n", jadwal.getTanggalMain().format(dateOnly)));
        sb.append(String.format("• Waktu Main        : %s WIB\n", jadwal.getFormatJam()));
        sb.append(String.format("• Durasi Sewa       : %d Jam\n", jadwal.getDurasiJam()));
        sb.append("------------------------------------------------\n");
        sb.append("RINCIAN PEMBAYARAN:\n");
        sb.append(String.format("• Total Biaya Sewa  : %s\n", jadwal.getTotalTarifRupiah()));
        sb.append(String.format("• Uang Muka (DP)    : %s\n", jadwal.getNominalDPRupiah()));
        sb.append(String.format("• Sisa Pembayaran   : %s\n", jadwal.getSisaPembayaranRupiah()));
        sb.append(String.format("• Status Pembayaran : [%s]\n", jadwal.getStatusDP()));
        sb.append("================================================\n");
        sb.append("CATATAN & PERATURAN ARENA:\n");
        sb.append("1. Tunjukkan struk ini kepada kasir/petugas arena.\n");
        sb.append("2. Hadir minimal 15 menit sebelum kick-off.\n");
        sb.append("3. Wajib melunasi sisa sewa sebelum bertanding.\n");
        sb.append("4. Wajib menggunakan sepatu futsal/turf standar.\n");
        sb.append("5. Pembatalan H-1 dikenakan penalti hangus DP.\n");
        sb.append("================================================\n");
        sb.append("    Terima kasih atas reservasi Anda di Malang! \n");
        sb.append("      Junjung Tinggi Sportivitas & Fair Play!   \n");
        sb.append("================================================\n");

        return sb.toString();
    }

    private void simpanStrukFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("Struk_" + jadwal.getIdBooking() + ".txt"));
        int res = fileChooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File target = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(target)) {
                writer.write(txtStruk.getText());
                JOptionPane.showMessageDialog(this, "Struk berhasil disimpan ke:\n" + target.getAbsolutePath(),
                        "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Gagal menyimpan file: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
