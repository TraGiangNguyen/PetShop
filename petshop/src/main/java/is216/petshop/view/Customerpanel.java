package is216.petshop.view;

import is216.petshop.dao.CustomerDAO;
import is216.petshop.model.Customer;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Customerpanel extends JPanel {

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

    private CustomerDAO dao = new CustomerDAO();
    private ArrayList<Customer> list = new ArrayList<>();
    private DefaultTableModel model;
    private JTable table;
    private JTextField txtSearch;

    public Customerpanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        
        add(buildTop(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        
        loadData(dao.getAll());
    }

    private JPanel buildTop() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG);
        bar.setBorder(new EmptyBorder(0, 0, 25, 0));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(BG);

        JLabel lbTitle = new JLabel("Quản lý khách hàng");
        lbTitle.setFont(new Font("Helvetica Neue", Font.BOLD, 30));
        lbTitle.setForeground(TXT);

        JLabel lbSub = new JLabel("Quản lý thông tin và điểm tích lũy khách hàng");
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
        
        String ph = "Tìm theo Tên, SĐT, Email...";
        txtSearch.setText(ph);
        txtSearch.setForeground(TXT2);
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { if (txtSearch.getText().equals(ph)) { txtSearch.setText(""); txtSearch.setForeground(TXT); } }
            @Override public void focusLost(FocusEvent e) { if (txtSearch.getText().isEmpty()) { txtSearch.setText(ph); txtSearch.setForeground(TXT2); } }
        });

        FlatBtn btnSearch = new FlatBtn("Tìm kiếm");
        btnSearch.setPreferredSize(new Dimension(100, 42));
        btnSearch.addActionListener(e -> doSearch());

        FlatBtn btnRefresh = new FlatBtn("Làm mới");
        btnRefresh.setPreferredSize(new Dimension(100, 42));
        btnRefresh.addActionListener(e -> {
            txtSearch.setText(ph);
            txtSearch.setForeground(TXT2);
            loadData(dao.getAll());
        });

        JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftGroup.setBackground(BG);
        leftGroup.add(txtSearch);
        leftGroup.add(btnSearch);
        leftGroup.add(btnRefresh);

        FlatBtn btnAdd = new FlatBtn("+ Thêm khách hàng");
        btnAdd.setPreferredSize(new Dimension(180, 42));
        btnAdd.addActionListener(e -> showDialog(null));

        toolbar.add(leftGroup, BorderLayout.WEST);
        toolbar.add(btnAdd, BorderLayout.EAST);

        TableCard card = new TableCard();
        card.setLayout(new BorderLayout());
        buildTable(card);

        body.add(toolbar, BorderLayout.NORTH);
        body.add(card, BorderLayout.CENTER);
        return body;
    }

    private void buildTable(JPanel card) {
        model = new DefaultTableModel(
            new Object[]{"Mã", "Khách hàng", "Liên hệ", "Địa chỉ", "Phân loại", "Ngày tham gia", "Điểm", "Hạng", "Thao tác"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 8; }
        };

        table = new JTable(model);
        table.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        table.setRowHeight(85); 
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

        table.getColumnModel().getColumn(0).setPreferredWidth(60);   // Mã
        table.getColumnModel().getColumn(1).setPreferredWidth(200);  // Khách hàng
        table.getColumnModel().getColumn(2).setPreferredWidth(180);  // Liên hệ
        table.getColumnModel().getColumn(3).setPreferredWidth(250);  // Địa chỉ
        table.getColumnModel().getColumn(4).setPreferredWidth(120);  // Phân loại
        table.getColumnModel().getColumn(5).setPreferredWidth(120);  // Ngày tham gia
        table.getColumnModel().getColumn(6).setPreferredWidth(80);   // Điểm
        table.getColumnModel().getColumn(7).setPreferredWidth(100);  // Hạng
        table.getColumnModel().getColumn(8).setPreferredWidth(100);  // Thao tác

        table.getColumnModel().getColumn(0).setCellRenderer(new IdRender());
        table.getColumnModel().getColumn(1).setCellRenderer(new NameRender());
        table.getColumnModel().getColumn(2).setCellRenderer(new ContactRender());
        table.getColumnModel().getColumn(3).setCellRenderer(new AddrRender());
        table.getColumnModel().getColumn(4).setCellRenderer(new TypeBadgeRender());
        table.getColumnModel().getColumn(6).setCellRenderer(new PointRender());
        table.getColumnModel().getColumn(7).setCellRenderer(new RankRender());
        table.getColumnModel().getColumn(8).setCellRenderer(new ActRender());
        table.getColumnModel().getColumn(8).setCellEditor(new ActEditor());

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(WHITE);
        card.add(sp, BorderLayout.CENTER);
    }

    private void loadData(ArrayList<Customer> data) {
        list = data;
        model.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        for (Customer c : list) {
            model.addRow(new Object[]{ 
                c.getId(), c, c, c.getAddress(), c.getPartnerType(),
                c.getJoinDate() != null ? sdf.format(c.getJoinDate()) : "-",
                c.getLoyaltyPoints(), c, "" 
            });
        }
    }

    private void doSearch() {
        String kw = txtSearch.getText().trim();
        if (kw.isEmpty() || kw.equals("Tìm theo Tên, SĐT, Email...")) { loadData(dao.getAll()); return; }
        loadData(dao.search(kw));
    }

    private void showDialog(Customer cust) {
        Window parent = SwingUtilities.getWindowAncestor(this);
        CustomerDialog d = new CustomerDialog((Frame) parent, cust, dao, () -> loadData(dao.getAll()));
        d.setVisible(true);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    // --- RENDERERS ---

    class NameRender extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            Customer cust = (Customer) v;
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 32));
            p.setBackground(sel ? SEL_BG : WHITE);
            p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

            JLabel name = new JLabel("<html><b>" + cust.getName() + "</b></html>");
            name.setFont(new Font("Helvetica Neue", Font.BOLD, 15));
            name.setForeground(TXT);
            
            p.add(name);
            return p;
        }
    }

    class IdRender extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, f, r, c);
            setFont(new Font("Helvetica Neue", Font.BOLD, 14));
            setForeground(TXT2);
            setBackground(sel ? SEL_BG : WHITE);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
            return this;
        }
    }

    class TypeBadgeRender extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 28));
            p.setBackground(sel ? SEL_BG : WHITE);
            p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

            String type = v != null ? v.toString() : "KHACH_HANG";
            String text = "Khách hàng";
            Color bg = new Color(243, 244, 246), fg = new Color(75, 85, 99);

            if ("NHA_CUNG_CAP".equals(type)) {
                text = "Nhà cung cấp"; bg = new Color(238, 242, 255); fg = new Color(79, 70, 229);
            } else if ("CA_HAI".equals(type)) {
                text = "Cả hai"; bg = new Color(236, 253, 245); fg = new Color(5, 150, 105);
            }

            JLabel badge = new JLabel(text);
            badge.setFont(new Font("Helvetica Neue", Font.BOLD, 12));
            badge.setOpaque(true);
            badge.setBackground(bg);
            badge.setForeground(fg);
            badge.setBorder(new EmptyBorder(4, 10, 4, 10));
            
            p.add(badge);
            return p;
        }
    }

    class ContactRender extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            Customer cust = (Customer) v;
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBackground(sel ? SEL_BG : WHITE);
            p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(15, 0, 15, 0)
            ));

            JLabel phone = new JLabel(cust.getPhone());
            phone.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
            phone.setForeground(TXT);

            JLabel email = new JLabel(cust.getEmail() == null || cust.getEmail().isEmpty() ? "-" : cust.getEmail());
            email.setFont(new Font("Helvetica Neue", Font.PLAIN, 12));
            email.setForeground(TXT2);

            p.add(phone); p.add(Box.createVerticalStrut(3)); p.add(email);
            return p;
        }
    }

    class AddrRender extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, f, r, c);
            l.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
            l.setForeground(TXT);
            l.setBackground(sel ? SEL_BG : WHITE);
            l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(0, 5, 0, 5)
            ));
            return l;
        }
    }

    class PointRender extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, f, r, c);
            setFont(new Font("Helvetica Neue", Font.BOLD, 14));
            setForeground(PRIMARY);
            setBackground(sel ? SEL_BG : WHITE);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
            if (v != null) setText(v.toString() + " pts");
            return this;
        }
    }

    class RankRender extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            Customer cust = (Customer) v;
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 28));
            p.setBackground(sel ? SEL_BG : WHITE);
            p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

            JLabel rank = new JLabel(cust.getType());
            rank.setFont(new Font("Helvetica Neue", Font.BOLD, 12));
            rank.setOpaque(true);
            rank.setBorder(new EmptyBorder(4, 10, 4, 10));
            
            if ("Vàng".equals(cust.getType())) {
                rank.setBackground(new Color(254, 249, 195));
                rank.setForeground(new Color(161, 98, 7));
            } else if ("Bạc".equals(cust.getType())) {
                rank.setBackground(new Color(243, 244, 246));
                rank.setForeground(new Color(75, 85, 99));
            } else {
                rank.setBackground(new Color(255, 237, 213));
                rank.setForeground(new Color(154, 52, 18));
            }

            p.add(rank);
            return p;
        }
    }

    class HdrRender extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            JLabel l = new JLabel(v != null ? v.toString() : "");
            l.setFont(new Font("Helvetica Neue", Font.BOLD, 14));
            l.setForeground(TXT2);
            if (c == 0) {
                l.setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                l.setBorder(new EmptyBorder(0, 15, 0, 0));
            }
            return l;
        }
    }

    class ActRender implements TableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            JPanel p = mkActPanel(sel ? SEL_BG : WHITE, false);
            p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
            return p;
        }
    }

    class ActEditor extends DefaultCellEditor {
        JPanel panel; IcoBtn bEdit, bDel; int cur = -1;
        ActEditor() {
            super(new JCheckBox());
            setClickCountToStart(1);
            bEdit = new IcoBtn(true, true); bDel = new IcoBtn(false, true);
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 28));
            panel.add(bEdit); panel.add(bDel);
            bEdit.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) { fireEditingStopped(); if (cur >= 0) showDialog(list.get(cur)); }
            });
            bDel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) { 
                    fireEditingStopped(); 
                    if (cur >= 0) {
                        Customer c = list.get(cur);
                        if (JOptionPane.showConfirmDialog(null, "Xoá khách hàng " + c.getName() + "?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            try {
                                dao.delete(c.getId()); 
                                loadData(dao.getAll());
                            } catch (SQLException ex) {
                                showError(ex.getMessage());
                            }
                        }
                    }
                }
            });
        }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean sel, int r, int c) {
            cur = r; panel.setBackground(SEL_BG); return panel;
        }
        @Override public Object getCellEditorValue() { return ""; }
    }

    private JPanel mkActPanel(Color bg, boolean live) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 28));
        p.setBackground(bg); p.add(new IcoBtn(true, live)); p.add(new IcoBtn(false, live));
        return p;
    }

    static class IcoBtn extends JPanel {
        boolean pen, live, hov;
        IcoBtn(boolean pen, boolean live) {
            this.pen = pen; this.live = live;
            setPreferredSize(new Dimension(28, 28)); setOpaque(false);
            if (live) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hov = true; repaint(); }
                    public void mouseExited(MouseEvent e) { hov = false; repaint(); }
                });
            }
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(), cx = w / 2, cy = h / 2;
            Color color = pen ? new Color(99, 102, 241) : RED;
            if (hov) g2.setColor(color.brighter()); else g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if (pen) { g2.drawRect(cx - 4, cy - 4, 8, 8); g2.drawLine(cx + 4, cy - 4, cx + 6, cy - 6); }
            else { g2.drawRect(cx - 3, cy - 2, 6, 7); g2.drawLine(cx - 5, cy - 2, cx + 5, cy - 2); g2.drawLine(cx - 2, cy - 4, cx + 2, cy - 4); }
            g2.dispose();
        }
    }

    static class FlatBtn extends JButton {
        FlatBtn(String text) {
            super(text);
            setFont(new Font("Helvetica Neue", Font.BOLD, 15));
            setForeground(WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isPressed() ? PRIMARY.darker() : getModel().isRollover() ? PRIMARY.brighter() : PRIMARY);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 18, 18));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class TableCard extends JPanel {
        TableCard() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(WHITE);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 24, 24));
            g2.setColor(BORDER);
            g2.draw(new RoundRectangle2D.Float(.5f, .5f, getWidth() - 1, getHeight() - 1, 24, 24));
            g2.dispose();
        }
    }

    static class RoundedBorder implements Border {
        private int r; private Color c;
        RoundedBorder(int r, Color c) { this.r = r; this.c = c; }
        public Insets getBorderInsets(Component c) { return new Insets(4, 15, 4, 15); }
        public boolean isBorderOpaque() { return true; }
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(this.c);
            g2.draw(new RoundRectangle2D.Float(x, y, w - 1, h - 1, r, r));
            g2.dispose();
        }
    }
}

