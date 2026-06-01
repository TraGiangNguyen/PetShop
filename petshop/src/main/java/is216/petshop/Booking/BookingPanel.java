package is216.petshop.Booking;

import is216.petshop.Customer.Customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * View – premium Swing UI for the Booking / Services module.
 * Fully rewritten as a modern Dashboard with statistic summary cards,
 * JTable with high-fidelity renderers, and a modal dialog for new booking form.
 */
public class BookingPanel extends JPanel {

    // ── Form fields (moved to BookingDialog content) ───────────────────────────
    private JTextField       txtPhone;
    private JButton          btnFindCustomer;
    private JButton          btnCreateCustomer;
    private JButton          btnCreatePet;
    private JLabel           lblCustomerName;
    private JComboBox<String> cbPet;          // pets loaded after customer lookup
    private Map<Integer, String> petMap = new LinkedHashMap<>();

    private JComboBox<String> cbServices;
    private List<BookingServiceLine> serviceList = new ArrayList<>();
    private JButton          btnCreateService;

    // Fields for multiple services selection in new booking
    private List<BookingServiceLine> selectedServicesInForm = new ArrayList<>();
    private JPanel           pnlSelectedServicesList;
    private JLabel           lblTotalEstimate;

    private JTextField       txtDate;
    private JTextField       txtTime;
    private JTextArea        txtNote;
    private JButton          btnBook;

    // ── Dialog reference ──────────────────────────────────────────────────────
    private BookingDialog    bookingDialog;

    // ── Dashboard components ──────────────────────────────────────────────────
    private JLabel           lblWaitingCount;
    private JLabel           lblProcessingCount;
    private JLabel           lblCompletedCount;
    private JLabel           lblCancelledCount;

    private DefaultTableModel model;
    private JTable           table;

    // ── State ─────────────────────────────────────────────────────────────────
    private Customer         selectedCustomer = null;
    private List<Booking>    currentBookings = new ArrayList<>();
    private ActionListener   activeComplete = null;
    private ActionListener   activeCancel = null;

    // ── Design tokens ─────────────────────────────────────────────────────────
    private static final Color COLOR_SURFACE    = Color.WHITE;
    private static final Color COLOR_BACKGROUND = new Color(245, 246, 250);
    private static final Color COLOR_BORDER     = new Color(229, 231, 235);
    private static final Color COLOR_TEXT_PRI   = new Color(17, 24, 39);
    private static final Color COLOR_TEXT_SEC   = new Color(107, 114, 128);
    private static final Color COLOR_PRIMARY    = new Color(79, 70, 229);
    private static final Color COLOR_DANGER     = new Color(220, 38, 38);
    private static final Color COLOR_SUCCESS    = new Color(34, 197, 94);
    private static final Color WHITE            = Color.WHITE;
    private static final Color SEL_BG           = new Color(245, 243, 255);

