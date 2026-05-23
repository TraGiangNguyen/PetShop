package is216.petshop.Booking;

import is216.petshop.Customer.Customer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * View – pure Swing UI for the Booking / Services module.
 * No business logic or direct DB calls; all actions delegated to BookingController.
 */
public class BookingPanel extends JPanel {

    // ── Form fields ───────────────────────────────────────────────────────────
    private JTextField       txtPhone;
    private JButton          btnFindCustomer;
    private JButton          btnCreateCustomer;
    private JButton          btnCreatePet;
    private JLabel           lblCustomerName;
    private JComboBox<String> cbPet;          // pets loaded after customer lookup
    private Map<Integer, String> petMap = new LinkedHashMap<>();

    private JComboBox<String> cbServices;
    private List<BookingServiceLine> serviceList = new ArrayList<>();

    private JTextField       txtDate;
    private JTextField       txtTime;
    private JTextArea        txtNote;
    private JButton          btnBook;

    // ── Booking list area ─────────────────────────────────────────────────────
    private JPanel           pnlBookingsContainer;

    // ── State ─────────────────────────────────────────────────────────────────
    private Customer         selectedCustomer = null;

    // ── Design tokens ─────────────────────────────────────────────────────────
    private static final Color COLOR_SURFACE    = Color.WHITE;
    private static final Color COLOR_BACKGROUND = new Color(244, 246, 249);
    private static final Color COLOR_BORDER     = new Color(226, 232, 240);
    private static final Color COLOR_TEXT_PRI   = new Color(15, 23, 42);
    private static final Color COLOR_TEXT_SEC   = new Color(100, 116, 139);
    private static final Color COLOR_PRIMARY    = new Color(108, 93, 211);
    private static final Color COLOR_DANGER     = new Color(239, 68, 68);
    private static final Color COLOR_SUCCESS    = new Color(34, 197, 94);

