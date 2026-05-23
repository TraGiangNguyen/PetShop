package is216.petshop.view;

import is216.petshop.util.DBConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class InvoicePanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private List<OrderData> orderList = new ArrayList<>();

    // Design Tokens matching the App styling
    private static final Color COLOR_SURFACE = Color.WHITE;
    private static final Color COLOR_BACKGROUND = new Color(244, 246, 249);
    private static final Color COLOR_BORDER = new Color(226, 232, 240);
    private static final Color COLOR_TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color COLOR_TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color COLOR_SELECTION = new Color(238, 242, 255);
    private static final Color COLOR_ALT_ROW = new Color(249, 250, 251);
    private static final Color COLOR_PRIMARY = new Color(79, 70, 229); // Indigo theme

    public InvoicePanel() {
        initComponents();
        loadDataFromDB();
    }

    private void initComponents() {
        this.setBackground(COLOR_BACKGROUND);
        this.setLayout(new BorderLayout(20, 20));
        this.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // --- HEADER SECTION ---
        JPanel topPanel = new JPanel(new BorderLayout(0, 15));
        topPanel.setBackground(COLOR_BACKGROUND);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_BACKGROUND);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 4));
        titlePanel.setBackground(COLOR_BACKGROUND);
        JLabel lblTitle = new JLabel("Lịch sử hóa đơn");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(COLOR_TEXT_PRIMARY);

        JLabel lblSubtitle = new JLabel("Xem danh sách hóa đơn và chi tiết đơn hàng");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(COLOR_TEXT_SECONDARY);

        titlePanel.add(lblTitle);
        titlePanel.add(lblSubtitle);
        headerPanel.add(titlePanel, BorderLayout.WEST);

        // --- FILTER PANEL ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        filterPanel.setBackground(COLOR_BACKGROUND);

        txtSearch = new JTextField(25);
        txtSearch.setPreferredSize(new Dimension(300, 36));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo Mã HĐ, Khách hàng, Nhân viên...");
        txtSearch.putClientProperty("JTextField.leadingIcon", new SearchIcon());
        txtSearch.putClientProperty("FlatLaf.style", "arc: 8; margin: 4,8,4,8");
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton btnRefresh = createStyledButton("Làm mới", Color.WHITE, COLOR_TEXT_SECONDARY);
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnRefresh.setPreferredSize(new Dimension(120, 36));
        btnRefresh.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        btnRefresh.setBorderPainted(true);

        filterPanel.add(txtSearch);
        filterPanel.add(btnRefresh);

        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.CENTER);

        // --- TABLE SECTION ---
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

        String[] columnNames = {"MÃ HÓA ĐƠN", "NGÀY TẠO", "KHÁCH HÀNG", "NHÂN VIÊN", "TỔNG TIỀN", "TRẠNG THÁI", "THAO TÁC"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only allow clicks on the action button
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

        // Set widths
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(160);
        table.getColumnModel().getColumn(4).setPreferredWidth(140);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);

        // Header Renderer
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBackground(COLOR_SURFACE);
                setForeground(COLOR_TEXT_SECONDARY);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                if (column == table.getColumnCount() - 1) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 8));
                return this;
            }
        };
        table.getTableHeader().setDefaultRenderer(headerRenderer);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, COLOR_BORDER));
        table.getTableHeader().setPreferredSize(new Dimension(0, 44));
        table.getTableHeader().setReorderingAllowed(false);

        // Default cell renderer
        DefaultTableCellRenderer defRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBackground(sel ? COLOR_SELECTION : (row % 2 == 0 ? COLOR_SURFACE : COLOR_ALT_ROW));
                setForeground(COLOR_TEXT_PRIMARY);
                setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 8));
                if (col == 0) {
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                } else {
                    setFont(new Font("Segoe UI", Font.PLAIN, 13));
                }
                return this;
            }
        };

        table.getColumnModel().getColumn(0).setCellRenderer(defRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(defRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(defRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(defRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(defRenderer);

        // Badge pill renderer for Status
        table.getColumnModel().getColumn(5).setCellRenderer(new BadgeRenderer());

        // Action view details renderer & editor
        table.getColumnModel().getColumn(6).setCellRenderer(new ActionCellRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ActionCellEditor());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(COLOR_SURFACE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Search Filter
        TableRowSorter<DefaultTableModel> rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }

            private void filter() {
                String text = txtSearch.getText().toLowerCase().trim();
                if (text.isEmpty()) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                        @Override
                        public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                            for (int i = 0; i < entry.getValueCount() - 1; i++) {
                                if (entry.getStringValue(i).toLowerCase().contains(text)) {
                                    return true;
                                }
                            }
                            return false;
                        }
                    });
                }
            }
        });

        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadDataFromDB();
        });

        this.add(topPanel, BorderLayout.NORTH);
        this.add(contentPanel, BorderLayout.CENTER);
    }

    private void loadDataFromDB() {
        tableModel.setRowCount(0);
        orderList.clear();

        String sql = "SELECT d.MADONHANG, d.NGAYTAO, d.TONGTIENTAMTINH, d.TRANGTHAI, d.GHICHU, " +
                     "       k.TENKH AS TENKH, k.SODIENTHOAI AS SDTKH, " +
                     "       n.HOTEN AS TENNV " +
                     "FROM don_hang d " +
                     "LEFT JOIN khach_hang k ON d.MAKH = k.MAKH " +
                     "LEFT JOIN nhan_vien n ON d.MANHANVIEN = n.MANHANVIEN " +
                     "ORDER BY d.NGAYTAO DESC";

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                OrderData order = new OrderData();
                order.id = rs.getInt("MADONHANG");
                order.date = rs.getTimestamp("NGAYTAO");
                order.totalAmount = rs.getLong("TONGTIENTAMTINH");
                order.status = rs.getString("TRANGTHAI");
                order.note = rs.getString("GHICHU");
                order.customerName = rs.getString("TENKH") != null ? rs.getString("TENKH") : "Khách vãng lai";
                order.customerPhone = rs.getString("SDTKH") != null ? rs.getString("SDTKH") : "N/A";
                order.employeeName = rs.getString("TENNV") != null ? rs.getString("TENNV") : "Hệ thống";

                orderList.add(order);

                Object[] row = {
                        "HD-" + order.id,
                        order.date != null ? sdf.format(order.date) : "N/A",
                        order.customerName,
                        order.employeeName,
                        String.format("%,dđ", order.totalAmount),
                        order.status != null ? order.status : "Hoàn thành",
                        "view"
                };
                tableModel.addRow(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi lấy dữ liệu hóa đơn từ CSDL!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showOrderDetailDialog(OrderData order) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, "Chi tiết hóa đơn HD-" + order.id, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new BorderLayout());
        dlg.setSize(550, 600);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        // Header Panel
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(COLOR_PRIMARY);
        pnlHeader.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel lblDlgTitle = new JLabel("HÓA ĐƠN BÁN LẺ");
        lblDlgTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblDlgTitle.setForeground(Color.WHITE);

        JLabel lblDlgSub = new JLabel("HD-" + order.id);
        lblDlgSub.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDlgSub.setForeground(new Color(199, 210, 254));

        pnlHeader.add(lblDlgTitle, BorderLayout.WEST);
        pnlHeader.add(lblDlgSub, BorderLayout.EAST);

        // Body Panel (Invoice layout)
        JPanel pnlBody = new JPanel();
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Metadata grid
        JPanel pnlMeta = new JPanel(new GridLayout(4, 2, 10, 8));
        pnlMeta.setBackground(Color.WHITE);
        pnlMeta.setAlignmentX(Component.LEFT_ALIGNMENT);

        addMetaRow(pnlMeta, "Khách hàng:", order.customerName + " (" + order.customerPhone + ")");
        addMetaRow(pnlMeta, "Nhân viên lập:", order.employeeName);
        addMetaRow(pnlMeta, "Thời gian:", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(order.date));
        addMetaRow(pnlMeta, "Trạng thái:", order.status);

        pnlBody.add(pnlMeta);
        pnlBody.add(Box.createVerticalStrut(20));

        // Items Table
        String[] columns = {"Sản phẩm", "SL", "Đơn giá", "Thành tiền"};
        DefaultTableModel modelDetails = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tblDetails = new JTable(modelDetails);
        tblDetails.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblDetails.setRowHeight(32);
        tblDetails.setShowGrid(false);
        tblDetails.setShowHorizontalLines(true);
        tblDetails.setGridColor(COLOR_BORDER);
        tblDetails.setFocusable(false);
        tblDetails.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblDetails.getTableHeader().setBackground(new Color(249, 250, 251));
        tblDetails.getTableHeader().setForeground(COLOR_TEXT_SECONDARY);

        // Load details from DB
        String sqlDetails = "SELECT c.SOLUONG, c.DONGIA, c.THANHTIEN, s.TENSANPHAM " +
                            "FROM chi_tiet_don_hang c " +
                            "JOIN san_pham s ON c.MASANPHAM = s.MASANPHAM " +
                            "WHERE c.MADONHANG = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlDetails)) {
            ps.setInt(1, order.id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modelDetails.addRow(new Object[]{
                            rs.getString("TENSANPHAM"),
                            rs.getInt("SOLUONG"),
                            String.format("%,dđ", rs.getLong("DONGIA")),
                            String.format("%,dđ", rs.getLong("THANHTIEN"))
                    });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        tblDetails.getColumnModel().getColumn(0).setPreferredWidth(220);
        tblDetails.getColumnModel().getColumn(1).setPreferredWidth(50);
        tblDetails.getColumnModel().getColumn(2).setPreferredWidth(100);
        tblDetails.getColumnModel().getColumn(3).setPreferredWidth(110);

        JScrollPane spDetails = new JScrollPane(tblDetails);
        spDetails.setPreferredSize(new Dimension(480, 220));
        spDetails.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        spDetails.getViewport().setBackground(Color.WHITE);
        spDetails.setAlignmentX(Component.LEFT_ALIGNMENT);

        pnlBody.add(spDetails);
        pnlBody.add(Box.createVerticalStrut(15));

        // Invoice Totals
        JPanel pnlTotal = new JPanel(new BorderLayout());
        pnlTotal.setBackground(Color.WHITE);
        pnlTotal.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, COLOR_PRIMARY));
        pnlTotal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        pnlTotal.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTotalLabel = new JLabel("TỔNG TIỀN THANH TOÁN:");
        lblTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotalLabel.setForeground(COLOR_TEXT_PRIMARY);

        JLabel lblTotalVal = new JLabel(String.format("%,d VNĐ", order.totalAmount));
        lblTotalVal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalVal.setForeground(new Color(220, 53, 69)); // red

        pnlTotal.add(lblTotalLabel, BorderLayout.WEST);
        pnlTotal.add(lblTotalVal, BorderLayout.EAST);
        pnlBody.add(pnlTotal);

        // Footer buttons
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        pnlFooter.setBackground(new Color(249, 250, 251));
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));

        JButton btnClose = createStyledButton("Đóng cửa sổ", COLOR_PRIMARY, Color.WHITE);
        btnClose.setPreferredSize(new Dimension(140, 36));
        btnClose.addActionListener(e -> dlg.dispose());

        pnlFooter.add(btnClose);

        dlg.add(pnlHeader, BorderLayout.NORTH);
        dlg.add(pnlBody,   BorderLayout.CENTER);
        dlg.add(pnlFooter, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void addMetaRow(JPanel parent, String label, String value) {
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblLabel.setForeground(COLOR_TEXT_SECONDARY);
        
        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblVal.setForeground(COLOR_TEXT_PRIMARY);

        parent.add(lblLabel);
        parent.add(lblVal);
    }

    // Helper to style buttons
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

    // --- INNER HELPER DATA CLASS ---
    private static class OrderData {
        int id;
        Timestamp date;
        long totalAmount;
        String status;
        String note;
        String customerName;
        String customerPhone;
        String employeeName;
    }

    // --- RENDERERS ---
    private class BadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            setBackground(sel ? COLOR_SELECTION : (row % 2 == 0 ? COLOR_SURFACE : COLOR_ALT_ROW));
            setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 8));
            setText("");
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            String text = "Hoàn thành"; // Default
            
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color pillBgColor = new Color(220, 252, 231); // Soft green
            Color textColor = new Color(22, 163, 74);     // Green

            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(text);
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
            g2.drawString(text, x + padX, y + ((pillHeight - textHeight) / 2) + fm.getAscent());

            g2.dispose();
        }
    }

    // View detail action renderer
    private class ActionCellRenderer extends JPanel implements TableCellRenderer {
        public ActionCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 6, 8));
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            removeAll();
            setBackground(sel ? COLOR_SELECTION : (row % 2 == 0 ? COLOR_SURFACE : COLOR_ALT_ROW));
            JButton b = new JButton("Chi tiết");
            b.setFont(new Font("Segoe UI", Font.BOLD, 12));
            b.setBackground(COLOR_PRIMARY);
            b.setForeground(Color.WHITE);
            b.setPreferredSize(new Dimension(80, 28));
            b.setFocusPainted(false);
            b.setBorderPainted(false);
            b.putClientProperty("FlatLaf.style", "arc: 6");
            add(b);
            return this;
        }
    }

    // View detail action editor
    private class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 8));
        private int editingRow;

        public ActionCellEditor() {
            panel.setOpaque(true);
            panel.setBackground(COLOR_SELECTION);
            JButton b = new JButton("Chi tiết");
            b.setFont(new Font("Segoe UI", Font.BOLD, 12));
            b.setBackground(COLOR_PRIMARY);
            b.setForeground(Color.WHITE);
            b.setPreferredSize(new Dimension(80, 28));
            b.setFocusPainted(false);
            b.setBorderPainted(false);
            b.putClientProperty("FlatLaf.style", "arc: 6");
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            b.addActionListener(e -> {
                fireEditingStopped();
                if (editingRow >= 0 && editingRow < orderList.size()) {
                    showOrderDetailDialog(orderList.get(editingRow));
                }
            });
            panel.add(b);
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean sel, int row, int col) {
            editingRow = table.convertRowIndexToModel(row);
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "view";
        }
    }

    // Custom Search Icon
    private static class SearchIcon implements Icon {
        @Override public int getIconWidth() { return 20; }
        @Override public int getIconHeight() { return 20; }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(new Color(148, 163, 184));
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawOval(4, 4, 10, 10);
            g2.drawLine(13, 13, 16, 16);
            g2.dispose();
        }
    }
}
