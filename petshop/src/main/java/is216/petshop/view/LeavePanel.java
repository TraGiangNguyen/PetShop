package is216.petshop.view;

import is216.petshop.dao.DonNghiPhepDAO;
import is216.petshop.dao.UserDAO;
import is216.petshop.model.DonNghiPhepModel;
import is216.petshop.model.NhanVienModel;
import is216.petshop.model.QuanLyPhepModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class LeavePanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private DonNghiPhepDAO donNghiPhepDAO;
    private NhanVienModel currentEmp;
    private boolean isManager;
    private String username;

    private JLabel lblTotal;
    private JLabel lblUsed;
    private JLabel lblRemaining;
    private List<DonNghiPhepModel> currentList;

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
    private static final Color COLOR_DANGER = new Color(220, 38, 38);

    public LeavePanel(String username) {
        this.username = username;
        this.donNghiPhepDAO = new DonNghiPhepDAO();

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
        loadBalance();
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
        JLabel lblTitle = new JLabel("Nghỉ phép");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(COLOR_TEXT_PRIMARY);

        JLabel lblSubtitle = new JLabel(isManager ? "Duyệt đơn nghỉ phép của toàn bộ nhân viên" : "Xem số dư ngày phép và gửi đơn xin nghỉ");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(COLOR_TEXT_SECONDARY);

        titlePanel.add(lblTitle);
        titlePanel.add(lblSubtitle);
        headerPanel.add(titlePanel, BorderLayout.WEST);

        // Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actionPanel.setBackground(COLOR_BACKGROUND);

        if (isManager) {
            JButton btnApprove = createStyledButton("Duyệt đơn ✔", COLOR_SUCCESS, Color.WHITE);
            btnApprove.setPreferredSize(new Dimension(140, 40));
            btnApprove.addActionListener(e -> updateStatus("Đã duyệt"));

            JButton btnReject = createStyledButton("Từ chối ❌", COLOR_DANGER, Color.WHITE);
            btnReject.setPreferredSize(new Dimension(140, 40));
            btnReject.addActionListener(e -> updateStatus("Từ chối"));

            actionPanel.add(btnApprove);
            actionPanel.add(btnReject);
        } else {
            JButton btnCreate = createStyledButton("+ Xin nghỉ phép", COLOR_PRIMARY, Color.WHITE);
            btnCreate.setPreferredSize(new Dimension(160, 40));
            btnCreate.addActionListener(e -> {
                if (currentEmp == null) return;
                LeaveRequestDialog dlg = new LeaveRequestDialog((Frame) SwingUtilities.getWindowAncestor(this), currentEmp.getMaNhanVien());
                dlg.setVisible(true);
                if (dlg.isSubmitted()) {
                    loadBalance();
                    loadDataToTable();
                }
            });
            actionPanel.add(btnCreate);
        }
        headerPanel.add(actionPanel, BorderLayout.EAST);
        topPanel.add(headerPanel, BorderLayout.NORTH);

        // --- STATS/BALANCE CARDS (For Employees) ---
        if (!isManager && currentEmp != null) {
            JPanel cardRow = new JPanel(new GridLayout(1, 3, 20, 0));
            cardRow.setBackground(COLOR_BACKGROUND);
            cardRow.setPreferredSize(new Dimension(0, 80));

            lblTotal = new JLabel("12 ngày", SwingConstants.CENTER);
            lblUsed = new JLabel("0 ngày", SwingConstants.CENTER);
            lblRemaining = new JLabel("12 ngày", SwingConstants.CENTER);

            cardRow.add(createStatCard("TỔNG PHÉP NĂM", lblTotal, new Color(241, 245, 249)));
            cardRow.add(createStatCard("ĐÃ DÙNG", lblUsed, new Color(254, 226, 226)));
            cardRow.add(createStatCard("CÒN LẠI", lblRemaining, new Color(220, 252, 231)));

            topPanel.add(cardRow, BorderLayout.CENTER);
        }

        // --- TABLE PANEL ---
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

        String[] columnNames = { "NHÂN VIÊN", "LOẠI NGHỈ", "TỪ NGÀY", "ĐẾN NGÀY", "SỐ NGÀY", "LÝ DO", "TRẠNG THÁI", "NGƯỜI DUYỆT" };
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

    private JPanel createStatCard(String title, JLabel lblVal, Color bg) {
        JPanel card = new JPanel(new BorderLayout(5, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(COLOR_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(COLOR_TEXT_SECONDARY);

        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblVal.setForeground(COLOR_TEXT_PRIMARY);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);
        return card;
    }

    private void loadBalance() {
        if (isManager || currentEmp == null) return;
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        QuanLyPhepModel balance = donNghiPhepDAO.getLeaveBalance(currentEmp.getMaNhanVien(), currentYear);

        lblTotal.setText(balance.getTongPhep() + " ngày");
        lblUsed.setText(balance.getDaDung() + " ngày");
        lblRemaining.setText(balance.getConLai() + " ngày");
    }

    private void loadDataToTable() {
        tableModel.setRowCount(0);
        if (isManager) {
            currentList = donNghiPhepDAO.getAllRequests();
        } else {
            if (currentEmp != null) {
                currentList = donNghiPhepDAO.getRequestsByEmployee(currentEmp.getMaNhanVien());
            } else {
                return;
            }
        }

        SimpleDateFormat dayFormat = new SimpleDateFormat("dd/MM/yyyy");

        for (DonNghiPhepModel don : currentList) {
            Object[] row = {
                    don.getHoTen() != null ? don.getHoTen() : (currentEmp != null ? currentEmp.getHoTen() : "Nhân viên #" + don.getMaNhanVien()),
                    don.getLoaiNghi(),
                    dayFormat.format(don.getTuNgay()),
                    dayFormat.format(don.getDenNgay()),
                    don.getSoNgay() + " ngày",
                    don.getLyDo(),
                    don.getTrangThai(),
                    don.getTenNguoiDuyet() != null ? don.getTenNguoiDuyet() : ""
            };
            tableModel.addRow(row);
        }
    }

    private void updateStatus(String action) {
        int selected = table.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đơn xin nghỉ phép từ danh sách!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(selected);
        DonNghiPhepModel don = currentList.get(modelRow);

        if (!"Chờ duyệt".equals(don.getTrangThai())) {
            JOptionPane.showMessageDialog(this, "Đơn nghỉ phép này đã được xử lý rồi!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
                "Bạn có chắc muốn " + action.toLowerCase() + " đơn xin nghỉ phép của nhân viên " + don.getHoTen() + " không?", 
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        if (currentEmp == null) return;

        boolean success = donNghiPhepDAO.updateRequestStatus(don.getMaDon(), action, currentEmp.getMaNhanVien());
        if (success) {
            JOptionPane.showMessageDialog(this, "Đã cập nhật trạng thái đơn nghỉ phép thành: " + action);
            loadDataToTable();
        } else {
            JOptionPane.showMessageDialog(this, "Gặp lỗi khi xử lý phê duyệt đơn. Vui lòng kiểm tra lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
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

            String lower = badgeText.toLowerCase();
            if (lower.contains("chờ duyệt")) {
                pillBgColor = new Color(254, 243, 199); // Vàng
                textColor = new Color(180, 83, 9);
            } else if (lower.contains("đã duyệt")) {
                pillBgColor = new Color(220, 252, 231); // Xanh lá
                textColor = new Color(22, 163, 74);
            } else {
                pillBgColor = new Color(254, 226, 226); // Đỏ
                textColor = new Color(220, 38, 38);
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
