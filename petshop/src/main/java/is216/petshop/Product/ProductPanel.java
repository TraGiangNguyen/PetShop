package is216.petshop.Product;

import is216.petshop.Product.Product;
import is216.petshop.Product.ProductDAO;
import is216.petshop.Product.ProductDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class ProductPanel extends JPanel {

    // --- DESIGN SYSTEM ---
    private static final Color BG       = new Color(245, 246, 250);
    private static final Color WHITE    = Color.WHITE;
    private static final Color PRIMARY  = new Color(79, 70, 229);
    private static final Color TXT      = new Color(17, 24, 39);
    private static final Color TXT2     = new Color(107, 114, 128);
    private static final Color BORDER   = new Color(229, 231, 235);
    private static final Color ROW_LINE = new Color(243, 244, 246);
    private static final Color SEL_BG   = new Color(245, 243, 255);
    private static final Color RED      = new Color(220, 38, 38);
    
    private static final Color COLOR_DANGER_BG  = new Color(254, 226, 226);
    private static final Color COLOR_DANGER_FG  = new Color(239, 68, 68);
    private static final Color COLOR_SUCCESS_BG = new Color(220, 252, 231);
    private static final Color COLOR_SUCCESS_FG = new Color(34, 197, 94);

    private final ProductDAO dao = new ProductDAO();
    private List<Product> list = new ArrayList<>();
    private DefaultTableModel model;
    private JTable table;
    private JTextField txtSearch;

    public ProductPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        
        add(buildTop(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        
        loadData();
    }

    private JPanel buildTop() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG);
        bar.setBorder(new EmptyBorder(0, 0, 25, 0));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(BG);

        JLabel lbTitle = new JLabel("Quản lý sản phẩm & dịch vụ");
        lbTitle.setFont(new Font("Helvetica Neue", Font.BOLD, 30));
        lbTitle.setForeground(TXT);

        JLabel lbSub = new JLabel("Quản lý danh mục sản phẩm, dịch vụ, đơn giá, thuế suất và thuộc tính hàng hóa");
        lbSub.setFont(new Font("Helvetica Neue", Font.PLAIN, 15));
        lbSub.setForeground(TXT2);

        left.add(lbTitle);
        left.add(Box.createVerticalStrut(5));
        left.add(lbSub);

        bar.add(left, BorderLayout.CENTER);
        return bar;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setBackground(BG);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(BG);

        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        txtSearch.setPreferredSize(new Dimension(300, 45));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(15, BORDER), 
            new EmptyBorder(5, 15, 5, 15)));
        
        String ph = "Tìm theo Tên, Nhãn hiệu, Mã vạch...";
        txtSearch.setText(ph);
        txtSearch.setForeground(TXT2);
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override 
            public void focusGained(FocusEvent e) { 
                if (txtSearch.getText().equals(ph)) { 
                    txtSearch.setText(""); 
                    txtSearch.setForeground(TXT); 
                } 
            }
            @Override 
            public void focusLost(FocusEvent e) { 
                if (txtSearch.getText().isEmpty()) { 
                    txtSearch.setText(ph); 
                    txtSearch.setForeground(TXT2); 
                } 
            }
        });

        FlatBtn btnSearch = new FlatBtn("Tìm kiếm");
        btnSearch.setPreferredSize(new Dimension(100, 42));
        btnSearch.addActionListener(e -> doSearch());

        FlatBtn btnRefresh = new FlatBtn("Làm mới");
        btnRefresh.setPreferredSize(new Dimension(100, 42));
        btnRefresh.addActionListener(e -> {
            txtSearch.setText(ph);
            txtSearch.setForeground(TXT2);
            loadData();
        });

        JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftGroup.setBackground(BG);
        leftGroup.add(txtSearch);
        leftGroup.add(btnSearch);
        leftGroup.add(btnRefresh);

        FlatBtn btnAdd = new FlatBtn("+ Thêm sản phẩm");
        btnAdd.setPreferredSize(new Dimension(170, 42));
        btnAdd.addActionListener(e -> showDialog(null, false));

        FlatBtn btnAddService = new FlatBtn("+ Thêm dịch vụ", new Color(34, 197, 94));
        btnAddService.setPreferredSize(new Dimension(160, 42));
        btnAddService.addActionListener(e -> showDialog(null, true));

        JPanel rightGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightGroup.setBackground(BG);
        rightGroup.add(btnAdd);
        rightGroup.add(btnAddService);

        toolbar.add(leftGroup, BorderLayout.WEST);
        toolbar.add(rightGroup, BorderLayout.EAST);

        TableCard card = new TableCard();
        card.setLayout(new BorderLayout());
        buildTable(card);

        body.add(toolbar, BorderLayout.NORTH);
        body.add(card, BorderLayout.CENTER);
        return body;
    }

    private void buildTable(JPanel card) {
        model = new DefaultTableModel(
            new Object[]{"Mã SP", "Hình ảnh", "Tên sản phẩm", "Nhãn hiệu", "Đơn vị", "Đơn giá", "Thuế", "Tồn kho", "Trạng thái", "Thao tác"}, 0) {
            @Override 
            public boolean isCellEditable(int r, int c) { 
                return c == 9; 
            }
        };

        table = new JTable(model);
        table.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        table.setRowHeight(72); 
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER);
        table.setBackground(WHITE);
        table.setSelectionBackground(SEL_BG);
        table.setSelectionForeground(TXT);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFocusable(false);

        JTableHeader hdr = table.getTableHeader();
        hdr.setReorderingAllowed(false);
        hdr.setResizingAllowed(false);
        hdr.setBackground(WHITE);
        hdr.setPreferredSize(new Dimension(0, 55));
        hdr.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        hdr.setDefaultRenderer(new HdrRender());

        table.getColumnModel().getColumn(0).setPreferredWidth(60);   // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(70);   // Image
        table.getColumnModel().getColumn(2).setPreferredWidth(260);  // Name
        table.getColumnModel().getColumn(3).setPreferredWidth(120);  // Brand
        table.getColumnModel().getColumn(4).setPreferredWidth(80);   // Unit
        table.getColumnModel().getColumn(5).setPreferredWidth(110);  // Price
        table.getColumnModel().getColumn(6).setPreferredWidth(70);   // Tax
        table.getColumnModel().getColumn(7).setPreferredWidth(90);   // Stock
        table.getColumnModel().getColumn(8).setPreferredWidth(110);  // Status
        table.getColumnModel().getColumn(9).setPreferredWidth(100);  // Action

        table.getColumnModel().getColumn(0).setCellRenderer(new IdRender());
        table.getColumnModel().getColumn(1).setCellRenderer(new ImageRender());
        table.getColumnModel().getColumn(2).setCellRenderer(new NameRender());
        table.getColumnModel().getColumn(3).setCellRenderer(new TextRender());
        table.getColumnModel().getColumn(4).setCellRenderer(new CenterTextRender());
        table.getColumnModel().getColumn(5).setCellRenderer(new PriceRender());
        table.getColumnModel().getColumn(6).setCellRenderer(new TaxRender());
        table.getColumnModel().getColumn(7).setCellRenderer(new StockRender());
        table.getColumnModel().getColumn(8).setCellRenderer(new StatusRender());
        table.getColumnModel().getColumn(9).setCellRenderer(new ActRender());
        table.getColumnModel().getColumn(9).setCellEditor(new ActEditor());

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(WHITE);
        card.add(sp, BorderLayout.CENTER);
    }

    private void loadData() {
        list = dao.getAllProducts();
        renderTable(list);
    }

    private void renderTable(List<Product> data) {
        model.setRowCount(0);
        for (Product p : data) {
            model.addRow(new Object[]{ 
                p.getId(), 
                p, 
                p.getName(), 
                p.getCategory(), 
                p.getUnit(), 
                p.getPrice(), 
                p.getTax(), 
                p.getStock(), 
                p, 
                "" 
            });
        }
    }

    private void doSearch() {
        String kw = txtSearch.getText().trim();
        if (kw.isEmpty() || kw.equals("Tìm theo Tên, Nhãn hiệu, Mã vạch...")) { 
            loadData(); 
            return; 
        }
        renderTable(dao.searchProducts(kw));
    }

    private void showDialog(Product prod) {
        showDialog(prod, false);
    }

    private void showDialog(Product prod, boolean isServicePreset) {
        Window parent = SwingUtilities.getWindowAncestor(this);
        ProductDialog d = new ProductDialog((Frame) parent, prod, dao, this::loadData, isServicePreset);
        d.setVisible(true);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    // --- CUSTOM RENDERERS ---

    class HdrRender extends DefaultTableCellRenderer {
        @Override 
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            JLabel l = new JLabel(v != null ? v.toString() : "");
            l.setFont(new Font("Helvetica Neue", Font.BOLD, 14));
            l.setForeground(TXT2);
            if (c == 0 || c == 1 || c == 4 || c == 6 || c == 7 || c == 8 || c == 9) {
                l.setHorizontalAlignment(SwingConstants.CENTER);
            } else if (c == 5) {
                l.setHorizontalAlignment(SwingConstants.RIGHT);
                l.setBorder(new EmptyBorder(0, 0, 0, 15));
            } else {
                l.setHorizontalAlignment(SwingConstants.LEFT);
                l.setBorder(new EmptyBorder(0, 15, 0, 0));
            }
            return l;
        }
    }

    class IdRender extends DefaultTableCellRenderer {
        @Override 
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, f, r, c);
            setFont(new Font("Helvetica Neue", Font.BOLD, 14));
            setForeground(TXT2);
            setBackground(sel ? SEL_BG : WHITE);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
            return this;
        }
    }

    class NameRender extends DefaultTableCellRenderer {
        @Override 
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, f, r, c);
            setFont(new Font("Helvetica Neue", Font.BOLD, 14));
            setForeground(TXT);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(0, 15, 0, 0)
            ));
            setBackground(sel ? SEL_BG : WHITE);
            return this;
        }
    }

    class TextRender extends DefaultTableCellRenderer {
        @Override 
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, f, r, c);
            setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
            setForeground(TXT);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(0, 15, 0, 0)
            ));
            setBackground(sel ? SEL_BG : WHITE);
            return this;
        }
    }

    class CenterTextRender extends DefaultTableCellRenderer {
        @Override 
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, f, r, c);
            setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
            setForeground(TXT);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
            setBackground(sel ? SEL_BG : WHITE);
            return this;
        }
    }

    class PriceRender extends DefaultTableCellRenderer {
        @Override 
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, f, r, c);
            setFont(new Font("Helvetica Neue", Font.BOLD, 14));
            setForeground(TXT);
            setHorizontalAlignment(SwingConstants.RIGHT);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(0, 0, 0, 15)
            ));
            setBackground(sel ? SEL_BG : WHITE);
            if (v instanceof Long || v instanceof Integer) {
                setText(String.format("%,dđ", v));
            }
            return this;
        }
    }

    class TaxRender extends DefaultTableCellRenderer {
        @Override 
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, f, r, c);
            setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
            setForeground(TXT2);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
            setBackground(sel ? SEL_BG : WHITE);
            if (v != null) {
                setText(v.toString() + "%");
            }
            return this;
        }
    }

    class StockRender extends DefaultTableCellRenderer {
        @Override 
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, f, r, c);
            setFont(new Font("Helvetica Neue", Font.BOLD, 14));
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
            
            int qty = v instanceof Integer ? (Integer) v : 0;
            if (qty <= 0) {
                setForeground(COLOR_DANGER_FG);
                setBackground(COLOR_DANGER_BG);
            } else if (qty <= 3) {
                setForeground(new Color(217, 119, 6)); // Amber
                setBackground(new Color(254, 243, 199)); // Amber Light
            } else {
                setForeground(PRIMARY);
                setBackground(sel ? SEL_BG : WHITE);
            }
            return this;
        }
    }

    class ImageRender extends DefaultTableCellRenderer {
        @Override 
        public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
            super.getTableCellRendererComponent(table, val, isSel, hasF, r, c);
            setText("");
            setHorizontalAlignment(SwingConstants.CENTER);
            setBackground(isSel ? table.getSelectionBackground() : WHITE);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
            
            if (val instanceof Product) {
                Product p = (Product) val;
                ImageIcon img = getProductImageByName(p.getName());
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
    }

    class StatusRender extends DefaultTableCellRenderer {
        @Override 
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 22)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (v instanceof Product) {
                        Product prod = (Product) v;
                        boolean active = prod.getActiveSell() == 1 && prod.getActiveBuy() == 1;
                        Color bg = active ? COLOR_SUCCESS_BG : COLOR_DANGER_BG;
                        g2.setColor(bg);
                        g2.fill(new RoundRectangle2D.Float(10, 2, getWidth() - 20, getHeight() - 4, 12, 12));
                    }
                    g2.dispose();
                }
            };
            p.setOpaque(true);
            p.setBackground(sel ? SEL_BG : WHITE);
            p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
            
            if (v instanceof Product) {
                Product prod = (Product) v;
                boolean active = prod.getActiveSell() == 1 && prod.getActiveBuy() == 1;
                String labelText = active ? "Kinh doanh" : "Ngừng bán";
                Color textCol = active ? COLOR_SUCCESS_FG : COLOR_DANGER_FG;
                
                JLabel lbl = new JLabel(labelText);
                lbl.setFont(new Font("Helvetica Neue", Font.BOLD, 12));
                lbl.setForeground(textCol);
                p.add(lbl);
            }
            return p;
        }
    }

    class ActRender implements TableCellRenderer {
        @Override 
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            JPanel p = mkActPanel(sel ? SEL_BG : WHITE, false);
            p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
            return p;
        }
    }

    class ActEditor extends DefaultCellEditor {
        JPanel panel; 
        IcoBtn bEdit, bDel; 
        int cur = -1;
        
        ActEditor() {
            super(new JCheckBox());
            setClickCountToStart(1);
            bEdit = new IcoBtn(true, true); 
            bDel = new IcoBtn(false, true);
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 22));
            panel.add(bEdit); 
            panel.add(bDel);
            bEdit.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) { 
                    fireEditingStopped(); 
                    if (cur >= 0) {
                        showDialog(list.get(cur)); 
                    }
                }
            });
            bDel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) { 
                    fireEditingStopped(); 
                    if (cur >= 0) {
                        Product p = list.get(cur);
                        int opt = JOptionPane.showConfirmDialog(
                            null, 
                            "Bạn có chắc muốn xoá sản phẩm \"" + p.getName() + "\"?\n(Nếu sản phẩm có trong hoá đơn, hệ thống sẽ tự động ngừng kinh doanh thay vì xoá cứng)", 
                            "Xác nhận xoá", 
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                        );
                        if (opt == JOptionPane.YES_OPTION) {
                            boolean ok = dao.deleteProduct(p.getId());
                            if (ok) {
                                JOptionPane.showMessageDialog(null, "Thao tác thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                                loadData();
                            } else {
                                showError("Có lỗi xảy ra khi thực hiện xoá sản phẩm.");
                            }
                        }
                    }
                }
            });
        }
        
        @Override 
        public Component getTableCellEditorComponent(JTable t, Object v, boolean sel, int r, int c) {
            cur = r; 
            panel.setBackground(SEL_BG); 
            return panel;
        }
        
        @Override 
        public Object getCellEditorValue() { 
            return ""; 
        }
    }

    private JPanel mkActPanel(Color bg, boolean live) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 22));
        p.setBackground(bg); 
        p.add(new IcoBtn(true, live)); 
        p.add(new IcoBtn(false, live));
        return p;
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

    // Dynamic product image matching using standard resource assets
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

    // --- INNER STYLING VIEWS ---

    static class IcoBtn extends JPanel {
        boolean pen, live, hov;
        IcoBtn(boolean pen, boolean live) {
            this.pen = pen; 
            this.live = live;
            setPreferredSize(new Dimension(28, 28)); 
            setOpaque(false);
            if (live) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) { hov = true; repaint(); }
                    @Override
                    public void mouseExited(MouseEvent e) { hov = false; repaint(); }
                });
            }
        }
        
        @Override 
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(), cx = w / 2, cy = h / 2;
            Color color = pen ? new Color(99, 102, 241) : RED;
            if (hov) g2.setColor(color.brighter()); else g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if (pen) { 
                g2.drawRect(cx - 4, cy - 4, 8, 8); 
                g2.drawLine(cx + 4, cy - 4, cx + 6, cy - 6); 
            } else { 
                g2.drawRect(cx - 3, cy - 2, 6, 7); 
                g2.drawLine(cx - 5, cy - 2, cx + 5, cy - 2); 
                g2.drawLine(cx - 2, cy - 4, cx + 2, cy - 4); 
            }
            g2.dispose();
        }
    }

    static class FlatBtn extends JButton {
        private Color customColor = null;
        FlatBtn(String text) {
            this(text, null);
        }
        FlatBtn(String text, Color color) {
            super(text);
            this.customColor = color;
            setFont(new Font("Helvetica Neue", Font.BOLD, 15));
            setForeground(WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        
        @Override 
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color bg = customColor != null ? customColor : PRIMARY;
            g2.setColor(getModel().isPressed() ? bg.darker() : getModel().isRollover() ? bg.brighter() : bg);
            g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 18, 18));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class TableCard extends JPanel {
        TableCard() { setOpaque(false); }
        @Override 
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(WHITE);
            g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 24, 24));
            g2.setColor(BORDER);
            g2.draw(new java.awt.geom.RoundRectangle2D.Float(.5f, .5f, getWidth() - 1, getHeight() - 1, 24, 24));
            g2.dispose();
        }
    }

    static class RoundedBorder implements Border {
        private final int r; 
        private final Color c;
        RoundedBorder(int r, Color c) { this.r = r; this.c = c; }
        public Insets getBorderInsets(Component c) { return new Insets(4, 15, 4, 15); }
        public boolean isBorderOpaque() { return true; }
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(this.c);
            g2.draw(new java.awt.geom.RoundRectangle2D.Float(x, y, w - 1, h - 1, r, r));
            g2.dispose();
        }
    }
}