    // ─────────────────────────────────────────────────────────────────────────
    public BookingPanel() {
        initComponents();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI CONSTRUCTION
    // ─────────────────────────────────────────────────────────────────────────
    private void initComponents() {
        setBackground(COLOR_BACKGROUND);
        setLayout(new BorderLayout(0, 20));
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // 1. Initialize Form Components for binding
        initFormComponents();

        // 2. HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_BACKGROUND);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 4));
        titlePanel.setBackground(COLOR_BACKGROUND);

        JLabel lblTitle = new JLabel("Đặt lịch & Spa chăm sóc");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(COLOR_TEXT_PRI);

        JLabel lblSubtitle = new JLabel("Đặt lịch tắm sấy, tỉa lông nghệ thuật, lưu thông tin chăm sóc thú cưng");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(COLOR_TEXT_SEC);

        titlePanel.add(lblTitle);
        titlePanel.add(lblSubtitle);
        headerPanel.add(titlePanel, BorderLayout.WEST);

        // Nút thêm lịch mới màu cam
        JButton btnAddBooking = new JButton("+ Đặt lịch hẹn mới") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed() ? new Color(211, 84, 0)
                         : getModel().isRollover() ? new Color(243, 156, 18)
                         : new Color(234, 88, 12); // #EA580C Orange
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btnAddBooking.setOpaque(false);
        btnAddBooking.setContentAreaFilled(false);
        btnAddBooking.setBorderPainted(false);
        btnAddBooking.setFocusPainted(false);
        btnAddBooking.setForeground(Color.WHITE);
        btnAddBooking.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAddBooking.setPreferredSize(new Dimension(180, 45));
        btnAddBooking.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAddBooking.addActionListener(e -> showBookingDialog());

        headerPanel.add(btnAddBooking, BorderLayout.EAST);

        // 3. MAIN CENTER BODY (Cards + Toolbar + Table)
        JPanel centerBody = new JPanel();
        centerBody.setLayout(new BoxLayout(centerBody, BoxLayout.Y_AXIS));
        centerBody.setOpaque(false);

        // 3.1. Summary cards panel
        JPanel pnlSummary = new JPanel(new GridLayout(1, 4, 20, 0));
        pnlSummary.setOpaque(false);
        pnlSummary.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        lblWaitingCount = new JLabel("0");
        lblProcessingCount = new JLabel("0");
        lblCompletedCount = new JLabel("0");
        lblCancelledCount = new JLabel("0");

        pnlSummary.add(createSummaryCard("ĐỢI CHECK-IN", "Chờ khách đến", lblWaitingCount, new Color(239, 246, 255), new Color(29, 78, 216), COLOR_TEXT_PRI));
        pnlSummary.add(createSummaryCard("ĐANG SPA", "Đang xử lý", lblProcessingCount, new Color(255, 247, 237), new Color(194, 65, 12), COLOR_TEXT_PRI));
        pnlSummary.add(createSummaryCard("HOÀN THÀNH", "Đã bàn giao", lblCompletedCount, new Color(240, 253, 244), new Color(21, 128, 61), COLOR_TEXT_PRI));
        pnlSummary.add(createSummaryCard("ĐÃ HỦY", "Hủy bỏ lịch", lblCancelledCount, new Color(254, 226, 226), new Color(185, 28, 28), COLOR_TEXT_PRI));

        centerBody.add(pnlSummary);
        centerBody.add(Box.createVerticalStrut(20));

        // 3.2. Search Toolbar + Table Card
        TableCard card = new TableCard();
        card.setLayout(new BorderLayout(0, 15));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Search panel
        JPanel searchBarPanel = new JPanel(new BorderLayout());
        searchBarPanel.setOpaque(false);
        searchBarPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        JTextField txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setPreferredSize(new Dimension(300, 42));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm khách hàng, thú cưng, dịch vụ...");
        txtSearch.putClientProperty("JTextField.showClearButton", true);
        
        searchBarPanel.add(txtSearch, BorderLayout.WEST);
        card.add(searchBarPanel, BorderLayout.NORTH);

        // Build Table inside Card
        buildTable(card);

        centerBody.add(card);

        add(headerPanel, BorderLayout.NORTH);
        add(centerBody, BorderLayout.CENTER);

        // Document Listener for filtering
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { notifyFilter(txtSearch.getText()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { notifyFilter(txtSearch.getText()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { notifyFilter(txtSearch.getText()); }
        });
    }

    private void initFormComponents() {
        txtPhone = new JTextField();
        txtPhone.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPhone.putClientProperty("JTextField.placeholderText", "Nhập SĐT...");

        btnFindCustomer = createStyledButton("Tìm", COLOR_PRIMARY, Color.WHITE);
        btnFindCustomer.setPreferredSize(new Dimension(60, 36));

        btnCreateCustomer = createStyledButton("Tạo mới", COLOR_SUCCESS, Color.WHITE);
        btnCreateCustomer.setPreferredSize(new Dimension(84, 36));
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

        lblCustomerName = new JLabel(" ");
        lblCustomerName.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 12));
        lblCustomerName.setForeground(COLOR_SUCCESS);
        lblCustomerName.setAlignmentX(Component.LEFT_ALIGNMENT);

        cbPet = new JComboBox<>(new String[]{"— Chọn sau khi tìm khách —"});
        cbPet.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbPet.setEnabled(false);

        btnCreatePet = createStyledButton("Thêm", COLOR_SUCCESS, Color.WHITE);
        btnCreatePet.setPreferredSize(new Dimension(68, 36));
        btnCreatePet.setVisible(false);
        btnCreatePet.addActionListener(e -> {
            if (selectedCustomer == null) return;
            Window owner = SwingUtilities.getWindowAncestor(this);
            JDialog dlg = (owner instanceof Frame)
                    ? new JDialog((Frame) owner, "Thêm thú cưng mới", true)
                    : new JDialog((java.awt.Dialog) owner, "Thêm thú cưng mới", true);
            dlg.setLayout(new BorderLayout());
            dlg.setSize(360, 240);
            dlg.setLocationRelativeTo(this);
            dlg.setResizable(false);
            
            JPanel form = new JPanel(new GridLayout(2, 2, 10, 10));
            form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            JLabel lblName = new JLabel("Tên thú cưng:");
            JTextField tfName = new JTextField();
            JLabel lblType = new JLabel("Loài (Chó/Mèo...):");
            JTextField tfType = new JTextField();
            
            form.add(lblName); form.add(tfName);
            form.add(lblType); form.add(tfType);
            
            JButton btnSave = createStyledButton("Lưu lại", COLOR_PRIMARY, Color.WHITE);
            btnSave.setPreferredSize(new Dimension(0, 40));
            btnSave.addActionListener(ev -> {
                String name = tfName.getText().trim();
                String type = tfType.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(dlg, "Tên thú cưng không được trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                BookingDAO dao = new BookingDAO();
                boolean ok = dao.insertPet(selectedCustomer.getId(), name, type);
                if (ok) {
                    dlg.dispose();
                    Map<Integer, String> pets = dao.getPetsByCustomer(selectedCustomer.getId());
                    populatePets(pets);
                } else {
                    JOptionPane.showMessageDialog(dlg, "Lỗi khi thêm thú cưng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });
            dlg.add(form, BorderLayout.CENTER);
            dlg.add(btnSave, BorderLayout.SOUTH);
            dlg.setVisible(true);
        });

        cbServices = new JComboBox<>(new String[]{"— Đang tải dịch vụ —"});
        cbServices.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbServices.addActionListener(e -> {
            int idx = cbServices.getSelectedIndex();
            if (idx > 0 && idx - 1 < serviceList.size()) {
                BookingServiceLine selectedSvc = serviceList.get(idx - 1);
                boolean exists = false;
                for (BookingServiceLine s : selectedServicesInForm) {
                    if (s.getMaDichVu() == selectedSvc.getMaDichVu()) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    selectedServicesInForm.add(selectedSvc);
                    updateSelectedServicesUI();
                }
                SwingUtilities.invokeLater(() -> cbServices.setSelectedIndex(0));
            }
        });

        btnCreateService = createStyledButton("Thêm", COLOR_SUCCESS, Color.WHITE);
        btnCreateService.setPreferredSize(new Dimension(68, 36));
        btnCreateService.addActionListener(e -> {
            Window owner = SwingUtilities.getWindowAncestor(this);
            JDialog dlg = (owner instanceof Frame)
                    ? new JDialog((Frame) owner, "Thêm dịch vụ mới", true)
                    : new JDialog((java.awt.Dialog) owner, "Thêm dịch vụ mới", true);
            dlg.setLayout(new BorderLayout());
            dlg.setSize(360, 240);
            dlg.setLocationRelativeTo(this);
            dlg.setResizable(false);

            JPanel form = new JPanel(new GridLayout(2, 2, 10, 10));
            form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JLabel lblName = new JLabel("Tên dịch vụ:");
            JTextField tfName = new JTextField();
            JLabel lblPrice = new JLabel("Đơn giá (đ):");
            JTextField tfPrice = new JTextField();

            form.add(lblName); form.add(tfName);
            form.add(lblPrice); form.add(tfPrice);

            JButton btnSave = createStyledButton("Lưu lại", COLOR_PRIMARY, Color.WHITE);
            btnSave.setPreferredSize(new Dimension(0, 40));
            btnSave.addActionListener(ev -> {
                String name = tfName.getText().trim();
                String priceStr = tfPrice.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(dlg, "Tên dịch vụ không được trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                double price = 0;
                try {
                    price = Double.parseDouble(priceStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dlg, "Đơn giá không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                BookingDAO dao = new BookingDAO();
                boolean ok = dao.insertService(name, price);
                if (ok) {
                    dlg.dispose();
                    populateServices(dao.getAllServices());
                } else {
                    JOptionPane.showMessageDialog(dlg, "Lỗi khi thêm dịch vụ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });

            dlg.add(form, BorderLayout.CENTER);
            dlg.add(btnSave, BorderLayout.SOUTH);
            dlg.setVisible(true);
        });

        txtDate = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        txtDate.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        txtTime = new JTextField("09:00");
        txtTime.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        txtNote = new JTextArea(3, 10);
        txtNote.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNote.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        btnBook = createStyledButton("Xác nhận đặt lịch", COLOR_PRIMARY, Color.WHITE);
        btnBook.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBook.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnBook.setPreferredSize(new Dimension(0, 44));
        btnBook.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void showBookingDialog() {
        if (bookingDialog == null) {
            Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
            bookingDialog = new BookingDialog(owner);
        }
        bookingDialog.setLocationRelativeTo(this);
        bookingDialog.setVisible(true);
    }

    // ── Booking form JComponent ──────────────────────────────────────────────
    private JPanel buildFormPanel() {
        JPanel pnlForm = new JPanel();
        pnlForm.setBackground(WHITE);
        pnlForm.setLayout(new BoxLayout(pnlForm, BoxLayout.Y_AXIS));
        pnlForm.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // ── Phone + search ────────────────────────────────────────────────────
        pnlForm.add(buildLabel("Số điện thoại khách hàng"));
        pnlForm.add(Box.createVerticalStrut(5));

        JPanel pnlSearch = new JPanel(new BorderLayout(8, 0));
        pnlSearch.setOpaque(false);
        pnlSearch.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        pnlSearch.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlButtons.setOpaque(false);

        pnlButtons.add(btnFindCustomer);
        pnlButtons.add(btnCreateCustomer);

        pnlSearch.add(txtPhone, BorderLayout.CENTER);
        pnlSearch.add(pnlButtons, BorderLayout.EAST);
        pnlForm.add(pnlSearch);
        pnlForm.add(Box.createVerticalStrut(4));

        lblCustomerName.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(lblCustomerName);
        pnlForm.add(Box.createVerticalStrut(12));

        // ── Pet selection ─────────────────────────────────────────────────────
        pnlForm.add(buildLabel("Thú cưng"));
        pnlForm.add(Box.createVerticalStrut(5));

        JPanel pnlPetRow = new JPanel(new BorderLayout(8, 0));
        pnlPetRow.setOpaque(false);
        pnlPetRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        pnlPetRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        pnlPetRow.add(cbPet, BorderLayout.CENTER);

        pnlForm.add(pnlPetRow);
        pnlForm.add(Box.createVerticalStrut(12));

        // ── Services selection ────────────────────────────────────────────────
        pnlForm.add(buildLabel("Dịch vụ"));
        pnlForm.add(Box.createVerticalStrut(5));

        JPanel pnlServiceRow = new JPanel(new BorderLayout(8, 0));
        pnlServiceRow.setOpaque(false);
        pnlServiceRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        pnlServiceRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        pnlServiceRow.add(cbServices, BorderLayout.CENTER);
        pnlForm.add(pnlServiceRow);
        pnlForm.add(Box.createVerticalStrut(8));

        // Sub-panel to display selected services list
        pnlSelectedServicesList = new JPanel();
        pnlSelectedServicesList.setLayout(new BoxLayout(pnlSelectedServicesList, BoxLayout.Y_AXIS));
        pnlSelectedServicesList.setOpaque(false);
        pnlSelectedServicesList.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(pnlSelectedServicesList);
        pnlForm.add(Box.createVerticalStrut(4));

        // Label to display total price estimate
        lblTotalEstimate = new JLabel("Tổng tiền: 0 đ");
        lblTotalEstimate.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotalEstimate.setForeground(COLOR_SUCCESS);
        lblTotalEstimate.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(lblTotalEstimate);
        pnlForm.add(Box.createVerticalStrut(12));

        // ── Date & Time ───────────────────────────────────────────────────────
        JPanel pnlDateTime = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlDateTime.setOpaque(false);
        pnlDateTime.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlDateTime.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel colDate = new JPanel(new BorderLayout(0, 4));
        colDate.setOpaque(false);
        JLabel lblDate = buildLabel("Ngày hẹn");
        colDate.add(lblDate, BorderLayout.NORTH);
        colDate.add(txtDate, BorderLayout.CENTER);

        JPanel colTime = new JPanel(new BorderLayout(0, 4));
        colTime.setOpaque(false);
        JLabel lblTime = buildLabel("Giờ hẹn");
        colTime.add(lblTime, BorderLayout.NORTH);
        colTime.add(txtTime, BorderLayout.CENTER);

        pnlDateTime.add(colDate);
        pnlDateTime.add(colTime);
        pnlForm.add(pnlDateTime);
        pnlForm.add(Box.createVerticalStrut(12));

        // ── Note ──────────────────────────────────────────────────────────────
        pnlForm.add(buildLabel("Ghi chú"));
        pnlForm.add(Box.createVerticalStrut(5));
        
        JScrollPane scrollNote = new JScrollPane(txtNote);
        scrollNote.setBorder(BorderFactory.createEmptyBorder());
        scrollNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollNote.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        pnlForm.add(scrollNote);
        pnlForm.add(Box.createVerticalStrut(20));

        // ── Submit ────────────────────────────────────────────────────────────
        btnBook.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(btnBook);
        pnlForm.add(Box.createVerticalGlue());

        return pnlForm;
    }

    private void buildTable(JPanel card) {
        model = new DefaultTableModel(
            new Object[]{"Mã lịch", "Khách hàng", "Thú cưng", "Thời gian", "Dịch vụ Spa", "Tổng tiền", "Nhân viên", "Trạng thái", "Hành động"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 8; }
        };

        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(85); 
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(COLOR_BORDER);
        table.setBackground(WHITE);
        table.setSelectionBackground(SEL_BG);
        table.setSelectionForeground(COLOR_TEXT_PRI);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFocusable(false);

        JTableHeader hdr = table.getTableHeader();
        hdr.setReorderingAllowed(false);
        hdr.setResizingAllowed(false);
        hdr.setBackground(WHITE);
        hdr.setPreferredSize(new Dimension(0, 55));
        hdr.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
        hdr.setDefaultRenderer(new HdrRender());

        table.getColumnModel().getColumn(0).setPreferredWidth(80);   // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(280);  // Customer
        table.getColumnModel().getColumn(2).setPreferredWidth(200);  // Pet
        table.getColumnModel().getColumn(3).setPreferredWidth(120);  // Time
        table.getColumnModel().getColumn(4).setPreferredWidth(210);  // Services
        table.getColumnModel().getColumn(5).setPreferredWidth(130);  // Price
        table.getColumnModel().getColumn(6).setPreferredWidth(200);  // Staff
        table.getColumnModel().getColumn(7).setPreferredWidth(140);  // Status
        table.getColumnModel().getColumn(8).setPreferredWidth(110);  // Action

        table.getColumnModel().getColumn(0).setCellRenderer(new IdRender());
        table.getColumnModel().getColumn(1).setCellRenderer(new NameRender());
        table.getColumnModel().getColumn(2).setCellRenderer(new PetRender());
        table.getColumnModel().getColumn(3).setCellRenderer(new TimeRender());
        table.getColumnModel().getColumn(4).setCellRenderer(new ServiceSummaryRender());
        table.getColumnModel().getColumn(5).setCellRenderer(new PriceRender());
        table.getColumnModel().getColumn(6).setCellRenderer(new StaffRender());
        table.getColumnModel().getColumn(7).setCellRenderer(new StatusPillRender());
        table.getColumnModel().getColumn(8).setCellRenderer(new ActionButtonsRender());
        table.getColumnModel().getColumn(8).setCellEditor(new ActionButtonsEditor());

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(WHITE);
        card.add(sp, BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API  (called by BookingController)
    // ─────────────────────────────────────────────────────────────────────────

    public void addFindCustomerListener(ActionListener l) { btnFindCustomer.addActionListener(l); }
    public void addBookListener(ActionListener l)          { btnBook.addActionListener(l); }

    private ActionListener filterListener;
    public void addFilterListener(ActionListener l) { this.filterListener = l; }
    private void notifyFilter(String text) {
        if (filterListener != null)
            filterListener.actionPerformed(
                    new java.awt.event.ActionEvent(text, java.awt.event.ActionEvent.ACTION_PERFORMED, text));
    }

    public void populateServices(List<BookingServiceLine> services) {
        serviceList = services != null ? services : new ArrayList<>();
        cbServices.removeAllItems();
        if (serviceList.isEmpty()) {
            cbServices.addItem("— Chưa có dịch vụ nào —");
            cbServices.setEnabled(false);
        } else {
            cbServices.addItem("— Chọn dịch vụ —");
            for (BookingServiceLine s : serviceList) {
                cbServices.addItem(s.getTenDichVu() + "  —  " + String.format("%,.0f đ", s.getGia()));
            }
            cbServices.setEnabled(true);
        }
    }

    public void populatePets(Map<Integer, String> pets) {
        petMap = pets;
        cbPet.removeAllItems();
        if (pets.isEmpty()) {
            cbPet.addItem("— Khách chưa có thú cưng —");
            cbPet.setEnabled(false);
        } else {
            cbPet.addItem("— Chọn thú cưng —");
            for (String name : pets.values()) cbPet.addItem(name);
            cbPet.setEnabled(true);
        }
        btnCreatePet.setVisible(selectedCustomer != null);
    }

    public void clearPets() {
        petMap = new LinkedHashMap<>();
        cbPet.removeAllItems();
        cbPet.addItem("— Chọn sau khi tìm khách —");
        cbPet.setEnabled(false);
        if (btnCreatePet != null) {
            btnCreatePet.setVisible(false);
        }
    }

    public void setSelectedCustomer(Customer c) {
        this.selectedCustomer = c;
        if (c != null) {
            lblCustomerName.setText("👤 " + c.getName() + "  (" + c.getLoyaltyPoints() + " điểm)");
            if (btnCreatePet != null) {
                btnCreatePet.setVisible(true);
            }
        } else {
            lblCustomerName.setText("⚠️ Không tìm thấy khách hàng");
            if (btnCreatePet != null) {
                btnCreatePet.setVisible(false);
            }
        }
    }

    public void clearForm() {
        txtPhone.setText("");
        lblCustomerName.setText(" ");
        clearPets();
        if (cbServices != null && cbServices.getItemCount() > 0) {
            cbServices.setSelectedIndex(0);
        }
        txtDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        txtTime.setText("09:00");
        txtNote.setText("");
        selectedCustomer = null;
        selectedServicesInForm.clear();
        updateSelectedServicesUI();
        if (bookingDialog != null) {
            bookingDialog.setVisible(false);
        }
    }

    private void updateSelectedServicesUI() {
        if (pnlSelectedServicesList == null || lblTotalEstimate == null) return;
        pnlSelectedServicesList.removeAll();
        double total = 0;
        
        for (BookingServiceLine s : selectedServicesInForm) {
            total += s.getGia();
            
            JPanel item = new JPanel(new BorderLayout(8, 0));
            item.setBackground(new Color(243, 244, 246)); // Light gray background
            item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
            ));
            item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            item.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            JLabel lblName = new JLabel(s.getTenDichVu());
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblName.setForeground(COLOR_TEXT_PRI);
            
            JLabel lblPrice = new JLabel(String.format("%,.0f đ", s.getGia()));
            lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblPrice.setForeground(COLOR_SUCCESS);
            
            JButton btnRemove = new JButton("×") {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isRollover() ? COLOR_DANGER : COLOR_TEXT_SEC);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth("×")) / 2;
                    int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent() - 2;
                    g2.drawString("×", x, y);
                    g2.dispose();
                }
            };
            btnRemove.setOpaque(false);
            btnRemove.setContentAreaFilled(false);
            btnRemove.setBorderPainted(false);
            btnRemove.setFocusPainted(false);
            btnRemove.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnRemove.setPreferredSize(new Dimension(20, 20));
            btnRemove.addActionListener(e -> {
                selectedServicesInForm.remove(s);
                updateSelectedServicesUI();
            });
            
            JPanel pnlPriceRemove = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            pnlPriceRemove.setOpaque(false);
            pnlPriceRemove.add(lblPrice);
            pnlPriceRemove.add(btnRemove);
            
            item.add(lblName, BorderLayout.CENTER);
            item.add(pnlPriceRemove, BorderLayout.EAST);
            
            pnlSelectedServicesList.add(item);
            pnlSelectedServicesList.add(Box.createVerticalStrut(6));
        }
        
        lblTotalEstimate.setText(String.format("Tổng tiền: %,.0f đ", total));
        
        pnlSelectedServicesList.revalidate();
        pnlSelectedServicesList.repaint();
    }

    public void showMessage(String msg, boolean isError) {
        JOptionPane.showMessageDialog(this, msg,
                isError ? "Lỗi" : "Thông báo",
                isError ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    public String   getPhoneInput()   { return txtPhone.getText().trim(); }
    public String   getDateInput()    { return txtDate.getText().trim(); }
    public String   getTimeInput()    { return txtTime.getText().trim(); }
    public String   getNoteInput()    { return txtNote.getText().trim(); }
    public Customer getSelectedCustomer() { return selectedCustomer; }

    public Integer getSelectedPetId() {
        int idx = cbPet.getSelectedIndex();
        if (idx <= 0 || petMap.isEmpty()) return null;
        List<Integer> keys = new ArrayList<>(petMap.keySet());
        int petIdx = idx - 1; 
        return (petIdx >= 0 && petIdx < keys.size()) ? keys.get(petIdx) : null;
    }

    public List<Integer> getSelectedServiceIds() {
        List<Integer> selected = new ArrayList<>();
        for (BookingServiceLine s : selectedServicesInForm) {
            selected.add(s.getMaDichVu());
        }
        return selected;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BOOKING LIST BINDING & STATISTICS
    // ─────────────────────────────────────────────────────────────────────────
    public void displayBookings(List<Booking> bookings,
                                 ActionListener onComplete,
                                 ActionListener onCancel) {
        currentBookings = bookings != null ? bookings : new ArrayList<>();
        activeComplete = onComplete;
        activeCancel = onCancel;

        // 1. Calculate dynamic statistics
        int waiting = 0;
        int processing = 0;
        int completed = 0;
        int cancelled = 0;
        for (Booking b : currentBookings) {
            String st = b.getTrangThai();
            if ("Đợi check-in".equalsIgnoreCase(st)) waiting++;
            else if ("Đang thực hiện".equalsIgnoreCase(st)) processing++;
            else if ("Hoàn thành".equalsIgnoreCase(st) || "Chờ thanh toán".equalsIgnoreCase(st)) completed++;
            else if ("Đã hủy".equalsIgnoreCase(st)) cancelled++;
        }
        lblWaitingCount.setText(String.valueOf(waiting));
        lblProcessingCount.setText(String.valueOf(processing));
        lblCompletedCount.setText(String.valueOf(completed));
        lblCancelledCount.setText(String.valueOf(cancelled));

        // 2. Load JTable rows
        model.setRowCount(0);
        for (Booking b : currentBookings) {
            model.addRow(new Object[]{
                b, // ID
                b, // Customer
                b, // Pet
                b, // Time
                b.getServicesSummary(), // Services
                b, // Price
                b.getTenNhanVien() != null ? b.getTenNhanVien() + " - " + (b.getMaNv() != null ? b.getMaNv() : "") : "Chưa phân công",
                b.getTrangThai() != null ? b.getTrangThai() : "Đợi check-in",
                b  // Action
            });
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DETAIL DIALOG WITH INTEGRATED CHECK-IN / CHECK-OUT
    // ─────────────────────────────────────────────────────────────────────────
    private void showBookingDetailDialog(Booking b, String status) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = (owner instanceof Frame)
                ? new JDialog((Frame) owner, "Chi tiết lịch hẹn", true)
                : new JDialog((java.awt.Dialog) owner, "Chi tiết lịch hẹn", true);
        dlg.setLayout(new BorderLayout());
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(this);

        // ── Gradient header ────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color grad1 = COLOR_PRIMARY;
                Color grad2 = new Color(79, 65, 180);
                if ("Đang thực hiện".equalsIgnoreCase(status)) {
                    grad1 = COLOR_SUCCESS;
                    grad2 = new Color(22, 101, 52); // Darker green
                } else if ("Chờ thanh toán".equalsIgnoreCase(status)) {
                    grad1 = new Color(245, 158, 11); // Amber
                    grad2 = new Color(180, 83, 9);
                } else if ("Hoàn thành".equalsIgnoreCase(status)) {
                    grad1 = COLOR_SUCCESS;
                    grad2 = new Color(21, 128, 61);
                } else if ("Đã hủy".equalsIgnoreCase(status)) {
                    grad1 = COLOR_DANGER;
                    grad2 = new Color(153, 27, 27);
                }
                g2.setPaint(new java.awt.GradientPaint(0, 0, grad1, 0, getHeight(), grad2));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        String dayLabel = "??", monthYear = "---";
        if (b.getThoiGianHen() != null) {
            dayLabel   = new SimpleDateFormat("dd").format(b.getThoiGianHen());
            monthYear  = new SimpleDateFormat("MMMM yyyy", new java.util.Locale("vi"))
                             .format(b.getThoiGianHen()).toUpperCase();
        }
        JPanel calBadge = new JPanel(new BorderLayout(0, 2)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
            }
        };
        calBadge.setOpaque(false);
        calBadge.setPreferredSize(new Dimension(80, 80));
        calBadge.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        
        JLabel calDay = new JLabel(dayLabel, SwingConstants.CENTER);
        calDay.setFont(new Font("Segoe UI", Font.BOLD, 36));
        calDay.setForeground(Color.WHITE);
        
        JLabel calMon = new JLabel(monthYear, SwingConstants.CENTER);
        calMon.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        calMon.setForeground(new Color(230, 220, 255));
        
        calBadge.add(calDay, BorderLayout.CENTER);
        calBadge.add(calMon, BorderLayout.SOUTH);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        titleBlock.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));

        JLabel lblDlgTitle = new JLabel("Chi tiết lịch hẹn");
        lblDlgTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblDlgTitle.setForeground(Color.WHITE);

        JLabel lblId = new JLabel("Mã lịch: #" + b.getMaLichHen());
        lblId.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblId.setForeground(new Color(210, 200, 255));

        String statusText = status.toUpperCase();
        if ("Đợi check-in".equalsIgnoreCase(status)) statusText = "CHỜ CHECK-IN 🕒";
        else if ("Đang thực hiện".equalsIgnoreCase(status)) statusText = "ĐANG THỰC HIỆN SPA";
        else if ("Chờ thanh toán".equalsIgnoreCase(status)) statusText = "CHỜ THANH TOÁN 💵";
        else if ("Hoàn thành".equalsIgnoreCase(status)) statusText = "ĐÃ HOÀN THÀNH ✅";
        else if ("Đã hủy".equalsIgnoreCase(status)) statusText = "ĐÃ HỦY LỊCH ❌";

        JLabel lblStatusHdr = new JLabel("  " + statusText + "  ");
        lblStatusHdr.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStatusHdr.setForeground(Color.WHITE);
        lblStatusHdr.setOpaque(true);
        lblStatusHdr.setBackground(new Color(255, 255, 255, 45));
        lblStatusHdr.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

        titleBlock.add(lblDlgTitle);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(lblId);
        titleBlock.add(Box.createVerticalStrut(6));
        titleBlock.add(lblStatusHdr);

        header.add(calBadge, BorderLayout.WEST);
        header.add(titleBlock, BorderLayout.CENTER);
        dlg.add(header, BorderLayout.NORTH);

        // ── Form body ─────────────────────────────────────────────────────────
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // ── CARD 1: Khách hàng & Thú cưng ──────────────────────────────────────
        JPanel pnlCustPet = new JPanel();
        pnlCustPet.setLayout(new BoxLayout(pnlCustPet, BoxLayout.Y_AXIS));
        pnlCustPet.setOpaque(false);
        pnlCustPet.add(makeFieldRow("Khách hàng:", nvl(b.getTenKhachHang())));
        pnlCustPet.add(makeFieldRow("Số điện thoại:", nvl(b.getSoDienThoai())));
        String petInfo = nvl(b.getTenThuCung(), "Chưa chỉ định");
        if (b.getLoaiThuCung() != null && !b.getLoaiThuCung().isBlank()) {
            petInfo += " (" + b.getLoaiThuCung() + ")";
        }
        pnlCustPet.add(makeFieldRow("Thú cưng:", petInfo));

        body.add(createSectionCard("👤 Khách hàng & Thú cưng", pnlCustPet));
        body.add(Box.createVerticalStrut(14));

        // ── CARD 2: Dịch vụ & Lịch hẹn ─────────────────────────────────────────
        JPanel pnlSvcTime = new JPanel();
        pnlSvcTime.setLayout(new BoxLayout(pnlSvcTime, BoxLayout.Y_AXIS));
        pnlSvcTime.setOpaque(false);
        pnlSvcTime.add(makeFieldRow("Dịch vụ:", b.getServicesSummary()));
        String timeStr = b.getThoiGianHen() != null
                ? new SimpleDateFormat("HH:mm  —  dd/MM/yyyy").format(b.getThoiGianHen()) : "—";
        pnlSvcTime.add(makeFieldRow("Thời gian:", timeStr));

        JPanel notesPanel = new JPanel(new BorderLayout(12, 0));
        notesPanel.setOpaque(false);
        notesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        notesPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        JLabel lblNotes = new JLabel("Ghi chú:");
        lblNotes.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNotes.setForeground(COLOR_TEXT_SEC);
        lblNotes.setPreferredSize(new Dimension(130, 24));

        String noteText = b.getServices().isEmpty() ? "—"
                : nvl(b.getServices().get(0).getGhiChu(), "Không có ghi chú");
        JLabel lblNoteVal = new JLabel("<html><div style='width: 280px; color:#111827; font-family:Segoe UI; font-size:13px;'>" + noteText + "</div></html>");

        notesPanel.add(lblNotes, BorderLayout.WEST);
        notesPanel.add(lblNoteVal, BorderLayout.CENTER);
        pnlSvcTime.add(notesPanel);

        body.add(createSectionCard("🛠️ Dịch vụ & Lịch hẹn", pnlSvcTime));
        body.add(Box.createVerticalStrut(14));

        // ── CARD 3: Phân công nhân viên ────────────────────────────────────────
        JPanel pnlStaffAssign = new JPanel(new BorderLayout(10, 0));
        pnlStaffAssign.setOpaque(false);
        pnlStaffAssign.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlStaffAssign.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel lblStaff = new JLabel("Nhân viên thực hiện:");
        lblStaff.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblStaff.setForeground(COLOR_TEXT_SEC);
        lblStaff.setPreferredSize(new Dimension(130, 28));
        pnlStaffAssign.add(lblStaff, BorderLayout.WEST);

        // Fetch list of staff
        is216.petshop.dao.NhanVienDAO nvDao = new is216.petshop.dao.NhanVienDAO();
        List<is216.petshop.model.NhanVienModel> staffList = nvDao.getDanhSachNhanVien();

        // Populate ComboBox
        JComboBox<String> cbStaff = new JComboBox<>();
        cbStaff.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbStaff.addItem("— Chưa phân công —");

        int selectedIndex = 0;
        for (int i = 0; i < staffList.size(); i++) {
            is216.petshop.model.NhanVienModel nv = staffList.get(i);
            cbStaff.addItem(nv.getHoTen() + " (" + nv.getChucVu() + ")");
            if (b.getMaNv() != null && b.getMaNv() == nv.getMaNhanVien()) {
                selectedIndex = i + 1;
            }
        }
        cbStaff.setSelectedIndex(selectedIndex);
        pnlStaffAssign.add(cbStaff, BorderLayout.CENTER);

        // Small save assignment button
        JButton btnSaveStaff = createStyledButton("Lưu", COLOR_PRIMARY, Color.WHITE);
        btnSaveStaff.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSaveStaff.setPreferredSize(new Dimension(68, 30));
        btnSaveStaff.addActionListener(ev -> {
            int idx = cbStaff.getSelectedIndex();
            Integer newStaffId = null;
            if (idx > 0) {
                newStaffId = staffList.get(idx - 1).getMaNhanVien();
            }

            BookingDAO bookingDao = new BookingDAO();
            boolean ok = bookingDao.updateStaff(b.getMaLichHen(), newStaffId);
            if (ok) {
                b.setMaNv(newStaffId);
                if (idx > 0) {
                    b.setTenNhanVien(staffList.get(idx - 1).getHoTen());
                } else {
                    b.setTenNhanVien(null);
                }
                // Notify filter to reload JTable
                notifyFilter("");
                showMessage("Đã phân công nhân viên thành công! 👥", false);
            } else {
                showMessage("Lỗi khi phân công nhân viên!", true);
            }
        });

        JPanel pnlSaveBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlSaveBtn.setOpaque(false);
        pnlSaveBtn.add(btnSaveStaff);
        pnlStaffAssign.add(pnlSaveBtn, BorderLayout.EAST);

        body.add(createSectionCard("👥 Phân công thực hiện", pnlStaffAssign));

        JScrollPane bodyScroll = new JScrollPane(body);
        bodyScroll.setBorder(BorderFactory.createEmptyBorder());
        bodyScroll.getViewport().setBackground(Color.WHITE);
        bodyScroll.getVerticalScrollBar().setUnitIncrement(12);

        // ── Main Content Area ──
        JPanel mainArea = new JPanel(new BorderLayout(0, 0));
        mainArea.setBackground(Color.WHITE);
        dlg.setSize(540, 680);
        mainArea.add(bodyScroll, BorderLayout.CENTER);
        
        dlg.add(mainArea, BorderLayout.CENTER);
        dlg.setLocationRelativeTo(this);

        // ── Footer ────────────────────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 12));
        footer.setBackground(new Color(249, 250, 251));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));

        if ("Đợi check-in".equalsIgnoreCase(status) || "Đang thực hiện".equalsIgnoreCase(status)) {
            String btnText = "Check-in";
            Color btnBg = COLOR_PRIMARY;
            if ("Đang thực hiện".equalsIgnoreCase(status)) {
                btnText = "Check-out";
                btnBg = COLOR_SUCCESS;
            }

            JButton btnAction = createStyledButton(btnText, btnBg, Color.WHITE);
            btnAction.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnAction.setPreferredSize(new Dimension(120, 38));
            btnAction.addActionListener(e -> {
                int idx = cbStaff.getSelectedIndex();
                Integer newStaffId = null;
                if (idx > 0) {
                    newStaffId = staffList.get(idx - 1).getMaNhanVien();
                }
                BookingDAO bookingDao = new BookingDAO();
                bookingDao.updateStaff(b.getMaLichHen(), newStaffId);
                b.setMaNv(newStaffId);
                if (idx > 0) {
                    b.setTenNhanVien(staffList.get(idx - 1).getHoTen());
                } else {
                    b.setTenNhanVien(null);
                }

                if ("Đợi check-in".equalsIgnoreCase(status)) {
                    bookingDao.updateStatus(b.getMaLichHen(), "Đang thực hiện");
                    b.setTrangThai("Đang thực hiện");
                    notifyFilter(""); // refresh table
                    showMessage("Check-in thành công! Lịch hẹn đang được thực hiện.", false);
                    dlg.dispose();
                    showBookingDetailDialog(b, "Đang thực hiện");
                } else if ("Đang thực hiện".equalsIgnoreCase(status)) {
                    bookingDao.updateStatus(b.getMaLichHen(), "Chờ thanh toán");
                    b.setTrangThai("Chờ thanh toán");
                    notifyFilter(""); // refresh table
                    showMessage("Check-out thành công! Lịch hẹn đã chuyển sang chờ thanh toán.", false);
                    dlg.dispose();
                    showBookingDetailDialog(b, "Chờ thanh toán");
                }
            });
            footer.add(btnAction);
        }

        if ("Đợi check-in".equalsIgnoreCase(status)) {
            JButton btnCancel = createStyledButton("Hủy lịch", COLOR_DANGER, Color.WHITE);
            btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnCancel.setPreferredSize(new Dimension(100, 38));
            btnCancel.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(dlg,
                        "Bạn có chắc muốn hủy lịch hẹn #" + b.getMaLichHen() + "?", "Xác nhận hủy",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    dlg.dispose();
                    if (activeCancel != null) {
                        activeCancel.actionPerformed(new java.awt.event.ActionEvent(
                                b.getMaLichHen(), java.awt.event.ActionEvent.ACTION_PERFORMED, "CANCEL"));
                    }
                }
            });
            footer.add(btnCancel);
        }

        JButton btnClose = createStyledButton("Đóng", new Color(229, 231, 235), COLOR_TEXT_PRI);
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnClose.setPreferredSize(new Dimension(90, 38));
        btnClose.addActionListener(e -> dlg.dispose());
        footer.add(btnClose);

        dlg.add(footer, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI HELPERS
    // ─────────────────────────────────────────────────────────────────────────
    private JLabel buildLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(COLOR_TEXT_PRI);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JLabel makeSection(String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(COLOR_PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        return lbl;
    }

    private JPanel makeFieldRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        row.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(COLOR_TEXT_SEC);
        lbl.setPreferredSize(new Dimension(130, 24));

        JTextField fld = new JTextField(value);
        fld.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        fld.setForeground(COLOR_TEXT_PRI);
        fld.setEditable(false);
        fld.setBorder(null);
        fld.setOpaque(false);
        fld.setBackground(new Color(0, 0, 0, 0));

        row.add(lbl, BorderLayout.WEST);
        row.add(fld, BorderLayout.CENTER);
        return row;
    }

    private JPanel createSectionCard(String title, JPanel contentPanel) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(248, 250, 252)); // Light grayish blue background
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(COLOR_BORDER);
                g2.draw(new RoundRectangle2D.Float(.5f, .5f, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(COLOR_PRIMARY);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(contentPanel, BorderLayout.CENTER);
        return card;
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

    private String nvl(String s)                  { return s != null ? s : ""; }
    private String nvl(String s, String fallback) { return (s != null && !s.isBlank()) ? s : fallback; }

    // ── Dynamic Summary Cards ────────────────────────────────────────────────
    private JPanel createSummaryCard(String title, String subtitle, JLabel lblCount, Color bg, Color fg, Color textTitleColor) {
        JPanel card = new JPanel(new BorderLayout(15, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(COLOR_BORDER);
                g2.draw(new RoundRectangle2D.Float(.5f, .5f, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        card.setPreferredSize(new Dimension(0, 80));

        JPanel pnlBadge = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
            }
        };
        pnlBadge.setOpaque(false);
        pnlBadge.setPreferredSize(new Dimension(48, 48));
        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblCount.setForeground(fg);
        pnlBadge.add(lblCount);

        card.add(pnlBadge, BorderLayout.WEST);

        JPanel pnlLabels = new JPanel();
        pnlLabels.setLayout(new BoxLayout(pnlLabels, BoxLayout.Y_AXIS));
        pnlLabels.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(COLOR_TEXT_SEC);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSub.setForeground(COLOR_TEXT_PRI);

        pnlLabels.add(Box.createVerticalGlue());
        pnlLabels.add(lblTitle);
        pnlLabels.add(Box.createVerticalStrut(4));
        pnlLabels.add(lblSub);
        pnlLabels.add(Box.createVerticalGlue());

        card.add(pnlLabels, BorderLayout.CENTER);
        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CUSTOM DIALOG & JTABLE SUB-CLASSES
    // ─────────────────────────────────────────────────────────────────────────

    class BookingDialog extends JDialog {
        BookingDialog(Frame owner) {
            super(owner, "Đặt lịch hẹn mới", true);
            setSize(480, 680);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout());
            getContentPane().setBackground(WHITE);
            
            JPanel dTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
            dTop.setBackground(COLOR_PRIMARY);
            JLabel dLbl = new JLabel("Đặt lịch hẹn mới");
            dLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
            dLbl.setForeground(WHITE);
            dTop.add(dLbl);
            add(dTop, BorderLayout.NORTH);
            
            JPanel formPanel = buildFormPanel();
            JScrollPane scrollDlg = new JScrollPane(formPanel);
            scrollDlg.setBorder(BorderFactory.createEmptyBorder());
            scrollDlg.getViewport().setBackground(WHITE);
            scrollDlg.getVerticalScrollBar().setUnitIncrement(12);
            add(scrollDlg, BorderLayout.CENTER);
        }
    }

    class TableCard extends JPanel {
        TableCard() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(WHITE);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 24, 24));
            g2.setColor(COLOR_BORDER);
            g2.draw(new RoundRectangle2D.Float(.5f, .5f, getWidth() - 1, getHeight() - 1, 24, 24));
            g2.dispose();
        }
    }

    class HdrRender extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, f, r, c);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(COLOR_TEXT_SEC);
            setBackground(WHITE);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
            
            if (c == 0 || c == 7 || c == 8) {
                setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
                    new EmptyBorder(0, 15, 0, 15)
                ));
            }
            return this;
        }
    }

    class IdRender extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, f, r, c);
            Booking b = (Booking) v;
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(COLOR_TEXT_PRI);
            setBackground(sel ? SEL_BG : WHITE);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
            setText("#" + b.getMaLichHen());
            return this;
        }
    }

    class NameRender extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            Booking b = (Booking) v;
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBackground(sel ? SEL_BG : WHITE);
            p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
            ));

