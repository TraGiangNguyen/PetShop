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

public class StockPanel extends JPanel {

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

    private final StockDAO stockDAO = new StockDAO();
    private final String currentUsername;
    private NhanVienModel currentEmployee = null;

    private JTable tblStock;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    
    // Dashboard Cards Indicators
    private JLabel lblTotalItemsValue;
    private JLabel lblOutOfStockValue;

    private List<Product> productList = new ArrayList<>();

    // 3-Tab Card Layout components
    private CardLayout cardLayout;
    private JPanel pnlCards;
    
    // Tab selector buttons
    private JButton btnTabStock;
    private JButton btnTabImport;
    private JButton btnTabAudit;
    
    // Active Tab Color (Orange style from ERP screenshot)
    private static final Color COLOR_ORANGE_ACTIVE = new Color(249, 115, 22); // #f97316
    
    // Tab 2: Nhập kho
    private JTable tblPendingImport;
    private DefaultTableModel modelPendingImport;
    private JTable tblCompletedImport;
    private DefaultTableModel modelCompletedImport;
    
    // Tab 3: Kiểm kê
    private JTable tblAudit;
    private DefaultTableModel modelAudit;

    public StockPanel(String username) {
        this.currentUsername = username;
        this.stockDAO.ensureTablesExist();
        
        // Fetch current employee info for link
        UserDAO uDao = new UserDAO();
        this.currentEmployee = uDao.getNhanVienByUsername(username);

        initComponents();
        loadData();
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

        JLabel lblTitle = new JLabel("Kho hàng & Nhập kho");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(COLOR_TEXT_PRI);

        JLabel lblSubtitle = new JLabel("Quản lý danh mục hàng hóa, số lượng tồn kho và nhập hàng bổ sung");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(COLOR_TEXT_SEC);

        pnlTitle.add(lblTitle);
        pnlTitle.add(lblSubtitle);
        pnlTopHeader.add(pnlTitle, BorderLayout.WEST);

        pnlTopHeader.add(pnlTitle, BorderLayout.WEST);

        pnlHeader.add(pnlTopHeader, BorderLayout.NORTH);

        // ─── TAB NAVIGATION SELECTOR ──────────────────────────────────────────
        JPanel pnlTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlTabs.setOpaque(false);
        pnlTabs.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));

        btnTabStock = createTabButton("Tồn kho");
        btnTabImport = createTabButton("Nhập kho");
        btnTabAudit = createTabButton("Kiểm kê");

        pnlTabs.add(btnTabStock);
        pnlTabs.add(Box.createHorizontalStrut(8));
        pnlTabs.add(btnTabImport);
        pnlTabs.add(Box.createHorizontalStrut(8));
        pnlTabs.add(btnTabAudit);

        pnlHeader.add(pnlTabs, BorderLayout.SOUTH);
        add(pnlHeader, BorderLayout.NORTH);

        // ─── CENTER BODY CARD PANEL ───────────────────────────────────────────
        cardLayout = new CardLayout();
        pnlCards = new JPanel(cardLayout);
        pnlCards.setOpaque(false);

        // 1. STOCK TAB (Original Inventory view)
        JPanel pnlInventoryView = new JPanel(new BorderLayout(0, 16));
        pnlInventoryView.setOpaque(false);

        // 1.1 Dashboard quick metrics
        JPanel pnlMetrics = new JPanel(new GridLayout(1, 1, 0, 0));
        pnlMetrics.setOpaque(false);
        pnlMetrics.setPreferredSize(new Dimension(0, 85));
        pnlMetrics.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        JPanel cardOut = createMetricCard("MẶT HÀNG HẾT HÀNG", "0 sản phẩm", COLOR_DANGER_FG);

        pnlMetrics.add(cardOut);

        // 1.2 Search & Filter Area
        JPanel pnlFilter = new JPanel(new BorderLayout(10, 0));
        pnlFilter.setOpaque(false);
        pnlFilter.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JPanel pnlSearchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlSearchBox.setOpaque(false);

        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(280, 36));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm kiếm sản phẩm...");
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(0, 12, 0, 12)
        ));
        txtSearch.setBackground(Color.WHITE);
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { doFilterSearch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { doFilterSearch(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { doFilterSearch(); }
        });

        pnlSearchBox.add(txtSearch);
        pnlFilter.add(pnlSearchBox, BorderLayout.WEST);

        // Create top container for metrics and filter to place in NORTH of pnlInventoryView
        JPanel pnlTopBody = new JPanel();
        pnlTopBody.setLayout(new BoxLayout(pnlTopBody, BoxLayout.Y_AXIS));
        pnlTopBody.setOpaque(false);
        pnlTopBody.add(pnlMetrics);
        pnlTopBody.add(Box.createVerticalStrut(16));
        pnlTopBody.add(pnlFilter);

        pnlInventoryView.add(pnlTopBody, BorderLayout.NORTH);

        // 1.3 JTable Card Container
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
        pnlTableCard.setBorder(new EmptyBorder(8, 8, 8, 8));

        String[] columns = {"Mã SP", "Hình ảnh", "Tên sản phẩm", "Nhãn hiệu", "Giá bán", "Tồn kho", "Trạng thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tblStock = new JTable(tableModel);
        tblStock.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblStock.setRowHeight(64);
        tblStock.setShowVerticalLines(false);
        tblStock.setShowHorizontalLines(true);
        tblStock.setGridColor(COLOR_BG);
        tblStock.setBackground(COLOR_SURFACE);
        tblStock.setSelectionBackground(new Color(243, 241, 255));
        tblStock.setSelectionForeground(COLOR_TEXT_PRI);
        tblStock.setIntercellSpacing(new Dimension(0, 0));
        tblStock.setFocusable(false);

        // Header Styling
        JTableHeader header = tblStock.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(COLOR_SURFACE);
        header.setForeground(COLOR_TEXT_SEC);
        header.setPreferredSize(new Dimension(0, 44));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
        header.setReorderingAllowed(false);
        
        // Column alignment & sizes
        tblStock.getColumnModel().getColumn(0).setPreferredWidth(60);  // ID
        tblStock.getColumnModel().getColumn(1).setPreferredWidth(70);  // Image
        tblStock.getColumnModel().getColumn(2).setPreferredWidth(320); // Name
        tblStock.getColumnModel().getColumn(3).setPreferredWidth(120); // Brand
        tblStock.getColumnModel().getColumn(4).setPreferredWidth(120); // Price
        tblStock.getColumnModel().getColumn(5).setPreferredWidth(90);  // Stock
        tblStock.getColumnModel().getColumn(6).setPreferredWidth(110); // Status Pill

        // Custom Cell Renderers
        tblStock.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
                super.getTableCellRendererComponent(table, val, isSel, hasF, r, c);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setForeground(COLOR_TEXT_SEC);
                setHorizontalAlignment(SwingConstants.CENTER);
                setOpaque(true);
                setBackground(isSel ? table.getSelectionBackground() : COLOR_SURFACE);
                return this;
            }
        });

        // Image Renderer mapping to cached assets
        tblStock.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
                super.getTableCellRendererComponent(table, val, isSel, hasF, r, c);
                setText("");
                setHorizontalAlignment(SwingConstants.CENTER);
                setBackground(isSel ? table.getSelectionBackground() : COLOR_SURFACE);
                
                Object nameObj = table.getValueAt(r, 2);
                if (nameObj != null) {
                    ImageIcon img = getProductImageByName(nameObj.toString());
                    if (img != null) {
                        Image scaled = img.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH);
                        setIcon(new ImageIcon(scaled));
                    } else {
                        setIcon(null);
                        setText("—");
                    }
                } else {
                    setIcon(null);
                }
                return this;
            }
        });

        // Name Renderer
        tblStock.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
                super.getTableCellRendererComponent(table, val, isSel, hasF, r, c);
                setFont(new Font("Segoe UI", Font.BOLD, 14));
                setForeground(COLOR_TEXT_PRI);
                setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
                setBackground(isSel ? table.getSelectionBackground() : COLOR_SURFACE);
                return this;
            }
        });

        // Brand Renderer (Category)
        tblStock.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
                super.getTableCellRendererComponent(table, val, isSel, hasF, r, c);
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                setForeground(COLOR_TEXT_SEC);
                setHorizontalAlignment(SwingConstants.LEFT);
                setBackground(isSel ? table.getSelectionBackground() : COLOR_SURFACE);
                return this;
            }
        });

        // Price Renderer
        tblStock.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
                super.getTableCellRendererComponent(table, val, isSel, hasF, r, c);
                setFont(new Font("Segoe UI", Font.BOLD, 14));
                setForeground(COLOR_TEXT_PRI);
                setHorizontalAlignment(SwingConstants.RIGHT);
                setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
                setBackground(isSel ? table.getSelectionBackground() : COLOR_SURFACE);
                return this;
            }
        });

        // Stock Level Renderer (Alert background if zero)
        tblStock.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
                super.getTableCellRendererComponent(table, val, isSel, hasF, r, c);
                setFont(new Font("Segoe UI", Font.BOLD, 14));
                setHorizontalAlignment(SwingConstants.CENTER);
                
                int qty = val instanceof Integer ? (Integer) val : 0;
                if (qty <= 0) {
                    setForeground(COLOR_DANGER_FG);
                    setBackground(COLOR_DANGER_BG);
                } else if (qty <= 3) {
                    setForeground(new Color(217, 119, 6)); // Amber
                    setBackground(new Color(254, 243, 199)); // Amber Light
                } else {
                    setForeground(COLOR_TEXT_PRI);
                    setBackground(isSel ? table.getSelectionBackground() : COLOR_SURFACE);
                }
                return this;
            }
        });

        // Status Pill Renderer
        tblStock.getColumnModel().getColumn(6).setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
                int qty = (Integer) table.getValueAt(r, 5);
                
                JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 18)) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        Color bg = qty <= 0 ? COLOR_DANGER_BG : qty <= 3 ? new Color(254, 243, 199) : COLOR_SUCCESS_BG;
                        g2.setColor(bg);
                        g2.fill(new RoundRectangle2D.Float(10, 6, getWidth() - 20, getHeight() - 12, 14, 14));
                        g2.dispose();
                    }
                };
                pnl.setOpaque(true);
                pnl.setBackground(isSel ? table.getSelectionBackground() : COLOR_SURFACE);
                
                String labelText = qty <= 0 ? "Hết hàng" : qty <= 3 ? "Sắp hết" : "Sẵn có";
                Color textCol = qty <= 0 ? COLOR_DANGER_FG : qty <= 3 ? new Color(217, 119, 6) : COLOR_SUCCESS_FG;
                
                JLabel lbl = new JLabel(labelText);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setForeground(textCol);
                pnl.add(lbl);
                return pnl;
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblStock);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(COLOR_SURFACE);
        pnlTableCard.add(scrollPane, BorderLayout.CENTER);

        pnlInventoryView.add(pnlTableCard, BorderLayout.CENTER);
        pnlCards.add(pnlInventoryView, "STOCK");

        // 2. IMPORT TAB
        JPanel pnlImportView = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlImportView.setOpaque(false);

        // 2.1 Pending Purchase Orders card
        JPanel pnlPendingCard = new JPanel(new BorderLayout(0, 10)) {
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
        pnlPendingCard.setOpaque(false);
        pnlPendingCard.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblPendingTitle = new JLabel("Đơn mua hàng chờ nhập kho");
        lblPendingTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblPendingTitle.setForeground(COLOR_PRIMARY);
        pnlPendingCard.add(lblPendingTitle, BorderLayout.NORTH);

        String[] colsPending = {"Mã ĐH", "Ngày lập", "Nhà cung cấp", "Tổng tiền", "Hành động"};
        modelPendingImport = new DefaultTableModel(colsPending, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 4;
            }
        };
        tblPendingImport = new JTable(modelPendingImport);
        tblPendingImport.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblPendingImport.setRowHeight(38);
        tblPendingImport.setShowVerticalLines(false);
        tblPendingImport.setGridColor(COLOR_BG);
        tblPendingImport.setFocusable(false);

        JTableHeader hPending = tblPendingImport.getTableHeader();
        hPending.setFont(new Font("Segoe UI", Font.BOLD, 12));
        hPending.setBackground(COLOR_SURFACE);
        hPending.setForeground(COLOR_TEXT_SEC);
        hPending.setPreferredSize(new Dimension(0, 36));
        hPending.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
        hPending.setReorderingAllowed(false);
        
        tblPendingImport.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblPendingImport.getColumnModel().getColumn(1).setPreferredWidth(110);
        tblPendingImport.getColumnModel().getColumn(2).setPreferredWidth(140);
        tblPendingImport.getColumnModel().getColumn(3).setPreferredWidth(100);
        tblPendingImport.getColumnModel().getColumn(4).setPreferredWidth(100);

        tblPendingImport.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer("Nhập kho", COLOR_PRIMARY, Color.WHITE, 80));
        tblPendingImport.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox(), "Nhập kho", COLOR_PRIMARY, Color.WHITE, 80, this::showPurchaseOrderDetailDialog));

        JScrollPane spPending = new JScrollPane(tblPendingImport);
        spPending.getViewport().setBackground(COLOR_SURFACE);
        spPending.setBorder(BorderFactory.createEmptyBorder());
        pnlPendingCard.add(spPending, BorderLayout.CENTER);

        // 2.2 Completed Imports card
        JPanel pnlCompletedCard = new JPanel(new BorderLayout(0, 10)) {
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
        pnlCompletedCard.setOpaque(false);
        pnlCompletedCard.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblCompletedTitle = new JLabel("Lịch sử nhập kho thành công");
        lblCompletedTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblCompletedTitle.setForeground(COLOR_SUCCESS_FG);
        pnlCompletedCard.add(lblCompletedTitle, BorderLayout.NORTH);

        String[] colsCompleted = {"Mã PN", "Ngày nhập", "Nhà cung cấp", "Tổng tiền", "Hành động"};
        modelCompletedImport = new DefaultTableModel(colsCompleted, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 4;
            }
        };
        tblCompletedImport = new JTable(modelCompletedImport);
        tblCompletedImport.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblCompletedImport.setRowHeight(38);
        tblCompletedImport.setShowVerticalLines(false);
        tblCompletedImport.setGridColor(COLOR_BG);
        tblCompletedImport.setFocusable(false);

        JTableHeader hCompleted = tblCompletedImport.getTableHeader();
        hCompleted.setFont(new Font("Segoe UI", Font.BOLD, 12));
        hCompleted.setBackground(COLOR_SURFACE);
        hCompleted.setForeground(COLOR_TEXT_SEC);
        hCompleted.setPreferredSize(new Dimension(0, 36));
        hCompleted.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
        hCompleted.setReorderingAllowed(false);

        tblCompletedImport.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblCompletedImport.getColumnModel().getColumn(1).setPreferredWidth(110);
        tblCompletedImport.getColumnModel().getColumn(2).setPreferredWidth(140);
        tblCompletedImport.getColumnModel().getColumn(3).setPreferredWidth(100);
        tblCompletedImport.getColumnModel().getColumn(4).setPreferredWidth(100);

        tblCompletedImport.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer("Chi tiết", COLOR_SUCCESS_FG, Color.WHITE, 80));
        tblCompletedImport.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox(), "Chi tiết", COLOR_SUCCESS_FG, Color.WHITE, 80, this::showImportDetailDialog));

        JScrollPane spCompleted = new JScrollPane(tblCompletedImport);
        spCompleted.getViewport().setBackground(COLOR_SURFACE);
        spCompleted.setBorder(BorderFactory.createEmptyBorder());
        pnlCompletedCard.add(spCompleted, BorderLayout.CENTER);

        pnlImportView.add(pnlPendingCard);
        pnlImportView.add(pnlCompletedCard);
        pnlCards.add(pnlImportView, "IMPORT");

        // 3. AUDIT TAB
        JPanel pnlAuditView = new JPanel(new BorderLayout(0, 16));
        pnlAuditView.setOpaque(false);

        JPanel pnlAuditHeader = new JPanel(new BorderLayout());
        pnlAuditHeader.setOpaque(false);

        JButton btnNewAudit = createStyledButton("+ Lập phiếu kiểm kê mới", COLOR_ORANGE_ACTIVE, Color.WHITE);
        btnNewAudit.setPreferredSize(new Dimension(220, 42));
        btnNewAudit.addActionListener(e -> showCreateAuditDialog());
        pnlAuditHeader.add(btnNewAudit, BorderLayout.WEST);

        pnlAuditView.add(pnlAuditHeader, BorderLayout.NORTH);

        JPanel pnlAuditTableCard = new JPanel(new BorderLayout()) {
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
        pnlAuditTableCard.setOpaque(false);
        pnlAuditTableCard.setBorder(new EmptyBorder(8, 8, 8, 8));

        String[] colsAudit = {"Mã phiếu KK", "Ngày lập", "Nhân viên kiểm kê", "Ghi chú", "Hành động"};
        modelAudit = new DefaultTableModel(colsAudit, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 4;
            }
        };
        tblAudit = new JTable(modelAudit);
        tblAudit.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblAudit.setRowHeight(40);
        tblAudit.setShowVerticalLines(false);
        tblAudit.setGridColor(COLOR_BG);
        tblAudit.setFocusable(false);

        JTableHeader auditHeader = tblAudit.getTableHeader();
        auditHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        auditHeader.setBackground(COLOR_SURFACE);
        auditHeader.setForeground(COLOR_TEXT_SEC);
        auditHeader.setPreferredSize(new Dimension(0, 40));
        auditHeader.setReorderingAllowed(false);

        tblAudit.getColumnModel().getColumn(0).setPreferredWidth(90);
        tblAudit.getColumnModel().getColumn(1).setPreferredWidth(140);
        tblAudit.getColumnModel().getColumn(2).setPreferredWidth(180);
        tblAudit.getColumnModel().getColumn(3).setPreferredWidth(260);
        tblAudit.getColumnModel().getColumn(4).setPreferredWidth(130);

        tblAudit.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer("Xem chi tiết", COLOR_ORANGE_ACTIVE, Color.WHITE, 110));
        tblAudit.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox(), "Xem chi tiết", COLOR_ORANGE_ACTIVE, Color.WHITE, 110, this::showAuditDetailDialog));

        JScrollPane auditScroll = new JScrollPane(tblAudit);
        auditScroll.getViewport().setBackground(COLOR_SURFACE);
        auditScroll.setBorder(BorderFactory.createEmptyBorder());
        pnlAuditTableCard.add(auditScroll, BorderLayout.CENTER);

        pnlAuditView.add(pnlAuditTableCard, BorderLayout.CENTER);
        pnlCards.add(pnlAuditView, "AUDIT");

        // Add cards container to center
        add(pnlCards, BorderLayout.CENTER);

        // Tab Selection Action Listeners
        btnTabStock.addActionListener(e -> {
            cardLayout.show(pnlCards, "STOCK");
            updateTabs(btnTabStock);
            loadData();
        });

        btnTabImport.addActionListener(e -> {
            cardLayout.show(pnlCards, "IMPORT");
            updateTabs(btnTabImport);
            loadImportTab();
        });

        btnTabAudit.addActionListener(e -> {
            cardLayout.show(pnlCards, "AUDIT");
            updateTabs(btnTabAudit);
            loadAuditTab();
        });

        updateTabs(btnTabStock);
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
        JButton[] buttons = {btnTabStock, btnTabImport, btnTabAudit};
        for (JButton btn : buttons) {
            if (btn == activeBtn) {
                btn.setForeground(COLOR_ORANGE_ACTIVE);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 3, 0, COLOR_ORANGE_ACTIVE),
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

        if (title.contains("TỔNG")) {
            lblTotalItemsValue = lblVal;
        } else {
            lblOutOfStockValue = lblVal;
        }

        return card;
    }

    // ─── CONTROLLER LOADING METHODS ───────────────────────────────────────
    public void loadData() {
        productList = stockDAO.getAllProducts();
        renderTable(productList);
    }

    private void renderTable(List<Product> list) {
        tableModel.setRowCount(0);
        int outCount = 0;
        
        for (Product p : list) {
            if (p.getStock() <= 0) outCount++;
            tableModel.addRow(new Object[]{
                    p.getId(),
                    "",
                    p.getName(),
                    p.getCategory(),
                    String.format("%,dđ", p.getPrice()),
                    p.getStock(),
                    ""
            });
        }

        // Update indicators
        if (lblTotalItemsValue != null) {
            lblTotalItemsValue.setText(list.size() + " sản phẩm");
        }
        if (lblOutOfStockValue != null) {
            lblOutOfStockValue.setText(outCount + " sản phẩm");
            if (outCount > 0) {
                lblOutOfStockValue.setForeground(COLOR_DANGER_FG);
            } else {
                lblOutOfStockValue.setForeground(COLOR_SUCCESS_FG);
            }
        }
    }

    private void doFilterSearch() {
        String query = txtSearch.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            renderTable(productList);
            return;
        }
        List<Product> filtered = new ArrayList<>();
        for (Product p : productList) {
            if (p.getName().toLowerCase().contains(query) ||
                p.getCategory().toLowerCase().contains(query) ||
                String.valueOf(p.getId()).contains(query)) {
                filtered.add(p);
            }
        }
        renderTable(filtered);
    }

    // ─── RESTOCK INVOICE SHEET FORM DIALOG ────────────────────────────────
    private void showRestockDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = (owner instanceof Frame)
                ? new JDialog((Frame) owner, "Hóa đơn nhập kho", true)
                : new JDialog((java.awt.Dialog) owner, "Hóa đơn nhập kho", true);
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
        
        JLabel lblSheetTitle = new JLabel("HÓA ĐƠN NHẬP HÀNG");
        lblSheetTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblSheetTitle.setForeground(Color.WHITE);
        
        JLabel lblSheetSub = new JLabel("Phiếu đề xuất mua hàng & Cập nhật tồn kho tự động");
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

        // Sheet Paper inside (the sheet form itself)
        JPanel pnlSheetPaper = new JPanel(new BorderLayout(15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                // Draw elegant watermark or light dotted receipt line
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
        JLabel lblCreator = new JLabel("Người lập phiếu: " + creatorName);
        lblCreator.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCreator.setForeground(COLOR_TEXT_PRI);

        JLabel lblRecommendationText = new JLabel("🔥 Đã tự động thêm các mặt hàng hết hàng (SL <= 0)");
        lblRecommendationText.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 11));
        lblRecommendationText.setForeground(COLOR_DANGER_FG);
        lblRecommendationText.setHorizontalAlignment(SwingConstants.RIGHT);

        pnlMetaInfo.add(lblCreator);
        pnlMetaInfo.add(lblRecommendationText);

        // Top container for header metadata and toolbar
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
                return c == 2 || c == 3 || c == 5; // Quantity, Cost, and Action column are editable
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

        // Set Custom Delete Row Action Button on Column 5
        tblSheet.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer("Xóa", COLOR_DANGER_FG, COLOR_DANGER_BG, 60));
        tblSheet.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox(), "Xóa", COLOR_DANGER_FG, COLOR_DANGER_BG, 60, productId -> {
            for (int i = 0; i < sheetModel.getRowCount(); i++) {
                if ((Integer) sheetModel.getValueAt(i, 0) == productId) {
                    sheetModel.removeRow(i);
                    updateSum.run();
                    break;
                }
            }
        }));

        // Auto recommend out-of-stock items
        for (Product p : productList) {
            if (p.getStock() <= 0) {
                long defaultCost = (long) (p.getPrice() * 0.6); // 60% of retail price
                long subtotal = 10 * defaultCost;
                sheetModel.addRow(new Object[]{
                        p.getId(),
                        p.getName(),
                        10, // Recommended Restock quantity
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

        // Custom Renderers for sheet
        tblSheet.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
                super.getTableCellRendererComponent(table, val, isSel, hasF, r, c);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setForeground(COLOR_PRIMARY);
                setHorizontalAlignment(SwingConstants.CENTER);
                return this;
            }
        });
        tblSheet.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
                super.getTableCellRendererComponent(table, val, isSel, hasF, r, c);
                setHorizontalAlignment(SwingConstants.RIGHT);
                return this;
            }
        });
        tblSheet.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
                super.getTableCellRendererComponent(table, val, isSel, hasF, r, c);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setForeground(COLOR_TEXT_PRI);
                setHorizontalAlignment(SwingConstants.RIGHT);
                return this;
            }
        });

        JScrollPane sheetScroll = new JScrollPane(tblSheet);
        sheetScroll.getViewport().setBackground(Color.WHITE);
        sheetScroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        pnlSheetPaper.add(sheetScroll, BorderLayout.CENTER);

        // Sum and buttons panel at paper bottom
        JPanel pnlPaperBottom = new JPanel(new BorderLayout());
        pnlPaperBottom.setOpaque(false);
        pnlPaperBottom.setBorder(new EmptyBorder(10, 0, 0, 0));
        pnlPaperBottom.add(lblTotalSum, BorderLayout.EAST);
        pnlSheetPaper.add(pnlPaperBottom, BorderLayout.SOUTH);

        dlg.add(pnlSheetPaper, BorderLayout.CENTER);

        // ─── DIALOG FOOTER (ACTIONS & THÊM MỚI BUTTON AT THE BOTTOM OF THE SHEET) ───
        JPanel pnlSheetFooter = new JPanel(new BorderLayout(15, 0));
        pnlSheetFooter.setBackground(new Color(248, 250, 252));
        pnlSheetFooter.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER),
                new EmptyBorder(14, 20, 14, 20)
        ));

        // LEFT FOOTER ACTION: "Thêm sản phẩm mới" (At the bottom of HOA_DON_NHAP_HANG sheet form)
        JButton btnAddNewProduct = createStyledButton("+ Thêm sản phẩm mới", new Color(241, 245, 249), COLOR_TEXT_PRI);
        btnAddNewProduct.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAddNewProduct.setPreferredSize(new Dimension(185, 38));
        btnAddNewProduct.addActionListener(e -> {
            // Sub-dialog to create totally new product in database
            showCreateNewProductDialog(dlg, sheetModel, updateSum);
        });
        pnlSheetFooter.add(btnAddNewProduct, BorderLayout.WEST);

        // RIGHT FOOTER ACTIONS: Confirm & Cancel
        JPanel pnlRightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlRightButtons.setOpaque(false);

        JButton btnCancel = createStyledButton("Hủy bỏ", new Color(229, 231, 235), COLOR_TEXT_PRI);
        btnCancel.setPreferredSize(new Dimension(90, 38));
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnConfirm = createStyledButton("Xác nhận nhập kho", COLOR_PRIMARY, Color.WHITE);
        btnConfirm.setPreferredSize(new Dimension(180, 38));
        btnConfirm.addActionListener(e -> {
            if (sheetModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng thêm ít nhất một mặt hàng để nhập kho!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Parse lines
            List<StockDAO.RestockItem> restockLines = new ArrayList<>();
            long grandTotal = 0;
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

                restockLines.add(new StockDAO.RestockItem(pid, pName, qty, cost));
                grandTotal += (long) qty * cost;
            }

            int employeeId = currentEmployee != null ? currentEmployee.getMaNhanVien() : 1;
            boolean ok = stockDAO.createRestockOrder(employeeId, grandTotal, restockLines);
            if (ok) {
                JOptionPane.showMessageDialog(dlg, "Hoàn tất lập hóa đơn nhập kho! Tồn kho đã được cập nhật.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
                loadData(); // Reload main inventory levels
                loadImportTab(); // Reload import history list on the Import Tab!
            } else {
                JOptionPane.showMessageDialog(dlg, "Lỗi kết nối cơ sở dữ liệu khi nhập kho!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        pnlRightButtons.add(btnCancel);
        pnlRightButtons.add(btnConfirm);
        pnlSheetFooter.add(pnlRightButtons, BorderLayout.CENTER);

        dlg.add(pnlSheetFooter, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    // ─── ADD NEW PRODUCT SUB-DIALOG ───────────────────────────────────────
    private void showCreateNewProductDialog(JDialog parent, DefaultTableModel sheetModel, Runnable updateSum) {
        JDialog dlg = new JDialog(parent, "Thêm sản phẩm hoàn toàn mới", true);
        dlg.setSize(400, 360);
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(parent);
        dlg.setLayout(new BorderLayout());

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, COLOR_SUCCESS_FG, 0, getHeight(), new Color(30, 175, 80)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        JLabel lblH = new JLabel("TẠO MỚI SẢN PHẨM");
        lblH.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblH.setForeground(Color.WHITE);
        header.add(lblH);
        dlg.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridLayout(4, 2, 10, 16));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(25, 20, 20, 20));

        JTextField tfName = new JTextField();
        tfName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        JTextField tfBrand = new JTextField("PetShop");
        tfBrand.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        JTextField tfUnit = new JTextField("Cái");
        tfUnit.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JTextField tfPrice = new JTextField("100000");
        tfPrice.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        body.add(buildFormLabel("Tên sản phẩm:")); body.add(tfName);
        body.add(buildFormLabel("Nhãn hiệu:")); body.add(tfBrand);
        body.add(buildFormLabel("Đơn vị tính:")); body.add(tfUnit);
        body.add(buildFormLabel("Giá bán niêm yết:")); body.add(tfPrice);

        dlg.add(body, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(new Color(248, 250, 252));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));

        JButton btnCancel = createStyledButton("Huỷ", new Color(229, 231, 235), COLOR_TEXT_PRI);
        btnCancel.setPreferredSize(new Dimension(80, 36));
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnSave = createStyledButton("Tạo & Thêm vào hóa đơn", COLOR_SUCCESS_FG, Color.WHITE);
        btnSave.setPreferredSize(new Dimension(190, 36));
        btnSave.addActionListener(e -> {
            String name = tfName.getText().trim();
            String brand = tfBrand.getText().trim();
            String unit = tfUnit.getText().trim();
            String priceStr = tfPrice.getText().trim();

            if (name.isEmpty() || priceStr.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập đầy đủ tên và giá!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            long price = 0;
            try {
                price = Long.parseLong(priceStr);
                if (price <= 0) throw new NumberFormatException();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Giá bán niêm yết không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Insert new product into catalog
            boolean inserted = stockDAO.insertProduct(name, price, brand, unit);
            if (inserted) {
                // Fetch product catalogue again to get the generated product ID
                List<Product> updatedList = stockDAO.getAllProducts();
                Product newest = null;
                for (Product p : updatedList) {
                    if (p.getName().equals(name)) {
                        newest = p;
                        break;
                    }
                }

                if (newest != null) {
                    long cost = (long) (newest.getPrice() * 0.6);
                    sheetModel.addRow(new Object[]{
                            newest.getId(),
                            newest.getName(),
                            10, // Default restock Qty
                            cost,
                            10 * cost,
                            "Xóa"
                    });
                    updateSum.run();
                }

                JOptionPane.showMessageDialog(dlg, "Tạo sản phẩm thành công và đã thêm vào hóa đơn nhập!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
                
                // Fetch catalog in background
                productList = updatedList;
                renderTable(productList);
            } else {
                JOptionPane.showMessageDialog(dlg, "Có lỗi xảy ra khi ghi sản phẩm vào database!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        footer.add(btnCancel);
        footer.add(btnSave);
        dlg.add(footer, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    // ─── IMPORT TAB METHODS ───────────────────────────────────────────────
    private void loadImportTab() {
        modelPendingImport.setRowCount(0);
        List<StockDAO.PurchaseOrder> pending = stockDAO.getPendingPurchaseOrders();
        for (StockDAO.PurchaseOrder po : pending) {
            modelPendingImport.addRow(new Object[]{
                    po.orderId,
                    po.date != null ? new SimpleDateFormat("dd/MM/yyyy HH:mm").format(po.date) : "—",
                    po.supplier != null ? po.supplier : "—",
                    String.format("%,.0fđ", po.total),
                    "Nhập kho"
            });
        }

        modelCompletedImport.setRowCount(0);
        List<StockDAO.RestockOrder> completed = stockDAO.getImportHistory();
        for (StockDAO.RestockOrder ro : completed) {
            modelCompletedImport.addRow(new Object[]{
                    ro.id,
                    ro.date != null ? new SimpleDateFormat("dd/MM/yyyy HH:mm").format(ro.date) : "—",
                    ro.supplier != null ? ro.supplier : "—",
                    String.format("%,.0fđ", ro.total),
                    "Chi tiết"
            });
        }
    }

    private void showPurchaseOrderDetailDialog(int orderId) {
        String supplierName = stockDAO.getPurchaseOrderSupplier(orderId);
        List<Product> allProducts = stockDAO.getAllProducts();
        
        List<StockDAO.PurchaseOrderDetail> details = stockDAO.getPurchaseOrderDetails(orderId);
        if (details.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy chi tiết cho đơn mua hàng này!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Automatically add other products from the same supplier/brand that are not in the order
        List<Product> sameSupplierProducts = new ArrayList<>();
        for (Product p : allProducts) {
            boolean alreadyInOrder = false;
            for (StockDAO.PurchaseOrderDetail item : details) {
                if (item.productId == p.getId()) {
                    alreadyInOrder = true;
                    break;
                }
            }
            if (!alreadyInOrder && isSameSupplier(supplierName, p.getCategory())) {
                sameSupplierProducts.add(p);
            }
        }

        for (Product p : sameSupplierProducts) {
            double defaultCost = p.getPrice() * 0.6; // 60% of retail price
            details.add(new StockDAO.PurchaseOrderDetail(
                    p.getId(),
                    p.getName(),
                    5, // Default restock Qty
                    defaultCost,
                    5 * defaultCost
            ));
        }

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = (owner instanceof Frame)
                ? new JDialog((Frame) owner, "Xác nhận nhập kho đơn hàng #" + orderId, true)
                : new JDialog((java.awt.Dialog) owner, "Xác nhận nhập kho đơn hàng #" + orderId, true);
        dlg.setSize(750, 560); // Enlarged slightly to fit controls nicely
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
        JLabel lblTitle = new JLabel("NHẬP KHO ĐƠN MUA HÀNG #" + orderId + " (" + supplierName + ")");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        
        JLabel lblSub = new JLabel("Nhập số lượng thực tế kiểm đếm, thêm/xóa sản phẩm tùy ý trước khi xác nhận");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(210, 200, 255));
        pnlHeader.add(lblTitle);
        pnlHeader.add(lblSub);
        dlg.add(pnlHeader, BorderLayout.NORTH);

        // Center Panel: Toolbar + Table
        JPanel pnlCenter = new JPanel(new BorderLayout(10, 10));
        pnlCenter.setBackground(Color.WHITE);
        pnlCenter.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Toolbar for adding products
        JPanel pnlTableActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlTableActions.setOpaque(false);

        JComboBox<Product> cbProducts = new JComboBox<>();
        cbProducts.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbProducts.setPreferredSize(new Dimension(250, 32));
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
        
        JButton btnAddProduct = createStyledButton("Thêm sản phẩm", COLOR_PRIMARY, Color.WHITE);
        btnAddProduct.setPreferredSize(new Dimension(140, 32));
        
        pnlTableActions.add(cbProducts);
        pnlTableActions.add(btnAddProduct);
        pnlCenter.add(pnlTableActions, BorderLayout.NORTH);

        String[] cols = {"Mã SP", "Tên sản phẩm", "Số lượng thực nhập", "Giá nhập (VNĐ)", "Thành tiền", "Hành động"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 2 || c == 3 || c == 5; // Allow editing Qty (col 2), Cost (col 3), and Action Button (col 5)
            }
        };
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(38); // Enlarged slightly to fit the interactive spinner and button nicely
        table.setShowGrid(true);
        table.setGridColor(COLOR_BG);
        table.setBackground(Color.WHITE);

        for (StockDAO.PurchaseOrderDetail item : details) {
            model.addRow(new Object[]{
                    item.productId,
                    item.productName,
                    item.quantity,
                    item.costPrice,
                    item.subtotal,
                    "Xóa"
            });
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        pnlCenter.add(scroll, BorderLayout.CENTER);
        dlg.add(pnlCenter, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBackground(new Color(248, 250, 252));
        pnlFooter.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel lblTotal = new JLabel("Tổng tiền nhập: 0đ");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotal.setForeground(COLOR_PRIMARY);
        pnlFooter.add(lblTotal, BorderLayout.WEST);

        // Update Total logic
        Runnable updateTotal = () -> {
            double grandTotal = 0;
            for (int i = 0; i < model.getRowCount(); i++) {
                try {
                    int qty = Integer.parseInt(model.getValueAt(i, 2).toString());
                    double price = Double.parseDouble(model.getValueAt(i, 3).toString());
                    double sub = qty * price;
                    model.setValueAt(sub, i, 4);
                    grandTotal += sub;
                } catch (Exception ex) {}
            }
            lblTotal.setText("Tổng tiền nhập: " + String.format("%,.0fđ", grandTotal));
        };

        table.getModel().addTableModelListener(e -> {
            int col = e.getColumn();
            if (col == 2 || col == 3) {
                updateTotal.run();
            }
        });
        updateTotal.run();

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(235);
        table.getColumnModel().getColumn(2).setPreferredWidth(130); // Quantity Spinner
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(65);  // Action Button

        // Set Custom Spinner Render/Editor on Column 2
        table.getColumnModel().getColumn(2).setCellRenderer(new QuantitySpinnerRenderer());
        table.getColumnModel().getColumn(2).setCellEditor(new QuantitySpinnerEditor(new JCheckBox(), table, updateTotal));

        // Custom Price Render
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
                super.getTableCellRendererComponent(table, val, isSel, hasF, r, c);
                setHorizontalAlignment(SwingConstants.RIGHT);
                if (val != null) {
                    try {
                        double d = Double.parseDouble(val.toString());
                        setText(String.format("%,.0fđ", d));
                    } catch (Exception e) {}
                }
                return this;
            }
        });
        
        // Custom Subtotal Render
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
                super.getTableCellRendererComponent(table, val, isSel, hasF, r, c);
                setHorizontalAlignment(SwingConstants.RIGHT);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setForeground(COLOR_TEXT_PRI);
                if (val != null) {
                    try {
                        double d = Double.parseDouble(val.toString());
                        setText(String.format("%,.0fđ", d));
                    } catch (Exception e) {}
                }
                return this;
            }
        });

        // Set Custom Delete Row Action Button on Column 5
        table.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer("Xóa", COLOR_DANGER_FG, COLOR_DANGER_BG, 55));
        table.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox(), "Xóa", COLOR_DANGER_FG, COLOR_DANGER_BG, 55, productId -> {
            // Remove the row that matches this product ID
            for (int i = 0; i < model.getRowCount(); i++) {
                if ((Integer) model.getValueAt(i, 0) == productId) {
                    model.removeRow(i);
                    updateTotal.run();
                    break;
                }
            }
        }));

        // Add Product Button Action
        btnAddProduct.addActionListener(e -> {
            Product selected = (Product) cbProducts.getSelectedItem();
            if (selected != null) {
                boolean exists = false;
                for (int i = 0; i < model.getRowCount(); i++) {
                    if ((Integer) model.getValueAt(i, 0) == selected.getId()) {
                        exists = true;
                        break;
                    }
                }
                if (exists) {
                    JOptionPane.showMessageDialog(dlg, "Sản phẩm này đã có trong danh sách nhập kho!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                double cost = selected.getPrice() * 0.6;
                model.addRow(new Object[]{
                        selected.getId(),
                        selected.getName(),
                        5,
                        cost,
                        5 * cost,
                        "Xóa"
                });
                updateTotal.run();
            }
        });

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBtns.setOpaque(false);
        JButton btnCancel = createStyledButton("Huỷ", new Color(229, 231, 235), COLOR_TEXT_PRI);
        btnCancel.setPreferredSize(new Dimension(80, 36));
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnConfirm = createStyledButton("Xác nhận nhập kho", COLOR_PRIMARY, Color.WHITE);
        btnConfirm.setPreferredSize(new Dimension(170, 36));
        btnConfirm.addActionListener(e -> {
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng thêm ít nhất một mặt hàng để nhập kho!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<StockDAO.PurchaseOrderDetail> editedDetails = new ArrayList<>();
            double grandTotal = 0;
            for (int i = 0; i < model.getRowCount(); i++) {
                try {
                    int pid = (Integer) model.getValueAt(i, 0);
                    String pName = model.getValueAt(i, 1).toString();
                    int qty = Integer.parseInt(model.getValueAt(i, 2).toString());
                    double cost = Double.parseDouble(model.getValueAt(i, 3).toString());
                    
                    if (qty <= 0) {
                        JOptionPane.showMessageDialog(dlg, "Số lượng dòng " + (i+1) + " phải lớn hơn 0!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (cost < 0) {
                        JOptionPane.showMessageDialog(dlg, "Giá nhập dòng " + (i+1) + " phải lớn hơn hoặc bằng 0!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    double sub = qty * cost;
                    editedDetails.add(new StockDAO.PurchaseOrderDetail(pid, pName, qty, cost, sub));
                    grandTotal += sub;
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dlg, "Định dạng dữ liệu dòng " + (i+1) + " không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            int employeeId = currentEmployee != null ? currentEmployee.getMaNhanVien() : 1;
            try {
                boolean ok = stockDAO.executeStockImport(orderId, employeeId, editedDetails, grandTotal);
                if (ok) {
                    JOptionPane.showMessageDialog(dlg, "Đã nhập kho thành công đơn hàng #" + orderId, "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    dlg.dispose();
                    loadImportTab();
                    loadData(); // reload main inventory
                } else {
                    JOptionPane.showMessageDialog(dlg, "Có lỗi xảy ra khi thực hiện nhập kho!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dlg, "Lỗi kết nối CSDL: " + ex.getMessage(), "Lỗi nhập kho", JOptionPane.ERROR_MESSAGE);
            }
        });
        pnlBtns.add(btnCancel);
        pnlBtns.add(btnConfirm);
        pnlFooter.add(pnlBtns, BorderLayout.EAST);
        dlg.add(pnlFooter, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private boolean isSameSupplier(String supplier, String brand) {
        if (supplier == null || brand == null) return false;
        String s = supplier.toLowerCase().trim();
        String b = brand.toLowerCase().trim();
        if (s.contains(b) || b.contains(s)) return true;
        
        String[] sWords = s.split("\\s+");
        String[] bWords = b.split("\\s+");
        for (String sw : sWords) {
            if (sw.length() < 3 || sw.equals("tnhh") || sw.equals("công") || sw.equals("ty") || sw.equals("dịch") || sw.equals("vụ")) continue;
            for (String bw : bWords) {
                if (bw.length() < 3) continue;
                if (sw.equals(bw)) return true;
            }
        }
        return false;
    }

    private void showImportDetailDialog(int importId) {
        List<StockDAO.RestockOrderDetail> details = stockDAO.getImportDetails(importId);
        if (details.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy chi tiết cho phiếu nhập kho này!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = (owner instanceof Frame)
                ? new JDialog((Frame) owner, "Chi tiết phiếu nhập kho #" + importId, true)
                : new JDialog((java.awt.Dialog) owner, "Chi tiết phiếu nhập kho #" + importId, true);
        dlg.setSize(650, 480);
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel pnlHeader = new JPanel(new GridLayout(2, 1, 0, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, COLOR_SUCCESS_FG, 0, getHeight(), new Color(30, 175, 80)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        pnlHeader.setBorder(new EmptyBorder(12, 16, 12, 16));
        JLabel lblTitle = new JLabel("CHI TIẾT PHIẾU NHẬP KHO #" + importId);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        
        JLabel lblSub = new JLabel("Thông tin lưu trữ lịch sử nhập hàng (Chỉ đọc)");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(210, 255, 220));
        pnlHeader.add(lblTitle);
        pnlHeader.add(lblSub);
        dlg.add(pnlHeader, BorderLayout.NORTH);

        String[] cols = {"Mã SP", "Tên sản phẩm", "Số lượng nhập", "Giá nhập", "Thành tiền"};
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
        for (StockDAO.RestockOrderDetail item : details) {
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

        JLabel lblTotal = new JLabel("Tổng tiền đã nhập: " + String.format("%,.0fđ", total));
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotal.setForeground(COLOR_SUCCESS_FG);
        pnlFooter.add(lblTotal, BorderLayout.WEST);

        JButton btnClose = createStyledButton("Đóng", COLOR_SUCCESS_FG, Color.WHITE);
        btnClose.setPreferredSize(new Dimension(100, 36));
        btnClose.addActionListener(e -> dlg.dispose());
        pnlFooter.add(btnClose, BorderLayout.EAST);
        dlg.add(pnlFooter, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    // ─── AUDIT TAB METHODS ────────────────────────────────────────────────
    private void loadAuditTab() {
        modelAudit.setRowCount(0);
        List<StockDAO.AuditOrder> list = stockDAO.getAuditHistory();
        for (StockDAO.AuditOrder ao : list) {
            modelAudit.addRow(new Object[]{
                    ao.id,
                    ao.date != null ? new SimpleDateFormat("dd/MM/yyyy HH:mm").format(ao.date) : "—",
                    ao.employeeName != null ? ao.employeeName : "—",
                    ao.note != null && !ao.note.isEmpty() ? ao.note : "—",
                    "Xem chi tiết"
            });
        }
    }

    private void showCreateAuditDialog() {
        List<Product> products = stockDAO.getAllProducts();
        if (products.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có sản phẩm nào trong hệ thống để kiểm kê!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = (owner instanceof Frame)
                ? new JDialog((Frame) owner, "Lập phiếu kiểm kê kho mới", true)
                : new JDialog((java.awt.Dialog) owner, "Lập phiếu kiểm kê kho mới", true);
        dlg.setSize(800, 600);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel pnlHeader = new JPanel(new GridLayout(2, 1, 0, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, COLOR_ORANGE_ACTIVE, 0, getHeight(), new Color(217, 119, 6)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        pnlHeader.setBorder(new EmptyBorder(12, 16, 12, 16));
        JLabel lblTitle = new JLabel("LẬP PHIẾU KIỂM KÊ KHO");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        
        JLabel lblSub = new JLabel("Nhập số lượng thực tế kiểm đếm. Hệ thống tự động tính số lượng thừa/thiếu.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(254, 243, 199));
        pnlHeader.add(lblTitle);
        pnlHeader.add(lblSub);
        dlg.add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlCenter = new JPanel(new BorderLayout(10, 10));
        pnlCenter.setBackground(Color.WHITE);
        pnlCenter.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel pnlNote = new JPanel(new BorderLayout(10, 0));
        pnlNote.setOpaque(false);
        JLabel lblNote = new JLabel("Ghi chú kiểm kê: ");
        lblNote.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNote.setForeground(COLOR_TEXT_PRI);
        pnlNote.add(lblNote, BorderLayout.WEST);

        JTextField txtNote = new JTextField();
        txtNote.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNote.putClientProperty("JTextField.placeholderText", "Ví dụ: Kiểm kê định kỳ cuối tháng 5...");
        txtNote.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(5, 8, 5, 8)
        ));
        pnlNote.add(txtNote, BorderLayout.CENTER);
        pnlCenter.add(pnlNote, BorderLayout.NORTH);

        String[] cols = {"Mã SP", "Tên sản phẩm", "Tồn hệ thống", "Số thực tế", "Lệch", "Lý do"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 3 || c == 5;
            }
        };

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(36);
        table.setShowGrid(true);
        table.setGridColor(COLOR_BG);
        table.setBackground(Color.WHITE);

        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(260);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(180);

        for (Product p : products) {
            model.addRow(new Object[]{
                    p.getId(),
                    p.getName(),
                    p.getStock(),
                    p.getStock(),
                    0,
                    ""
            });
        }

        table.getModel().addTableModelListener(e -> {
            int row = e.getFirstRow();
            int col = e.getColumn();
            if (col == 3) {
                try {
                    Object sysVal = model.getValueAt(row, 2);
                    Object actVal = model.getValueAt(row, 3);
                    int sys = Integer.parseInt(sysVal.toString());
                    int act = Integer.parseInt(actVal.toString());
                    if (act < 0) {
                        JOptionPane.showMessageDialog(dlg, "Số thực tế không được âm!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        model.setValueAt(sys, row, 3);
                        return;
                    }
                    int diff = act - sys;
                    model.setValueAt(diff, row, 4);
                } catch (Exception ex) {
                    Object sysVal = model.getValueAt(row, 2);
                    model.setValueAt(sysVal, row, 3);
                }
            }
        });

        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
                super.getTableCellRendererComponent(table, val, isSel, hasF, r, c);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setForeground(COLOR_TEXT_SEC);
                return this;
            }
        });

        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
                super.getTableCellRendererComponent(table, val, isSel, hasF, r, c);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setForeground(COLOR_ORANGE_ACTIVE);
                return this;
            }
        });

        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
                super.getTableCellRendererComponent(table, val, isSel, hasF, r, c);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                int diff = val instanceof Integer ? (Integer) val : 0;
                if (diff > 0) {
                    setText("+" + diff);
                    setForeground(COLOR_SUCCESS_FG);
                } else if (diff < 0) {
                    setText(String.valueOf(diff));
                    setForeground(COLOR_DANGER_FG);
                } else {
                    setText("0");
                    setForeground(COLOR_TEXT_SEC);
                }
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        pnlCenter.add(scroll, BorderLayout.CENTER);

        dlg.add(pnlCenter, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        pnlFooter.setBackground(new Color(248, 250, 252));
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));

        JButton btnCancel = createStyledButton("Huỷ bỏ", new Color(229, 231, 235), COLOR_TEXT_PRI);
        btnCancel.setPreferredSize(new Dimension(100, 38));
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnConfirm = createStyledButton("Xác nhận kiểm kê", COLOR_ORANGE_ACTIVE, Color.WHITE);
        btnConfirm.setPreferredSize(new Dimension(180, 38));
        btnConfirm.addActionListener(e -> {
            List<StockDAO.AuditOrderDetail> items = new ArrayList<>();
            for (int i = 0; i < model.getRowCount(); i++) {
                int pid = (Integer) model.getValueAt(i, 0);
                String pName = model.getValueAt(i, 1).toString();
                int sys = (Integer) model.getValueAt(i, 2);
                int act = Integer.parseInt(model.getValueAt(i, 3).toString());
                int diff = (Integer) model.getValueAt(i, 4);
                String reason = model.getValueAt(i, 5) != null ? model.getValueAt(i, 5).toString().trim() : "";

                items.add(new StockDAO.AuditOrderDetail(pid, pName, act, diff, reason));
            }

            int employeeId = currentEmployee != null ? currentEmployee.getMaNhanVien() : 1;
            String note = txtNote.getText().trim();
            boolean ok = stockDAO.createAuditOrder(employeeId, note, items);
            if (ok) {
                JOptionPane.showMessageDialog(dlg, "Đã lưu phiếu kiểm kê! Tồn kho hệ thống được cập nhật theo số thực tế.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
                loadAuditTab();
                loadData();
            } else {
                JOptionPane.showMessageDialog(dlg, "Có lỗi xảy ra khi tạo phiếu kiểm kê!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        pnlFooter.add(btnCancel);
        pnlFooter.add(btnConfirm);
        dlg.add(pnlFooter, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private void showAuditDetailDialog(int auditId) {
        List<StockDAO.AuditOrderDetail> details = stockDAO.getAuditDetails(auditId);
        if (details.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy chi tiết cho phiếu kiểm kê này!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = (owner instanceof Frame)
                ? new JDialog((Frame) owner, "Chi tiết phiếu kiểm kê #" + auditId, true)
                : new JDialog((java.awt.Dialog) owner, "Chi tiết phiếu kiểm kê #" + auditId, true);
        dlg.setSize(700, 500);
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel pnlHeader = new JPanel(new GridLayout(2, 1, 0, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, COLOR_ORANGE_ACTIVE, 0, getHeight(), new Color(217, 119, 6)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        pnlHeader.setBorder(new EmptyBorder(12, 16, 12, 16));
        JLabel lblTitle = new JLabel("CHI TIẾT PHIẾU KIỂM KÊ #" + auditId);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        
        JLabel lblSub = new JLabel("Bản ghi đối soát và chênh lệch chênh lệch tồn kho thực tế");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(254, 243, 199));
        pnlHeader.add(lblTitle);
        pnlHeader.add(lblSub);
        dlg.add(pnlHeader, BorderLayout.NORTH);

        String[] cols = {"Mã SP", "Tên sản phẩm", "Số thực tế", "Lệch thừa/thiếu", "Lý do chênh lệch"};
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

        for (StockDAO.AuditOrderDetail item : details) {
            model.addRow(new Object[]{
                    item.productId,
                    item.productName,
                    item.actualQty,
                    item.diffQty,
                    item.reason != null && !item.reason.isEmpty() ? item.reason : "—"
            });
        }

        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
                super.getTableCellRendererComponent(table, val, isSel, hasF, r, c);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setForeground(COLOR_TEXT_PRI);
                return this;
            }
        });

        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
                super.getTableCellRendererComponent(table, val, isSel, hasF, r, c);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                int diff = val instanceof Integer ? (Integer) val : 0;
                if (diff > 0) {
                    setText("+" + diff);
                    setForeground(COLOR_SUCCESS_FG);
                } else if (diff < 0) {
                    setText(String.valueOf(diff));
                    setForeground(COLOR_DANGER_FG);
                } else {
                    setText("0");
                    setForeground(COLOR_TEXT_SEC);
                }
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(new EmptyBorder(15, 15, 15, 15));
        dlg.add(scroll, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        pnlFooter.setBackground(new Color(248, 250, 252));
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));

        JButton btnClose = createStyledButton("Đóng", COLOR_ORANGE_ACTIVE, Color.WHITE);
        btnClose.setPreferredSize(new Dimension(100, 36));
        btnClose.addActionListener(e -> dlg.dispose());
        pnlFooter.add(btnClose);
        dlg.add(pnlFooter, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    // ─── CUSTOM TABLECELL RENDERER & EDITOR FOR ACTION BUTTONS ─────────────
    public static class ButtonRenderer extends JPanel implements TableCellRenderer {
        private final JButton button;
        public ButtonRenderer(String text, Color bg, Color fg) {
            this(text, bg, fg, 100);
        }
        public ButtonRenderer(String text, Color bg, Color fg, int width) {
            setLayout(new FlowLayout(FlowLayout.CENTER, 0, 5));
            setOpaque(true);

            button = new JButton(text);
            button.setFont(new Font("Segoe UI", Font.BOLD, 12));
            button.setBackground(bg);
            button.setForeground(fg);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setPreferredSize(new Dimension(width, 26));
            button.putClientProperty("FlatLaf.style", "arc: 8");

            add(button);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
            setBackground(isSel ? table.getSelectionBackground() : table.getBackground());
            return this;
        }
    }

    public static class ButtonEditor extends DefaultCellEditor {
        private final JPanel panel;
        protected JButton button;
        private String label;
        private boolean isPushed;
        private final java.util.function.Consumer<Integer> action;
        private JTable table;

        public ButtonEditor(JCheckBox checkBox, String text, Color bg, Color fg, java.util.function.Consumer<Integer> action) {
            this(checkBox, text, bg, fg, 100, action);
        }

        public ButtonEditor(JCheckBox checkBox, String text, Color bg, Color fg, int width, java.util.function.Consumer<Integer> action) {
            super(checkBox);
            this.action = action;

            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
            panel.setOpaque(true);

            button = new JButton(text);
            button.setFont(new Font("Segoe UI", Font.BOLD, 12));
            button.setBackground(bg);
            button.setForeground(fg);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setPreferredSize(new Dimension(width, 26));
            button.putClientProperty("FlatLaf.style", "arc: 8");
            button.addActionListener(e -> fireEditingStopped());

            panel.add(button);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object val, boolean isSel, int r, int c) {
            this.table = table;
            isPushed = true;
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    int id = (Integer) table.getValueAt(row, 0);
                    action.accept(id);
                }
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }

    private JLabel buildFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(COLOR_TEXT_PRI);
        return lbl;
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

    public static String getProductImageSlug(String name) {
        if (name == null) return "placeholder.png";
        String lower = name.toLowerCase()
            .replace('đ', 'd')
            .replace('á', 'a').replace('à', 'a').replace('ả', 'a').replace('ã', 'a').replace('ạ', 'a')
            .replace('â', 'a').replace('ấ', 'a').replace('ầ', 'a').replace('ẩ', 'a').replace('ẫ', 'a').replace('ậ', 'a')
            .replace('ă', 'a').replace('ắ', 'a').replace('ằ', 'a').replace('ẳ', 'a').replace('ẵ', 'a').replace('ặ', 'a')
            .replace('é', 'e').replace('è', 'e').replace('ẻ', 'e').replace('ẽ', 'e').replace('ẹ', 'e')
            .replace('ê', 'e').replace('ế', 'e').replace('ề', 'e').replace('ể', 'e').replace('ễ', 'e').replace('ệ', 'e')
            .replace('í', 'i').replace('ì', 'i').replace('ỉ', 'i').replace('ĩ', 'i').replace('ị', 'i')
            .replace('ó', 'o').replace('ò', 'o').replace('ỏ', 'o').replace('õ', 'o').replace('ọ', 'o')
            .replace('ô', 'o').replace('ố', 'o').replace('ồ', 'o').replace('ổ', 'o').replace('ỗ', 'o').replace('ộ', 'o')
            .replace('ơ', 'o').replace('ớ', 'o').replace('ờ', 'o').replace('ở', 'o').replace('ỡ', 'o').replace('ợ', 'o')
            .replace('ú', 'u').replace('ù', 'u').replace('ủ', 'u').replace('ũ', 'u').replace('ụ', 'u')
            .replace('ư', 'u').replace('ứ', 'u').replace('ừ', 'u').replace('ử', 'u').replace('ữ', 'u').replace('ự', 'u')
            .replace('ý', 'y').replace('ỳ', 'y').replace('ỷ', 'y').replace('ỹ', 'y').replace('ỵ', 'y');
        return lower.replaceAll("[^a-z0-9]", "_") + ".png";
    }

    private ImageIcon getProductImageByName(String name) {
        if (name == null || name.isEmpty()) return null;
        String slug = getProductImageSlug(name);
        try {
            java.net.URL imgURL = getClass().getClassLoader().getResource("images/" + slug);
            if (imgURL != null) {
                return new ImageIcon(imgURL);
            }
        } catch (Exception e) {}

        String filename = null;
        String lower = name.toLowerCase();
        if (lower.contains("royal canin") || lower.contains("royal")) {
            filename = "royal_canin.png";
        } else if (lower.contains("whiskas")) {
            filename = "whiskas.png";
        } else if (lower.contains("vòng cổ") || lower.contains("xích") || lower.contains("collar") || lower.contains("dây dắt")) {
            filename = "dog_collar.png";
        } else if (lower.contains("nệm") || lower.contains("giường") || lower.contains("bed")) {
            filename = "pet_bed.png";
        } else if (lower.contains("shampoo") || lower.contains("sữa tắm") || lower.contains("dầu gội")) {
            filename = "pet_shampoo.png";
        } else if (lower.contains("máy lọc nước") || lower.contains("máy uống nước") || lower.contains("fountain") || lower.contains("đài phun nước")) {
            filename = "water_fountain.png";
        } else if (lower.contains("cần câu") || lower.contains("lông vũ") || lower.contains("cát tặc")) {
            filename = "cat_toy.png";
        } else if (lower.contains("đồ chơi") || lower.contains("xương") || lower.contains("toy") || lower.contains("bóng")) {
            filename = "dog_toy.png";
        } else if (lower.contains("cát") || lower.contains("litter") || lower.contains("vệ sinh")) {
            filename = "cat_litter.png";
        } else if (lower.contains("chuồng") || lower.contains("lồng") || lower.contains("carrier") || lower.contains("vận chuyển") || lower.contains("túi xách")) {
            filename = "pet_carrier.png";
        } else if (lower.contains("lược") || lower.contains("bàn chải") || lower.contains("brush") || lower.contains("chải lông") || lower.contains("tỉa lông") || lower.contains("cắt tỉa")) {
            filename = "grooming_brush.png";
        } else if (lower.contains("smartheart") || lower.contains("smart")) {
            filename = "smartheart_puppy.png";
        } else if (lower.contains("ciao") || lower.contains("churu") || lower.contains("súp thưởng")) {
            filename = "ciao_churu.png";
        } else if (lower.contains("gel") || lower.contains("megaderm") || lower.contains("dinh dưỡng")) {
            filename = "virbac_megaderm.png";
        } else if (lower.contains("khách sạn") || lower.contains("deluxe") || lower.contains("phòng")) {
            filename = "pet_hotel.png";
        } else if (lower.contains("đưa đón") || lower.contains("taxi")) {
            filename = "pet_taxi.png";
        } else if (lower.contains("găng tay") || lower.contains("y tế")) {
            filename = "medical_gloves.png";
        }
        
        if (filename != null) {
            try {
                java.net.URL imgURL = getClass().getClassLoader().getResource("images/" + filename);
                if (imgURL != null) {
                    return new ImageIcon(imgURL);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // ─── CUSTOM SPINNER COMPONENTS FOR QUANTITY IN TABLE ──────────────────
    public static class SpinnerPanel extends JPanel {
        public JButton btnMinus;
        public JButton btnPlus;
        public JLabel lblValue;

        public SpinnerPanel() {
            setLayout(new BorderLayout(5, 0));
            setOpaque(true);
            setBackground(Color.WHITE);

            btnMinus = new JButton("-");
            btnMinus.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnMinus.setFocusPainted(false);
            btnMinus.setPreferredSize(new Dimension(24, 24));
            btnMinus.setBackground(new Color(241, 245, 249));
            btnMinus.setForeground(new Color(15, 23, 42));
            btnMinus.putClientProperty("FlatLaf.style", "arc: 4");

            btnPlus = new JButton("+");
            btnPlus.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnPlus.setFocusPainted(false);
            btnPlus.setPreferredSize(new Dimension(24, 24));
            btnPlus.setBackground(new Color(241, 245, 249));
            btnPlus.setForeground(new Color(15, 23, 42));
            btnPlus.putClientProperty("FlatLaf.style", "arc: 4");

            lblValue = new JLabel("1", SwingConstants.CENTER);
            lblValue.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblValue.setPreferredSize(new Dimension(30, 24));

            add(btnMinus, BorderLayout.WEST);
            add(lblValue, BorderLayout.CENTER);
            add(btnPlus, BorderLayout.EAST);
        }
    }

    public static class QuantitySpinnerRenderer implements TableCellRenderer {
        private final SpinnerPanel panel = new SpinnerPanel();
        @Override
        public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
            panel.lblValue.setText(val != null ? val.toString() : "1");
            panel.setBackground(isSel ? table.getSelectionBackground() : table.getBackground());
            return panel;
        }
    }

    public static class QuantitySpinnerEditor extends DefaultCellEditor {
        private final SpinnerPanel panel = new SpinnerPanel();
        private int value = 1;
        private JTable table;
        private int currentRow;

        public QuantitySpinnerEditor(JCheckBox checkBox, JTable table, Runnable onUpdate) {
            super(checkBox);
            this.table = table;

            panel.btnMinus.addActionListener(e -> {
                if (value > 1) {
                    value--;
                    panel.lblValue.setText(String.valueOf(value));
                    table.getModel().setValueAt(value, currentRow, 2);
                    onUpdate.run();
                }
            });

            panel.btnPlus.addActionListener(e -> {
                value++;
                panel.lblValue.setText(String.valueOf(value));
                table.getModel().setValueAt(value, currentRow, 2);
                onUpdate.run();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object val, boolean isSel, int r, int c) {
            this.currentRow = r;
            try {
                value = Integer.parseInt(val.toString());
            } catch (Exception ex) {
                value = 1;
            }
            panel.lblValue.setText(String.valueOf(value));
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return value;
        }
    }
}
