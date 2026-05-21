package is216.petshop.Sales;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

public class SalesPanel extends JPanel {

    // ── Global UI fields ──────────────────────────────────────────────────────
    private JTable          tblCart;
    private JLabel          lblTotalPrice;
    private DefaultTableModel cartModel;
    private JButton         btnCheckout;
    private JPanel          pnlProducts;
    private JLabel          lblEmptyCart;   // empty-state label in cart

    // ── New payment / customer fields ─────────────────────────────────────────
    private JTextField      txtPhone;           // phone lookup input
    private JButton         btnFindCustomer;    // search button
    private JLabel          lblCustomerName;    // resolved customer name
    private JTextField      txtCashReceived;    // cash the customer hands over
    private JLabel          lblChange;          // auto-calculated change
    private long            currentTotal = 0;   // kept in sync with updateTotalPrice()

    // ── Customer selection state ──────────────────────────────────────────────
    private is216.petshop.model.Customer selectedCustomer = null;
    private ActionListener                  findCustomerListener    = null; // Controller supplies the search
    private ActionListener                  customerSelectedListener = null; // Controller notified on pick

    // ── MVC bridge ───────────────────────────────────────────────────────────
    private Object[]        currentSelectedProduct;
    private ActionListener  addToCartListener;

    // ── Design tokens ─────────────────────────────────────────────────────────
    private static final Color PRIMARY      = new Color(79, 70, 229);   // indigo
    private static final Color ACCENT       = new Color(245, 158, 11);  // amber  (pending-order btn)
    private static final Color DANGER       = new Color(220, 53, 69);
    private static final Color BG_PAGE      = new Color(243, 244, 246); // gray-100
    private static final Color BG_CARD      = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    private static final Color TEXT_MAIN    = new Color(17, 24, 39);
    private static final Color TEXT_SUB     = new Color(107, 114, 128);
    private static final Font  FONT_TITLE   = new Font("Segoe UI", Font.BOLD,  22);
    private static final Font  FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font  FONT_BOLD    = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font  FONT_PRICE   = new Font("Segoe UI", Font.BOLD,  15);
    private static final Font  FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);

    // ─────────────────────────────────────────────────────────────────────────
    public SalesPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_PAGE);

        // ── TOP HEADER ──────────────────────────────────────────────────────
        add(buildHeaderPanel(), BorderLayout.NORTH);

        // ── BODY (left + right) ─────────────────────────────────────────────
        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(16, 20, 20, 20));

        body.add(buildProductPanel(), BorderLayout.CENTER);
        body.add(buildCartPanel(),    BorderLayout.EAST);

        add(body, BorderLayout.CENTER);
    }

    // =========================================================================
    // HEADER
    // =========================================================================
    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_PAGE);
        header.setBorder(new EmptyBorder(20, 20, 4, 20));

        // Left: title + subtitle
        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel lblTitle = new JLabel("Bán hàng");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(TEXT_MAIN);

        JLabel lblSub = new JLabel("Quản lý giao dịch bán hàng");
        lblSub.setFont(FONT_BODY);
        lblSub.setForeground(TEXT_SUB);

        titleBlock.add(lblTitle);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(lblSub);

        // Right: pending-order button (amber)
        JButton btnPending = createRoundedButton("Đơn chờ thanh toán", ACCENT, Color.WHITE, 10);
        btnPending.setIcon(createClockIcon(18, Color.WHITE));
        btnPending.setIconTextGap(8);
        btnPending.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnPending.setPreferredSize(new Dimension(230, 44));

        header.add(titleBlock, BorderLayout.WEST);
        header.add(btnPending,  BorderLayout.EAST);
        return header;
    }

    // =========================================================================
    // LEFT PANEL – search + product grid
    // =========================================================================
    private JPanel buildProductPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setOpaque(false);

        // ── Search bar ───────────────────────────────────────────────────────
        JPanel pnlSearch = new JPanel(new BorderLayout());
        pnlSearch.setOpaque(false);

        JTextField txtSearch = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        txtSearch.setOpaque(false);
        txtSearch.setFont(FONT_BODY);
        txtSearch.setForeground(TEXT_MAIN);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(0, 14, 0, 14)));
        txtSearch.setPreferredSize(new Dimension(0, 44));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm kiếm sản phẩm...");

        pnlSearch.add(txtSearch, BorderLayout.CENTER);

        // ── Product grid ─────────────────────────────────────────────────────
        pnlProducts = new JPanel(new GridLayout(0, 3, 14, 14));
        pnlProducts.setOpaque(false);

        // Wrapper pushes the grid to NORTH so empty space stays below the cards.
        // It also overrides getPreferredSize so its width always equals the
        // viewport width — this prevents any horizontal scrollbar from appearing.
        JPanel gridWrapper = new JPanel(new BorderLayout()) {
            @Override public Dimension getPreferredSize() {
                // Height = grid's preferred height; width = viewport width
                int viewW = getParent() != null ? getParent().getWidth() : super.getPreferredSize().width;
                int gridH = pnlProducts.getPreferredSize().height;
                return new Dimension(viewW, gridH);
            }
        };
        gridWrapper.setOpaque(false);
        gridWrapper.add(pnlProducts, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(gridWrapper,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);   // ← no horizontal bar
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        // When the viewport resizes, tell the wrapper to reflow so the grid
        // always fills the available width without creating horizontal overflow.
        scroll.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                gridWrapper.revalidate();
            }
        });

        // Outer card wrapping the search + grid
        JPanel card = createCard(new BorderLayout(0, 12), new EmptyBorder(16, 16, 16, 16));
        card.add(pnlSearch, BorderLayout.NORTH);
        card.add(scroll,    BorderLayout.CENTER);

        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    // =========================================================================
    // RIGHT PANEL – cart
    // =========================================================================
    private JPanel buildCartPanel() {
        JPanel pnlRight = new JPanel(new BorderLayout(0, 12));
        pnlRight.setOpaque(false);
        pnlRight.setPreferredSize(new Dimension(340, 0));

        JPanel card = createCard(new BorderLayout(0, 0), new EmptyBorder(18, 18, 18, 18));

        // ── Cart header ──────────────────────────────────────────────────────
        JPanel cartHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        cartHeader.setOpaque(false);

        JLabel iconLabel = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PRIMARY);
                g2.fillOval(0, 0, getWidth(), getHeight());
                
                // Draw cart icon
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                
                g2.drawRect(cx - 5, cy - 4, 10, 8);
                g2.drawLine(cx - 5, cy - 4, cx - 8, cy - 8); // handle
                g2.fillOval(cx - 3, cy + 5, 3, 3); // wheel 1
                g2.fillOval(cx + 3, cy + 5, 3, 3); // wheel 2
                
                g2.dispose();
            }
        };
        iconLabel.setPreferredSize(new Dimension(40, 40));

        JLabel lblTitle = new JLabel("Giỏ hàng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(TEXT_MAIN);

        cartHeader.add(iconLabel);
        cartHeader.add(lblTitle);

        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);

        // ── Phone search row ─────────────────────────────────────────────────
        //   [ 📞 0901 234 567_________ ] [ Tìm ]
        JPanel pnlPhoneRow = new JPanel(new BorderLayout(6, 0));
        pnlPhoneRow.setOpaque(false);
        pnlPhoneRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        txtPhone = new JTextField();
        txtPhone.setFont(FONT_BODY);
        txtPhone.setPreferredSize(new Dimension(0, 40));
        txtPhone.putClientProperty("JTextField.placeholderText", "Số điện thoại khách hàng...");
        txtPhone.putClientProperty("JComponent.roundRect", true);
        txtPhone.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(0, 10, 0, 10)));

        btnFindCustomer = createRoundedButton("Tìm", PRIMARY, Color.WHITE, 8);
        btnFindCustomer.setFont(FONT_BOLD);
        btnFindCustomer.setPreferredSize(new Dimension(56, 40));
        // Fire the Controller's search listener (registered via addFindCustomerListener).
        // Also allow Enter key inside txtPhone to trigger the same search.
        btnFindCustomer.addActionListener(e -> {
            if (findCustomerListener != null) {
                findCustomerListener.actionPerformed(
                        new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "FIND_CUSTOMER"));
            }
        });
        txtPhone.addActionListener(e -> btnFindCustomer.doClick()); // Enter key shortcut

        pnlPhoneRow.add(txtPhone,       BorderLayout.CENTER);
        pnlPhoneRow.add(btnFindCustomer, BorderLayout.EAST);

        // Customer name result (hidden until a lookup succeeds)
        lblCustomerName = new JLabel();
        lblCustomerName.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblCustomerName.setForeground(new Color(5, 150, 105)); // green-600
        lblCustomerName.setVisible(false);

        // ── Cart table ───────────────────────────────────────────────────────
        String[] cartCols = {"Mã SP", "Tên SP", "SL", "Thành tiền"};
        cartModel = new DefaultTableModel(cartCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblCart = new JTable(cartModel);
        tblCart.setFont(FONT_BODY);
        tblCart.setRowHeight(28);
        tblCart.setShowGrid(false);
        tblCart.setIntercellSpacing(new Dimension(0, 0));
        tblCart.getTableHeader().setFont(FONT_BOLD);
        tblCart.getTableHeader().setBackground(new Color(249, 250, 251));
        tblCart.getTableHeader().setForeground(TEXT_SUB);
        tblCart.setSelectionBackground(new Color(238, 242, 255));

        JScrollPane scrollCart = new JScrollPane(tblCart);
        scrollCart.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        scrollCart.setPreferredSize(new Dimension(0, 180));

        // Empty-cart state
        lblEmptyCart = new JLabel("Chưa có sản phẩm nào", SwingConstants.CENTER);
        lblEmptyCart.setFont(FONT_BODY);
        lblEmptyCart.setForeground(TEXT_SUB);
        lblEmptyCart.setPreferredSize(new Dimension(0, 80));

        JPanel pnlCartBody = new JPanel(new BorderLayout());
        pnlCartBody.setOpaque(false);
        pnlCartBody.add(scrollCart,   BorderLayout.CENTER);
        pnlCartBody.add(lblEmptyCart, BorderLayout.SOUTH);
        updateEmptyCartVisibility();

        // ── Payment section ───────────────────────────────────────────────────
        //  Row 1: Tổng tiền
        lblTotalPrice = new JLabel("Tổng tiền: 0 VNĐ", SwingConstants.CENTER);
        lblTotalPrice.setOpaque(true);
        lblTotalPrice.setBackground(new Color(245, 245, 250));
        lblTotalPrice.setForeground(DANGER);
        lblTotalPrice.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalPrice.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        lblTotalPrice.setPreferredSize(new Dimension(0, 40));

        //  Row 2: Cash received input
        JPanel pnlCashRow = new JPanel(new BorderLayout(8, 0));
        pnlCashRow.setOpaque(false);

        JLabel lblCashLabel = new JLabel("Tiền khách đưa:");
        lblCashLabel.setFont(FONT_BOLD);
        lblCashLabel.setForeground(TEXT_MAIN);
        lblCashLabel.setPreferredSize(new Dimension(110, 36));

        txtCashReceived = new JTextField("0");
        txtCashReceived.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtCashReceived.setForeground(TEXT_MAIN);
        txtCashReceived.setHorizontalAlignment(JTextField.RIGHT);
        txtCashReceived.setPreferredSize(new Dimension(0, 36));
        txtCashReceived.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(199, 210, 254), 1, true),
                new EmptyBorder(0, 8, 0, 8)));

        // Recalculate change whenever the cash field changes
        txtCashReceived.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { recalcChange(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { recalcChange(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { recalcChange(); }
        });

        pnlCashRow.add(lblCashLabel,    BorderLayout.WEST);
        pnlCashRow.add(txtCashReceived, BorderLayout.CENTER);

        //  Row 3: Change
        JPanel pnlChangeRow = new JPanel(new BorderLayout(8, 0));
        pnlChangeRow.setOpaque(false);
        pnlChangeRow.setBackground(new Color(240, 253, 244)); // faint green tint

        JLabel lblChangeKey = new JLabel("Tiền thối lại:");
        lblChangeKey.setFont(FONT_BOLD);
        lblChangeKey.setForeground(TEXT_SUB);
        lblChangeKey.setPreferredSize(new Dimension(110, 36));

        lblChange = new JLabel("0 VNĐ", SwingConstants.RIGHT);
        lblChange.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblChange.setForeground(new Color(5, 150, 105)); // green

        pnlChangeRow.add(lblChangeKey, BorderLayout.WEST);
        pnlChangeRow.add(lblChange,    BorderLayout.CENTER);

        // Divider above payment rows
        JSeparator sep2 = new JSeparator();
        sep2.setForeground(BORDER_COLOR);

        //  Checkout button
        btnCheckout = createRoundedButton("THANH TOÁN", PRIMARY, Color.WHITE, 10);
        btnCheckout.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCheckout.setPreferredSize(new Dimension(0, 46));

        JPanel pnlCheckout = new JPanel(new GridLayout(5, 1, 0, 6));
        pnlCheckout.setOpaque(false);
        pnlCheckout.add(sep2);
        pnlCheckout.add(lblTotalPrice);
        pnlCheckout.add(pnlCashRow);
        pnlCheckout.add(pnlChangeRow);
        pnlCheckout.add(btnCheckout);

        // ── Assemble card ────────────────────────────────────────────────────
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        top.add(cartHeader);
        top.add(Box.createVerticalStrut(10));
        top.add(sep);
        top.add(Box.createVerticalStrut(10));
        top.add(pnlPhoneRow);
        top.add(Box.createVerticalStrut(4));
        top.add(lblCustomerName);
        top.add(Box.createVerticalStrut(8));

        card.add(top,          BorderLayout.NORTH);
        card.add(pnlCartBody,  BorderLayout.CENTER);
        card.add(pnlCheckout,  BorderLayout.SOUTH);

        pnlRight.add(card, BorderLayout.CENTER);
        return pnlRight;
    }

    /** Recomputes change = cash received − total and updates lblChange. */
    private void recalcChange() {
        try {
            String raw = txtCashReceived.getText().replaceAll("[^\\d]", "");
            long cash = raw.isEmpty() ? 0 : Long.parseLong(raw);
            long change = cash - currentTotal;
            lblChange.setText(String.format("%,d VNĐ", Math.max(change, 0)));
            lblChange.setForeground(change >= 0
                    ? new Color(5, 150, 105)   // green  – enough cash
                    : DANGER);                  // red    – insufficient
        } catch (NumberFormatException ex) {
            lblChange.setText("—");
        }
    }

    // =========================================================================
    // PRODUCT CARD
    // =========================================================================
    private JPanel createProductCard(int id, String name, long price, int stock) {
        return createProductCard(id, name, price, stock, null, null);
    }

    private JPanel createProductCard(int id, String name, long price, int stock,
                                     String category, ImageIcon image) {

        JPanel card = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        // Let GridLayout control the column width; only lock the card height.
        card.setPreferredSize(new Dimension(0, 290));
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDER_COLOR, 1, 16),
                new EmptyBorder(0, 0, 10, 0)));

        // ── Image area ────────────────────────────────────────────────────────
        // Use BorderLayout so the image label stretches to fill whatever width
        // GridLayout assigns to this card column — no horizontal overflow.
        JPanel imgPanel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight() + 16, 16, 16));
                g2.setColor(new Color(243, 244, 246));
                g2.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
                g2.dispose();
            }
        };
        imgPanel.setOpaque(false);
        imgPanel.setPreferredSize(new Dimension(0, 155));  // width=0 → stretch to column

        // Product image or placeholder emoji
        JLabel lblImg;
        if (image != null) {
            Image scaled = image.getImage().getScaledInstance(-1, 155, Image.SCALE_SMOOTH);
            lblImg = new JLabel(new ImageIcon(scaled), SwingConstants.CENTER);
        } else {
            lblImg = new JLabel("\uD83D\uDCE6", SwingConstants.CENTER); // 📦
            lblImg.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 44));
        }
        imgPanel.add(lblImg, BorderLayout.CENTER);


        // Category badge — painted as an overlay on top of the image using a
        // ComponentListener so its position tracks the panel's actual width.
        if (category != null && !category.isEmpty()) {
            JLabel badge = new JLabel(category, SwingConstants.CENTER) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(255, 255, 255, 220));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                    super.paintComponent(g);
                    g2.dispose();
                }
            };
            badge.setOpaque(false);
            badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            badge.setForeground(TEXT_MAIN);
            badge.setBorder(new EmptyBorder(3, 9, 3, 9));

            // Re-position badge whenever imgPanel is resized
            imgPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override public void componentResized(java.awt.event.ComponentEvent e) {
                    Dimension bs = badge.getPreferredSize();
                    badge.setBounds(imgPanel.getWidth() - bs.width - 8, 8,
                                    bs.width, bs.height + 2);
                }
            });
            imgPanel.setLayout(null);      // switch to null just for this panel so badge can overlay
            imgPanel.add(lblImg);          // re-add image label under null layout
            imgPanel.add(badge);

            // Make lblImg always fill the full panel
            imgPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override public void componentResized(java.awt.event.ComponentEvent e) {
                    lblImg.setBounds(0, 0, imgPanel.getWidth(), imgPanel.getHeight());
                }
            });
        }
        // (imgPanel was already populated with lblImg via BorderLayout.CENTER above
        //  when category == null; the if-block above switches to null layout + re-adds.)

        // ── Info area ─────────────────────────────────────────────────────────
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(8, 12, 0, 12));

        // Product name (2 lines)
        JLabel lblName = new JLabel(
                "<html><div style='text-align:left;line-height:1.3'>" + name + "</div></html>");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblName.setForeground(TEXT_MAIN);
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Price + stock row
        JPanel priceRow = new JPanel(new BorderLayout());
        priceRow.setOpaque(false);
        priceRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblPrice = new JLabel(String.format("%,dđ", price));
        lblPrice.setFont(FONT_PRICE);
        lblPrice.setForeground(PRIMARY);

        JPanel stockBadge = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        stockBadge.setOpaque(false);
        JLabel lblStockKey = new JLabel("Kho:");
        lblStockKey.setFont(FONT_SMALL);
        lblStockKey.setForeground(TEXT_SUB);
        JLabel lblStockVal = new JLabel(stock <= 0 ? "Hết" : String.valueOf(stock));
        lblStockVal.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStockVal.setForeground(stock <= 0 ? DANGER : TEXT_MAIN);
        stockBadge.add(lblStockKey);
        stockBadge.add(lblStockVal);

        priceRow.add(lblPrice,   BorderLayout.WEST);
        priceRow.add(stockBadge, BorderLayout.EAST);

        infoPanel.add(lblName);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(priceRow);

        // ── Add-to-cart button ────────────────────────────────────────────────
        JButton btnAdd = new JButton(stock <= 0 ? "Tạm hết" : "+ Thêm");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAdd.setPreferredSize(new Dimension(0, 34));
        styleAddButton(btnAdd, stock > 0);

        JPanel btnWrapper = new JPanel(new BorderLayout());
        btnWrapper.setOpaque(false);
        btnWrapper.setBorder(new EmptyBorder(6, 12, 0, 12));
        btnWrapper.add(btnAdd, BorderLayout.CENTER);

        if (stock > 0) {
            btnAdd.addActionListener(e -> {
                currentSelectedProduct = new Object[]{id, name, price, stock};
                if (addToCartListener != null) {
                    addToCartListener.actionPerformed(
                            new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "ADD_TO_CART"));
                }
            });
        }

        card.add(imgPanel,   BorderLayout.NORTH);
        card.add(infoPanel,  BorderLayout.CENTER);
        card.add(btnWrapper, BorderLayout.SOUTH);

        return card;
    }

    // =========================================================================
    // MVC BRIDGE METHODS  (unchanged signatures)
    // =========================================================================

    public void addAddToCartListener(ActionListener listener) {
        this.addToCartListener = listener;
    }

    public void addCheckoutListener(ActionListener listener) {
        btnCheckout.addActionListener(listener);
    }

    /**
     * Controller registers this listener to handle the "Tìm" button.
     * Inside the listener, call {@link #showCustomerSelectionDialog} with the results.
     * Example:
     * <pre>
     *   view.addFindCustomerListener(e -> {
     *       List&lt;Customer&gt; results = dao.findByPhone(view.getPhoneInput());
     *       view.showCustomerSelectionDialog(results);
     *   });
     * </pre>
     */
    public void addFindCustomerListener(ActionListener listener) {
        this.findCustomerListener = listener;
    }

    /**
     * Controller registers this to know when the user picks a customer from the dialog.
     * Call {@link #getSelectedCustomer()} inside to get the chosen object.
     */
    public void addCustomerSelectedListener(ActionListener listener) {
        this.customerSelectedListener = listener;
    }

    /** Returns whatever the cashier typed in the phone field. */
    public String getPhoneInput() {
        return txtPhone.getText().trim();
    }

    /** Returns the customer the cashier selected from the dialog (null if none). */
    public is216.petshop.model.Customer getSelectedCustomer() {
        return selectedCustomer;
    }

    /**
     * Opens a styled selection dialog with the provided customer list.
     * Called by the Controller after querying the database.
     * If the list is empty, shows a "not found" message instead.
     */
    public void showCustomerSelectionDialog(
            java.util.List<is216.petshop.model.Customer> customers) {

        if (customers == null || customers.isEmpty()) {
            showMessage("Không tìm thấy khách hàng với số điện thoại này.", false);
            return;
        }

        // ── Build dialog ─────────────────────────────────────────────────────
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = owner instanceof Frame
                ? new JDialog((Frame) owner, "Chọn khách hàng", true)
                : new JDialog((Dialog) owner, "Chọn khách hàng", true);
        dlg.setLayout(new BorderLayout(0, 0));
        dlg.setSize(520, 380);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        // Header strip
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setBorder(new EmptyBorder(14, 18, 14, 18));
        JLabel lblDlgTitle = new JLabel("Danh sách khách hàng tìm được");
        lblDlgTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblDlgTitle.setForeground(Color.WHITE);
        JLabel lblCount = new JLabel(customers.size() + " kết quả");
        lblCount.setFont(FONT_BODY);
        lblCount.setForeground(new Color(199, 210, 254));
        header.add(lblDlgTitle, BorderLayout.WEST);
        header.add(lblCount,    BorderLayout.EAST);

        // Table
        String[] cols = {"ID", "Họ tên", "Số điện thoại", "Điểm tích lũy"};
        DefaultTableModel dlgModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (is216.petshop.model.Customer c : customers) {
            dlgModel.addRow(new Object[]{
                c.getId(),
                c.getName(),
                c.getPhone(),
                String.format("%,d điểm", c.getLoyaltyPoints())
            });
        }

        JTable tbl = new JTable(dlgModel);
        tbl.setFont(FONT_BODY);
        tbl.setRowHeight(34);
        tbl.setShowGrid(false);
        tbl.setIntercellSpacing(new Dimension(0, 0));
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl.setSelectionBackground(new Color(238, 242, 255));
        tbl.setSelectionForeground(TEXT_MAIN);
        tbl.getTableHeader().setFont(FONT_BOLD);
        tbl.getTableHeader().setBackground(new Color(249, 250, 251));
        tbl.getTableHeader().setForeground(TEXT_SUB);
        tbl.getTableHeader().setReorderingAllowed(false);
        // Hide ID column (still accessible by row index)
        tbl.getColumnModel().getColumn(0).setMinWidth(0);
        tbl.getColumnModel().getColumn(0).setMaxWidth(0);
        tbl.getColumnModel().getColumn(0).setWidth(0);
        // Auto-select first row
        if (dlgModel.getRowCount() > 0) tbl.setRowSelectionInterval(0, 0);

        JScrollPane scrollDlg = new JScrollPane(tbl);
        scrollDlg.setBorder(BorderFactory.createEmptyBorder());

        // Footer buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(new Color(249, 250, 251));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JButton btnCancel = createRoundedButton("Hủy", new Color(229, 231, 235),
                TEXT_MAIN, 8);
        btnCancel.setFont(FONT_BOLD);
        btnCancel.setPreferredSize(new Dimension(90, 36));

        JButton btnSelect = createRoundedButton("Chọn khách hàng", PRIMARY,
                Color.WHITE, 8);
        btnSelect.setFont(FONT_BOLD);
        btnSelect.setPreferredSize(new Dimension(160, 36));

        footer.add(btnCancel);
        footer.add(btnSelect);

        dlg.add(header,    BorderLayout.NORTH);
        dlg.add(scrollDlg, BorderLayout.CENTER);
        dlg.add(footer,    BorderLayout.SOUTH);

        // ── Actions ──────────────────────────────────────────────────────────
        btnCancel.addActionListener(e -> dlg.dispose());

        Runnable doSelect = () -> {
            int row = tbl.getSelectedRow();
            if (row < 0) return;
            selectedCustomer = customers.get(row);
            // Update the name tag in the cart panel
            setCustomerName(selectedCustomer.getName()
                    + "  ·  " + String.format("%,d", selectedCustomer.getLoyaltyPoints()) + " điểm");
            dlg.dispose();
            // Notify Controller
            if (customerSelectedListener != null) {
                customerSelectedListener.actionPerformed(
                        new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "CUSTOMER_SELECTED"));
            }
        };

        btnSelect.addActionListener(e -> doSelect.run());
        // Double-click row also selects
        tbl.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) doSelect.run();
            }
        });

        dlg.setVisible(true); // blocks until disposed (modal)
    }

    /**
     * Updates the customer name tag under the phone row.
     * Pass {@code null} to clear.
     */
    public void setCustomerName(String name) {
        if (name != null && !name.isBlank()) {
            lblCustomerName.setText("\u2713 " + name);
            lblCustomerName.setVisible(true);
        } else {
            lblCustomerName.setText("");
            lblCustomerName.setVisible(false);
        }
    }

    /** Returns the cash amount the cashier entered (0 if blank/invalid). */
    public long getCashReceived() {
        try {
            String raw = txtCashReceived.getText().replaceAll("[^\\d]", "");
            return raw.isEmpty() ? 0 : Long.parseLong(raw);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public Object[] getSelectedProductInfo() {
        return currentSelectedProduct;
    }

    public int getInputQuantity() {
        return 1;
    }

    public void addProductToCartTable(int id, String name, long price, int quantity) {
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            int currentId = Integer.parseInt(cartModel.getValueAt(i, 0).toString());
            if (currentId == id) {
                int currentQty = Integer.parseInt(cartModel.getValueAt(i, 2).toString());
                int newQty = currentQty + quantity;
                cartModel.setValueAt(newQty,       i, 2);
                cartModel.setValueAt(newQty * price, i, 3);
                updateEmptyCartVisibility();
                return;
            }
        }
        cartModel.addRow(new Object[]{id, name, quantity, price * quantity});
        updateEmptyCartVisibility();
    }

    public void updateTotalPrice() {
        long total = 0;
        for (int i = 0; i < tblCart.getRowCount(); i++) {
            total += Long.parseLong(tblCart.getValueAt(i, 3).toString());
        }
        currentTotal = total;
        lblTotalPrice.setText("Tổng tiền: " + String.format("%,d", total) + " VNĐ");
        recalcChange();
    }

    public int getCartRowCount() { return tblCart.getRowCount(); }

    public void clearCart() {
        if (cartModel != null) {
            cartModel.setRowCount(0);
            currentTotal = 0;
            selectedCustomer = null;
            lblTotalPrice.setText("Tổng tiền: 0 VNĐ");
            txtCashReceived.setText("0");
            lblChange.setText("0 VNĐ");
            lblChange.setForeground(new Color(5, 150, 105));
            setCustomerName(null);
            txtPhone.setText("");
            updateEmptyCartVisibility();
        }
    }

    public void showMessage(String message, boolean isError) {
        JOptionPane.showMessageDialog(this, message,
                isError ? "Lỗi" : "Thông báo",
                isError ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    public void displayProducts(java.util.List<is216.petshop.Product.Product> productList) {
        pnlProducts.removeAll();
        for (is216.petshop.Product.Product p : productList) {
            pnlProducts.add(createProductCard(p.getId(), p.getName(), p.getPrice(), p.getStock()));
        }
        pnlProducts.revalidate();
        pnlProducts.repaint();
    }

    /**
     * Extended display – pass category + image for richer cards.
     * The Controller may call this overload if it has that data.
     */
    public void displayProducts(java.util.List<is216.petshop.Product.Product> productList,
                                java.util.Map<Integer, String>    categoryMap,
                                java.util.Map<Integer, ImageIcon> imageMap) {
        pnlProducts.removeAll();
        for (is216.petshop.Product.Product p : productList) {
            String    cat = categoryMap != null ? categoryMap.getOrDefault(p.getId(), null) : null;
            ImageIcon img = imageMap    != null ? imageMap.getOrDefault(p.getId(), null)    : null;
            pnlProducts.add(createProductCard(p.getId(), p.getName(), p.getPrice(), p.getStock(), cat, img));
        }
        pnlProducts.revalidate();
        pnlProducts.repaint();
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    /** Show/hide the empty-cart label based on row count. */
    private void updateEmptyCartVisibility() {
        if (lblEmptyCart == null) return;
        boolean empty = (cartModel == null || cartModel.getRowCount() == 0);
        lblEmptyCart.setVisible(empty);
    }

    /** Creates a white rounded-corner card panel. */
    private JPanel createCard(LayoutManager layout, EmptyBorder padding) {
        JPanel card = new JPanel(layout) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // subtle drop shadow
                g2.setColor(new Color(0, 0, 0, 12));
                g2.fill(new RoundRectangle2D.Float(2, 3, getWidth() - 4, getHeight() - 3, 18, 18));
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 18, 18));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(padding);
        return card;
    }

    /** Creates a rounded JButton with custom colors. */
    private JButton createRoundedButton(String text, Color bg, Color fg, int radius) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()  ? bg.darker()  :
                            getModel().isRollover() ? bg.brighter() : bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius * 2, radius * 2));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setForeground(fg);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Styles the add-to-cart button (enabled / disabled). */
    private void styleAddButton(JButton btn, boolean enabled) {
        if (enabled) {
            btn.setBackground(new Color(238, 242, 255));
            btn.setForeground(PRIMARY);
            btn.setBorder(BorderFactory.createLineBorder(new Color(199, 210, 254), 1, true));
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        } else {
            btn.setBackground(new Color(243, 244, 246));
            btn.setForeground(new Color(156, 163, 175));
            btn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setEnabled(false);
        }
    }

    // ── Custom border for rounded panels ─────────────────────────────────────
    private static class RoundedBorder implements javax.swing.border.Border {
        private final Color color;
        private final int   thickness;
        private final int   radius;

        RoundedBorder(Color color, int thickness, int radius) {
            this.color = color; this.thickness = thickness; this.radius = radius;
        }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Float(x, y, w - 1, h - 1, radius, radius));
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }
        @Override public boolean isBorderOpaque() { return false; }
    }

    /** Creates a custom drawn clock icon for the pending orders button */
    private ImageIcon createClockIcon(int size, Color color) {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(color);
        g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // Draw outer circle
        g.drawOval(2, 2, size - 4, size - 4);
        
        // Draw clock hands
        int cx = size / 2;
        int cy = size / 2;
        g.drawLine(cx, cy, cx, cy - size / 4 + 1); // Minute hand
        g.drawLine(cx, cy, cx + size / 4 - 1, cy); // Hour hand
        
        g.dispose();
        return new ImageIcon(img);
    }
}