            JLabel name = new JLabel("<html><div style='width:230px;'>" + nvl(b.getTenKhachHang()) + "</div></html>");
            name.setFont(new Font("Segoe UI", Font.BOLD, 14));
            name.setForeground(COLOR_TEXT_PRI);
            name.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel phone = new JLabel(nvl(b.getSoDienThoai()));
            phone.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            phone.setForeground(COLOR_TEXT_SEC);
            phone.setAlignmentX(Component.LEFT_ALIGNMENT);

            p.add(name); p.add(Box.createVerticalStrut(4)); p.add(phone);
            return p;
        }
    }

    class PetRender extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            Booking b = (Booking) v;
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBackground(sel ? SEL_BG : WHITE);
            p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
            ));

            String petName = nvl(b.getTenThuCung(), "Chưa chỉ định");
            String petType = nvl(b.getLoaiThuCung(), "—");
            
            String color = "#EA580C"; 
            if (petType.toLowerCase().contains("mèo") || petType.toLowerCase().contains("cat")) {
                color = "#2563EB"; 
            } else if (petType.toLowerCase().contains("chó") || petType.toLowerCase().contains("dog")) {
                color = "#EA580C"; 
            }

            JLabel name = new JLabel("<html><div style='width:160px;'>" + petName + "</div></html>");
            name.setFont(new Font("Segoe UI", Font.BOLD, 14));
            name.setForeground(Color.decode(color));
            name.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel type = new JLabel(petType);
            type.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            type.setForeground(COLOR_TEXT_SEC);
            type.setAlignmentX(Component.LEFT_ALIGNMENT);

            p.add(name); p.add(Box.createVerticalStrut(4)); p.add(type);
            return p;
        }
    }

    class TimeRender extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            Booking b = (Booking) v;
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBackground(sel ? SEL_BG : WHITE);
            p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));

            String timeStr = "00:00";
            String dateStr = "01/01/1970";
            if (b.getThoiGianHen() != null) {
                timeStr = new SimpleDateFormat("HH:mm").format(b.getThoiGianHen());
                dateStr = new SimpleDateFormat("dd/M/yy").format(b.getThoiGianHen());
            }

            JLabel time = new JLabel("<html><b>" + timeStr + "</b></html>");
            time.setFont(new Font("Segoe UI", Font.BOLD, 14));
            time.setForeground(COLOR_TEXT_PRI);
            time.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel date = new JLabel(dateStr);
            date.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            date.setForeground(COLOR_TEXT_SEC);
            date.setAlignmentX(Component.LEFT_ALIGNMENT);

            p.add(time); p.add(Box.createVerticalStrut(4)); p.add(date);
            return p;
        }
    }

    class ServiceSummaryRender extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, f, r, c);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(new Color(234, 88, 12)); 
            setBackground(sel ? SEL_BG : WHITE);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
                BorderFactory.createEmptyBorder(0, 15, 0, 15)
            ));
            String sText = v != null ? v.toString() : "—";
            setText("<html><div style='width:180px;'>" + sText + "</div></html>");
            return this;
        }
    }

    class PriceRender extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, f, r, c);
            Booking b = (Booking) v;
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(new Color(22, 163, 74)); 
            setBackground(sel ? SEL_BG : WHITE);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
                BorderFactory.createEmptyBorder(0, 15, 0, 15)
            ));
            setText(String.format("%,.0f đ", b.getTongTien()));
            return this;
        }
    }

    class StaffRender extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, f, r, c);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setForeground(COLOR_TEXT_PRI);
            setBackground(sel ? SEL_BG : WHITE);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
                BorderFactory.createEmptyBorder(0, 15, 0, 15)
            ));
            return this;
        }
    }

    class StatusPillRender extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 28));
            p.setBackground(sel ? SEL_BG : WHITE);
            p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));

            String status = v != null ? v.toString() : "Đợi check-in";
            
            Color bg = new Color(239, 246, 255); 
            Color fg = new Color(29, 78, 216);
            String text = status;

            if ("Đang thực hiện".equalsIgnoreCase(status)) {
                bg = new Color(255, 247, 237); 
                fg = new Color(194, 65, 12);
                text = "Đang xử lý";
            } else if ("Hoàn thành".equalsIgnoreCase(status)) {
                bg = new Color(240, 253, 244); 
                fg = new Color(21, 128, 61);
                text = "Đã bàn giao";
            } else if ("Chờ thanh toán".equalsIgnoreCase(status)) {
                bg = new Color(254, 243, 199); 
                fg = new Color(180, 83, 9);
            } else if ("Đã hủy".equalsIgnoreCase(status)) {
                bg = new Color(254, 226, 226); 
                fg = new Color(185, 28, 28);
                text = "Đã hủy";
            }

            JLabel badge = new JLabel(text);
            badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            badge.setOpaque(true);
            badge.setBackground(bg);
            badge.setForeground(fg);
            badge.setBorder(new EmptyBorder(5, 14, 5, 14));
            badge.putClientProperty("FlatLaf.style", "arc: 99"); 
            
            p.add(badge);
            return p;
        }
    }

    class ActionButtonsRender implements TableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            Booking b = (Booking) v;
            JPanel p = mkActPanel(sel ? SEL_BG : WHITE, b.getTrangThai());
            p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
            return p;
        }
    }

    private JPanel mkActPanel(Color bg, String status) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 28));
        p.setBackground(bg);
        
        IcoBtn bEdit = new IcoBtn(true, false); 
        p.add(bEdit);
        
        if (!"Đã hủy".equalsIgnoreCase(status) && !"Hoàn thành".equalsIgnoreCase(status) && !"Chờ thanh toán".equalsIgnoreCase(status)) {
            IcoBtn bDel = new IcoBtn(false, false); 
            p.add(bDel);
        }
        return p;
    }

    class ActionButtonsEditor extends DefaultCellEditor {
        JPanel panel; IcoBtn bEdit, bDel; int cur = -1;
        ActionButtonsEditor() {
            super(new JCheckBox());
            setClickCountToStart(1);
            bEdit = new IcoBtn(true, true); bDel = new IcoBtn(false, true);
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 28));
            panel.add(bEdit); panel.add(bDel);
            bEdit.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) { 
                    fireEditingStopped(); 
                    if (cur >= 0) {
                        Booking b = currentBookings.get(cur);
                        showBookingDetailDialog(b, b.getTrangThai());
                    }
                }
            });
            bDel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) { 
                    fireEditingStopped(); 
                    if (cur >= 0) {
                        Booking b = currentBookings.get(cur);
                        int confirm = JOptionPane.showConfirmDialog(BookingPanel.this,
                                "Bạn có chắc muốn hủy lịch hẹn #" + b.getMaLichHen() + "?", "Xác nhận hủy",
                                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (confirm == JOptionPane.YES_OPTION && activeCancel != null) {
                            activeCancel.actionPerformed(new java.awt.event.ActionEvent(
                                    b.getMaLichHen(), java.awt.event.ActionEvent.ACTION_PERFORMED, "CANCEL"));
                        }
                    }
                }
            });
        }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean sel, int r, int c) {
            cur = r; 
            Booking b = currentBookings.get(r);
            panel.setBackground(SEL_BG); 
            panel.removeAll();
            panel.add(bEdit);
            if (!"Đã hủy".equalsIgnoreCase(b.getTrangThai()) && !"Hoàn thành".equalsIgnoreCase(b.getTrangThai()) && !"Chờ thanh toán".equalsIgnoreCase(b.getTrangThai())) {
                panel.add(bDel);
            }
            panel.revalidate();
            panel.repaint();
            return panel;
        }
        @Override public Object getCellEditorValue() { return ""; }
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
            Color color = pen ? new Color(99, 102, 241) : COLOR_DANGER; 
            if (hov) g2.setColor(color.brighter()); else g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if (pen) { g2.drawRect(cx - 4, cy - 4, 8, 8); g2.drawLine(cx + 4, cy - 4, cx + 6, cy - 6); }
            else { g2.drawRect(cx - 3, cy - 2, 6, 7); g2.drawLine(cx - 5, cy - 2, cx + 5, cy - 2); g2.drawLine(cx - 2, cy - 4, cx + 2, cy - 4); }
            g2.dispose();
        }
    }
}
