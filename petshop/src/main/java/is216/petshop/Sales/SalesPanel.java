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
    private JButton         btnPending;     // pending checkout order
    private JButton         btnViewPending; // button in header to view all pending orders
    private JButton         btnFinishedServices; // button in header to view finished services waiting for payment
    private Integer         currentBookingId = null; // links cart to booking ID
    private JPanel          pnlAuxButtons;  // holds Cancel and Pending buttons
    private JPanel          pnlProducts;
    private JLabel          lblEmptyCart;   // empty-state label in cart

    // ── New payment / customer fields ─────────────────────────────────────────
    private JTextField      txtPhone;           // phone lookup input
    private JButton         btnFindCustomer;    // search button
    private JButton         btnCreateCustomer;  // create new customer button
    private JLabel          lblCustomerName;    // resolved customer name
    private JPanel          pnlCustomerRow;     // holds resolved customer name on left, use points button on right
    private JTextField      txtCashReceived;    // cash the customer hands over
    private JLabel          lblChange;          // auto-calculated change
    private JButton         btnUsePoints;       // toggles points usage panel
    private JPanel          pnlPointsUsage;     // points usage container
    private JLabel          lblLoyaltyPointsAvailable; // customer available points label
    private JTextField      txtPointsToUse;     // points user wants to spend
    private long            currentTotal = 0;   // kept in sync with updateTotalPrice()

    // ── Customer selection state ──────────────────────────────────────────────
    private is216.petshop.Customer.Customer selectedCustomer = null;
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

        // Right: button container
        JPanel pnlHeaderButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlHeaderButtons.setOpaque(false);

        btnViewPending = createRoundedButton("Đơn chờ thanh toán", ACCENT, Color.WHITE, 10);
        btnViewPending.setIcon(createClockIcon(18, Color.WHITE));
        btnViewPending.setIconTextGap(8);
        btnViewPending.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnViewPending.setPreferredSize(new Dimension(200, 44));

        btnFinishedServices = createRoundedButton("Dịch vụ hoàn thành", new Color(16, 185, 129), Color.WHITE, 10);
        btnFinishedServices.setIcon(createClockIcon(18, Color.WHITE));
        btnFinishedServices.setIconTextGap(8);
        btnFinishedServices.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnFinishedServices.setPreferredSize(new Dimension(200, 44));

        pnlHeaderButtons.add(btnFinishedServices);
        pnlHeaderButtons.add(btnViewPending);

        header.add(titleBlock, BorderLayout.WEST);
        header.add(pnlHeaderButtons,  BorderLayout.EAST);
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
        pnlRight.setPreferredSize(new Dimension(400, 0));

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

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlButtons.setOpaque(false);

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

        btnCreateCustomer = createRoundedButton("Tạo mới", new Color(5, 150, 105), Color.WHITE, 8);
        btnCreateCustomer.setFont(FONT_BOLD);
        btnCreateCustomer.setPreferredSize(new Dimension(84, 40));
        btnCreateCustomer.addActionListener(e -> {
            Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
            is216.petshop.Customer.CustomerDAO cDao = new is216.petshop.Customer.CustomerDAO();
            is216.petshop.Customer.CustomerDialog dlg = new is216.petshop.Customer.CustomerDialog(owner, null, cDao, () -> {
                String phone = txtPhone.getText().trim();
                if (!phone.isEmpty()) {
                    btnFindCustomer.doClick();
                }
            });
            dlg.setVisible(true);
        });

        pnlButtons.add(btnFindCustomer);
        pnlButtons.add(btnCreateCustomer);

        pnlPhoneRow.add(txtPhone,   BorderLayout.CENTER);
        pnlPhoneRow.add(pnlButtons, BorderLayout.EAST);

        // Customer name result (hidden until a lookup succeeds)
        lblCustomerName = new JLabel();
        lblCustomerName.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblCustomerName.setForeground(new Color(5, 150, 105)); // green-600
        lblCustomerName.setVisible(false);

        // ── Cart table ───────────────────────────────────────────────────────
        String[] cartCols = {"Mã SP", "Tên SP", "SL", "Thành tiền"};
        cartModel = new DefaultTableModel(cartCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 2; }
        };
        tblCart = new JTable(cartModel);
        tblCart.setFont(FONT_BODY);
        tblCart.setRowHeight(34);
        tblCart.setShowGrid(false);
        tblCart.setIntercellSpacing(new Dimension(0, 0));
        tblCart.getTableHeader().setFont(FONT_BOLD);
        tblCart.getTableHeader().setBackground(new Color(249, 250, 251));
        tblCart.getTableHeader().setForeground(TEXT_SUB);
        tblCart.setSelectionBackground(new Color(238, 242, 255));
        tblCart.setSelectionForeground(PRIMARY);

        // Use custom renderer and editor for quantity column
        tblCart.getColumnModel().getColumn(2).setCellRenderer(new QuantityRenderer());
        tblCart.getColumnModel().getColumn(2).setCellEditor(new QuantityEditor(tblCart, cartModel, this));

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

        //  Action buttons panel containing Hủy, Chờ, and Thanh toán
        JPanel pnlActionButtons = new JPanel();
        pnlActionButtons.setLayout(new BoxLayout(pnlActionButtons, BoxLayout.Y_AXIS));
        pnlActionButtons.setOpaque(false);

        // Cancel and Pending buttons in one horizontal row
        pnlAuxButtons = new JPanel(new GridLayout(1, 2, 8, 0));
        pnlAuxButtons.setOpaque(false);
        pnlAuxButtons.setPreferredSize(new Dimension(0, 36));
        pnlAuxButtons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        pnlAuxButtons.setVisible(false); // Hidden by default when cart is empty

        JButton btnCancel = createRoundedButton("HỦY", DANGER, Color.WHITE, 8);
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancel.addActionListener(e -> {
            if (getCartRowCount() == 0) {
                showMessage("Giỏ hàng đang trống!", true);
                return;
            }
            int option = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn hủy giỏ hàng và xóa hết sản phẩm?",
                    "Xác nhận hủy",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (option == JOptionPane.YES_OPTION) {
                clearCart();
            }
        });

        btnPending = createRoundedButton("CHỜ", ACCENT, Color.WHITE, 8);
        btnPending.setFont(new Font("Segoe UI", Font.BOLD, 13));

        pnlAuxButtons.add(btnCancel);
        pnlAuxButtons.add(btnPending);

        // Big Checkout button at the bottom
        btnCheckout = createRoundedButton("THANH TOÁN", PRIMARY, Color.WHITE, 10);
        btnCheckout.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCheckout.setPreferredSize(new Dimension(0, 44));
        btnCheckout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        pnlActionButtons.add(pnlAuxButtons);
        pnlActionButtons.add(Box.createVerticalStrut(6));
        pnlActionButtons.add(btnCheckout);

        // Checkout container with vertical BoxLayout for natural collapsing
        JPanel pnlCheckout = new JPanel();
        pnlCheckout.setLayout(new BoxLayout(pnlCheckout, BoxLayout.Y_AXIS));
        pnlCheckout.setOpaque(false);

        pnlCheckout.add(sep2);
        pnlCheckout.add(Box.createVerticalStrut(6));
        pnlCheckout.add(lblTotalPrice);
        pnlCheckout.add(Box.createVerticalStrut(6));
        pnlCheckout.add(pnlCashRow);
        pnlCheckout.add(Box.createVerticalStrut(6));
        pnlCheckout.add(pnlChangeRow);
        pnlCheckout.add(Box.createVerticalStrut(6));
        pnlCheckout.add(pnlActionButtons);

        // ── Points usage toggle button ────────────────────────────────────────
        btnUsePoints = createRoundedButton("Đổi điểm", ACCENT, Color.WHITE, 6);
        btnUsePoints.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnUsePoints.setPreferredSize(new Dimension(95, 26));
        btnUsePoints.setMaximumSize(new Dimension(95, 26));
        btnUsePoints.setVisible(false);
        btnUsePoints.addActionListener(e -> {
            pnlPointsUsage.setVisible(!pnlPointsUsage.isVisible());
            if (!pnlPointsUsage.isVisible()) {
                txtPointsToUse.setText("0");
            }
            updateTotalPrice();
            revalidate();
            repaint();
        });

        // ── Customer Row Panel (Name on Left, Đổi điểm Button on Right) ───────
        pnlCustomerRow = new JPanel(new BorderLayout(8, 0));
        pnlCustomerRow.setOpaque(false);
        pnlCustomerRow.setVisible(false);
        
        // Expose name tag with green-600 theme
        lblCustomerName.setFont(FONT_BOLD);
        lblCustomerName.setForeground(new Color(5, 150, 105));
        lblCustomerName.setVisible(true); // Always visible inside the row parent pnlCustomerRow
        
        pnlCustomerRow.add(lblCustomerName, BorderLayout.CENTER);
        pnlCustomerRow.add(btnUsePoints, BorderLayout.EAST);

        // ── Points usage panel ────────────────────────────────────────────────
        pnlPointsUsage = new JPanel();
        pnlPointsUsage.setLayout(new BoxLayout(pnlPointsUsage, BoxLayout.Y_AXIS));
        pnlPointsUsage.setOpaque(false);
        pnlPointsUsage.setVisible(false);
        pnlPointsUsage.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)));

        lblLoyaltyPointsAvailable = new JLabel("Điểm tích lũy hiện tại: 0");
        lblLoyaltyPointsAvailable.setFont(FONT_BOLD);
        lblLoyaltyPointsAvailable.setForeground(TEXT_MAIN);

        JPanel pnlInputRow = new JPanel(new BorderLayout(6, 0));
        pnlInputRow.setOpaque(false);
        JLabel lblInputLabel = new JLabel("Điểm muốn dùng: ");
        lblInputLabel.setFont(FONT_BODY);
        lblInputLabel.setForeground(TEXT_SUB);

        txtPointsToUse = new JTextField("0");
        txtPointsToUse.setFont(FONT_BOLD);
        txtPointsToUse.setPreferredSize(new Dimension(80, 28));
        txtPointsToUse.setHorizontalAlignment(JTextField.RIGHT);
        txtPointsToUse.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(0, 6, 0, 6)));

        txtPointsToUse.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { updateTotalPrice(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { updateTotalPrice(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateTotalPrice(); }
        });

        pnlInputRow.add(lblInputLabel, BorderLayout.WEST);
        pnlInputRow.add(txtPointsToUse, BorderLayout.CENTER);

        pnlPointsUsage.add(lblLoyaltyPointsAvailable);
        pnlPointsUsage.add(Box.createVerticalStrut(6));
        pnlPointsUsage.add(pnlInputRow);

        // ── Assemble card ────────────────────────────────────────────────────
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        top.add(cartHeader);
        top.add(Box.createVerticalStrut(10));
        top.add(sep);
        top.add(Box.createVerticalStrut(10));
        top.add(pnlPhoneRow);
        top.add(Box.createVerticalStrut(6));
        top.add(pnlCustomerRow);
        top.add(Box.createVerticalStrut(6));
        top.add(pnlPointsUsage);
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
        ImageIcon img = getProductImageByName(name);
        return createProductCard(id, name, price, stock, null, img);
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
        } else if (lower.contains("pomeranian") || lower.contains("phốc sóc") || lower.contains("chó phốc")) {
            filename = "dog_logo.png"; // Fallback to logo for real dogs
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
    public is216.petshop.Customer.Customer getSelectedCustomer() {
        return selectedCustomer;
    }

    public void setSelectedCustomer(is216.petshop.Customer.Customer customer) {
        this.selectedCustomer = customer;
        if (customer != null) {
            txtPhone.setText(customer.getPhone());
            setCustomerName(customer.getName()
                    + " (" + String.format("%,d", customer.getLoyaltyPoints()) + " điểm)");
            if (pnlCustomerRow != null) pnlCustomerRow.setVisible(true);
            if (btnUsePoints != null) btnUsePoints.setVisible(true);
            if (lblLoyaltyPointsAvailable != null) {
                lblLoyaltyPointsAvailable.setText("Điểm tích lũy hiện tại: " + String.format("%,d", customer.getLoyaltyPoints()));
            }
        } else {
            txtPhone.setText("");
            setCustomerName(null);
            if (pnlCustomerRow != null) pnlCustomerRow.setVisible(false);
            if (btnUsePoints != null) btnUsePoints.setVisible(false);
            if (pnlPointsUsage != null) pnlPointsUsage.setVisible(false);
            if (txtPointsToUse != null) txtPointsToUse.setText("0");
        }
        revalidate();
        repaint();
    }

    /**
     * Opens a styled selection dialog with the provided customer list.
     * Called by the Controller after querying the database.
     * If the list is empty, shows a "not found" message instead.
     */
    public void showCustomerSelectionDialog(
            java.util.List<is216.petshop.Customer.Customer> customers) {

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
        for (is216.petshop.Customer.Customer c : customers) {
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
        tbl.setSelectionForeground(PRIMARY);
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
            setSelectedCustomer(customers.get(row));
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
            lblCustomerName.setText(name);
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
                updateCartButtonsVisibility();
                return;
            }
        }
        cartModel.addRow(new Object[]{id, name, quantity, price * quantity});
        updateEmptyCartVisibility();
        updateCartButtonsVisibility();
    }

    public void updateTotalPrice() {
        long total = 0;
        for (int i = 0; i < tblCart.getRowCount(); i++) {
            total += Long.parseLong(tblCart.getValueAt(i, 3).toString());
        }
        
        int points = getPointsToUse();
        if (points > 0) {
            double discount = points * 0.1;
            total = Math.max(total - (long) discount, 0);
        }

        currentTotal = total;
        lblTotalPrice.setText("Tổng tiền: " + String.format("%,d", total) + " VNĐ");
        recalcChange();
    }

    public int getPointsToUse() {
        if (selectedCustomer == null || pnlPointsUsage == null || !pnlPointsUsage.isVisible()) return 0;
        try {
            String raw = txtPointsToUse.getText().replaceAll("[^\\d]", "");
            if (raw.isEmpty()) return 0;
            int points = Integer.parseInt(raw);
            long maxPoints = selectedCustomer.getLoyaltyPoints();
            if (points > maxPoints) {
                points = (int) maxPoints;
                final int finalPoints = points;
                SwingUtilities.invokeLater(() -> txtPointsToUse.setText(String.valueOf(finalPoints)));
            }
            return points;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int getCartRowCount() { return tblCart.getRowCount(); }

    public long getCurrentTotal() {
        return currentTotal;
    }

    public void clearCart() {
        if (cartModel != null) {
            cartModel.setRowCount(0);
            currentTotal = 0;
            setSelectedCustomer(null);
            lblTotalPrice.setText("Tổng tiền: 0 VNĐ");
            txtCashReceived.setText("0");
            lblChange.setText("0 VNĐ");
            lblChange.setForeground(new Color(5, 150, 105));
            updateEmptyCartVisibility();
            updateCartButtonsVisibility();
        }
    }

    private void updateCartButtonsVisibility() {
        if (pnlAuxButtons != null) {
            boolean hasItems = getCartRowCount() > 0;
            pnlAuxButtons.setVisible(hasItems);
            pnlAuxButtons.revalidate();
            pnlAuxButtons.repaint();
        }
    }

    public void addPendingListener(ActionListener listener) {
        if (btnPending != null) {
            btnPending.addActionListener(listener);
        }
    }

    public void addViewPendingListener(ActionListener listener) {
        if (btnViewPending != null) {
            btnViewPending.addActionListener(listener);
        }
    }

    public void addFinishedServicesListener(ActionListener listener) {
        if (btnFinishedServices != null) {
            btnFinishedServices.addActionListener(listener);
        }
    }

    public Integer getCurrentBookingId() {
        return currentBookingId;
    }

    public void setCurrentBookingId(Integer bookingId) {
        this.currentBookingId = bookingId;
    }

    public void loadBookingIntoCart(is216.petshop.Booking.Booking booking) {
        this.currentBookingId = booking.getMaLichHen();
        
        // 1. Clear cart
        cartModel.setRowCount(0);
        currentTotal = 0;
        
        // 2. Resolve customer
        is216.petshop.Customer.CustomerDAO cDao = new is216.petshop.Customer.CustomerDAO();
        java.util.List<is216.petshop.Customer.Customer> custs = cDao.search(booking.getSoDienThoai());
        if (!custs.isEmpty()) {
            setSelectedCustomer(custs.get(0));
        } else {
            // Fallback: manually construct a Customer object
            is216.petshop.Customer.Customer temp = new is216.petshop.Customer.Customer();
            temp.setId(booking.getMaKh());
            temp.setName(booking.getTenKhachHang());
            temp.setPhone(booking.getSoDienThoai());
            setSelectedCustomer(temp);
        }
        
        // 3. For each service in the booking, match it against a product in the san_pham table
        // to retrieve the MASANPHAM (for inventory/transaction consistency).
        // If not found, use a fallback ID (e.g. 3 which is a service product).
        try (java.sql.Connection conn = is216.petshop.util.DBConnection.getConnection()) {
            for (is216.petshop.Booking.BookingServiceLine line : booking.getServices()) {
                int productId = -1;
                // Query by name match
                String sql = "SELECT MASANPHAM FROM san_pham WHERE TENSANPHAM = ?";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, line.getTenDichVu());
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            productId = rs.getInt("MASANPHAM");
                        }
                    }
                }
                
                // Fallback to searching with LIKE operator
                if (productId == -1) {
                    String sqlLike = "SELECT MASANPHAM FROM san_pham WHERE TENSANPHAM LIKE ? LIMIT 1";
                    try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlLike)) {
                        ps.setString(1, "%" + line.getTenDichVu() + "%");
                        try (java.sql.ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                productId = rs.getInt("MASANPHAM");
                            }
                        }
                    }
                }
                
                // Final fallback: use a default service ID from san_pham (e.g. 3)
                if (productId == -1) {
                    productId = 3; // Fallback to 'Combo Tắm & Cắt tỉa lông mèo dưới 5kg'
                }
                
                // Add to cart table
                addProductToCartTable(productId, line.getTenDichVu(), (long) line.getGia(), 1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            // Fallback addition if DB connection is unavailable
            for (is216.petshop.Booking.BookingServiceLine line : booking.getServices()) {
                addProductToCartTable(3, line.getTenDichVu(), (long) line.getGia(), 1);
            }
        }
        
        // Update totals
        updateTotalPrice();
    }

    public void showFinishedServicesDialog(java.util.List<is216.petshop.Booking.Booking> bookings,
                                           java.util.function.Consumer<is216.petshop.Booking.Booking> onSelected) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, "Dịch vụ hoàn thành chờ thanh toán", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new BorderLayout());
        dlg.setSize(750, 480);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        // Header Panel
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(16, 185, 129)); // green-500
        header.setBorder(new EmptyBorder(16, 24, 16, 24));
        JLabel lblTitle = new JLabel("DỊCH VỤ HOÀN THÀNH CHỜ THANH TOÁN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        JLabel lblSub = new JLabel("Chọn một dịch vụ đã hoàn thành để lập hóa đơn thanh toán");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(209, 250, 229));
        header.add(lblTitle, BorderLayout.NORTH);
        header.add(lblSub, BorderLayout.SOUTH);

        // Table Model
        String[] columns = {"MÃ LH", "THỜI GIAN", "KHÁCH HÀNG", "SỐ ĐIỆN THOẠI", "DỊCH VỤ", "THÀNH TIỀN"};
        DefaultTableModel dlgModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (is216.petshop.Booking.Booking b : bookings) {
            double total = 0;
            for (is216.petshop.Booking.BookingServiceLine line : b.getServices()) {
                total += line.getGia();
            }
            dlgModel.addRow(new Object[]{
                "LH-" + b.getMaLichHen(),
                b.getThoiGianHen() != null ? sdf.format(b.getThoiGianHen()) : "N/A",
                b.getTenKhachHang() != null ? b.getTenKhachHang() : "Khách vãng lai",
                b.getSoDienThoai() != null ? b.getSoDienThoai() : "",
                b.getServicesSummary(),
                String.format("%,.0f VNĐ", total)
            });
        }

        JTable tbl = new JTable(dlgModel);
        tbl.setFont(FONT_BODY);
        tbl.setRowHeight(36);
        tbl.setShowGrid(false);
        tbl.setShowHorizontalLines(true);
        tbl.setGridColor(BORDER_COLOR);
        tbl.setSelectionBackground(new Color(209, 250, 229));
        tbl.setSelectionForeground(new Color(6, 95, 70));
        tbl.getTableHeader().setFont(FONT_BOLD);
        tbl.getTableHeader().setBackground(new Color(249, 250, 251));
        tbl.getTableHeader().setForeground(TEXT_SUB);
        tbl.getTableHeader().setReorderingAllowed(false);

        if (dlgModel.getRowCount() > 0) tbl.setRowSelectionInterval(0, 0);

        JScrollPane scrollDlg = new JScrollPane(tbl);
        scrollDlg.setBorder(BorderFactory.createEmptyBorder());

        // Footer buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(new Color(249, 250, 251));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JButton btnCancel = createRoundedButton("Hủy", new Color(229, 231, 235), TEXT_MAIN, 8);
        btnCancel.setFont(FONT_BOLD);
        btnCancel.setPreferredSize(new Dimension(90, 36));

        JButton btnSelect = createRoundedButton("Chọn lập hóa đơn", new Color(16, 185, 129), Color.WHITE, 8);
        btnSelect.setFont(FONT_BOLD);
        btnSelect.setPreferredSize(new Dimension(160, 36));

        footer.add(btnCancel);
        footer.add(btnSelect);

        dlg.add(header,    BorderLayout.NORTH);
        dlg.add(scrollDlg, BorderLayout.CENTER);
        dlg.add(footer,    BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dlg.dispose());

        Runnable doSelect = () -> {
            int row = tbl.getSelectedRow();
            if (row < 0) return;
            is216.petshop.Booking.Booking selectedBooking = bookings.get(row);
            onSelected.accept(selectedBooking);
            dlg.dispose();
        };

        btnSelect.addActionListener(e -> doSelect.run());
        tbl.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) doSelect.run();
            }
        });

        dlg.setVisible(true);
    }


    public void showPendingOrdersDialog(java.util.List<is216.petshop.dao.InvoiceDAO.PendingOrder> pendingOrders,
                                        java.util.function.Consumer<is216.petshop.dao.InvoiceDAO.PendingOrder> onSelected) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, "Danh sách đơn hàng chờ thanh toán", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new BorderLayout());
        dlg.setSize(750, 480);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        // Header Panel
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setBorder(new EmptyBorder(16, 24, 16, 24));
        JLabel lblTitle = new JLabel("ĐƠN HÀNG CHỜ THANH TOÁN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        JLabel lblSub = new JLabel("Chọn một đơn hàng chờ để tiếp tục thanh toán");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(199, 210, 254));
        header.add(lblTitle, BorderLayout.NORTH);
        header.add(lblSub, BorderLayout.SOUTH);

        // Table Model
        String[] columns = {"MÃ HĐ", "THỜI GIAN", "KHÁCH HÀNG", "NHÂN VIÊN", "TỔNG TIỀN", "GHI CHÚ"};
        DefaultTableModel dlgModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (is216.petshop.dao.InvoiceDAO.PendingOrder o : pendingOrders) {
            dlgModel.addRow(new Object[]{
                "HD-" + o.id,
                o.date != null ? sdf.format(o.date) : "N/A",
                o.customerName,
                o.employeeName,
                String.format("%,d VNĐ", o.totalAmount),
                o.note != null ? o.note : ""
            });
        }

        JTable tbl = new JTable(dlgModel);
        tbl.setFont(FONT_BODY);
        tbl.setRowHeight(36);
        tbl.setShowGrid(false);
        tbl.setShowHorizontalLines(true);
        tbl.setGridColor(BORDER_COLOR);
        tbl.setSelectionBackground(new Color(238, 242, 255));
        tbl.setSelectionForeground(PRIMARY);
        tbl.getTableHeader().setFont(FONT_BOLD);
        tbl.getTableHeader().setBackground(new Color(249, 250, 251));
        tbl.getTableHeader().setForeground(TEXT_SUB);
        tbl.getTableHeader().setReorderingAllowed(false);

        if (dlgModel.getRowCount() > 0) tbl.setRowSelectionInterval(0, 0);

        JScrollPane scrollDlg = new JScrollPane(tbl);
        scrollDlg.setBorder(BorderFactory.createEmptyBorder());

        // Footer buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(new Color(249, 250, 251));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JButton btnCancel = createRoundedButton("Hủy", new Color(229, 231, 235), TEXT_MAIN, 8);
        btnCancel.setFont(FONT_BOLD);
        btnCancel.setPreferredSize(new Dimension(90, 36));

        JButton btnSelect = createRoundedButton("Chọn đơn hàng", PRIMARY, Color.WHITE, 8);
        btnSelect.setFont(FONT_BOLD);
        btnSelect.setPreferredSize(new Dimension(150, 36));

        footer.add(btnCancel);
        footer.add(btnSelect);

        dlg.add(header,    BorderLayout.NORTH);
        dlg.add(scrollDlg, BorderLayout.CENTER);
        dlg.add(footer,    BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dlg.dispose());

        Runnable doSelect = () -> {
            int row = tbl.getSelectedRow();
            if (row < 0) return;
            is216.petshop.dao.InvoiceDAO.PendingOrder selectedOrder = pendingOrders.get(row);
            onSelected.accept(selectedOrder);
            dlg.dispose();
        };

        btnSelect.addActionListener(e -> doSelect.run());
        tbl.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) doSelect.run();
            }
        });

        dlg.setVisible(true);
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

    public DefaultTableModel getCartModel() {
        return cartModel;
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

    // ── Quantity adjustments inside cart JTable ──────────────────────────────
    private static class QuantityPanel extends JPanel {
        final JButton btnMinus = new JButton("-");
        final JLabel  lblQty   = new JLabel("1", SwingConstants.CENTER);
        final JButton btnPlus  = new JButton("+");

        QuantityPanel() {
            setLayout(new BorderLayout(4, 0));
            setOpaque(true);
            setBackground(Color.WHITE);

            btnMinus.setPreferredSize(new Dimension(22, 22));
            btnPlus.setPreferredSize(new Dimension(22, 22));
            
            styleBtn(btnMinus);
            styleBtn(btnPlus);

            add(btnMinus, BorderLayout.WEST);
            add(lblQty,   BorderLayout.CENTER);
            add(btnPlus,  BorderLayout.EAST);
        }

        private void styleBtn(JButton btn) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setForeground(new Color(79, 70, 229)); // PRIMARY color
            btn.setBackground(new Color(238, 242, 255));
            btn.setBorder(BorderFactory.createLineBorder(new Color(199, 210, 254), 1, true));
            btn.setFocusPainted(false);
            btn.setContentAreaFilled(true);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setMargin(new Insets(0, 0, 0, 0));
        }
    }

    private static class QuantityRenderer implements javax.swing.table.TableCellRenderer {
        private final QuantityPanel panel = new QuantityPanel();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            panel.lblQty.setText(value != null ? value.toString() : "1");
            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
                panel.lblQty.setForeground(table.getSelectionForeground());
            } else {
                panel.setBackground(table.getBackground());
                panel.lblQty.setForeground(table.getForeground());
            }
            return panel;
        }
    }

    private static class QuantityEditor extends javax.swing.AbstractCellEditor 
            implements javax.swing.table.TableCellEditor {
        private final QuantityPanel panel = new QuantityPanel();
        private final JTable table;
        private final DefaultTableModel model;
        private final SalesPanel salesPanel;

        QuantityEditor(JTable table, DefaultTableModel model, SalesPanel salesPanel) {
            this.table = table;
            this.model = model;
            this.salesPanel = salesPanel;

            panel.btnMinus.addActionListener(e -> {
                int row = table.getEditingRow();
                if (row >= 0) {
                    int currentQty = Integer.parseInt(model.getValueAt(row, 2).toString());
                    if (currentQty > 1) {
                        int newQty = currentQty - 1;
                        model.setValueAt(newQty, row, 2);
                        long price = getUnitPrice(row);
                        model.setValueAt(newQty * price, row, 3);
                        salesPanel.updateTotalPrice();
                        panel.lblQty.setText(String.valueOf(newQty));
                    } else if (currentQty == 1) {
                        int option = JOptionPane.showConfirmDialog(table,
                                "Bạn có muốn xóa sản phẩm này khỏi giỏ hàng?",
                                "Xác nhận xóa",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
                        if (option == JOptionPane.YES_OPTION) {
                            fireEditingCanceled();
                            model.removeRow(row);
                            salesPanel.updateTotalPrice();
                            return;
                        }
                    }
                }
                fireEditingStopped();
            });

            panel.btnPlus.addActionListener(e -> {
                int row = table.getEditingRow();
                if (row >= 0) {
                    int currentQty = Integer.parseInt(model.getValueAt(row, 2).toString());
                    int id = Integer.parseInt(model.getValueAt(row, 0).toString());
                    
                    // Validate stock limit
                    int maxStock = getProductStock(id);
                    if (currentQty >= maxStock) {
                        JOptionPane.showMessageDialog(table, 
                                "Không thể tăng thêm! Số lượng tồn kho tối đa là: " + maxStock,
                                "Vượt quá tồn kho", 
                                JOptionPane.WARNING_MESSAGE);
                        fireEditingCanceled();
                        return;
                    }

                    int newQty = currentQty + 1;
                    model.setValueAt(newQty, row, 2);
                    long price = getUnitPrice(row);
                    model.setValueAt(newQty * price, row, 3);
                    salesPanel.updateTotalPrice();
                    panel.lblQty.setText(String.valueOf(newQty));
                }
                fireEditingStopped();
            });
        }

        private long getUnitPrice(int row) {
            long totalPrice = Long.parseLong(model.getValueAt(row, 3).toString());
            int qty = Integer.parseInt(model.getValueAt(row, 2).toString());
            return qty == 0 ? 0 : totalPrice / qty;
        }

        private int getProductStock(int id) {
            try (java.sql.Connection conn = is216.petshop.util.DBConnection.getConnection();
                 java.sql.PreparedStatement pstmt = conn.prepareStatement("SELECT SL FROM SAN_PHAM WHERE MASANPHAM = ?")) {
                pstmt.setInt(1, id);
                try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("SL");
                    }
                }
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
            return 999; // fallback if DB check fails
        }

        @Override
        public Object getCellEditorValue() {
            return panel.lblQty.getText();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            panel.lblQty.setText(value != null ? value.toString() : "1");
            panel.setBackground(table.getSelectionBackground());
            panel.lblQty.setForeground(table.getSelectionForeground());
            return panel;
        }

        @Override
        public boolean isCellEditable(java.util.EventObject e) {
            if (e instanceof java.awt.event.MouseEvent) {
                return ((java.awt.event.MouseEvent) e).getClickCount() >= 1;
            }
            return true;
        }
    }
}