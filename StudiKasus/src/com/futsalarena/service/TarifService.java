package com.futsalarena.service;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Service untuk mengelola perhitungan tarif otomatis:
 * - Siang: 08.00 - 18.00 (Tarif standar tanpa lampu)
 * - Malam: 18.00 - 24.00 (Tarif malam + lampu penerangan)
 * - Perhitungan uang muka (DP) minimal 50%
 */
public class TarifService {

    // Tarif Futsal Indoor (Vinyl)
    public static final double TARIF_FUTSAL_SIANG = 120_000.0;
    public static final double TARIF_FUTSAL_MALAM = 175_000.0; // Termasuk biaya lampu stadion

    // Tarif Mini Soccer Outdoor (Rumput Sintetis)
    public static final double TARIF_MINISOCCER_SIANG = 250_000.0;
    public static final double TARIF_MINISOCCER_MALAM = 350_000.0; // Termasuk floodlight penerangan lapangan

    public static final int BATAS_JAM_MALAM = 18; // 18:00 ke atas dihitung tarif malam
    public static final double PERSENTASE_MINIMAL_DP = 0.50; // 50%

    public static class RincianTarif {
        private final double totalTarif;
        private final double minimalDP;
        private final int jamSiang;
        private final int jamMalam;
        private final double tarifSiangPerJam;
        private final double tarifMalamPerJam;
        private final double subtotalSiang;
        private final double subtotalMalam;
        private final String rincianTeks;

        public RincianTarif(double totalTarif, double minimalDP, int jamSiang, int jamMalam,
                            double tarifSiangPerJam, double tarifMalamPerJam,
                            double subtotalSiang, double subtotalMalam, String rincianTeks) {
            this.totalTarif = totalTarif;
            this.minimalDP = minimalDP;
            this.jamSiang = jamSiang;
            this.jamMalam = jamMalam;
            this.tarifSiangPerJam = tarifSiangPerJam;
            this.tarifMalamPerJam = tarifMalamPerJam;
            this.subtotalSiang = subtotalSiang;
            this.subtotalMalam = subtotalMalam;
            this.rincianTeks = rincianTeks;
        }

        public double getTotalTarif() {
            return totalTarif;
        }

        public double getMinimalDP() {
            return minimalDP;
        }

        public int getJamSiang() {
            return jamSiang;
        }

        public int getJamMalam() {
            return jamMalam;
        }

        public double getTarifSiangPerJam() {
            return tarifSiangPerJam;
        }

        public double getTarifMalamPerJam() {
            return tarifMalamPerJam;
        }

        public double getSubtotalSiang() {
            return subtotalSiang;
        }

        public double getSubtotalMalam() {
            return subtotalMalam;
        }

        public String getRincianTeks() {
            return rincianTeks;
        }

        public String formatRupiah(double amount) {
            NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));
            return nf.format(amount).replace("Rp", "Rp ");
        }
    }

    /**
     * Menghitung total tarif berdasarkan jam slot dan jenis lapangan
     */
    public RincianTarif hitung(String jenisLapangan, int jamMulai, int durasiJam) {
        boolean isMiniSoccer = jenisLapangan != null && jenisLapangan.toLowerCase().contains("mini soccer");
        double rateSiang = isMiniSoccer ? TARIF_MINISOCCER_SIANG : TARIF_FUTSAL_SIANG;
        double rateMalam = isMiniSoccer ? TARIF_MINISOCCER_MALAM : TARIF_FUTSAL_MALAM;

        int jamSiang = 0;
        int jamMalam = 0;

        for (int i = 0; i < durasiJam; i++) {
            int currentHour = jamMulai + i;
            if (currentHour < BATAS_JAM_MALAM) {
                jamSiang++;
            } else {
                jamMalam++;
            }
        }

        double subtotalSiang = jamSiang * rateSiang;
        double subtotalMalam = jamMalam * rateMalam;
        double total = subtotalSiang + subtotalMalam;
        double minimalDP = total * PERSENTASE_MINIMAL_DP;

        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));

        StringBuilder sb = new StringBuilder();
        if (jamSiang > 0 && jamMalam > 0) {
            sb.append(jamSiang).append(" jam Siang (").append(nf.format(rateSiang)).append(") + ")
              .append(jamMalam).append(" jam Malam+Lampu (").append(nf.format(rateMalam)).append(")");
        } else if (jamSiang > 0) {
            sb.append(jamSiang).append(" jam Siang (").append(nf.format(rateSiang)).append("/jam)");
        } else {
            sb.append(jamMalam).append(" jam Malam + Lampu Penerangan (").append(nf.format(rateMalam)).append("/jam)");
        }

        return new RincianTarif(total, minimalDP, jamSiang, jamMalam, rateSiang, rateMalam,
                subtotalSiang, subtotalMalam, sb.toString());
    }

    /**
     * Validasi nominal DP minimal 50%
     */
    public boolean isValidDP(double totalTarif, double nominalDP) {
        double minDP = totalTarif * PERSENTASE_MINIMAL_DP;
        // toleransi pembulatan kecil
        return (nominalDP >= minDP - 0.01);
    }

    /**
     * Menghasilkan teks status DP
     */
    public String tentukanStatusDP(double totalTarif, double nominalDP) {
        if (nominalDP >= totalTarif) {
            return "LUNAS 100%";
        } else if (isValidDP(totalTarif, nominalDP)) {
            double persen = (nominalDP / totalTarif) * 100.0;
            return String.format(Locale.US, "DP %.0f%% (Belum Lunas)", persen);
        } else {
            return "DP Kurang (< 50%)";
        }
    }
}
