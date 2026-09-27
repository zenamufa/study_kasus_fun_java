package com.futsalarena.repository;

import com.futsalarena.model.JadwalSewa;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository data persistence ke berkas 'jadwal_futsal.csv'.
 * Menyediakan validasi anti-bentrok jadwal, operasi CRUD, dan sinkronisasi berkas.
 */
public class JadwalRepository {

    private final File fileCsv;
    private final List<JadwalSewa> listJadwal = new ArrayList<>();

    public static final String CSV_HEADER = "idBooking;namaTim;noHp;tanggalMain;jamMulai;durasiJam;statusDP;totalTarif;nominalDP;jenisLapangan";

    public JadwalRepository(String filePath) {
        this.fileCsv = new File(filePath);
        loadFromFile();
        if (listJadwal.isEmpty() && !fileCsv.exists()) {
            initSampleData();
        }
    }

    public JadwalRepository() {
        this("jadwal_futsal.csv");
    }

    /**
     * Membaca data dari file CSV
     */
    public synchronized void loadFromFile() {
        listJadwal.clear();
        if (!fileCsv.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileCsv), StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine && line.toLowerCase().contains("idbooking")) {
                    firstLine = false;
                    continue;
                }
                firstLine = false;
                JadwalSewa jadwal = JadwalSewa.fromCsvLine(line);
                if (jadwal != null) {
                    listJadwal.add(jadwal);
                }
            }
        } catch (IOException e) {
            System.err.println("Gagal membaca CSV: " + e.getMessage());
        }
    }

    /**
     * Menyimpan seluruh list jadwal kembali ke file CSV
     */
    public synchronized void saveToFile() throws IOException {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileCsv), StandardCharsets.UTF_8))) {
            writer.println(CSV_HEADER);
            for (JadwalSewa j : listJadwal) {
                writer.println(j.toCsvLine());
            }
        }
    }

    /**
     * Inisialisasi data sampel awal jika berkas baru dibuat
     */
    private void initSampleData() {
        LocalDate today = LocalDate.now();
        listJadwal.add(new JadwalSewa("BK-" + today.toString().replace("-", "") + "-001",
                "Teknik Informatika 22", "081234567890", today, 9, 2, "DP 50% (Belum Lunas)", 240_000, 120_000, "Futsal Vinyl"));
        listJadwal.add(new JadwalSewa("BK-" + today.toString().replace("-", "") + "-002",
                "Kedokteran Brawijaya", "082345678901", today, 16, 2, "LUNAS 100%", 295_000, 295_000, "Futsal Vinyl"));
        listJadwal.add(new JadwalSewa("BK-" + today.toString().replace("-", "") + "-003",
                "Ilmu Komunikasi 23", "085678901234", today, 19, 2, "DP 50% (Belum Lunas)", 350_000, 175_000, "Futsal Vinyl"));
        listJadwal.add(new JadwalSewa("BK-" + today.plusDays(1).toString().replace("-", "") + "-004",
                "Fakultas Hukum FC", "087890123456", today.plusDays(1), 18, 2, "LUNAS 100%", 700_000, 700_000, "Mini Soccer Sintetis"));
        try {
            saveToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Validasi apakah ada jadwal yang bentrok dengan jadwal sewa baru.
     * Mengembalikan jadwal yang bentrok jika ada, atau null jika slot kosong.
     */
    public synchronized JadwalSewa cariJadwalBentrok(LocalDate tgl, int jamMulai, int durasi, String lapangan, String excludeIdBooking) {
        for (JadwalSewa existing : listJadwal) {
            if (excludeIdBooking != null && existing.getIdBooking().equalsIgnoreCase(excludeIdBooking)) {
                continue;
            }
            if (existing.isBentrok(tgl, jamMulai, durasi, lapangan)) {
                return existing;
            }
        }
        return null;
    }

    /**
     * Menambahkan jadwal sewa baru dengan validasi anti-bentrok
     */
    public synchronized void tambahJadwal(JadwalSewa jadwalBaru) throws Exception {
        JadwalSewa bentrok = cariJadwalBentrok(
                jadwalBaru.getTanggalMain(),
                jadwalBaru.getJamMulai(),
                jadwalBaru.getDurasiJam(),
                jadwalBaru.getJenisLapangan(),
                null
        );

        if (bentrok != null) {
            throw new IllegalArgumentException(String.format(
                    "JADWAL BENTROK!\n" +
                    "Lapangan [%s] pada tanggal %s sudah dipesan oleh:\n" +
                    "• Tim: %s (ID: %s)\n" +
                    "• Jam: %02d:00 s.d. %02d:00\n\n" +
                    "Silakan pilih slot jam atau tanggal lain.",
                    bentrok.getJenisLapangan(),
                    bentrok.getTanggalMain(),
                    bentrok.getNamaTim(),
                    bentrok.getIdBooking(),
                    bentrok.getJamMulai(),
                    bentrok.getJamSelesai()
            ));
        }

        listJadwal.add(jadwalBaru);
        saveToFile();
    }

    /**
     * Menghapus jadwal sewa berdasarkan idBooking
     */
    public synchronized boolean hapusJadwal(String idBooking) throws IOException {
        boolean removed = listJadwal.removeIf(j -> j.getIdBooking().equalsIgnoreCase(idBooking));
        if (removed) {
            saveToFile();
        }
        return removed;
    }

    /**
     * Memperbarui status pembayaran / pelunasan
     */
    public synchronized boolean updatePelunasan(String idBooking, double tambahanBayar) throws IOException {
        for (JadwalSewa j : listJadwal) {
            if (j.getIdBooking().equalsIgnoreCase(idBooking)) {
                double totalDPBaru = j.getNominalDP() + tambahanBayar;
                j.setNominalDP(totalDPBaru);
                if (totalDPBaru >= j.getTotalTarif()) {
                    j.setStatusDP("LUNAS 100%");
                } else {
                    double persen = (totalDPBaru / j.getTotalTarif()) * 100.0;
                    j.setStatusDP(String.format(Locale.US, "DP %.0f%% (Belum Lunas)", persen));
                }
                saveToFile();
                return true;
            }
        }
        return false;
    }

    /**
     * Mengambil daftar jadwal yang difilter berdasarkan tanggal dan lapangan
     */
    public synchronized List<JadwalSewa> getJadwalByTanggal(LocalDate tgl, String lapangan) {
        return listJadwal.stream()
                .filter(j -> j.getTanggalMain().equals(tgl))
                .filter(j -> lapangan == null || lapangan.isEmpty() || j.getJenisLapangan().equalsIgnoreCase(lapangan))
                .sorted(Comparator.comparingInt(JadwalSewa::getJamMulai))
                .collect(Collectors.toList());
    }

    /**
     * Mendapatkan list semua jadwal
     */
    public synchronized List<JadwalSewa> getAllJadwal() {
        return new ArrayList<>(listJadwal);
    }

    /**
     * Membuat ID Booking baru yang unik dan rapi: BK-YYYYMMDD-XXX
     */
    public synchronized String generateNextIdBooking(LocalDate tgl) {
        String prefix = "BK-" + tgl.toString().replace("-", "") + "-";
        int maxSeq = 0;
        for (JadwalSewa j : listJadwal) {
            if (j.getIdBooking() != null && j.getIdBooking().startsWith(prefix)) {
                try {
                    String seqStr = j.getIdBooking().substring(prefix.length());
                    int seq = Integer.parseInt(seqStr);
                    if (seq > maxSeq) {
                        maxSeq = seq;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("%s%03d", prefix, maxSeq + 1);
    }

    public File getFileCsv() {
        return fileCsv;
    }

    public static void main(String[] args) {
        JadwalRepository repo = new JadwalRepository();
        System.out.println("Berkas CSV berhasil dimuat: " + repo.getFileCsv().getAbsolutePath());
        System.out.println("Total jadwal tersimpan: " + repo.getAllJadwal().size());
        for (JadwalSewa j : repo.getAllJadwal()) {
            System.out.printf("- [%s] %s | %s (%s) | Lapangan: %s | Total: %s | Status: %s%n",
                    j.getIdBooking(), j.getNamaTim(), j.getTanggalMain(), j.getFormatJam(),
                    j.getJenisLapangan(), j.getTotalTarifRupiah(), j.getStatusDP());
        }
    }
}