    // ─────────────────────────────────────────────────────────────────────────
    public BookingPanel() {
        initComponents();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI CONSTRUCTION
    // ─────────────────────────────────────────────────────────────────────────
    private void initComponents() {
        setBackground(COLOR_BACKGROUND);
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // ── HEADER ────────────────────────────────────────────────────────────
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_BACKGROUND);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 4));
        titlePanel.setBackground(COLOR_BACKGROUND);

        JLabel lblTitle = new JLabel("Đặt lịch & Dịch vụ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(COLOR_TEXT_PRI);

        JLabel lblSubtitle = new JLabel("Lên lịch dịch vụ Spa, khách sạn, chăm sóc thú cưng");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(COLOR_TEXT_SEC);

        titlePanel.add(lblTitle);
        titlePanel.add(lblSubtitle);
        headerPanel.add(titlePanel, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // ── BODY ──────────────────────────────────────────────────────────────
        JPanel bodyPanel = new JPanel(new GridBagLayout());
        bodyPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        gbc.gridx   = 0;
        gbc.gridy   = 0;
        gbc.weightx = 0.38;
        gbc.insets  = new Insets(0, 0, 0, 16);
        bodyPanel.add(buildFormPanel(), gbc);

        gbc.gridx   = 1;
        gbc.weightx = 0.62;
        gbc.insets  = new Insets(0, 0, 0, 0);
        bodyPanel.add(buildListPanel(), gbc);

        add(bodyPanel, BorderLayout.CENTER);
    }

    // ── Left – booking form ───────────────────────────────────────────────────
    private JPanel buildFormPanel() {
        JPanel pnlForm = createRoundedCard();
        pnlForm.setLayout(new BoxLayout(pnlForm, BoxLayout.Y_AXIS));
        pnlForm.setBorder(BorderFactory.createCompoundBorder(
                pnlForm.getBorder(),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel lblFormTitle = new JLabel("Đặt lịch mới");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblFormTitle.setForeground(COLOR_TEXT_PRI);
        lblFormTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(lblFormTitle);
        pnlForm.add(Box.createVerticalStrut(18));

        // ── Phone + search ────────────────────────────────────────────────────
        pnlForm.add(buildLabel("Số điện thoại khách hàng"));
        pnlForm.add(Box.createVerticalStrut(5));

        JPanel pnlSearch = new JPanel(new BorderLayout(8, 0));
        pnlSearch.setOpaque(false);
        pnlSearch.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        pnlSearch.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPhone = new JTextField();
        txtPhone.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPhone.putClientProperty("JTextField.placeholderText", "Nhập SĐT...");

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlButtons.setOpaque(false);

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

        pnlButtons.add(btnFindCustomer);
        pnlButtons.add(btnCreateCustomer);

        pnlSearch.add(txtPhone, BorderLayout.CENTER);
        pnlSearch.add(pnlButtons, BorderLayout.EAST);
        pnlForm.add(pnlSearch);
        pnlForm.add(Box.createVerticalStrut(4));

        lblCustomerName = new JLabel(" ");
        lblCustomerName.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 12));
        lblCustomerName.setForeground(COLOR_SUCCESS);
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

        cbPet = new JComboBox<>(new String[]{"— Chọn sau khi tìm khách —"});
        cbPet.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbPet.setEnabled(false);

        btnCreatePet = createStyledButton("Thêm", COLOR_SUCCESS, Color.WHITE);
        btnCreatePet.setPreferredSize(new Dimension(68, 36));
        btnCreatePet.setVisible(false); // Hidden until a customer is searched/found

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
                    // Reload pets for this customer
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

        pnlPetRow.add(cbPet, BorderLayout.CENTER);
        pnlPetRow.add(btnCreatePet, BorderLayout.EAST);

        pnlForm.add(pnlPetRow);
        pnlForm.add(Box.createVerticalStrut(12));

        // ── Services selection ────────────────────────────────────────────────
        pnlForm.add(buildLabel("Dịch vụ"));
        pnlForm.add(Box.createVerticalStrut(5));
        cbServices = new JComboBox<>(new String[]{"— Đang tải dịch vụ —"});
        cbServices.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbServices.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbServices.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(cbServices);
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
        txtDate = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        txtDate.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        colDate.add(txtDate, BorderLayout.CENTER);

        JPanel colTime = new JPanel(new BorderLayout(0, 4));
        colTime.setOpaque(false);
        JLabel lblTime = buildLabel("Giờ hẹn");
        colTime.add(lblTime, BorderLayout.NORTH);
        txtTime = new JTextField("09:00");
        txtTime.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        colTime.add(txtTime, BorderLayout.CENTER);

        pnlDateTime.add(colDate);
        pnlDateTime.add(colTime);
        pnlForm.add(pnlDateTime);
        pnlForm.add(Box.createVerticalStrut(12));

        // ── Note ──────────────────────────────────────────────────────────────
        pnlForm.add(buildLabel("Ghi chú"));
        pnlForm.add(Box.createVerticalStrut(5));
        txtNote = new JTextArea(3, 10);
        txtNote.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNote.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        JScrollPane scrollNote = new JScrollPane(txtNote);
        scrollNote.setBorder(BorderFactory.createEmptyBorder());
        scrollNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollNote.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        pnlForm.add(scrollNote);
        pnlForm.add(Box.createVerticalStrut(16));

        // ── Submit ────────────────────────────────────────────────────────────
        btnBook = createStyledButton("Xác nhận đặt lịch 🐾", COLOR_PRIMARY, Color.WHITE);
        btnBook.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBook.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnBook.setPreferredSize(new Dimension(0, 44));
        btnBook.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(btnBook);
        pnlForm.add(Box.createVerticalGlue());

        return pnlForm;
    }

    // ── Right – booking list ──────────────────────────────────────────────────
    private JPanel buildListPanel() {
        JPanel pnlList = createRoundedCard();
        pnlList.setLayout(new BorderLayout(15, 15));
        pnlList.setBorder(BorderFactory.createCompoundBorder(
                pnlList.getBorder(),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JPanel listHeader = new JPanel(new BorderLayout());
        listHeader.setOpaque(false);

        JLabel lblListTitle = new JLabel("Lịch hẹn sắp tới");
        lblListTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblListTitle.setForeground(COLOR_TEXT_PRI);
        listHeader.add(lblListTitle, BorderLayout.WEST);

        JTextField txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(210, 34));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm lịch hẹn...");
        listHeader.add(txtSearch, BorderLayout.EAST);
        pnlList.add(listHeader, BorderLayout.NORTH);

        pnlBookingsContainer = new JPanel();
        pnlBookingsContainer.setLayout(new BoxLayout(pnlBookingsContainer, BoxLayout.Y_AXIS));
        pnlBookingsContainer.setBackground(COLOR_SURFACE);

        JScrollPane scroll = new JScrollPane(pnlBookingsContainer);
        scroll.getViewport().setBackground(COLOR_SURFACE);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        pnlList.add(scroll, BorderLayout.CENTER);

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { notifyFilter(txtSearch.getText()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { notifyFilter(txtSearch.getText()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { notifyFilter(txtSearch.getText()); }
        });

        return pnlList;
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

    /** Populate combobox from available DICH_VU rows. */
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

    /** Populate the pet combo after a customer is found. */
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
    }

    public void showMessage(String msg, boolean isError) {
        JOptionPane.showMessageDialog(this, msg,
                isError ? "Lỗi" : "Thông báo",
                isError ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Accessors ─────────────────────────────────────────────────────────────
    public String   getPhoneInput()   { return txtPhone.getText().trim(); }
    public String   getDateInput()    { return txtDate.getText().trim(); }
    public String   getTimeInput()    { return txtTime.getText().trim(); }
    public String   getNoteInput()    { return txtNote.getText().trim(); }
    public Customer getSelectedCustomer() { return selectedCustomer; }

    /** Returns the MATHUCUNG of the selected pet, or null if none/default. */
    public Integer getSelectedPetId() {
        int idx = cbPet.getSelectedIndex();
        if (idx <= 0 || petMap.isEmpty()) return null;
        List<Integer> keys = new ArrayList<>(petMap.keySet());
        int petIdx = idx - 1; // offset by the "— Chọn —" placeholder
        return (petIdx >= 0 && petIdx < keys.size()) ? keys.get(petIdx) : null;
    }

    /** Returns the list of MADICHVU for the selected service dropdown. */
    public List<Integer> getSelectedServiceIds() {
        List<Integer> selected = new ArrayList<>();
        int idx = cbServices.getSelectedIndex();
        if (idx > 0 && idx - 1 < serviceList.size()) {
            selected.add(serviceList.get(idx - 1).getMaDichVu());
        }
        return selected;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BOOKING LIST
    // ─────────────────────────────────────────────────────────────────────────
    public void displayBookings(List<Booking> bookings,
                                 ActionListener onComplete,
                                 ActionListener onCancel) {
        pnlBookingsContainer.removeAll();

        if (bookings == null || bookings.isEmpty()) {
            JLabel lbl = new JLabel("Chưa có lịch hẹn nào sắp tới", SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            lbl.setForeground(COLOR_TEXT_SEC);
            lbl.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));
            pnlBookingsContainer.add(lbl);
        } else {
            for (Booking b : bookings) {
                pnlBookingsContainer.add(createCalendarCard(b, onComplete, onCancel));
                pnlBookingsContainer.add(Box.createVerticalStrut(12));
            }
        }

        pnlBookingsContainer.revalidate();
        pnlBookingsContainer.repaint();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CARD BUILDER
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel createCalendarCard(Booking b,
                                       ActionListener onComplete,
                                       ActionListener onCancel) {
        final String status = b.getTrangThai() != null ? b.getTrangThai() : "Đợi check-in";

        JPanel card = new JPanel(new BorderLayout(15, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(248, 250, 252));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(COLOR_BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 86));
        card.setPreferredSize(new Dimension(0, 86));

        // ── LEFT: calendar badge ───────────────────────────────────────────────
        JPanel pnlCal = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color hdr = COLOR_PRIMARY;
                if ("Hoàn thành".equalsIgnoreCase(status)) hdr = COLOR_SUCCESS;
                else if ("Đã hủy".equalsIgnoreCase(status)) hdr = COLOR_DANGER;
                g2.setColor(hdr);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 22, 8, 8));
                g2.fillRect(0, 16, getWidth(), 6);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 22, getWidth(), getHeight() - 22, 8, 8));
                g2.setColor(COLOR_BORDER);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
            }
        };
        pnlCal.setOpaque(false);
        pnlCal.setPreferredSize(new Dimension(65, 62));
        pnlCal.setMinimumSize(new Dimension(65, 62));

        String monthStr = "---", dayStr = "??", yearStr = "----";
        if (b.getThoiGianHen() != null) {
            monthStr = "Thg " + new SimpleDateFormat("MM").format(b.getThoiGianHen());
            dayStr   = new SimpleDateFormat("dd").format(b.getThoiGianHen());
            yearStr  = new SimpleDateFormat("yyyy").format(b.getThoiGianHen());
        }
        JLabel lblM = new JLabel(monthStr, SwingConstants.CENTER);
        lblM.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblM.setForeground(Color.WHITE);
        lblM.setPreferredSize(new Dimension(0, 22));

        JLabel lblD = new JLabel(dayStr, SwingConstants.CENTER);
        lblD.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblD.setForeground(COLOR_TEXT_PRI);

        JLabel lblY = new JLabel(yearStr, SwingConstants.CENTER);
        lblY.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lblY.setForeground(COLOR_TEXT_SEC);
        lblY.setPreferredSize(new Dimension(0, 12));

        pnlCal.add(lblM, BorderLayout.NORTH);
        pnlCal.add(lblD, BorderLayout.CENTER);
        pnlCal.add(lblY, BorderLayout.SOUTH);

        JPanel calWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        calWrapper.setOpaque(false);
        calWrapper.add(pnlCal);
        card.add(calWrapper, BorderLayout.WEST);

        // ── CENTER: intentionally empty — all detail in the "Xem" dialog ─────
        // (no labels here)

        // ── EAST: Xem button + status pill + action buttons ───────────────────
        JPanel pnlEast = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 18));
        pnlEast.setOpaque(false);

        // "Xem" button
        JButton btnView = new JButton("Xem") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed()  ? COLOR_PRIMARY.darker()
                         : getModel().isRollover() ? new Color(91, 78, 180)
                         : COLOR_PRIMARY;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btnView.setOpaque(false);
        btnView.setContentAreaFilled(false);
        btnView.setBorderPainted(false);
        btnView.setFocusPainted(false);
        btnView.setForeground(Color.WHITE);
        btnView.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnView.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnView.setPreferredSize(new Dimension(54, 30));
        btnView.addActionListener(e -> showBookingDetailDialog(b, status));
        pnlEast.add(btnView);

        // Status pill
        JPanel pnlStatus = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = new Color(254, 243, 199);
                if ("Hoàn thành".equalsIgnoreCase(status)) bg = new Color(220, 252, 231);
                else if ("Đã hủy".equalsIgnoreCase(status)) bg = new Color(254, 226, 226);
                else if ("Đang thực hiện".equalsIgnoreCase(status)) bg = new Color(219, 234, 254);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
                g2.dispose();
            }
        };
        pnlStatus.setOpaque(false);
        pnlStatus.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        Color statusFg = new Color(217, 119, 6);
        if ("Hoàn thành".equalsIgnoreCase(status)) statusFg = COLOR_SUCCESS;
        else if ("Đã hủy".equalsIgnoreCase(status)) statusFg = COLOR_DANGER;
        else if ("Đang thực hiện".equalsIgnoreCase(status)) statusFg = new Color(29, 78, 216);

        JLabel lblStatus = new JLabel(status);
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStatus.setForeground(statusFg);
        pnlStatus.add(lblStatus);
        pnlEast.add(pnlStatus);

        // Action buttons (only when pending / in-progress)
        if ("Đợi check-in".equalsIgnoreCase(status) || "Đang thực hiện".equalsIgnoreCase(status)) {
            String btnText = "Check-in";
            Color btnBg = COLOR_PRIMARY;
            if ("Đang thực hiện".equalsIgnoreCase(status)) {
                btnText = "Check-out";
                btnBg = COLOR_SUCCESS;
            }

            JButton btnDone = createStyledButton(btnText, btnBg, Color.WHITE);
            btnDone.setPreferredSize(new Dimension(95, 30));
            btnDone.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btnDone.addActionListener(e -> {
                if (onComplete != null)
                    onComplete.actionPerformed(new java.awt.event.ActionEvent(
                            b.getMaLichHen(), java.awt.event.ActionEvent.ACTION_PERFORMED, "COMPLETE"));
            });

            JButton btnCancelB = createStyledButton("Hủy", COLOR_DANGER, Color.WHITE);
            btnCancelB.setPreferredSize(new Dimension(55, 30));
            btnCancelB.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btnCancelB.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Bạn có chắc muốn hủy lịch hẹn này?", "Xác nhận hủy",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION && onCancel != null)
                    onCancel.actionPerformed(new java.awt.event.ActionEvent(
                            b.getMaLichHen(), java.awt.event.ActionEvent.ACTION_PERFORMED, "CANCEL"));
            });

            pnlEast.add(btnDone);
            pnlEast.add(btnCancelB);
        }

        card.add(pnlEast, BorderLayout.EAST);
        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BOOKING DETAIL DIALOG
    // ─────────────────────────────────────────────────────────────────────────
    private void showBookingDetailDialog(Booking b, String status) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = (owner instanceof Frame)
                ? new JDialog((Frame) owner, "Chi tiết lịch hẹn", true)
                : new JDialog((java.awt.Dialog) owner, "Chi tiết lịch hẹn", true);
        dlg.setLayout(new BorderLayout());
        dlg.setSize(500, 580);
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(this);

        // ── Gradient header ────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new java.awt.GradientPaint(0, 0, COLOR_PRIMARY, 0, getHeight(), new Color(79, 65, 180)));
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
        JPanel calBadge = new JPanel(new BorderLayout(0, 2));
        calBadge.setOpaque(false);
        JLabel calDay = new JLabel(dayLabel, SwingConstants.CENTER);
        calDay.setFont(new Font("Segoe UI", Font.BOLD, 38));
        calDay.setForeground(Color.WHITE);
        JLabel calMon = new JLabel(monthYear, SwingConstants.CENTER);
        calMon.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        calMon.setForeground(new Color(210, 200, 255));
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

        JLabel lblStatusHdr = new JLabel("  " + status + "  ");
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
        body.setBorder(BorderFactory.createEmptyBorder(20, 28, 8, 28));

        body.add(makeSection("👤 Khách hàng"));
        body.add(makeFieldRow("Tên khách hàng", nvl(b.getTenKhachHang())));
        body.add(makeFieldRow("Số điện thoại",  nvl(b.getSoDienThoai())));
        body.add(Box.createVerticalStrut(14));

        body.add(makeSection("🐾 Thú cưng"));
        body.add(makeFieldRow("Tên thú cưng", nvl(b.getTenThuCung(), "Chưa chỉ định")));
        body.add(makeFieldRow("Loài",          nvl(b.getLoaiThuCung(), "—")));
        body.add(Box.createVerticalStrut(14));

        body.add(makeSection("🛠️ Dịch vụ & Thời gian"));
        body.add(makeFieldRow("Dịch vụ", b.getServicesSummary()));
        String timeStr = b.getThoiGianHen() != null
                ? new SimpleDateFormat("HH:mm  —  dd/MM/yyyy").format(b.getThoiGianHen()) : "—";
        body.add(makeFieldRow("Thời gian", timeStr));
        body.add(Box.createVerticalStrut(14));

        body.add(makeSection("📝 Ghi chú"));
        String noteText = b.getServices().isEmpty() ? "—"
                : nvl(b.getServices().get(0).getGhiChu(), "Không có ghi chú");
        JTextArea ta = new JTextArea(noteText);
        ta.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ta.setForeground(COLOR_TEXT_SEC);
        ta.setBackground(new Color(250, 250, 253));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setEditable(false);
        ta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        ta.setAlignmentX(Component.LEFT_ALIGNMENT);
        ta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        body.add(ta);

        JScrollPane bodyScroll = new JScrollPane(body);
        bodyScroll.setBorder(BorderFactory.createEmptyBorder());
        bodyScroll.getViewport().setBackground(Color.WHITE);
        bodyScroll.getVerticalScrollBar().setUnitIncrement(12);
        dlg.add(bodyScroll, BorderLayout.CENTER);

        // ── Footer ────────────────────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 12));
        footer.setBackground(new Color(249, 250, 251));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));

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
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(COLOR_TEXT_SEC);
        lbl.setPreferredSize(new Dimension(130, 28));

        JTextField fld = new JTextField(value);
        fld.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        fld.setForeground(COLOR_TEXT_PRI);
        fld.setEditable(false);
        fld.setBackground(new Color(250, 250, 253));
        fld.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        row.add(lbl, BorderLayout.WEST);
        row.add(fld, BorderLayout.CENTER);
        return row;
    }

    private JPanel createRoundedCard() {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 10));
                g2.fill(new RoundRectangle2D.Float(2, 3, getWidth() - 4, getHeight() - 3, 16, 16));
                g2.setColor(COLOR_SURFACE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
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
}
