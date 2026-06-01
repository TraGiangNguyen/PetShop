package is216.petshop.view;

import is216.petshop.Product.Product;
import is216.petshop.dao.StockDAO;
import is216.petshop.dao.UserDAO;
import is216.petshop.model.NhanVienModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PurchasePanel extends JPanel {

    private static final Color COLOR_BG = new Color(244, 246, 249);
    private static final Color COLOR_SURFACE = Color.WHITE;
    private static final Color COLOR_PRIMARY = new Color(108, 93, 211);
    private static final Color COLOR_PRIMARY_HOVER = new Color(91, 78, 180);
    private static final Color COLOR_BORDER = new Color(226, 232, 240);
    private static final Color COLOR_TEXT_PRI = new Color(15, 23, 42);
    private static final Color COLOR_TEXT_SEC = new Color(100, 116, 139);
    private static final Color COLOR_DANGER_BG = new Color(254, 226, 226);
    private static final Color COLOR_DANGER_FG = new Color(239, 68, 68);
    private static final Color COLOR_SUCCESS_BG = new Color(220, 252, 231);
    private static final Color COLOR_SUCCESS_FG = new Color(34, 197, 94);
    private static final Color COLOR_ORANGE_BG = new Color(254, 243, 199);
    private static final Color COLOR_ORANGE_FG = new Color(217, 119, 6);

    private final StockDAO stockDAO = new StockDAO();
    private final String currentUsername;
    private NhanVienModel currentEmployee = null;

    // Tab Selector Buttons
    private JButton btnTabOrders;
    private JButton btnTabSuppliers;
    private JButton btnHeaderAction;

    private CardLayout cardLayout;
    private JPanel pnlCards;

    // Tab 1: Orders Components
    private JTable tblOrders;
    private DefaultTableModel modelOrders;
    private JLabel lblPendingCount;
    private JLabel lblTotalSpent;
    private List<StockDAO.PurchaseOrder> purchaseOrders = new ArrayList<>();

    // Tab 2: Suppliers Components
    private JTable tblSuppliers;
    private DefaultTableModel modelSuppliers;
    private JTextField txtSearchSuppliers;
    private List<StockDAO.Supplier> supplierList = new ArrayList<>();

    public PurchasePanel(String username) {
        this.currentUsername = username;
        
        UserDAO uDao = new UserDAO();
        this.currentEmployee = uDao.getNhanVienByUsername(username);

        initComponents();
        loadOrdersData();
        loadSuppliersData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(20, 20));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(25, 30, 25, 30));

        // ─── HEADER ───────────────────────────────────────────────────────────
        JPanel pnlHeader = new JPanel(new BorderLayout(0, 16));
        pnlHeader.setOpaque(false);

        JPanel pnlTopHeader = new JPanel(new BorderLayout());
        pnlTopHeader.setOpaque(false);

        JPanel pnlTitle = new JPanel(new GridLayout(2, 1, 0, 4));
        pnlTitle.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản lý mua hàng & Đối tác");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(COLOR_TEXT_PRI);

        JLabel lblSubtitle = new JLabel("Lập đơn mua hàng từ nhà cung cấp, quản lý thông tin nhà cung cấp và đối soát");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(COLOR_TEXT_SEC);

        pnlTitle.add(lblTitle);
        pnlTitle.add(lblSubtitle);
        pnlTopHeader.add(pnlTitle, BorderLayout.WEST);

        // Header Action Button (Dynamic)
        btnHeaderAction = createStyledButton("+ Tạo đơn mua hàng mới", COLOR_PRIMARY, Color.WHITE);
        btnHeaderAction.setPreferredSize(new Dimension(220, 42));
        pnlTopHeader.add(btnHeaderAction, BorderLayout.EAST);
        pnlHeader.add(pnlTopHeader, BorderLayout.NORTH);

        // Tab selection bar
        JPanel pnlTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlTabs.setOpaque(false);
        pnlTabs.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));

        btnTabOrders = createTabButton("Đơn mua hàng");
        btnTabSuppliers = createTabButton("Nhà cung cấp");

        pnlTabs.add(btnTabOrders);
        pnlTabs.add(Box.createHorizontalStrut(8));
        pnlTabs.add(btnTabSuppliers);
        pnlHeader.add(pnlTabs, BorderLayout.SOUTH);

        add(pnlHeader, BorderLayout.NORTH);

        // ─── CENTER BODY (CARD PANEL) ─────────────────────────────────────────
        cardLayout = new CardLayout();
        pnlCards = new JPanel(cardLayout);
        pnlCards.setOpaque(false);

        // 1. ORDERS CARD VIEW
        JPanel pnlOrdersView = new JPanel(new BorderLayout(0, 20));
        pnlOrdersView.setOpaque(false);

        // Orders metrics
        JPanel pnlMetrics = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlMetrics.setOpaque(false);
        pnlMetrics.setPreferredSize(new Dimension(0, 85));

        JPanel cardPending = createMetricCard("ĐƠN HÀNG CHỜ NHẬP KHO", "0 đơn chờ", COLOR_ORANGE_FG);
        JPanel cardTotal = createMetricCard("TỔNG CHI TIÊU MUA HÀNG", "0đ", COLOR_PRIMARY);

        pnlMetrics.add(cardPending);
        pnlMetrics.add(cardTotal);
        pnlOrdersView.add(pnlMetrics, BorderLayout.NORTH);

        // Table container
        JPanel pnlTableCard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_SURFACE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(COLOR_BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        pnlTableCard.setOpaque(false);
        pnlTableCard.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] columns = {"Mã đơn mua", "Ngày lập", "Nhà cung cấp", "Tổng tiền", "Trạng thái", "Hành động"};
        modelOrders = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 5;
            }
        };

        tblOrders = new JTable(modelOrders);
        tblOrders.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblOrders.setRowHeight(40);
        tblOrders.setShowVerticalLines(false);
        tblOrders.setGridColor(COLOR_BG);
        tblOrders.setBackground(COLOR_SURFACE);
        tblOrders.setSelectionBackground(new Color(243, 241, 255));
        tblOrders.setSelectionForeground(COLOR_TEXT_PRI);
        tblOrders.setFocusable(false);

        JTableHeader header = tblOrders.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(COLOR_SURFACE);
        header.setForeground(COLOR_TEXT_SEC);
        header.setPreferredSize(new Dimension(0, 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
        header.setReorderingAllowed(false);

        tblOrders.getColumnModel().getColumn(0).setPreferredWidth(80);  // ID
        tblOrders.getColumnModel().getColumn(1).setPreferredWidth(140); // Date
        tblOrders.getColumnModel().getColumn(2).setPreferredWidth(260); // Supplier
        tblOrders.getColumnModel().getColumn(3).setPreferredWidth(130); // Total
        tblOrders.getColumnModel().getColumn(4).setPreferredWidth(130); // Status Pill
        tblOrders.getColumnModel().getColumn(5).setPreferredWidth(100); // Action Button

        tblOrders.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
                super.getTableCellRendererComponent(table, val, isSel, hasF, r, c);
                setFont(new Font("Segoe UI", Font.BOLD, 14));
                setForeground(COLOR_TEXT_PRI);
                setHorizontalAlignment(SwingConstants.RIGHT);
                setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
                return this;
            }
        });

        tblOrders.getColumnModel().getColumn(4).setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
                String status = val != null ? val.toString() : "Chờ nhập kho";
                boolean isCompleted = status.equals("Đã nhập kho");

                JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8)) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        Color bg = isCompleted ? COLOR_SUCCESS_BG : COLOR_ORANGE_BG;
                        g2.setColor(bg);
                        g2.fill(new RoundRectangle2D.Float(10, 4, getWidth() - 20, getHeight() - 8, 12, 12));
                        g2.dispose();
                    }
                };
                pnl.setOpaque(true);
                pnl.setBackground(isSel ? table.getSelectionBackground() : COLOR_SURFACE);

                JLabel lbl = new JLabel(status);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setForeground(isCompleted ? COLOR_SUCCESS_FG : COLOR_ORANGE_FG);
                pnl.add(lbl);
                return pnl;
            }
        });

        tblOrders.getColumnModel().getColumn(5).setCellRenderer(new StockPanel.ButtonRenderer("Chi tiết", COLOR_PRIMARY, Color.WHITE, 80));
        tblOrders.getColumnModel().getColumn(5).setCellEditor(new StockPanel.ButtonEditor(new JCheckBox(), "Chi tiết", COLOR_PRIMARY, Color.WHITE, 80, this::showOrderDetailDialog));

        JScrollPane scrollPane = new JScrollPane(tblOrders);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(COLOR_SURFACE);
        pnlTableCard.add(scrollPane, BorderLayout.CENTER);
        pnlOrdersView.add(pnlTableCard, BorderLayout.CENTER);

        pnlCards.add(pOrdersContainer(pnlOrdersView), "ORDERS");

        // 2. SUPPLIERS CARD VIEW
        JPanel pnlSuppliersView = new JPanel(new BorderLayout(0, 16));
        pnlSuppliersView.setOpaque(false);

        // Search panel
        JPanel pnlSearchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlSearchBox.setOpaque(false);

        txtSearchSuppliers = new JTextField();
        txtSearchSuppliers.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearchSuppliers.setPreferredSize(new Dimension(280, 36));
        txtSearchSuppliers.putClientProperty("JTextField.placeholderText", "Tìm nhà cung cấp (tên, sđt)...");
        txtSearchSuppliers.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(0, 12, 0, 12)
        ));
        txtSearchSuppliers.setBackground(Color.WHITE);
        
        JButton btnSearch = createStyledButton("Tìm kiếm", COLOR_PRIMARY, Color.WHITE);
        btnSearch.setPreferredSize(new Dimension(95, 36));
        btnSearch.addActionListener(e -> doSearchSuppliers());

        JButton btnRefresh = createStyledButton("Làm mới", new Color(241, 245, 249), COLOR_TEXT_PRI);
        btnRefresh.setPreferredSize(new Dimension(95, 36));
        btnRefresh.addActionListener(e -> {
            txtSearchSuppliers.setText("");
            loadSuppliersData();
        });

        pnlSearchBox.add(txtSearchSuppliers);
        pnlSearchBox.add(btnSearch);
        pnlSearchBox.add(btnRefresh);
        pnlSuppliersView.add(pnlSearchBox, BorderLayout.NORTH);

        // Table card
        JPanel pnlSupplierTableCard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_SURFACE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(COLOR_BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        pnlSupplierTableCard.setOpaque(false);
        pnlSupplierTableCard.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] colsSuppliers = {"Mã NCC", "Tên nhà cung cấp", "Liên hệ", "Địa chỉ", "MST / Điều khoản", "Ghi chú", "Thao tác"};
        modelSuppliers = new DefaultTableModel(colsSuppliers, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 6; // Action column is editable
            }
        };

        tblSuppliers = new JTable(modelSuppliers);
        tblSuppliers.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblSuppliers.setRowHeight(75);
        tblSuppliers.setShowVerticalLines(false);
        tblSuppliers.setGridColor(COLOR_BG);
        tblSuppliers.setBackground(COLOR_SURFACE);
        tblSuppliers.setSelectionBackground(new Color(243, 241, 255));
        tblSuppliers.setSelectionForeground(COLOR_TEXT_PRI);
        tblSuppliers.setFocusable(false);

        JTableHeader hSuppliers = tblSuppliers.getTableHeader();
        hSuppliers.setFont(new Font("Segoe UI", Font.BOLD, 13));
        hSuppliers.setBackground(COLOR_SURFACE);
        hSuppliers.setForeground(COLOR_TEXT_SEC);
        hSuppliers.setPreferredSize(new Dimension(0, 42));
        hSuppliers.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
        hSuppliers.setReorderingAllowed(false);

        tblSuppliers.getColumnModel().getColumn(0).setPreferredWidth(60);  // ID
        tblSuppliers.getColumnModel().getColumn(1).setPreferredWidth(180); // Name
        tblSuppliers.getColumnModel().getColumn(2).setPreferredWidth(170); // Contact
        tblSuppliers.getColumnModel().getColumn(3).setPreferredWidth(180); // Address
        tblSuppliers.getColumnModel().getColumn(4).setPreferredWidth(160); // Tax/Terms
        tblSuppliers.getColumnModel().getColumn(5).setPreferredWidth(90);  // Notes
        tblSuppliers.getColumnModel().getColumn(6).setPreferredWidth(150); // Actions

        // Custom Cell Renderers for suppliers
        tblSuppliers.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
                super.getTableCellRendererComponent(table, val, isSel, hasF, r, c);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setForeground(COLOR_TEXT_SEC);
                setHorizontalAlignment(SwingConstants.CENTER);
                return this;
            }
        });

        tblSuppliers.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
                super.getTableCellRendererComponent(table, val, isSel, hasF, r, c);
                setFont(new Font("Segoe UI", Font.BOLD, 14));
                setForeground(COLOR_TEXT_PRI);
                return this;
            }
        });

        // Contact details layout
        tblSuppliers.getColumnModel().getColumn(2).setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
                StockDAO.Supplier s = (StockDAO.Supplier) val;
                JPanel pnl = new JPanel();
                pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
                pnl.setBackground(isSel ? table.getSelectionBackground() : COLOR_SURFACE);
                pnl.setBorder(new EmptyBorder(12, 10, 12, 10));

                JLabel lblPhone = new JLabel(s != null ? s.phone : "—");
                lblPhone.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lblPhone.setForeground(COLOR_TEXT_PRI);

                JLabel lblEmail = new JLabel(s != null && s.email != null && !s.email.isEmpty() ? s.email : "—");
                lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lblEmail.setForeground(COLOR_TEXT_SEC);

                pnl.add(lblPhone);
                pnl.add(Box.createVerticalStrut(3));
                pnl.add(lblEmail);
                return pnl;
            }
        });

        tblSuppliers.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
                super.getTableCellRendererComponent(table, val, isSel, hasF, r, c);
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                setForeground(COLOR_TEXT_PRI);
                return this;
            }
        });

        // Tax & Terms
        tblSuppliers.getColumnModel().getColumn(4).setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
                StockDAO.Supplier s = (StockDAO.Supplier) val;
                JPanel pnl = new JPanel();
                pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
                pnl.setBackground(isSel ? table.getSelectionBackground() : COLOR_SURFACE);
                pnl.setBorder(new EmptyBorder(12, 10, 12, 10));

                JLabel lblTax = new JLabel("MST: " + (s != null && s.taxCode != null ? s.taxCode : "—"));
                lblTax.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lblTax.setForeground(COLOR_TEXT_PRI);

                JLabel lblTerms = new JLabel("ĐK: " + (s != null && s.paymentTerms != null ? s.paymentTerms : "—"));
                lblTerms.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                lblTerms.setForeground(COLOR_TEXT_SEC);

                pnl.add(lblTax);
                pnl.add(Box.createVerticalStrut(3));
                pnl.add(lblTerms);
                return pnl;
            }
        });

        tblSuppliers.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
                super.getTableCellRendererComponent(table, val, isSel, hasF, r, c);
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                setForeground(COLOR_TEXT_SEC);
                return this;
            }
        });

        // Supplier custom Edit / Delete row action buttons
        tblSuppliers.getColumnModel().getColumn(6).setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
                JPanel pnl = new JPanel(new GridBagLayout());
                pnl.setOpaque(true);
                pnl.setBackground(isSel ? table.getSelectionBackground() : COLOR_SURFACE);

                JButton btnEdit = new JButton("Sửa");
                btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 12));
                btnEdit.setBackground(COLOR_PRIMARY);
                btnEdit.setForeground(Color.WHITE);
                btnEdit.setPreferredSize(new Dimension(58, 28));
                btnEdit.putClientProperty("FlatLaf.style", "arc: 6");

                JButton btnDel = new JButton("Xóa");
                btnDel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                btnDel.setBackground(COLOR_DANGER_FG);
                btnDel.setForeground(Color.WHITE);
                btnDel.setPreferredSize(new Dimension(58, 28));
                btnDel.putClientProperty("FlatLaf.style", "arc: 6");

                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(0, 4, 0, 4);
                pnl.add(btnEdit, gbc);
                pnl.add(btnDel, gbc);
                return pnl;
            }
        });

        // Supplier row actions editor
        tblSuppliers.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            private JPanel pnl;
            private JButton btnEdit;
            private JButton btnDel;
            private int currentRow = -1;

            {
                pnl = new JPanel(new GridBagLayout());
                pnl.setOpaque(true);

                btnEdit = new JButton("Sửa");
                btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 12));
                btnEdit.setBackground(COLOR_PRIMARY);
                btnEdit.setForeground(Color.WHITE);
                btnEdit.setPreferredSize(new Dimension(58, 28));
                btnEdit.putClientProperty("FlatLaf.style", "arc: 6");
                btnEdit.addActionListener(e -> {
                    fireEditingStopped();
                    if (currentRow >= 0) {
                        StockDAO.Supplier s = supplierList.get(currentRow);
                        showSupplierDialog(s);
                    }
                });

                btnDel = new JButton("Xóa");
                btnDel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                btnDel.setBackground(COLOR_DANGER_FG);
                btnDel.setForeground(Color.WHITE);
                btnDel.setPreferredSize(new Dimension(58, 28));
                btnDel.putClientProperty("FlatLaf.style", "arc: 6");
                btnDel.addActionListener(e -> {
                    fireEditingStopped();
                    if (currentRow >= 0) {
                        StockDAO.Supplier s = supplierList.get(currentRow);
                        int opt = JOptionPane.showConfirmDialog(PurchasePanel.this, 
                                "Bạn có chắc chắn muốn xóa nhà cung cấp " + s.name + "?", 
                                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (opt == JOptionPane.YES_OPTION) {
                            boolean ok = stockDAO.deleteSupplier(s.id);
                            if (ok) {
                                JOptionPane.showMessageDialog(PurchasePanel.this, "Đã xóa nhà cung cấp thành công!");
                                loadSuppliersData();
                            } else {
                                JOptionPane.showMessageDialog(PurchasePanel.this, "Không thể xóa nhà cung cấp do có đơn hàng liên kết!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                });

                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(0, 4, 0, 4);
                pnl.add(btnEdit, gbc);
                pnl.add(btnDel, gbc);
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object val, boolean isSel, int r, int c) {
                this.currentRow = r;
                pnl.setBackground(table.getSelectionBackground());
                return pnl;
            }

            @Override
            public Object getCellEditorValue() {
                return "";
            }
        });

        JScrollPane spSuppliers = new JScrollPane(tblSuppliers);
        spSuppliers.setBorder(BorderFactory.createEmptyBorder());
        spSuppliers.getViewport().setBackground(COLOR_SURFACE);
        pnlSupplierTableCard.add(spSuppliers, BorderLayout.CENTER);
        pnlSuppliersView.add(pnlSupplierTableCard, BorderLayout.CENTER);

        pnlCards.add(pnlSuppliersView, "SUPPLIERS");

        add(pnlCards, BorderLayout.CENTER);

        // Tab selection listeners
        btnTabOrders.addActionListener(e -> {
            cardLayout.show(pnlCards, "ORDERS");
            updateTabs(btnTabOrders);
        });

        btnTabSuppliers.addActionListener(e -> {
            cardLayout.show(pnlCards, "SUPPLIERS");
            updateTabs(btnTabSuppliers);
        });

        // Set default active tab
        updateTabs(btnTabOrders);
    }

    private JButton createTabButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(COLOR_TEXT_SEC);
        btn.setBackground(COLOR_BG);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(0, 0, 0, 0)),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        
        return btn;
    }

    private void updateTabs(JButton activeBtn) {
        JButton[] buttons = {btnTabOrders, btnTabSuppliers};
        for (JButton btn : buttons) {
            if (btn == activeBtn) {
                btn.setForeground(COLOR_PRIMARY);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 3, 0, COLOR_PRIMARY),
                        BorderFactory.createEmptyBorder(8, 16, 8, 16)
                ));
            } else {
                btn.setForeground(COLOR_TEXT_SEC);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(0, 0, 0, 0)),
                        BorderFactory.createEmptyBorder(8, 16, 8, 16)
                ));
            }
        }

        // Change header action button behavior dynamically
        if (btnHeaderAction != null) {
            // Remove previous action listeners
            for (java.awt.event.ActionListener al : btnHeaderAction.getActionListeners()) {
                btnHeaderAction.removeActionListener(al);
            }

            if (activeBtn == btnTabOrders) {
                btnHeaderAction.setText("+ Tạo đơn mua hàng mới");
                btnHeaderAction.addActionListener(e -> showCreatePurchaseOrderDialog());
            } else {
                btnHeaderAction.setText("+ Thêm nhà cung cấp");
                btnHeaderAction.addActionListener(e -> showSupplierDialog(null));
            }
        }
    }

    private JPanel pOrdersContainer(JPanel view) {
        return view;
    }

    // ─── LOAD DATA CONTROLLER METHODS ────────────────────────────────────
    public void loadOrdersData() {
        purchaseOrders = stockDAO.getAllPurchaseOrders();
        renderTable(purchaseOrders);
    }

    private void loadSuppliersData() {
        supplierList = stockDAO.getSuppliers();
        renderSuppliersTable(supplierList);
    }

    private void renderSuppliersTable(List<StockDAO.Supplier> list) {
        modelSuppliers.setRowCount(0);
        for (StockDAO.Supplier s : list) {
            modelSuppliers.addRow(new Object[]{
                    s.id,
                    s.name,
                    s, // Pass full object to custom rendering (phone + email)
                    s.address != null && !s.address.isEmpty() ? s.address : "—",
                    s, // Pass full object to custom rendering (tax + terms)
                    s.note != null && !s.note.isEmpty() ? s.note : "—",
                    "" // Action row
            });
        }
    }

    private void doSearchSuppliers() {
        String query = txtSearchSuppliers.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            loadSuppliersData();
            return;
        }
        List<StockDAO.Supplier> filtered = new ArrayList<>();
        for (StockDAO.Supplier s : supplierList) {
            if (s.name.toLowerCase().contains(query) || 
                s.phone.toLowerCase().contains(query) || 
                (s.email != null && s.email.toLowerCase().contains(query))) {
                filtered.add(s);
            }
        }
        renderSuppliersTable(filtered);
    }

    // ─── SUPPLIER CRUD DIALOG (INTERNAL) ──────────────────────────────────
    private void showSupplierDialog(StockDAO.Supplier sup) {
        boolean isEdit = sup != null;
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = (owner instanceof Frame)
                ? new JDialog((Frame) owner, isEdit ? "Cập nhật nhà cung cấp" : "Thêm nhà cung cấp mới", true)
                : new JDialog((java.awt.Dialog) owner, isEdit ? "Cập nhật nhà cung cấp" : "Thêm nhà cung cấp mới", true);
        dlg.setSize(450, 480);
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        // Header
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, COLOR_PRIMARY, 0, getHeight(), new Color(79, 65, 180)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        JLabel lblH = new JLabel(isEdit ? "CẬP NHẬT NHÀ CUNG CẤP" : "THÊM MỚI NHÀ CUNG CẤP");
        lblH.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblH.setForeground(Color.WHITE);
        pnlHeader.add(lblH);
        dlg.add(pnlHeader, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel(new GridLayout(7, 2, 10, 14));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField tfName = new JTextField(isEdit ? sup.name : "");
        JTextField tfPhone = new JTextField(isEdit ? sup.phone : "");
        JTextField tfEmail = new JTextField(isEdit ? sup.email : "");
        JTextField tfAddress = new JTextField(isEdit ? sup.address : "");
        JTextField tfTaxCode = new JTextField(isEdit ? sup.taxCode : "");
        JTextField tfPaymentTerms = new JTextField(isEdit ? sup.paymentTerms : "");
        JTextField tfNote = new JTextField(isEdit ? sup.note : "");

        for (JTextField tf : new JTextField[]{tfName, tfPhone, tfEmail, tfAddress, tfTaxCode, tfPaymentTerms, tfNote}) {
            tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        }

        body.add(buildFormLabel("Tên nhà cung cấp:")); body.add(tfName);
        body.add(buildFormLabel("Số điện thoại:")); body.add(tfPhone);
        body.add(buildFormLabel("Email:")); body.add(tfEmail);
        body.add(buildFormLabel("Địa chỉ:")); body.add(tfAddress);
        body.add(buildFormLabel("Mã số thuế:")); body.add(tfTaxCode);
        body.add(buildFormLabel("Điều khoản thanh toán:")); body.add(tfPaymentTerms);
        body.add(buildFormLabel("Ghi chú:")); body.add(tfNote);

        dlg.add(body, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(new Color(248, 250, 252));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));

        JButton btnCancel = createStyledButton("Huỷ", new Color(229, 231, 235), COLOR_TEXT_PRI);
        btnCancel.setPreferredSize(new Dimension(80, 36));
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnSave = createStyledButton("Lưu thông tin", COLOR_PRIMARY, Color.WHITE);
        btnSave.setPreferredSize(new Dimension(130, 36));
        btnSave.addActionListener(e -> {
            String name = tfName.getText().trim();
            String phone = tfPhone.getText().trim();
            String email = tfEmail.getText().trim();
            String address = tfAddress.getText().trim();
            String taxCode = tfTaxCode.getText().trim();
            String paymentTerms = tfPaymentTerms.getText().trim();
            String note = tfNote.getText().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng điền tên và số điện thoại!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (isEdit) {
                StockDAO.Supplier updated = new StockDAO.Supplier(sup.id, name, phone, address, email, taxCode, paymentTerms, note);
                boolean ok = stockDAO.updateSupplier(updated);
                if (ok) {
                    JOptionPane.showMessageDialog(dlg, "Cập nhật thông tin nhà cung cấp thành công!");
                    dlg.dispose();
                    loadSuppliersData();
                } else {
                    JOptionPane.showMessageDialog(dlg, "Lỗi kết nối cơ sở dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                StockDAO.Supplier newSup = new StockDAO.Supplier(0, name, phone, address, email, taxCode, paymentTerms, note);
                boolean ok = stockDAO.insertSupplier(newSup);
                if (ok) {
                    JOptionPane.showMessageDialog(dlg, "Đã thêm nhà cung cấp mới thành công!");
                    dlg.dispose();
                    loadSuppliersData();
                } else {
                    JOptionPane.showMessageDialog(dlg, "Lỗi kết nối cơ sở dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        footer.add(btnCancel);
        footer.add(btnSave);
        dlg.add(footer, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private JLabel buildFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(COLOR_TEXT_PRI);
        return lbl;
    }

    // ─── CREATE PURCHASE ORDER DIALOG ────────────────────────────────────
    private void showCreatePurchaseOrderDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = (owner instanceof Frame)
                ? new JDialog((Frame) owner, "Lập đơn mua hàng mới", true)
                : new JDialog((java.awt.Dialog) owner, "Lập đơn mua hàng mới", true);
        dlg.setSize(750, 640);
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        // Header Sheet-style border line
        JPanel pnlSheetHeader = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, COLOR_PRIMARY, 0, getHeight(), new Color(79, 65, 180)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        pnlSheetHeader.setLayout(new BorderLayout());
        pnlSheetHeader.setBorder(new EmptyBorder(16, 24, 16, 24));
        
        JLabel lblSheetTitle = new JLabel("LẬP ĐƠN MUA HÀNG");
        lblSheetTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblSheetTitle.setForeground(Color.WHITE);
        
        JLabel lblSheetSub = new JLabel("Tạo yêu cầu mua hàng & Chờ nhân viên kho kiểm đếm thực tế");
        lblSheetSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSheetSub.setForeground(new Color(210, 200, 255));
        
        JPanel pnlHeaderTexts = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlHeaderTexts.setOpaque(false);
        pnlHeaderTexts.add(lblSheetTitle);
        pnlHeaderTexts.add(lblSheetSub);
        
        pnlSheetHeader.add(pnlHeaderTexts, BorderLayout.CENTER);
        
        String dateStr = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
        JLabel lblSheetDate = new JLabel("Ngày lập: " + dateStr);
        lblSheetDate.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSheetDate.setForeground(Color.WHITE);
        pnlSheetHeader.add(lblSheetDate, BorderLayout.EAST);

        dlg.add(pnlSheetHeader, BorderLayout.NORTH);

        // Sheet Paper inside
        JPanel pnlSheetPaper = new JPanel(new BorderLayout(15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(220, 225, 235));
                g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{4f, 4f}, 0.0f));
                g2.drawLine(20, 52, getWidth() - 20, 52);
                g2.dispose();
            }
        };
        pnlSheetPaper.setBackground(Color.WHITE);
        pnlSheetPaper.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Invoice Header metadata
        JPanel pnlMetaInfo = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlMetaInfo.setOpaque(false);
        pnlMetaInfo.setPreferredSize(new Dimension(0, 40));

        String creatorName = currentEmployee != null ? currentEmployee.getHoTen() : currentUsername;
        JLabel lblCreator = new JLabel("Người đề xuất: " + creatorName);
        lblCreator.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCreator.setForeground(COLOR_TEXT_PRI);

        JLabel lblRecommendationText = new JLabel("🔥 Tự động đề xuất các mặt hàng hết hàng (SL <= 0)");
        lblRecommendationText.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 11));
        lblRecommendationText.setForeground(COLOR_DANGER_FG);
        lblRecommendationText.setHorizontalAlignment(SwingConstants.RIGHT);

        pnlMetaInfo.add(lblCreator);
        pnlMetaInfo.add(lblRecommendationText);

        JPanel pnlTopPaper = new JPanel();
        pnlTopPaper.setLayout(new BoxLayout(pnlTopPaper, BoxLayout.Y_AXIS));
        pnlTopPaper.setOpaque(false);
        pnlTopPaper.add(pnlMetaInfo);
        pnlTopPaper.add(Box.createVerticalStrut(10));

        // Create toolbar for adding products
        JPanel pnlTableActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlTableActions.setOpaque(false);

        List<Product> allProducts = stockDAO.getAllProducts();
        JComboBox<Product> cbProducts = new JComboBox<>();
        cbProducts.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbProducts.setPreferredSize(new Dimension(280, 32));
        for (Product p : allProducts) {
            cbProducts.addItem(p);
        }
        cbProducts.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object val, int idx, boolean isSel, boolean hasF) {
                super.getListCellRendererComponent(list, val, idx, isSel, hasF);
                if (val instanceof Product) {
                    Product p = (Product) val;
                    setText(p.getName() + " (" + p.getCategory() + ")");
                }
                return this;
            }
        });

        // Editable Sheet Grid Table
        String[] sheetColumns = {"Mã SP", "Tên sản phẩm", "Số lượng nhập", "Giá nhập (VNĐ)", "Thành tiền", "Hành động"};
        DefaultTableModel sheetModel = new DefaultTableModel(sheetColumns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 2 || c == 3 || c == 5; 
            }
        };

        // Subtotal updater listener
        JLabel lblTotalSum = new JLabel("Tổng cộng: 0đ");
        lblTotalSum.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalSum.setForeground(COLOR_PRIMARY);

        Runnable updateSum = () -> {
            long total = 0;
            for (int i = 0; i < sheetModel.getRowCount(); i++) {
                Object qtyObj = sheetModel.getValueAt(i, 2);
                Object costObj = sheetModel.getValueAt(i, 3);
                int qty = 0;
                long cost = 0;
                try {
                    qty = Integer.parseInt(qtyObj.toString());
                    cost = Long.parseLong(costObj.toString());
                } catch (Exception ex) {}
                
                long sub = (long) qty * cost;
                sheetModel.setValueAt(sub, i, 4);
                total += sub;
            }
            lblTotalSum.setText("Tổng cộng: " + String.format("%,dđ", total));
        };

        JButton btnAddProduct = createStyledButton("+ Thêm vào hóa đơn", COLOR_PRIMARY, Color.WHITE);
        btnAddProduct.setPreferredSize(new Dimension(160, 32));
        btnAddProduct.addActionListener(evt -> {
            Product selected = (Product) cbProducts.getSelectedItem();
            if (selected != null) {
                boolean exists = false;
                for (int i = 0; i < sheetModel.getRowCount(); i++) {
                    if ((Integer) sheetModel.getValueAt(i, 0) == selected.getId()) {
                        exists = true;
                        break;
                    }
                }
                if (exists) {
                    JOptionPane.showMessageDialog(dlg, "Sản phẩm này đã có trong danh sách nhập kho!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                long cost = (long) (selected.getPrice() * 0.6);
                sheetModel.addRow(new Object[]{
                        selected.getId(),
                        selected.getName(),
                        5,
                        cost,
                        5 * cost,
                        "Xóa"
                });
                updateSum.run();
            }
        });

        pnlTableActions.add(cbProducts);
        pnlTableActions.add(Box.createHorizontalStrut(10));
        pnlTableActions.add(btnAddProduct);
        pnlTopPaper.add(pnlTableActions);

        pnlSheetPaper.add(pnlTopPaper, BorderLayout.NORTH);

        JTable tblSheet = new JTable(sheetModel);
        tblSheet.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblSheet.setRowHeight(36);
        tblSheet.setShowGrid(true);
        tblSheet.setGridColor(COLOR_BG);
        tblSheet.setBackground(Color.WHITE);
        
        tblSheet.getColumnModel().getColumn(0).setPreferredWidth(60);  // ID
        tblSheet.getColumnModel().getColumn(1).setPreferredWidth(250); // Name
        tblSheet.getColumnModel().getColumn(2).setPreferredWidth(90);  // Qty
        tblSheet.getColumnModel().getColumn(3).setPreferredWidth(110); // Cost Price
        tblSheet.getColumnModel().getColumn(4).setPreferredWidth(110); // Subtotal
        tblSheet.getColumnModel().getColumn(5).setPreferredWidth(70);  // Delete Button

        tblSheet.getColumnModel().getColumn(5).setCellRenderer(new StockPanel.ButtonRenderer("Xóa", COLOR_DANGER_FG, COLOR_DANGER_BG, 60));
        tblSheet.getColumnModel().getColumn(5).setCellEditor(new StockPanel.ButtonEditor(new JCheckBox(), "Xóa", COLOR_DANGER_FG, COLOR_DANGER_BG, 60, productId -> {
            for (int i = 0; i < sheetModel.getRowCount(); i++) {
                if ((Integer) sheetModel.getValueAt(i, 0) == productId) {
                    sheetModel.removeRow(i);
                    updateSum.run();
                    break;
                }
            }
        }));

        // Seed out of stock items
        List<Product> productsList = stockDAO.getAllProducts();
        for (Product p : productsList) {
            if (p.getStock() <= 0) {
                long defaultCost = (long) (p.getPrice() * 0.6);
                long subtotal = 10 * defaultCost;
                sheetModel.addRow(new Object[]{
                        p.getId(),
                        p.getName(),
                        10,
                        defaultCost,
                        subtotal,
                        "Xóa"
                });
            }
        }

        tblSheet.getModel().addTableModelListener(e -> {
            int col = e.getColumn();
            if (col == 2 || col == 3) {
                updateSum.run();
            }
        });
        updateSum.run();

        JScrollPane sheetScroll = new JScrollPane(tblSheet);
        sheetScroll.getViewport().setBackground(Color.WHITE);
        sheetScroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        pnlSheetPaper.add(sheetScroll, BorderLayout.CENTER);

        JPanel pnlPaperBottom = new JPanel(new BorderLayout());
        pnlPaperBottom.setOpaque(false);
        pnlPaperBottom.setBorder(new EmptyBorder(10, 0, 0, 0));
        pnlPaperBottom.add(lblTotalSum, BorderLayout.EAST);
        pnlSheetPaper.add(pnlPaperBottom, BorderLayout.SOUTH);

        dlg.add(pnlSheetPaper, BorderLayout.CENTER);

        // Footer Actions
        JPanel pnlSheetFooter = new JPanel(new BorderLayout(15, 0));
        pnlSheetFooter.setBackground(new Color(248, 250, 252));
        pnlSheetFooter.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER),
                new EmptyBorder(14, 20, 14, 20)
        ));

        // Right buttons
        JPanel pnlRightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlRightButtons.setOpaque(false);

        JButton btnCancel = createStyledButton("Hủy bỏ", new Color(229, 231, 235), COLOR_TEXT_PRI);
        btnCancel.setPreferredSize(new Dimension(90, 38));
        btnCancel.addActionListener(ev -> dlg.dispose());

        JButton btnConfirm = createStyledButton("Xác nhận tạo đơn mua", COLOR_PRIMARY, Color.WHITE);
        btnConfirm.setPreferredSize(new Dimension(190, 38));
        btnConfirm.addActionListener(ev -> {
            if (sheetModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng thêm ít nhất một mặt hàng để lập đơn mua!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<StockDAO.RestockItem> purchaseLines = new ArrayList<>();
            double grandTotal = 0;
            for (int i = 0; i < sheetModel.getRowCount(); i++) {
                int pid = (Integer) sheetModel.getValueAt(i, 0);
                String pName = sheetModel.getValueAt(i, 1).toString();
                int qty = Integer.parseInt(sheetModel.getValueAt(i, 2).toString());
                double cost = Double.parseDouble(sheetModel.getValueAt(i, 3).toString());
                
                if (qty <= 0) {
                    JOptionPane.showMessageDialog(dlg, "Số lượng dòng " + (i+1) + " phải lớn hơn 0!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (cost < 0) {
                    JOptionPane.showMessageDialog(dlg, "Giá nhập dòng " + (i+1) + " phải lớn hơn hoặc bằng 0!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                purchaseLines.add(new StockDAO.RestockItem(pid, pName, qty, cost));
                grandTotal += qty * cost;
            }

            // Get selected supplier
            String supplierName = "Công ty TNHH Pet Vina";
            if (!supplierList.isEmpty()) {
                supplierName = supplierList.get(0).name;
            }

            boolean ok = stockDAO.createPurchaseOrder(supplierName, grandTotal, purchaseLines);
            if (ok) {
                JOptionPane.showMessageDialog(dlg, "Đã lập đơn mua hàng thành công! Đơn hàng đang chờ nhân viên kho kiểm đếm thực nhập.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
                loadOrdersData(); 
            } else {
                JOptionPane.showMessageDialog(dlg, "Lỗi kết nối cơ sở dữ liệu khi tạo đơn mua!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        pnlRightButtons.add(btnCancel);
        pnlRightButtons.add(btnConfirm);
        pnlSheetFooter.add(pnlRightButtons, BorderLayout.CENTER);
        dlg.add(pnlSheetFooter, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private void showOrderDetailDialog(int orderId) {
        String supplierName = stockDAO.getPurchaseOrderSupplier(orderId);
        List<StockDAO.PurchaseOrderDetail> details = stockDAO.getPurchaseOrderDetails(orderId);
        if (details.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy chi tiết cho đơn mua hàng này!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = (owner instanceof Frame)
                ? new JDialog((Frame) owner, "Chi tiết đơn mua hàng #" + orderId, true)
                : new JDialog((java.awt.Dialog) owner, "Chi tiết đơn mua hàng #" + orderId, true);
        dlg.setSize(650, 480);
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel pnlHeader = new JPanel(new GridLayout(2, 1, 0, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, COLOR_PRIMARY, 0, getHeight(), new Color(79, 65, 180)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        pnlHeader.setBorder(new EmptyBorder(12, 16, 12, 16));
        JLabel lblTitle = new JLabel("CHI TIẾT ĐƠN MUA HÀNG #" + orderId + " (" + supplierName + ")");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        
        JLabel lblSub = new JLabel("Thông tin sản phẩm đề xuất nhập và đơn giá mua (Chỉ đọc)");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(210, 200, 255));
        pnlHeader.add(lblTitle);
        pnlHeader.add(lblSub);
        dlg.add(pnlHeader, BorderLayout.NORTH);

        String[] cols = {"Mã SP", "Tên sản phẩm", "Số lượng mua", "Đơn giá", "Thành tiền"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(32);
        table.setShowGrid(true);
        table.setGridColor(COLOR_BG);
        table.setBackground(Color.WHITE);

        double total = 0;
        for (StockDAO.PurchaseOrderDetail item : details) {
            model.addRow(new Object[]{
                    item.productId,
                    item.productName,
                    item.quantity,
                    String.format("%,.0fđ", item.costPrice),
                    String.format("%,.0fđ", item.subtotal)
            });
            total += item.subtotal;
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(new EmptyBorder(15, 15, 15, 15));
        dlg.add(scroll, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBackground(new Color(248, 250, 252));
        pnlFooter.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel lblTotal = new JLabel("Tổng giá trị đơn mua: " + String.format("%,.0fđ", total));
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotal.setForeground(COLOR_PRIMARY);
        pnlFooter.add(lblTotal, BorderLayout.WEST);

        JButton btnClose = createStyledButton("Đóng", COLOR_PRIMARY, Color.WHITE);
        btnClose.setPreferredSize(new Dimension(100, 36));
        btnClose.addActionListener(e -> dlg.dispose());
        pnlFooter.add(btnClose, BorderLayout.EAST);
        dlg.add(pnlFooter, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private JPanel createMetricCard(String title, String defaultValue, Color colorVal) {
        JPanel card = new JPanel(new BorderLayout(10, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_SURFACE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(COLOR_BORDER);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(COLOR_TEXT_SEC);
        card.add(lblTitle, BorderLayout.NORTH);

        JLabel lblVal = new JLabel(defaultValue);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblVal.setForeground(colorVal);
        card.add(lblVal, BorderLayout.CENTER);

        if (title.contains("CHỜ")) {
            lblPendingCount = lblVal;
        } else {
            lblTotalSpent = lblVal;
        }

        return card;
    }

    private void renderTable(List<StockDAO.PurchaseOrder> list) {
        modelOrders.setRowCount(0);
        int pendingCount = 0;
        double totalSpent = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (StockDAO.PurchaseOrder po : list) {
            String dateStr = po.date != null ? sdf.format(new Date(po.date.getTime())) : "—";
            modelOrders.addRow(new Object[]{
                    po.orderId,
                    dateStr,
                    po.supplier != null ? po.supplier : "—",
                    po.total,
                    po.status != null ? po.status : "Chờ nhập kho",
                    po.orderId
            });
            if ("Chờ nhập kho".equalsIgnoreCase(po.status) || po.status == null) {
                pendingCount++;
            }
            totalSpent += po.total;
        }
        if (lblPendingCount != null) {
            lblPendingCount.setText(pendingCount + " đơn chờ");
        }
        if (lblTotalSpent != null) {
            lblTotalSpent.setText(String.format("%,.0fđ", totalSpent));
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
        btn.putClientProperty("FlatLaf.style", "arc: 8");
        return btn;
    }
}
