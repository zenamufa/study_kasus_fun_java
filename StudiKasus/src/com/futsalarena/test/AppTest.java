package com.futsalarena.test;

import com.futsalarena.model.JadwalSewa;
import com.futsalarena.repository.JadwalRepository;
import com.futsalarena.service.TarifService;

import java.io.File;
import java.time.LocalDate;

/**
 * Unit testing komprehensif untuk memvalidasi seluruh fungsionalitas logika bisnis,
 * perhitungan tarif otomatis siang vs malam, validasi DP minimal 50%,
 * serta validasi anti-bentrok pada berkas CSV.
 */
public class AppTest {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  PENGUJIAN SISTEM RESERVASI FUTSAL ARENA MALANG  ");
        System.out.println("==================================================");

        int passed = 0;
        int failed = 0;

        // Test 1: Perhitungan Tarif Siang Penuh (Contoh: Jam 10:00, 2 Jam)
        try {
            TarifService tarifService = new TarifService();
            TarifService.RincianTarif r = tarifService.hitung("Futsal Vinyl", 10, 2);
            assert r.getTotalTarif() == 240_000.0 : "Expected 240000 but was " + r.getTotalTarif();
            assert r.getMinimalDP() == 120_000.0 : "Expected min DP 120000 but was " + r.getMinimalDP();
            assert r.getJamSiang() == 2 : "Jam siang harus 2";
            assert r.getJamMalam() == 0 : "Jam malam harus 0";
            System.out.println("✓ TEST 1 PASSED: Tarif Siang Penuh (10:00 - 12:00 = 2 jam @ Rp 120.000 = Rp 240.000)");
            passed++;
        } catch (Throwable t) {
            System.err.println("✗ TEST 1 FAILED: " + t.getMessage());
            failed++;
        }

        // Test 2: Perhitungan Tarif Campuran Siang & Malam (Contoh: Jam 17:00, 2 Jam -> 1 Jam Siang + 1 Jam Malam + Lampu)
        try {
            TarifService tarifService = new TarifService();
            TarifService.RincianTarif r = tarifService.hitung("Futsal Vinyl", 17, 2);
            // 17-18: Siang (120.000)
            // 18-19: Malam + Lampu (175.000)
            // Total: 295.000, Min DP: 147.500
            assert r.getTotalTarif() == 295_000.0 : "Expected 295000 but was " + r.getTotalTarif();
            assert r.getMinimalDP() == 147_500.0 : "Expected min DP 147500 but was " + r.getMinimalDP();
            assert r.getJamSiang() == 1 : "Jam siang harus 1";
            assert r.getJamMalam() == 1 : "Jam malam harus 1";
            System.out.println("✓ TEST 2 PASSED: Perbedaan Tarif Otomatis Siang vs Malam + Lampu Penerangan (17:00-19:00 = Rp 295.000)");
            passed++;
        } catch (Throwable t) {
            System.err.println("✗ TEST 2 FAILED: " + t.getMessage());
            failed++;
        }

        // Test 3: Validasi DP Minimal 50%
        try {
            TarifService tarifService = new TarifService();
            double total = 300_000.0;
            boolean dpKurang = tarifService.isValidDP(total, 140_000.0); // 140k < 150k -> false
            boolean dpPas = tarifService.isValidDP(total, 150_000.0);    // 150k == 50% -> true
            boolean dpLunas = tarifService.isValidDP(total, 300_000.0);  // 300k == 100% -> true

            assert !dpKurang : "DP 140.000 harusnya ditolak karena < 50%";
            assert dpPas : "DP 150.000 harusnya diterima karena == 50%";
            assert dpLunas : "DP 300.000 harusnya diterima karena 100%";
            System.out.println("✓ TEST 3 PASSED: Validasi Uang Muka (DP minimal 50%) Berjalan Tepat");
            passed++;
        } catch (Throwable t) {
            System.err.println("✗ TEST 3 FAILED: " + t.getMessage());
            failed++;
        }

        // Test 4: Serialisasi & Deserialisasi CSV JadwalSewa
        try {
            LocalDate tgl = LocalDate.of(2026, 9, 26);
            JadwalSewa j1 = new JadwalSewa("BK-TEST-001", "Teknik Elektro", "0811223344", tgl, 14, 2, "DP 50%", 240000, 120000, "Futsal Vinyl");
            String csvLine = j1.toCsvLine();
            JadwalSewa jParsed = JadwalSewa.fromCsvLine(csvLine);

            assert jParsed != null : "Parsing CSV tidak boleh null";
            assert jParsed.getIdBooking().equals("BK-TEST-001") : "ID booking mismatch";
            assert jParsed.getNamaTim().equals("Teknik Elektro") : "Nama tim mismatch";
            assert jParsed.getJamMulai() == 14 : "Jam mulai mismatch";
            assert jParsed.getDurasiJam() == 2 : "Durasi mismatch";
            assert jParsed.getTotalTarif() == 240000 : "Total tarif mismatch";
            System.out.println("✓ TEST 4 PASSED: Format Data Persistence CSV JadwalSewa Konsisten");
            passed++;
        } catch (Throwable t) {
            System.err.println("✗ TEST 4 FAILED: " + t.getMessage());
            failed++;
        }

        // Test 5: Validasi Deteksi Bentrok Jadwal Sewa
        try {
            File tempCsv = new File("test_jadwal.csv");
            if (tempCsv.exists()) tempCsv.delete();

            JadwalRepository testRepo = new JadwalRepository("test_jadwal.csv");
            LocalDate tglMain = LocalDate.of(2026, 10, 1);

            // Simpan booking 1: Jam 14:00 - 16:00
            JadwalSewa booking1 = new JadwalSewa("BK-001", "Fakultas Hukum", "0812345678", tglMain, 14, 2, "LUNAS", 240000, 240000, "Futsal Vinyl");
            testRepo.tambahJadwal(booking1);

            // Coba booking 2: Jam 15:00 - 17:00 (Overlap 1 jam dengan booking 1!) -> Harus melempar exception bentrok!
            JadwalSewa bookingBentrok = new JadwalSewa("BK-002", "Teknik Mesin", "0898765432", tglMain, 15, 2, "DP 50%", 240000, 120000, "Futsal Vinyl");
            boolean caught = false;
            try {
                testRepo.tambahJadwal(bookingBentrok);
            } catch (IllegalArgumentException ex) {
                caught = true;
                assert ex.getMessage().contains("JADWAL BENTROK") : "Pesan bentrok harus muncul";
            }
            assert caught : "Jadwal bentrok harus ditolak oleh sistem!";

            // Coba booking 3: Jam 16:00 - 18:00 (Tepat setelah booking 1 selesai) -> Harus berhasil!
            JadwalSewa bookingAman = new JadwalSewa("BK-003", "Ilmu Budaya", "0855667788", tglMain, 16, 2, "LUNAS", 295000, 295000, "Futsal Vinyl");
            testRepo.tambahJadwal(bookingAman);

            // Cleanup
            if (tempCsv.exists()) tempCsv.delete();

            System.out.println("✓ TEST 5 PASSED: Validasi Anti-Bentrok Berhasil Mencegah Overlapping Sewa");
            passed++;
        } catch (Throwable t) {
            System.err.println("✗ TEST 5 FAILED: " + t.getMessage());
            failed++;
        }

        System.out.println("==================================================");
        System.out.println("HASIL PENGUJIAN: " + passed + " Berhasil, " + failed + " Gagal.");
        System.out.println("==================================================");
        if (failed > 0) {
            System.exit(1);
        }
    }
}
