package com.futsalarena.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Dialog Kalender Interaktif Swing untuk pemilihan tanggal yang estetik dan mudah digunakan.
 */
public class DatePickerDialog extends JDialog {

    private LocalDate selectedDate;
    private YearMonth currentYearMonth;
    private final JLabel lblMonthYear;
    private final JPanel pnlDaysGrid;
    private boolean confirmed = false;

    private static final Color COLOR_PRIMARY = new Color(27, 67, 50);
    private static final Color COLOR_ACCENT = new Color(45, 106, 79);
    private static final Color COLOR_HOVER = new Color(82, 183, 136);
    private static final Color COLOR_BG_TODAY = new Color(216, 243, 220);

    public DatePickerDialog(Frame parent, LocalDate initialDate) {
        super(parent, "Pilih Tanggal Reservasi", true);
        this.selectedDate = initialDate != null ? initialDate : LocalDate.now();
        this.currentYearMonth = YearMonth.from(this.selectedDate);

        setSize(360, 380);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        // Header Panel (Navigasi Bulan/Tahun)
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(COLOR_PRIMARY);
        pnlHeader.setBorder(new EmptyBorder(12, 15, 12, 15));

        JButton btnPrev = createNavButton("◀");
        JButton btnNext = createNavButton("▶");
        lblMonthYear = new JLabel("", SwingConstants.CENTER);
        lblMonthYear.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblMonthYear.setForeground(Color.WHITE);

        btnPrev.addActionListener(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateCalendar();
        });

        btnNext.addActionListener(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateCalendar();
        });

        pnlHeader.add(btnPrev, BorderLayout.WEST);
        pnlHeader.add(lblMonthYear, BorderLayout.CENTER);
        pnlHeader.add(btnNext, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        // Center Panel (Grid Nama Hari & Tanggal)
        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.setBackground(Color.WHITE);
        pnlCenter.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header Hari (Sen - Min)
        JPanel pnlDayNames = new JPanel(new GridLayout(1, 7, 3, 3));
        pnlDayNames.setBackground(Color.WHITE);
        String[] days = {"Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"};
        for (String d : days) {
            JLabel lblDay = new JLabel(d, SwingConstants.CENTER);
            lblDay.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblDay.setForeground(d.equals("Min") || d.equals("Sab") ? new Color(180, 40, 40) : new Color(80, 80, 80));
            pnlDayNames.add(lblDay);
        }
        pnlCenter.add(pnlDayNames, BorderLayout.NORTH);

        pnlDaysGrid = new JPanel(new GridLayout(6, 7, 3, 3));
        pnlDaysGrid.setBackground(Color.WHITE);
        pnlCenter.add(pnlDaysGrid, BorderLayout.CENTER);
        add(pnlCenter, BorderLayout.CENTER);

        // Bottom Panel (Tombol Pintas: Hari Ini, Besok, Batal)
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlBottom.setBackground(new Color(245, 247, 246));

        JButton btnToday = new JButton("Hari Ini");
        btnToday.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnToday.addActionListener(e -> {
            selectedDate = LocalDate.now();
            confirmed = true;
            dispose();
        });

        JButton btnTomorrow = new JButton("Besok");
        btnTomorrow.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnTomorrow.addActionListener(e -> {
            selectedDate = LocalDate.now().plusDays(1);
            confirmed = true;
            dispose();
        });

        JButton btnCancel = new JButton("Batal");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancel.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        pnlBottom.add(btnToday);
        pnlBottom.add(btnTomorrow);
        pnlBottom.add(btnCancel);
        add(pnlBottom, BorderLayout.SOUTH);

        updateCalendar();
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(COLOR_ACCENT);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void updateCalendar() {
        Locale idLocale = Locale.forLanguageTag("id-ID");
        String monthName = currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, idLocale);
        lblMonthYear.setText(monthName + " " + currentYearMonth.getYear());

        pnlDaysGrid.removeAll();

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeekVal = firstOfMonth.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday
        int leadingBlanks = dayOfWeekVal - 1;

        // Kosongkan slot sebelum tanggal 1
        for (int i = 0; i < leadingBlanks; i++) {
            pnlDaysGrid.add(new JLabel(""));
        }

        int daysInMonth = currentYearMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();

        for (int day = 1; day <= daysInMonth; day++) {
            final LocalDate date = currentYearMonth.atDay(day);
            JButton btnDay = new JButton(String.valueOf(day));
            btnDay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btnDay.setFocusPainted(false);
            btnDay.setMargin(new Insets(2, 2, 2, 2));
            btnDay.setCursor(new Cursor(Cursor.HAND_CURSOR));

            boolean isToday = date.equals(today);
            boolean isSelected = date.equals(selectedDate);

            if (isSelected) {
                btnDay.setBackground(COLOR_PRIMARY);
                btnDay.setForeground(Color.WHITE);
                btnDay.setFont(new Font("Segoe UI", Font.BOLD, 12));
            } else if (isToday) {
                btnDay.setBackground(COLOR_BG_TODAY);
                btnDay.setForeground(COLOR_PRIMARY);
                btnDay.setBorder(BorderFactory.createLineBorder(COLOR_ACCENT, 1));
            } else {
                btnDay.setBackground(Color.WHITE);
                btnDay.setForeground(new Color(40, 40, 40));
                btnDay.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
            }

            btnDay.addActionListener(e -> {
                selectedDate = date;
                confirmed = true;
                dispose();
            });

            pnlDaysGrid.add(btnDay);
        }

        // Lengkapi sisa grid
        int totalCells = leadingBlanks + daysInMonth;
        int remainingCells = 42 - totalCells;
        for (int i = 0; i < remainingCells; i++) {
            pnlDaysGrid.add(new JLabel(""));
        }

        pnlDaysGrid.revalidate();
        pnlDaysGrid.repaint();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public static LocalDate showDialog(Frame parent, LocalDate initialDate) {
        DatePickerDialog dialog = new DatePickerDialog(parent, initialDate);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            return dialog.getSelectedDate();
        }
        return initialDate;
    }
}
