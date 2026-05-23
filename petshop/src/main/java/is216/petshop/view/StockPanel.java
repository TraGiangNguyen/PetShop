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
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);

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
        pnlHeader.add(pnlTitle, BorderLayout.WEST);

        // Action Buttons on Header
        JPanel pnlActionHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlActionHeader.setOpaque(false);

        JButton btnRestock = createStyledButton("Tạo hóa đơn nhập kho 📦", COLOR_PRIMARY, Color.WHITE);
        btnRestock.setPreferredSize(new Dimension(200, 42));
        btnRestock.addActionListener(e -> showRestockDialog());
        pnlActionHeader.add(btnRestock);
        pnlHeader.add(pnlActionHeader, BorderLayout.EAST);

        add(pnlHeader, BorderLayout.NORTH);

        // ─── CENTER BODY ──────────────────────────────────────────────────────
        JPanel pnlBody = new JPanel(new BorderLayout(0, 16));
        pnlBody.setOpaque(false);

        // 1. Dashboard quick metrics
        JPanel pnlMetrics = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlMetrics.setOpaque(false);
        pnlMetrics.setPreferredSize(new Dimension(0, 85));

        JPanel cardTotal = createMetricCard("TỔNG SỐ SẢN PHẨM", "0 sản phẩm", COLOR_PRIMARY);
        JPanel cardOut = createMetricCard("MẶT HÀNG HẾT HÀNG", "0 sản phẩm", COLOR_DANGER_FG);

        pnlMetrics.add(cardTotal);
        pnlMetrics.add(cardOut);
        pnlBody.add(pnlMetrics, BorderLayout.NORTH);

        // 2. Search & Filter Area
        JPanel pnlFilter = new JPanel(new BorderLayout(10, 0));
        pnlFilter.setOpaque(false);

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
        pnlBody.add(pnlFilter, BorderLayout.CENTER);

        // 3. JTable Card Container
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

        pnlBody.add(pnlTableCard, BorderLayout.SOUTH);

        add(pnlBody, BorderLayout.CENTER);
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
        pnlSheetPaper.add(pnlMetaInfo, BorderLayout.NORTH);

        // Editable Sheet Grid Table
        String[] sheetColumns = {"Mã SP", "Tên sản phẩm", "Số lượng nhập", "Giá nhập (VNĐ)", "Thành tiền"};
        DefaultTableModel sheetModel = new DefaultTableModel(sheetColumns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 2 || c == 3; // Only Quantity and Cost Price are editable
            }
        };

        JTable tblSheet = new JTable(sheetModel);
        tblSheet.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblSheet.setRowHeight(36);
        tblSheet.setShowGrid(true);
        tblSheet.setGridColor(COLOR_BG);
        tblSheet.setBackground(Color.WHITE);
        
        tblSheet.getColumnModel().getColumn(0).setPreferredWidth(60);  // ID
        tblSheet.getColumnModel().getColumn(1).setPreferredWidth(280); // Name
        tblSheet.getColumnModel().getColumn(2).setPreferredWidth(100); // Qty
        tblSheet.getColumnModel().getColumn(3).setPreferredWidth(120); // Cost Price
        tblSheet.getColumnModel().getColumn(4).setPreferredWidth(130); // Subtotal

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
                        subtotal
                });
            }
        }

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

        tblSheet.getModel().addTableModelListener(e -> updateSum.run());
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
        JButton btnAddNewProduct = createStyledButton("➕ Thêm sản phẩm mới", new Color(241, 245, 249), COLOR_TEXT_PRI);
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

        JButton btnConfirm = createStyledButton("Xác nhận nhập kho 💾", COLOR_PRIMARY, Color.WHITE);
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
                            10 * cost
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

    private ImageIcon getProductImageByName(String name) {
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
}
