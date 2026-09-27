package com.futsalarena.model;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Class JadwalSewa merepresentasikan data transaksi reservasi sewa lapangan.
 * Atribut sesuai rancangan OOP:
 * - idBooking
 * - namaTim
 * - noHp
 * - tanggalMain
 * - jamMulai
 * - durasiJam
 * - statusDP
 * - totalTarif
 */
public class JadwalSewa {
    private String idBooking;
    private String namaTim;
    private String noHp;
    private LocalDate tanggalMain;
    private int jamMulai;      // format 24 jam (misal 8 untuk 08:00, 19 untuk 19:00)
    private int durasiJam;     // durasi dalam satuan jam
    private String statusDP;   // e.g. "LUNAS", "DP 50%", "DP Rp 150.000"
    private double totalTarif; // total harga sewa keseluruhan
    
    // Atribut pendukung tambahan untuk kelengkapan bisnis
    private double nominalDP;       // nominal uang muka yang disetorkan
    private String jenisLapangan;   // e.g. "Futsal Vinyl", "Mini Soccer Sintetis"

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final Locale LOCALE_ID = Locale.forLanguageTag("id-ID");

    // Konstruktor lengkap
    public JadwalSewa(String idBooking, String namaTim, String noHp, LocalDate tanggalMain,
                      int jamMulai, int durasiJam, String statusDP, double totalTarif,
                      double nominalDP, String jenisLapangan) {
        this.idBooking = idBooking;
        this.namaTim = namaTim;
        this.noHp = noHp;
        this.tanggalMain = tanggalMain;
        this.jamMulai = jamMulai;
        this.durasiJam = durasiJam;
        this.statusDP = statusDP;
        this.totalTarif = totalTarif;
        this.nominalDP = nominalDP;
        this.jenisLapangan = (jenisLapangan != null && !jenisLapangan.isEmpty()) ? jenisLapangan : "Futsal Vinyl";
    }

    // Konstruktor utama sesuai spesifikasi dasar
    public JadwalSewa(String idBooking, String namaTim, String noHp, LocalDate tanggalMain,
                      int jamMulai, int durasiJam, String statusDP, double totalTarif) {
        this(idBooking, namaTim, noHp, tanggalMain, jamMulai, durasiJam, statusDP, totalTarif, totalTarif * 0.5, "Futsal Vinyl");
    }

    // Getter & Setter
    public String getIdBooking() {
        return idBooking;
    }

    public void setIdBooking(String idBooking) {
        this.idBooking = idBooking;
    }

    public String getNamaTim() {
        return namaTim;
    }

    public void setNamaTim(String namaTim) {
        this.namaTim = namaTim;
    }

    public String getNoHp() {
        return noHp;
    }

    public void setNoHp(String noHp) {
        this.noHp = noHp;
    }

    public LocalDate getTanggalMain() {
        return tanggalMain;
    }

    public void setTanggalMain(LocalDate tanggalMain) {
        this.tanggalMain = tanggalMain;
    }

    public int getJamMulai() {
        return jamMulai;
    }

    public void setJamMulai(int jamMulai) {
        this.jamMulai = jamMulai;
    }

    public int getDurasiJam() {
        return durasiJam;
    }

    public void setDurasiJam(int durasiJam) {
        this.durasiJam = durasiJam;
    }

    public String getStatusDP() {
        return statusDP;
    }

    public void setStatusDP(String statusDP) {
        this.statusDP = statusDP;
    }

    public double getTotalTarif() {
        return totalTarif;
    }

    public void setTotalTarif(double totalTarif) {
        this.totalTarif = totalTarif;
    }

    public double getNominalDP() {
        return nominalDP;
    }

    public void setNominalDP(double nominalDP) {
        this.nominalDP = nominalDP;
    }

    public String getJenisLapangan() {
        return jenisLapangan;
    }

    public void setJenisLapangan(String jenisLapangan) {
        this.jenisLapangan = jenisLapangan;
    }

    // Helper methods
    public int getJamSelesai() {
        return jamMulai + durasiJam;
    }

    public double getSisaPembayaran() {
        double sisa = totalTarif - nominalDP;
        return sisa > 0 ? sisa : 0;
    }

    public String getFormatJam() {
        return String.format("%02d:00 - %02d:00", jamMulai, getJamSelesai());
    }

    public String getFormatRupiah(double amount) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(LOCALE_ID);
        return nf.format(amount).replace("Rp", "Rp ");
    }

    public String getTotalTarifRupiah() {
        return getFormatRupiah(totalTarif);
    }

    public String getNominalDPRupiah() {
        return getFormatRupiah(nominalDP);
    }

    public String getSisaPembayaranRupiah() {
        return getFormatRupiah(getSisaPembayaran());
    }

    /**
     * Memeriksa apakah jadwal ini bentrok (overlap) dengan jadwal lain pada tanggal & lapangan yang sama
     */
    public boolean isBentrok(LocalDate tgl, int start, int durasi, String lapangan) {
        if (!this.tanggalMain.equals(tgl)) {
            return false;
        }
        if (lapangan != null && !this.jenisLapangan.equalsIgnoreCase(lapangan)) {
            return false;
        }
        int existingStart = this.jamMulai;
        int existingEnd = this.getJamSelesai();
        int newStart = start;
        int newEnd = start + durasi;

        // Overlap formula: startA < endB && startB < endA
        return (newStart < existingEnd && existingStart < newEnd);
    }

    /**
     * Konversi ke baris CSV
     * Format: idBooking;namaTim;noHp;tanggalMain;jamMulai;durasiJam;statusDP;totalTarif;nominalDP;jenisLapangan
     */
    public String toCsvLine() {
        return String.join(";",
                escapeCsv(idBooking),
                escapeCsv(namaTim),
                escapeCsv(noHp),
                tanggalMain.format(DATE_FORMATTER),
                String.valueOf(jamMulai),
                String.valueOf(durasiJam),
                escapeCsv(statusDP),
                String.format(Locale.US, "%.0f", totalTarif),
                String.format(Locale.US, "%.0f", nominalDP),
                escapeCsv(jenisLapangan)
        );
    }

    /**
     * Parsing dari baris CSV
     */
    public static JadwalSewa fromCsvLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split(";", -1);
        if (parts.length < 8) {
            return null;
        }
        try {
            String id = unescapeCsv(parts[0].trim());
            String nama = unescapeCsv(parts[1].trim());
            String hp = unescapeCsv(parts[2].trim());
            LocalDate tgl = LocalDate.parse(parts[3].trim(), DATE_FORMATTER);
            int start = Integer.parseInt(parts[4].trim());
            int durasi = Integer.parseInt(parts[5].trim());
            String status = unescapeCsv(parts[6].trim());
            double total = Double.parseDouble(parts[7].trim());
            
            double dp = (parts.length > 8 && !parts[8].trim().isEmpty()) ? Double.parseDouble(parts[8].trim()) : total * 0.5;
            String lap = (parts.length > 9 && !parts[9].trim().isEmpty()) ? unescapeCsv(parts[9].trim()) : "Futsal Vinyl";

            return new JadwalSewa(id, nama, hp, tgl, start, durasi, status, total, dp, lap);
        } catch (Exception e) {
            System.err.println("Gagal parsing baris CSV: " + line + " Error: " + e.getMessage());
            return null;
        }
    }

    private static String escapeCsv(String str) {
        if (str == null) return "";
        return str.replace(";", ",");
    }

    private static String unescapeCsv(String str) {
        if (str == null) return "";
        return str;
    }
}
