package is216.petshop.view;

import is216.petshop.dao.ChamCongDAO;
import is216.petshop.dao.NhanVienDAO;
import is216.petshop.dao.UserDAO;
import is216.petshop.model.ChamCongModel;
import is216.petshop.model.NhanVienModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class AttendancePanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private ChamCongDAO chamCongDAO;
    private NhanVienDAO nhanVienDAO;
    private NhanVienModel currentEmp;
    private boolean isManager;
    private String username;

    private JComboBox<String> cbEmployee;
    private JTextField txtMonth;
    private JButton btnCheckIn;
    private JButton btnCheckOut;
    private JLabel lblStatus;

    // Styling constants
    private static final Color COLOR_SURFACE = Color.WHITE;
    private static final Color COLOR_BACKGROUND = new Color(244, 246, 249);
    private static final Color COLOR_BORDER = new Color(226, 232, 240);
    private static final Color COLOR_TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color COLOR_TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color COLOR_SELECTION = new Color(219, 234, 254);
    private static final Color COLOR_ALT_ROW = new Color(249, 250, 251);
    private static final Color COLOR_PRIMARY = new Color(108, 93, 211);
    private static final Color COLOR_SUCCESS = new Color(22, 163, 74);
    private static final Color COLOR_WARNING = new Color(217, 119, 6);

    public AttendancePanel(String username) {
        this.username = username;
        this.chamCongDAO = new ChamCongDAO();
        this.nhanVienDAO = new NhanVienDAO();

        UserDAO userDAO = new UserDAO();
        this.currentEmp = userDAO.getNhanVienByUsername(username);

        String role = userDAO.getUserRole(username);
        this.isManager = role != null && (
                role.toLowerCase().contains("quản lý") ||
                role.toLowerCase().contains("qu?n l") ||
                role.toLowerCase().contains("quan ly") ||
                role.toLowerCase().contains("quản")
        );

        initComponents();
        refreshStatus();
        loadDataToTable();
    }

    private void initComponents() {
        this.setBackground(COLOR_BACKGROUND);
        this.setLayout(new BorderLayout(20, 20));
        this.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // --- TOP HEADER ---
        JPanel topPanel = new JPanel(new BorderLayout(0, 15));
        topPanel.setBackground(COLOR_BACKGROUND);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_BACKGROUND);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 4));
        titlePanel.setBackground(COLOR_BACKGROUND);
        JLabel lblTitle = new JLabel("Chấm công");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(COLOR_TEXT_PRIMARY);

        JLabel lblSubtitle = new JLabel(isManager ? "Xem lịch sử chấm công nhân viên toàn cửa hàng" : "Quản lý check-in/out và xem lịch sử làm việc cá nhân");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(COLOR_TEXT_SECONDARY);

        titlePanel.add(lblTitle);
        titlePanel.add(lblSubtitle);
        headerPanel.add(titlePanel, BorderLayout.WEST);

        // --- CHECK IN / OUT ACTIONS (For Employees) ---
        if (!isManager && currentEmp != null) {
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
            actionPanel.setBackground(COLOR_BACKGROUND);

            lblStatus = new JLabel("Hôm nay: Chưa làm việc");
            lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            lblStatus.setForeground(COLOR_TEXT_SECONDARY);

            btnCheckIn = createStyledButton("Check In 🔒", COLOR_SUCCESS, Color.WHITE);
            btnCheckIn.setPreferredSize(new Dimension(130, 40));
            btnCheckIn.addActionListener(e -> doCheckIn());

            btnCheckOut = createStyledButton("Check Out 🔓", COLOR_WARNING, Color.WHITE);
            btnCheckOut.setPreferredSize(new Dimension(130, 40));
            btnCheckOut.addActionListener(e -> doCheckOut());

            actionPanel.add(lblStatus);
            actionPanel.add(btnCheckIn);
            actionPanel.add(btnCheckOut);
            headerPanel.add(actionPanel, BorderLayout.EAST);
        }

        // --- FILTER PANEL ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        filterPanel.setBackground(COLOR_BACKGROUND);

        JLabel lblMonthFilter = new JLabel("Tháng/Năm (MM/YYYY):");
        lblMonthFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblMonthFilter.setForeground(COLOR_TEXT_PRIMARY);

        Calendar cal = Calendar.getInstance();
        String currentMonthStr = String.format("%02d/%d", cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
        txtMonth = new JTextField(currentMonthStr, 10);
        txtMonth.setPreferredSize(new Dimension(120, 36));
        txtMonth.putClientProperty("FlatLaf.style", "arc: 8; margin: 4,8,4,8");
        txtMonth.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        filterPanel.add(lblMonthFilter);
        filterPanel.add(txtMonth);

        if (isManager) {
            JLabel lblEmpFilter = new JLabel("Nhân viên:");
            lblEmpFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblEmpFilter.setForeground(COLOR_TEXT_PRIMARY);

            cbEmployee = new JComboBox<>();
            cbEmployee.setPreferredSize(new Dimension(200, 36));
            cbEmployee.putClientProperty("FlatLaf.style", "arc: 8");
            cbEmployee.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            
            cbEmployee.addItem("Tất cả nhân viên");
            List<NhanVienModel> list = nhanVienDAO.getDanhSachNhanVien();
            for (NhanVienModel nv : list) {
                cbEmployee.addItem(nv.getMaNhanVien() + " - " + nv.getHoTen());
            }

            filterPanel.add(lblEmpFilter);
            filterPanel.add(cbEmployee);
        }

        JButton btnFilter = createStyledButton("Tìm kiếm", COLOR_PRIMARY, Color.WHITE);
        btnFilter.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnFilter.setPreferredSize(new Dimension(100, 36));

        JButton btnRefresh = createStyledButton("Làm mới", Color.WHITE, COLOR_TEXT_SECONDARY);
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnRefresh.setPreferredSize(new Dimension(100, 36));
        btnRefresh.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        btnRefresh.setBorderPainted(true);

        filterPanel.add(btnFilter);
        filterPanel.add(btnRefresh);

        btnFilter.addActionListener(e -> loadDataToTable());
        btnRefresh.addActionListener(e -> {
            txtMonth.setText(currentMonthStr);
            if (isManager) {
                cbEmployee.setSelectedIndex(0);
            }
            loadDataToTable();
        });

        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.CENTER);

        // --- CONTENT TABLE ---
        JPanel contentPanel = new JPanel(new BorderLayout()) {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                RoundRectangle2D.Float roundRect = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setClip(roundRect);
                super.paint(g2);

                g2.setClip(null);
                g2.setColor(COLOR_BORDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1.5f, getHeight() - 1.5f, 16, 16));
                g2.dispose();
            }
        };
        contentPanel.setOpaque(false);
        contentPanel.setBackground(COLOR_SURFACE);

        String[] columnNames = { "NHÂN VIÊN", "NGÀY", "GIỜ VÀO", "GIỜ RA", "SỐ GIỜ LÀM", "TĂNG CA (OT)", "TRẠNG THÁI"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(52);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setGridColor(COLOR_BORDER);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(COLOR_SELECTION);
        table.setSelectionForeground(COLOR_TEXT_PRIMARY);
        table.setFocusable(false);

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBackground(COLOR_SURFACE);
                setForeground(COLOR_TEXT_SECONDARY);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 8));
                return this;
            }
        };

        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(headerRenderer);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, COLOR_BORDER));
        header.setPreferredSize(new Dimension(0, 44));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer defRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row,
                                                           int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    setBackground(row % 2 == 0 ? COLOR_SURFACE : COLOR_ALT_ROW);
                } else {
                    setBackground(COLOR_SELECTION);
                }
                setForeground(COLOR_TEXT_PRIMARY);
                setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 8));
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                return this;
            }
        };

        table.setDefaultRenderer(Object.class, defRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(new BadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(COLOR_SURFACE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        contentPanel.add(scrollPane, BorderLayout.CENTER);

        this.add(topPanel, BorderLayout.NORTH);
        this.add(contentPanel, BorderLayout.CENTER);
    }

    private void doCheckIn() {
        if (currentEmp == null) return;
        boolean ok = chamCongDAO.checkIn(currentEmp.getMaNhanVien());
        if (ok) {
            JOptionPane.showMessageDialog(this, "Check-in thành công! Chúc bạn một ngày làm việc hiệu quả 🐾");
            refreshStatus();
            loadDataToTable();
        } else {
            JOptionPane.showMessageDialog(this, "Không thể check-in. Bạn đã check-in hôm nay rồi!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doCheckOut() {
        if (currentEmp == null) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn muốn Check-out và kết thúc ngày làm việc không?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = chamCongDAO.checkOut(currentEmp.getMaNhanVien());
        if (ok) {
            JOptionPane.showMessageDialog(this, "Check-out thành công! Cảm ơn bạn vì một ngày nỗ lực 🐾");
            refreshStatus();
            loadDataToTable();
        } else {
            JOptionPane.showMessageDialog(this, "Không thể check-out. Hãy đảm bảo bạn đã check-in và chưa check-out hôm nay!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshStatus() {
        if (isManager || currentEmp == null) return;
        ChamCongModel today = chamCongDAO.getTodayRecord(currentEmp.getMaNhanVien());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        if (today == null) {
            lblStatus.setText("Trạng thái hôm nay: Chưa check-in 🔒");
            btnCheckIn.setEnabled(true);
            btnCheckOut.setEnabled(false);
        } else if (today.getGioRa() == null) {
            lblStatus.setText("Trạng thái: Đang làm việc (Vào lúc " + timeFormat.format(today.getGioVao()) + ") 🕒");
            btnCheckIn.setEnabled(false);
            btnCheckOut.setEnabled(true);
        } else {
            lblStatus.setText("Trạng thái: Đã kết thúc ngày làm việc (Ra lúc " + timeFormat.format(today.getGioRa()) + ") 🔓");
            btnCheckIn.setEnabled(false);
            btnCheckOut.setEnabled(false);
        }
    }

    private void loadDataToTable() {
        tableModel.setRowCount(0);
        String thangNam = txtMonth.getText();

        List<ChamCongModel> list;
        if (isManager) {
            int empIdx = cbEmployee.getSelectedIndex();
            if (empIdx > 0) {
                String selectedItem = (String) cbEmployee.getSelectedItem();
                int empId = Integer.parseInt(selectedItem.split(" - ")[0]);
                list = chamCongDAO.getRecordsByEmployee(empId, thangNam);
            } else {
                list = chamCongDAO.getAllRecords(thangNam);
            }
        } else {
            if (currentEmp != null) {
                list = chamCongDAO.getRecordsByEmployee(currentEmp.getMaNhanVien(), thangNam);
            } else {
                return;
            }
        }

        SimpleDateFormat dayFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        for (ChamCongModel cc : list) {
            Object[] row = {
                    cc.getHoTen() != null ? cc.getHoTen() : (currentEmp != null ? currentEmp.getHoTen() : "Nhân viên #" + cc.getMaNhanVien()),
                    dayFormat.format(cc.getNgay()),
                    cc.getGioVao() != null ? timeFormat.format(cc.getGioVao()) : "",
                    cc.getGioRa() != null ? timeFormat.format(cc.getGioRa()) : "Chưa check-out",
                    cc.getSoGioLam() + " giờ",
                    cc.getTangCa() + " giờ",
                    cc.getGioRa() != null ? "Có mặt" : "Đang làm việc"
            };
            tableModel.addRow(row);
        }
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("FlatLaf.style", "arc: 12");
        return btn;
    }

    private class BadgeRenderer extends DefaultTableCellRenderer {
        private String badgeText = "";

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            if (!sel)
                setBackground(row % 2 == 0 ? COLOR_SURFACE : COLOR_ALT_ROW);
            else
                setBackground(COLOR_SELECTION);

            setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 8));
            badgeText = v != null ? v.toString() : "";
            setText("");
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (badgeText.isEmpty())
                return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color pillBgColor;
            Color textColor;

            if (badgeText.contains("Đang làm việc")) {
                pillBgColor = new Color(254, 243, 199); // Vàng nhạt
                textColor = new Color(180, 83, 9);
            } else {
                pillBgColor = new Color(220, 252, 231); // Xanh lá nhạt
                textColor = new Color(22, 163, 74);
            }

            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(badgeText);
            int textHeight = fm.getHeight();

            int padX = 12;
            int padY = 4;
            int pillWidth = textWidth + 2 * padX;
            int pillHeight = textHeight + 2 * padY;

            int x = 16;
            int y = (getHeight() - pillHeight) / 2;

            g2.setColor(pillBgColor);
            g2.fillRoundRect(x, y, pillWidth, pillHeight, pillHeight, pillHeight);

            g2.setColor(textColor);
            g2.drawString(badgeText, x + padX, y + ((pillHeight - textHeight) / 2) + fm.getAscent());

            g2.dispose();
        }
    }
}
