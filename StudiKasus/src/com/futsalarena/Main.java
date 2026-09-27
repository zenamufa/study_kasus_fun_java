package com.futsalarena;

import com.futsalarena.ui.MainFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Entry point aplikasi Futsal & Mini Soccer Arena Malang
 */
public class Main {
    public static void main(String[] args) {
        // Konfigurasi rendering font & LookAndFeel yang modern
        try {
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");

            // Menggunakan System Look & Feel (Windows 11 / Modern Look)
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Penyesuaian font default sistem ke Segoe UI jika tersedia
            Font defaultFont = new Font("Segoe UI", Font.PLAIN, 12);
            setUIFont(new javax.swing.plaf.FontUIResource(defaultFont));
        } catch (Exception e) {
            // Fallback ke default jika tidak dapat memuat LookAndFeel
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }

    private static void setUIFont(javax.swing.plaf.FontUIResource f) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }
}
