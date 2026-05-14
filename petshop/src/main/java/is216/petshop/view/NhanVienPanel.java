package is216.petshop.view;

import is216.petshop.dao.NhanVienDAO;
import is216.petshop.model.NhanVienModel;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

import java.util.List;

import java.awt.geom.RoundRectangle2D;

public class NhanVienPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private NhanVienDAO nhanVienDAO;
    private List<NhanVienModel> currentList;

    // Định nghĩa các màu sắc dùng chung theo phong cách AirlineGUI
    private static final Color COLOR_SURFACE = Color.WHITE;
    private static final Color COLOR_BACKGROUND = new Color(244, 246, 249);
    private static final Color COLOR_BORDER = new Color(226, 232, 240);
    private static final Color COLOR_TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color COLOR_TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color COLOR_SELECTION = new Color(219, 234, 254);
    private static final Color COLOR_ALT_ROW = new Color(249, 250, 251);
    private static final Color COLOR_PRIMARY = new Color(108, 93, 211); // Trùng với màu nút Thêm

    public NhanVienPanel() {
        nhanVienDAO = new NhanVienDAO();
        initComponents();
        loadDataToTable();
    }

    private void initComponents() {
        this.setBackground(COLOR_BACKGROUND);
        this.setLayout(new BorderLayout(20, 20));
        this.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // --- PHẦN HEADER ---
        JPanel topPanel = new JPanel(new BorderLayout(0, 15));
        topPanel.setBackground(COLOR_BACKGROUND);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_BACKGROUND);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 4));
        titlePanel.setBackground(COLOR_BACKGROUND);
        JLabel lblTitle = new JLabel("Quản lý nhân viên");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(COLOR_TEXT_PRIMARY);

        JLabel lblSubtitle = new JLabel("Quản lý danh sách nhân viên cửa hàng");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(COLOR_TEXT_SECONDARY);

        titlePanel.add(lblTitle);
        titlePanel.add(lblSubtitle);

        JButton btnAdd = createStyledButton("+ Thêm nhân viên", COLOR_PRIMARY, Color.WHITE);
        btnAdd.setPreferredSize(new Dimension(160, 40));
        btnAdd.addActionListener(e -> {
            NhanVienDialog dialog = new NhanVienDialog((Frame) SwingUtilities.getWindowAncestor(this), true, this,
                    null);
            dialog.setVisible(true);
        });

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(btnAdd, BorderLayout.EAST);

        // --- PHẦN BỘ LỌC (FILTER PANEL) ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        filterPanel.setBackground(COLOR_BACKGROUND);

        JTextField txtSearch = new JTextField(25);
        txtSearch.setPreferredSize(new Dimension(250, 36));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm kiếm theo tên, sđt, email...");
        txtSearch.putClientProperty("JTextField.leadingIcon", new SearchIcon());
        txtSearch.putClientProperty("FlatLaf.style", "arc: 8; margin: 4,8,4,8");
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        String[] sortOptions = { "Sắp xếp mặc định", "Tên (A-Z)", "Tên (Z-A)", "Lương (Thấp - Cao)",
                "Lương (Cao - Thấp)" };
        JComboBox<String> cbSort = new JComboBox<>(sortOptions);
        cbSort.setPreferredSize(new Dimension(180, 36));
        cbSort.putClientProperty("FlatLaf.style", "arc: 8");
        cbSort.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton btnRefresh = createStyledButton("Làm mới", Color.WHITE, COLOR_TEXT_SECONDARY);
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnRefresh.setPreferredSize(new Dimension(120, 36));
        btnRefresh.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        btnRefresh.setBorderPainted(true);

        filterPanel.add(txtSearch);
        filterPanel.add(cbSort);
        filterPanel.add(btnRefresh);

        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.CENTER);

        // --- PHẦN BẢNG (JTABLE) ---
        // Sử dụng JPanel tùy chỉnh để bo góc (Clip children)
        JPanel contentPanel = new JPanel(new BorderLayout()) {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Tạo hình dáng bo góc cho Clip
                RoundRectangle2D.Float roundRect = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setClip(roundRect);
                super.paint(g2);

                // Vẽ đường viền bo góc ở trên cùng
                g2.setClip(null); // Bỏ clip để vẽ border chính xác ở mép
                g2.setColor(COLOR_BORDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1.5f, getHeight() - 1.5f, 16, 16));
                g2.dispose();
            }
        };
        contentPanel.setOpaque(false);
        contentPanel.setBackground(COLOR_SURFACE);

        String[] columnNames = { "HỌ TÊN", "CHỨC VỤ", "SỐ ĐIỆN THOẠI", "EMAIL", "LƯƠNG", "NGÀY VÀO LÀM", "TRẠNG THÁI",
                "THAO TÁC" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Chỉ cho phép edit cột thao tác để click nút
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(52); // Chiều cao hàng chuẩn theo AirlineGUI
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setGridColor(COLOR_BORDER);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(COLOR_SELECTION);
        table.setSelectionForeground(COLOR_TEXT_PRIMARY);
        table.setFocusable(false);

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
                    setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                    setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 8));
                }
                return this;
            }
        };

        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(headerRenderer);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, COLOR_BORDER));
        header.setPreferredSize(new Dimension(0, 44));
        header.setReorderingAllowed(false);

        // Set độ rộng cột
        table.getColumnModel().getColumn(0).setPreferredWidth(160);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);
        table.getColumnModel().getColumn(6).setPreferredWidth(130);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);

        // Default Renderer cho cột thường
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

                if (col == 0)
                    setFont(new Font("Segoe UI", Font.BOLD, 13)); // In đậm Họ Tên
                else
                    setFont(new Font("Segoe UI", Font.PLAIN, 13));

                return this;
            }
        };

        table.getColumnModel().getColumn(0).setCellRenderer(defRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(defRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(defRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(defRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(defRenderer);

        // Renderer cho cột Chức vụ & Trạng thái (Pill Badge)
        table.getColumnModel().getColumn(1).setCellRenderer(new BadgeRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new BadgeRenderer());

        // Renderer & Editor cho cột Thao tác
        table.getColumnModel().getColumn(7).setCellRenderer(new ActionCellRenderer());
        table.getColumnModel().getColumn(7).setCellEditor(new ActionCellEditor());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(COLOR_SURFACE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // --- CÀI ĐẶT TÌM KIẾM & SẮP XẾP ---
        TableRowSorter<DefaultTableModel> rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            private void filter() {
                String text = txtSearch.getText().toLowerCase();
                if (text.trim().isEmpty()) {
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

        cbSort.addActionListener(e -> {
            int idx = cbSort.getSelectedIndex();
            switch (idx) {
                case 0: // Mặc định
                    rowSorter.setSortKeys(null);
                    break;
                case 1: // Tên A-Z
                    rowSorter.setSortKeys(java.util.List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
                    break;
                case 2: // Tên Z-A
                    rowSorter.setSortKeys(java.util.List.of(new RowSorter.SortKey(0, SortOrder.DESCENDING)));
                    break;
                case 3: // Lương Thấp - Cao
                    rowSorter.setComparator(4, (o1, o2) -> {
                        Long l1 = parseCurrency(o1.toString());
                        Long l2 = parseCurrency(o2.toString());
                        return l1.compareTo(l2);
                    });
                    rowSorter.setSortKeys(java.util.List.of(new RowSorter.SortKey(4, SortOrder.ASCENDING)));
                    break;
                case 4: // Lương Cao - Thấp
                    rowSorter.setComparator(4, (o1, o2) -> {
                        Long l1 = parseCurrency(o1.toString());
                        Long l2 = parseCurrency(o2.toString());
                        return l1.compareTo(l2);
                    });
                    rowSorter.setSortKeys(java.util.List.of(new RowSorter.SortKey(4, SortOrder.DESCENDING)));
                    break;
            }
        });

        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            cbSort.setSelectedIndex(0);
            refreshData();
        });

        this.add(topPanel, BorderLayout.NORTH);
        this.add(contentPanel, BorderLayout.CENTER);
    }

    public void refreshData() {
        loadDataToTable();
    }

    private void loadDataToTable() {
        tableModel.setRowCount(0);
        currentList = nhanVienDAO.getDanhSachNhanVien();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        java.text.DecimalFormat formatter = new java.text.DecimalFormat("###,###,###đ");

        for (NhanVienModel nv : currentList) {
            Object[] row = {
                    nv.getHoTen(),
                    nv.getChucVu(),
                    nv.getSdt(),
                    nv.getEmail(),
                    formatter.format(nv.getLuong()),
                    nv.getNgayVaoLam() != null ? sdf.format(nv.getNgayVaoLam()) : "",
                    nv.getTrangThai(),
                    "actions" // Chuỗi giả để trigger Editor
            };
            tableModel.addRow(row);
        }
    }

    // =====================================================================
    // UI HELPERS
    // =====================================================================

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("FlatLaf.style", "arc: 12"); // Tuỳ chọn nếu dùng FlatLaf
        return btn;
    }

    private Long parseCurrency(String str) {
        try {
            return Long.parseLong(str.replaceAll("[^0-9]", ""));
        } catch (Exception ex) {
            return 0L;
        }
    }

    // =====================================================================
    // RENDERERS & EDITORS
    // =====================================================================

    /**
     * Renderer vẽ thẻ (Pill) bo góc
     */
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

            String textLower = badgeText.toLowerCase();
            if (textLower.contains("quản lý") || textLower.contains("admin")) {
                pillBgColor = new Color(239, 236, 255); // Tím nhạt
                textColor = new Color(90, 69, 209);
            } else if (textLower.contains("bán hàng")) {
                pillBgColor = new Color(229, 238, 255); // Xanh dương nhạt
                textColor = new Color(65, 105, 225);
            } else if (textLower.contains("đang làm việc")) {
                pillBgColor = new Color(220, 252, 231); // Xanh lá nhạt
                textColor = new Color(22, 163, 74);
            } else if (textLower.contains("nghỉ việc")) {
                pillBgColor = new Color(254, 226, 226); // Đỏ nhạt
                textColor = new Color(220, 38, 38);
            } else {
                pillBgColor = new Color(241, 245, 249);
                textColor = Color.DARK_GRAY;
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

    // --- ICONS & ACTION RENDERER TỪ AIRLINEGUI ---

    private static class EditIcon implements Icon {
        @Override
        public int getIconWidth() {
            return 20;
        }

        @Override
        public int getIconHeight() {
            return 20;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(new Color(108, 93, 211)); // Màu tím tương đồng nút Thêm
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int[] px = { 5, 12, 15, 8 };
            int[] py = { 15, 8, 11, 18 };
            g2.drawPolygon(px, py, 4);
            g2.drawLine(5, 15, 3, 17);
            g2.drawLine(3, 17, 5, 17);
            g2.drawLine(5, 17, 8, 18);
            g2.dispose();
        }
    }

    private static class DeleteIcon implements Icon {
        @Override
        public int getIconWidth() {
            return 20;
        }

        @Override
        public int getIconHeight() {
            return 20;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(new Color(239, 68, 68)); // Đỏ
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawRect(5, 7, 10, 10);
            g2.drawLine(3, 7, 17, 7);
            g2.drawLine(8, 4, 12, 4);
            g2.drawLine(8, 10, 8, 14);
            g2.drawLine(12, 10, 12, 14);
            g2.dispose();
        }
    }

    private static class SearchIcon implements Icon {
        @Override
        public int getIconWidth() {
            return 20;
        }

        @Override
        public int getIconHeight() {
            return 20;
        }

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

    private class ActionCellRenderer extends JPanel implements TableCellRenderer {
        public ActionCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 6, 8));
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            removeAll();
            setBackground(sel ? COLOR_SELECTION : (row % 2 == 0 ? COLOR_SURFACE : COLOR_ALT_ROW));
            add(makeBtn(true));
            add(makeBtn(false));
            return this;
        }

        private JButton makeBtn(boolean isEdit) {
            JButton b = new JButton(isEdit ? new EditIcon() : new DeleteIcon());
            b.setPreferredSize(new Dimension(32, 32));
            b.setBorderPainted(false);
            b.setContentAreaFilled(false);
            b.setFocusPainted(false);
            return b;
        }
    }

    private class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 8));
        private int editingRow;

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean sel, int row, int col) {
            // Chuyển đổi chỉ số hàng trên giao diện sang chỉ số hàng trong Model
            editingRow = table.convertRowIndexToModel(row);
            panel.removeAll();
            panel.setBackground(COLOR_SELECTION);

            JButton btnEdit = makeBtn(true);
            btnEdit.addActionListener(e -> {
                fireEditingStopped();
                if (currentList != null && editingRow >= 0 && editingRow < currentList.size()) {
                    NhanVienModel nv = currentList.get(editingRow);
                    NhanVienDialog dialog = new NhanVienDialog(
                            (Frame) SwingUtilities.getWindowAncestor(NhanVienPanel.this), true, NhanVienPanel.this, nv);
                    dialog.setVisible(true);
                }
            });

            JButton btnDel = makeBtn(false);
            btnDel.addActionListener(e -> {
                fireEditingStopped();
                if (currentList != null && editingRow >= 0 && editingRow < currentList.size()) {
                    NhanVienModel nv = currentList.get(editingRow);
                    int confirm = JOptionPane.showConfirmDialog(NhanVienPanel.this,
                            "Bạn có chắc muốn cho nhân viên " + nv.getHoTen() + " nghỉ việc không?",
                            "Xác nhận", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        boolean success = nhanVienDAO.deleteNhanVien(nv.getMaNhanVien());
                        if (success) {
                            JOptionPane.showMessageDialog(NhanVienPanel.this, "Đã chuyển trạng thái nghỉ việc!");
                            refreshData();
                        } else {
                            JOptionPane.showMessageDialog(NhanVienPanel.this, "Lỗi khi thao tác!", "Lỗi",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });

            panel.add(btnEdit);
            panel.add(btnDel);
            return panel;
        }

        private JButton makeBtn(boolean isEdit) {
            JButton b = new JButton(isEdit ? new EditIcon() : new DeleteIcon());
            b.setPreferredSize(new Dimension(32, 32));
            b.setBorderPainted(false);
            b.setContentAreaFilled(false);
            b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return b;
        }

        @Override
        public Object getCellEditorValue() {
            return "actions";
        }
    }
}