package is216.petshop.view;

import is216.petshop.dao.ProductDAO;
import is216.petshop.model.Product;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;

public class ProductForm extends JPanel {

    private static final Color BG       = new Color(245, 246, 250);
    private static final Color WHITE    = Color.WHITE;
    private static final Color PRIMARY  = new Color(79,  70, 229);
    private static final Color PRI_D    = new Color(67,  56, 202);
    private static final Color TAG_BG   = new Color(237, 233, 254);
    private static final Color TAG_FG   = new Color(109,  40, 217);
    private static final Color TXT      = new Color(17,   24,  39);
    private static final Color TXT2     = new Color(107, 114, 128);
    private static final Color BORDER   = new Color(229, 231, 235);
    private static final Color HDR_BG   = new Color(249, 250, 251);
    private static final Color ROW_LINE = new Color(243, 244, 246);
    private static final Color SEL_BG   = new Color(245, 243, 255);
    private static final Color RED      = new Color(220,  38,  38);

    private ProductDAO dao = new ProductDAO();
    private ArrayList<Product> list = new ArrayList<>();
    private DefaultTableModel model;
    private JTable table;
    private JTextField txtSearch;

    public ProductForm() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        add(buildTop(),  BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        loadData(dao.getAll());
    }

    // ─── TOP BAR ────────────────────────────────────────────────
    private JPanel buildTop() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG);
        bar.setBorder(new EmptyBorder(0, 0, 22, 0));

        // Tiêu đề bên trái
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(BG);

        JLabel lbTitle = new JLabel("Quản lý sản phẩm");
        lbTitle.setFont(new Font("Helvetica Neue", Font.BOLD, 24));
        lbTitle.setForeground(TXT);

        JLabel lbSub = new JLabel("Quản lý danh sách sản phẩm thú cưng");
        lbSub.setFont(new Font("Helvetica Neue", Font.PLAIN, 13));
        lbSub.setForeground(TXT2);

        left.add(lbTitle);
        left.add(Box.createVerticalStrut(5));
        left.add(lbSub);

        // Nút thêm bên phải — bọc trong FlowLayout để giữ kích thước
        FlatBtn btnAdd = new FlatBtn("+ Thêm sản phẩm");
        btnAdd.setPreferredSize(new Dimension(158, 38));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setBackground(BG);
        right.add(btnAdd);
        btnAdd.addActionListener(e -> showDialog(null));

        bar.add(left,  BorderLayout.CENTER);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ─── BODY ───────────────────────────────────────────────────
    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setBackground(BG);

        // Search row
        JPanel sr = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        sr.setBackground(BG);

        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Helvetica Neue", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(260, 36));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(0, 12, 0, 12)));
        txtSearch.setBackground(WHITE);
        txtSearch.addActionListener(e -> doSearch());

        FlatBtn btnS = new FlatBtn("Tìm kiếm");
        btnS.setPreferredSize(new Dimension(92, 36));
        btnS.addActionListener(e -> doSearch());

        sr.add(txtSearch);
        sr.add(Box.createHorizontalStrut(8));
        sr.add(btnS);

        // Table card
        TableCard card = new TableCard();
        card.setLayout(new BorderLayout());
        buildTable(card);

        body.add(sr,   BorderLayout.NORTH);
        body.add(card, BorderLayout.CENTER);
        return body;
    }

    // ─── TABLE ──────────────────────────────────────────────────
    private void buildTable(JPanel card) {
        model = new DefaultTableModel(
            new Object[]{"Tên sản phẩm","Danh mục","Giá","Tồn kho","Thao tác"}, 0) {
            public boolean isCellEditable(int r, int c) { return c == 4; }
        };

        table = new JTable(model);
        table.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        table.setRowHeight(54);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(ROW_LINE);
        table.setBackground(WHITE);
        table.setSelectionBackground(SEL_BG);
        table.setSelectionForeground(TXT);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFocusable(false);

        // AUTO_RESIZE_ALL_COLUMNS phân bổ theo tỉ lệ preferredWidth
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getColumnModel().getColumn(0).setPreferredWidth(380);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);

        // Header
        JTableHeader hdr = table.getTableHeader();
        hdr.setBackground(HDR_BG);
        hdr.setPreferredSize(new Dimension(0, 44));
        hdr.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        hdr.setReorderingAllowed(false);
        hdr.setResizingAllowed(false);
        hdr.setDefaultRenderer(new HdrRender());

        // Renderers
        table.getColumnModel().getColumn(0).setCellRenderer(new NameRender());
        table.getColumnModel().getColumn(1).setCellRenderer(new TagRender());
        table.getColumnModel().getColumn(2).setCellRenderer(new NumRender(false));
        table.getColumnModel().getColumn(3).setCellRenderer(new NumRender(false));
        table.getColumnModel().getColumn(4).setCellRenderer(new ActRender());
        table.getColumnModel().getColumn(4).setCellEditor(new ActEditor());

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(WHITE);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        card.add(sp, BorderLayout.CENTER);
    }

    // ─── DATA ───────────────────────────────────────────────────
    private void loadData(ArrayList<Product> data) {
        list = data;
        model.setRowCount(0);
        for (Product p : list)
            model.addRow(new Object[]{
                p.getName(),
                p.getType(),
                String.format("%,.0fđ", p.getPrice()),
                p.getStock(),
                ""
            });
    }

    private void doSearch() {
        String kw = txtSearch.getText().trim().toLowerCase();
        if (kw.isEmpty()) { loadData(dao.getAll()); return; }
        ArrayList<Product> r = new ArrayList<>();
        for (Product p : dao.getAll())
            if (p.getName().toLowerCase().contains(kw)) r.add(p);
        loadData(r);
    }

    // ─── DIALOG ─────────────────────────────────────────────────
    private void showDialog(Product prod) {
        boolean edit = prod != null;
        JDialog d = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            edit ? "Sửa sản phẩm" : "Thêm sản phẩm", true);
        d.setSize(440, 310);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());
        d.getContentPane().setBackground(WHITE);

        JPanel dTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        dTop.setBackground(PRIMARY);
        dTop.setBorder(new EmptyBorder(16, 22, 16, 22));
        JLabel dLbl = new JLabel(edit ? "Sửa sản phẩm" : "Thêm sản phẩm mới");
        dLbl.setFont(new Font("Helvetica Neue", Font.BOLD, 15));
        dLbl.setForeground(WHITE);
        dTop.add(dLbl);

        JPanel form = new JPanel(new GridLayout(3, 2, 12, 16));
        form.setBackground(WHITE);
        form.setBorder(new EmptyBorder(24, 22, 12, 22));

        JTextField tfName  = mkFld(edit ? prod.getName() : "");
        JComboBox<String> cb =
            new JComboBox<>(new String[]{"Dog","Cat","Accessory"});
        if (edit) cb.setSelectedItem(prod.getType());
        cb.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        JTextField tfPrice = mkFld(edit ? String.valueOf((int) prod.getPrice()) : "");

        form.add(mkLbl("Tên sản phẩm")); form.add(tfName);
        form.add(mkLbl("Danh mục"));     form.add(cb);
        form.add(mkLbl("Giá (đồng)"));   form.add(tfPrice);

        JPanel br = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        br.setBackground(WHITE);
        br.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));

        JButton bCancel = new JButton("Huỷ");
        bCancel.setFont(new Font("Helvetica Neue", Font.PLAIN, 13));
        bCancel.setForeground(TXT2);
        bCancel.setBackground(new Color(241,245,249));
        bCancel.setBorder(new EmptyBorder(9, 20, 9, 20));
        bCancel.setFocusPainted(false);
        bCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bCancel.addActionListener(e -> d.dispose());

        FlatBtn bSave = new FlatBtn(edit ? "Cập nhật" : "Thêm mới");
        bSave.setPreferredSize(new Dimension(110, 36));
        bSave.addActionListener(e -> {
            String name = tfName.getText().trim();
            String type = cb.getSelectedItem().toString();
            String ps   = tfPrice.getText().trim();
            if (name.isEmpty() || ps.isEmpty()) {
                JOptionPane.showMessageDialog(d,
                    "Vui lòng điền đầy đủ!", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE); return;
            }
            double price;
            try { price = Double.parseDouble(ps);
                if (price < 0) throw new NumberFormatException(); }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d,
                    "Giá phải là số dương!", "Lỗi",
                    JOptionPane.WARNING_MESSAGE); return;
            }
            if (edit) {
                prod.setName(name); prod.setType(type); prod.setPrice(price);
                dao.update(prod);
            } else {
                dao.insert(new Product(name, type, price));
            }
            loadData(dao.getAll()); d.dispose();
        });

        br.add(bCancel); br.add(bSave);
        d.add(dTop,  BorderLayout.NORTH);
        d.add(form,  BorderLayout.CENTER);
        d.add(br,    BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private void deleteProd(int id, String name) {
        if (JOptionPane.showConfirmDialog(this,
            "<html>Xoá sản phẩm <b>" + name + "</b>?</html>",
            "Xác nhận xoá", JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            dao.delete(id); loadData(dao.getAll());
        }
    }

    private JTextField mkFld(String v) {
        JTextField tf = new JTextField(v);
        tf.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(7, 10, 7, 10)));
        return tf;
    }

    private JLabel mkLbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Helvetica Neue", Font.BOLD, 13));
        l.setForeground(TXT); return l;
    }

    private Color rowBg(int row, boolean sel) { return sel ? SEL_BG : WHITE; }

    // ─── RENDERERS ──────────────────────────────────────────────

    class HdrRender extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean s, boolean f, int r, int c) {
            JLabel l = new JLabel(v != null ? v.toString() : "");
            l.setFont(new Font("Helvetica Neue", Font.PLAIN, 13));
            l.setForeground(TXT2);
            l.setBackground(HDR_BG);
            l.setOpaque(true);
            l.setBorder(new EmptyBorder(0, c == 0 ? 20 : 12, 0, 0));
            return l;
        }
    }

    class NameRender extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean f, int row, int c) {
            super.getTableCellRendererComponent(t, v, sel, f, row, c);
            setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
            setForeground(TXT);
            setBackground(rowBg(row, sel));
            setBorder(new EmptyBorder(0, 20, 0, 8));
            return this;
        }
    }

    // Tag: vẽ pill bằng paintComponent — không dùng layout lồng nhau
    class TagRender extends DefaultTableCellRenderer {
        private String val = "";

        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean f, int row, int c) {
            this.val = v != null ? v.toString() : "";
            setBackground(rowBg(row, sel));
            setText(""); setBorder(BorderFactory.createEmptyBorder());
            return this;
        }

        protected void paintComponent(Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            if (val.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Font font = new Font("Helvetica Neue", Font.PLAIN, 12);
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(val);
            int ph = 24, pw = tw + 26, px = 12;
            int py = (getHeight() - ph) / 2;

            g2.setColor(TAG_BG);
            g2.fill(new RoundRectangle2D.Float(px, py, pw, ph, ph, ph));
            g2.setColor(TAG_FG);
            int tx = px + (pw - tw) / 2;
            int ty = py + (ph + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(val, tx, ty);
            g2.dispose();
        }
    }

    // Số (giá, tồn kho)
    class NumRender extends DefaultTableCellRenderer {
        boolean bold;
        NumRender(boolean bold) { this.bold = bold; }
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean f, int row, int c) {
            super.getTableCellRendererComponent(t, v, sel, f, row, c);
            setFont(new Font("Helvetica Neue", Font.BOLD, 14));
            setForeground(TXT);
            setBackground(rowBg(row, sel));
            setBorder(new EmptyBorder(0, 12, 0, 0));
            return this;
        }
    }

    class ActRender implements TableCellRenderer {
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean f, int row, int c) {
            return mkActPanel(rowBg(row, sel), false);
        }
    }

    class ActEditor extends DefaultCellEditor {
        JPanel panel; IcoBtn bEdit, bDel; int cur = -1;

        ActEditor() {
            super(new JCheckBox());
            setClickCountToStart(1);
            bEdit = new IcoBtn(true,  true);
            bDel  = new IcoBtn(false, true);

            JPanel inner = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            inner.setBackground(SEL_BG);
            inner.add(bEdit); inner.add(bDel);

            panel = new JPanel(new GridBagLayout());
            panel.setBackground(SEL_BG);
            panel.add(inner);

            bEdit.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    fireEditingStopped();
                    if (cur >= 0 && cur < list.size()) showDialog(list.get(cur));
                }
            });
            bDel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    fireEditingStopped();
                    if (cur >= 0 && cur < list.size()) {
                        Product p = list.get(cur);
                        deleteProd(p.getId(), p.getName());
                    }
                }
            });
        }

        public Component getTableCellEditorComponent(
                JTable t, Object v, boolean sel, int row, int c) {
            cur = row; return panel;
        }
        public Object getCellEditorValue() { return ""; }
    }

    private JPanel mkActPanel(Color bg, boolean live) {
        JPanel inner = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        inner.setBackground(bg);
        inner.add(new IcoBtn(true,  live));
        inner.add(new IcoBtn(false, live));
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(bg);
        wrap.add(inner);
        return wrap;
    }

    // ─── ICON ───────────────────────────────────────────────────
    static class IcoBtn extends JPanel {
        boolean pen, live, hov;
        IcoBtn(boolean pen, boolean live) {
            this.pen = pen; this.live = live;
            setPreferredSize(new Dimension(28, 28));
            setOpaque(false);
            if (live) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e){hov=true; repaint();}
                    public void mouseExited (MouseEvent e){hov=false;repaint();}
                });
            }
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(), h=getHeight(), cx=w/2, cy=h/2;

            if (hov) {
                g2.setColor(pen ? new Color(237,233,254) : new Color(254,226,226));
                g2.fill(new RoundRectangle2D.Float(1,1,w-2,h-2,7,7));
            }
            Color ink = pen
                ? (hov ? new Color(67,56,202) : new Color(99,102,241))
                : (hov ? new Color(185,28,28) : RED);
            g2.setColor(ink);
            g2.setStroke(new BasicStroke(1.6f,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if (pen) {
                int[] xs={cx-5,cx+2,cx+5,cx-2};
                int[] ys={cy+4,cy-4,cy-1,cy+7};
                g2.drawPolygon(xs,ys,4);
                g2.drawLine(cx-5,cy+4,cx-7,cy+8);
                g2.drawLine(cx-7,cy+8,cx-3,cy+8);
                g2.drawLine(cx-5,cy+4,cx-3,cy+8);
                g2.setStroke(new BasicStroke(1.1f,
                    BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                g2.drawLine(cx-1,cy+1,cx+1,cy-1);
            } else {
                g2.drawLine(cx-5,cy-2,cx+5,cy-2);
                g2.drawLine(cx-3,cy-2,cx-2,cy-5);
                g2.drawLine(cx+3,cy-2,cx+2,cy-5);
                g2.drawLine(cx-2,cy-5,cx+2,cy-5);
                g2.drawRoundRect(cx-4,cy-1,8,9,2,2);
                g2.setStroke(new BasicStroke(1.0f,
                    BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                g2.drawLine(cx,  cy+1,cx,  cy+6);
                g2.drawLine(cx-2,cy+1,cx-2,cy+6);
                g2.drawLine(cx+2,cy+1,cx+2,cy+6);
            }
            g2.dispose();
        }
    }

    // ─── TABLE CARD ─────────────────────────────────────────────
    static class TableCard extends JPanel {
        TableCard() { setOpaque(false); }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(WHITE);
            g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),14,14));
            g2.setColor(BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(.5f,.5f,getWidth()-1,getHeight()-1,14,14));
            g2.dispose();
        }
    }

    // ─── FLAT BUTTON ────────────────────────────────────────────
    static class FlatBtn extends JButton {
        FlatBtn(String text) {
            super(text);
            setFont(new Font("Helvetica Neue", Font.BOLD, 13));
            setForeground(WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(0, 16, 0, 16));
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isPressed() ? PRI_D.darker()
                : getModel().isRollover()       ? PRI_D : PRIMARY);
            g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),10,10));
            g2.dispose();
            super.paintComponent(g);
        }
    }
}