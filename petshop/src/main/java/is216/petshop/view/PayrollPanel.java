package is216.petshop.view;

import is216.petshop.dao.PayrollDAO;
import is216.petshop.model.HoSoLuongModel;
import is216.petshop.model.PhieuLuongModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PayrollPanel extends JPanel {

    private static final Color COLOR_BG = new Color(244, 246, 249);
    private static final Color COLOR_SURFACE = Color.WHITE;
    private static final Color COLOR_PRIMARY = new Color(108, 93, 211);
    private static final Color COLOR_PRIMARY_HOVER = new Color(91, 78, 180);
    private static final Color COLOR_BORDER = new Color(226, 232, 240);
    private static final Color COLOR_TEXT_PRI = new Color(15, 23, 42);
    private static final Color COLOR_TEXT_SEC = new Color(100, 116, 139);
    
    private static final Color COLOR_DANGER_FG = new Color(239, 68, 68);
    private static final Color COLOR_SUCCESS_FG = new Color(34, 197, 94);
    private static final Color COLOR_ORANGE_BG = new Color(254, 243, 199);
    private static final Color COLOR_ORANGE_FG = new Color(217, 119, 6);
    private static final Color COLOR_INFO_BG = new Color(224, 242, 254);
    private static final Color COLOR_INFO_FG = new Color(3, 105, 161);
    private static final Color COLOR_SUCCESS_BG = new Color(220, 252, 231);

    private final PayrollDAO payrollDAO;
    
    // Tab Selectors
    private JButton btnTabPayroll;
    private JButton btnTabProfiles;
    private JButton btnTabTaxes;
    private JButton btnCalc;

    private CardLayout cardLayout;
    private JPanel pnlCards;

    // Tab 1: Payroll List
    private JTable tablePayroll;
    private DefaultTableModel modelPayroll;
    private JComboBox<String> cbMonth;
    private JComboBox<String> cbYear;
    private List<PhieuLuongModel> listPayroll = new ArrayList<>();

    // Tab 2: Salary Profiles
    private JTable tableProfiles;
    private DefaultTableModel modelProfiles;
    private List<HoSoLuongModel> listProfiles = new ArrayList<>();

    // Tab 3: Taxes & Coefficients fields
    private JTextField txtBhxhRate;
    private JTextField txtBhytRate;
    private JTextField txtBhtnRate;
    private JLabel lblTotalVal;
    private JTextField txtTranBaoHiem;
    private JTextField txtPitLimit1;
    private JTextField txtPitRate1;
    private JTextField txtPitLimit2;
    private JTextField txtPitRate2;
    private JTextField txtPitLimit3;
    private JTextField txtPitRate3;
    private JTextField txtPitLimit4;
    private JTextField txtPitRate4;
    private JTextField txtPitRate5;

    public PayrollPanel() {
        this.payrollDAO = new PayrollDAO();
        initComponents();
        loadPayrollData();
        loadProfilesData();
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

        JLabel lblTitle = new JLabel("Quản lý tiền lương & Thuế");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(COLOR_TEXT_PRI);

        JLabel lblSubtitle = new JLabel("Tính toán lương tự động dựa trên chấm công, cấu hình BHXH, thuế TNCN và thanh toán lương");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(COLOR_TEXT_SEC);

        pnlTitle.add(lblTitle);
        pnlTitle.add(lblSubtitle);
        pnlTopHeader.add(pnlTitle, BorderLayout.WEST);

        // Header Action Button: Tính lương tháng này
        btnCalc = createStyledButton("Tính lương tháng này", COLOR_SUCCESS_FG, Color.WHITE);
        btnCalc.setPreferredSize(new Dimension(220, 42));
        btnCalc.addActionListener(e -> calculatePayroll());
        pnlTopHeader.add(btnCalc, BorderLayout.EAST);
        pnlHeader.add(pnlTopHeader, BorderLayout.NORTH);

        // Tab selection bar
        JPanel pnlTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlTabs.setOpaque(false);
        pnlTabs.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));

        btnTabPayroll = createTabButton("Bảng lương nhân viên", new DollarIcon(16, COLOR_TEXT_SEC));
        btnTabProfiles = createTabButton("Cấu hình hồ sơ lương", new ProfileIcon(16, COLOR_TEXT_SEC));
        btnTabTaxes = createTabButton("Cấu hình hệ số & Thuế", new TaxesIcon(16, COLOR_TEXT_SEC));

        pnlTabs.add(btnTabPayroll);
        pnlTabs.add(Box.createHorizontalStrut(8));
        pnlTabs.add(btnTabProfiles);
        pnlTabs.add(Box.createHorizontalStrut(8));
        pnlTabs.add(btnTabTaxes);
        pnlHeader.add(pnlTabs, BorderLayout.SOUTH);

        add(pnlHeader, BorderLayout.NORTH);

        // ─── CENTER BODY (CARD PANEL) ─────────────────────────────────────────
        cardLayout = new CardLayout();
        pnlCards = new JPanel(cardLayout);
        pnlCards.setOpaque(false);

        // 1. Tab 1 CARD: PAYROLL VIEW
        pnlCards.add(buildPayrollViewPanel(), "PAYROLL");

        // 2. Tab 2 CARD: PROFILES VIEW
        pnlCards.add(buildProfilesViewPanel(), "PROFILES");

        // 3. Tab 3 CARD: TAXES CONFIG VIEW
        pnlCards.add(buildTaxesConfigPanel(), "TAXES");

        add(pnlCards, BorderLayout.CENTER);

        // Tab selection listeners
        btnTabPayroll.addActionListener(e -> {
            cardLayout.show(pnlCards, "PAYROLL");
            updateTabs(btnTabPayroll);
            if (btnCalc != null) btnCalc.setVisible(true);
        });

        btnTabProfiles.addActionListener(e -> {
            cardLayout.show(pnlCards, "PROFILES");
            updateTabs(btnTabProfiles);
            if (btnCalc != null) btnCalc.setVisible(false);
        });

        btnTabTaxes.addActionListener(e -> {
            cardLayout.show(pnlCards, "TAXES");
            updateTabs(btnTabTaxes);
            if (btnCalc != null) btnCalc.setVisible(false);
        });

        // Set default active tab
        updateTabs(btnTabPayroll);
    }

    private JPanel buildPayrollViewPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 20));
        pnl.setOpaque(false);

        // ─── Filter & Button Card ───
        JPanel pnlFilters = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_SURFACE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(COLOR_BORDER);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        pnlFilters.setOpaque(false);
        pnlFilters.setPreferredSize(new Dimension(0, 68));
        pnlFilters.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel pnlLeftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        pnlLeftFilters.setOpaque(false);

        cbMonth = new JComboBox<>(new String[]{
                "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
        });
        cbMonth.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbMonth.setPreferredSize(new Dimension(110, 36));
        cbMonth.putClientProperty("FlatLaf.style", "arc: 8");

        cbYear = new JComboBox<>(new String[]{"2024", "2025", "2026", "2027", "2028", "2029", "2030"});
        cbYear.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbYear.setPreferredSize(new Dimension(85, 36));
        cbYear.putClientProperty("FlatLaf.style", "arc: 8");

        // Set current month/year as default
        Calendar cal = Calendar.getInstance();
        cbMonth.setSelectedIndex(cal.get(Calendar.MONTH));
        cbYear.setSelectedItem(String.valueOf(cal.get(Calendar.YEAR)));

        cbMonth.addActionListener(e -> loadPayrollData());
        cbYear.addActionListener(e -> loadPayrollData());

        pnlLeftFilters.add(cbMonth);
        pnlLeftFilters.add(cbYear);
        pnlFilters.add(pnlLeftFilters, BorderLayout.WEST);

        // Right side button: Duyệt toàn bộ bảng lương
        JButton btnApproveAll = createStyledButton("Duyệt toàn bộ bảng lương", new Color(15, 138, 95), Color.WHITE);
        btnApproveAll.setPreferredSize(new Dimension(220, 36));
        btnApproveAll.addActionListener(e -> approveAllPayroll());
        pnlFilters.add(btnApproveAll, BorderLayout.EAST);

        pnl.add(pnlFilters, BorderLayout.NORTH);

        // ─── Table Container Card ───
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

        String[] columns = {"NHÂN VIÊN", "LƯƠNG GỘP (GROSS)", "BH NHÂN VIÊN (8.5%)", "THUẾ TNCN", "THỰC LĨNH (NET)", "TRẠNG THÁI", "HÀNH ĐỘNG"};
        modelPayroll = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 6; // Action column is editable for clicking
            }
        };

        tablePayroll = new JTable(modelPayroll);
        tablePayroll.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablePayroll.setRowHeight(64);
        tablePayroll.setShowVerticalLines(false);
        tablePayroll.setGridColor(COLOR_BG);
        tablePayroll.setBackground(COLOR_SURFACE);
        tablePayroll.setSelectionBackground(new Color(243, 241, 255));
        tablePayroll.setSelectionForeground(COLOR_TEXT_PRI);
        tablePayroll.setFocusable(false);

        JTableHeader header = tablePayroll.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(COLOR_SURFACE);
        header.setForeground(COLOR_TEXT_SEC);
        header.setPreferredSize(new Dimension(0, 42));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
        header.setReorderingAllowed(false);

        // Column widths
        tablePayroll.getColumnModel().getColumn(0).setPreferredWidth(220); // Name
        tablePayroll.getColumnModel().getColumn(1).setPreferredWidth(140); // Gross
        tablePayroll.getColumnModel().getColumn(2).setPreferredWidth(150); // Insurance
        tablePayroll.getColumnModel().getColumn(3).setPreferredWidth(130); // Tax
        tablePayroll.getColumnModel().getColumn(4).setPreferredWidth(140); // Net
        tablePayroll.getColumnModel().getColumn(5).setPreferredWidth(120); // Status
        tablePayroll.getColumnModel().getColumn(6).setPreferredWidth(100); // Action (Eye button)

        // Custom Cell Renderers
        tablePayroll.getColumnModel().getColumn(0).setCellRenderer(new EmployeeCellRenderer());
        
        tablePayroll.getColumnModel().getColumn(1).setCellRenderer(new MoneyCellRenderer(false, false));
        tablePayroll.getColumnModel().getColumn(2).setCellRenderer(new MoneyCellRenderer(true, false));
        tablePayroll.getColumnModel().getColumn(3).setCellRenderer(new MoneyCellRenderer(true, false));
        tablePayroll.getColumnModel().getColumn(4).setCellRenderer(new MoneyCellRenderer(false, true));
        tablePayroll.getColumnModel().getColumn(5).setCellRenderer(new BadgeRenderer());
        
        // Button eye renderer and editor
        tablePayroll.getColumnModel().getColumn(6).setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
                JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 18));
                pnl.setOpaque(true);
                pnl.setBackground(isSel ? table.getSelectionBackground() : COLOR_SURFACE);

                JButton btn = new JButton("👁");
                btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                btn.setForeground(COLOR_PRIMARY);
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(false);
                btn.setPreferredSize(new Dimension(36, 28));
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                pnl.add(btn);
                return pnl;
            }
        });

        tablePayroll.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            private JPanel pnl;
            private JButton btn;
            private int currentRow = -1;

            {
                pnl = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 18));
                pnl.setOpaque(true);
                btn = new JButton("👁");
                btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                btn.setForeground(COLOR_PRIMARY);
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(false);
                btn.setPreferredSize(new Dimension(36, 28));
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btn.addActionListener(e -> {
                    fireEditingStopped();
                    if (currentRow >= 0) {
                        PhieuLuongModel pl = listPayroll.get(currentRow);
                        showPayrollDetailDialog(pl);
                    }
                });
                pnl.add(btn);
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

        JScrollPane scrollPane = new JScrollPane(tablePayroll);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(COLOR_SURFACE);
        pnlTableCard.add(scrollPane, BorderLayout.CENTER);
        pnl.add(pnlTableCard, BorderLayout.CENTER);

        // Bottom manual action triggers: Duyệt phiếu, Thanh toán lương
        JPanel pnlBottomActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlBottomActions.setOpaque(false);
        
        JButton btnApprove = createStyledButton("Duyệt phiếu lương", COLOR_PRIMARY, Color.WHITE);
        btnApprove.setPreferredSize(new Dimension(180, 38));
        btnApprove.addActionListener(e -> updatePayrollStatus("Đã duyệt"));

        JButton btnPay = createStyledButton("Thanh toán lương", COLOR_SUCCESS_FG, Color.WHITE);
        btnPay.setPreferredSize(new Dimension(180, 38));
        btnPay.addActionListener(e -> updatePayrollStatus("Đã thanh toán"));

        pnlBottomActions.add(btnApprove);
        pnlBottomActions.add(btnPay);
        pnl.add(pnlBottomActions, BorderLayout.SOUTH);

        return pnl;
    }

    private JPanel buildProfilesViewPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 20));
        pnl.setOpaque(false);

        // Table profiles container
        JPanel pnlTableCard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_SURFACE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(COLOR_BORDER);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        pnlTableCard.setOpaque(false);
        pnlTableCard.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] colsProfiles = {"NHÂN VIÊN", "LƯƠNG CƠ BẢN (GROSS)", "TRẠNG THÁI", "CẤU HÌNH LƯƠNG"};
        modelProfiles = new DefaultTableModel(colsProfiles, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 3;
            }
        };

        tableProfiles = new JTable(modelProfiles);
        tableProfiles.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableProfiles.setRowHeight(64);
        tableProfiles.setShowVerticalLines(false);
        tableProfiles.setGridColor(COLOR_BG);
        tableProfiles.setBackground(COLOR_SURFACE);
        tableProfiles.setSelectionBackground(new Color(243, 241, 255));
        tableProfiles.setSelectionForeground(COLOR_TEXT_PRI);
        tableProfiles.setFocusable(false);

        JTableHeader hProfiles = tableProfiles.getTableHeader();
        hProfiles.setFont(new Font("Segoe UI", Font.BOLD, 13));
        hProfiles.setBackground(COLOR_SURFACE);
        hProfiles.setForeground(COLOR_TEXT_SEC);
        hProfiles.setPreferredSize(new Dimension(0, 42));
        hProfiles.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
        hProfiles.setReorderingAllowed(false);

        tableProfiles.getColumnModel().getColumn(0).setPreferredWidth(280);
        tableProfiles.getColumnModel().getColumn(1).setPreferredWidth(180);
        tableProfiles.getColumnModel().getColumn(2).setPreferredWidth(150);
        tableProfiles.getColumnModel().getColumn(3).setPreferredWidth(120);

        tableProfiles.getColumnModel().getColumn(0).setCellRenderer(new EmployeeSalaryCellRenderer());
        tableProfiles.getColumnModel().getColumn(1).setCellRenderer(new MoneyCellRenderer(false, false));
        tableProfiles.getColumnModel().getColumn(2).setCellRenderer(new BadgeRenderer());
        
        tableProfiles.getColumnModel().getColumn(3).setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
                JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 18));
                pnl.setOpaque(true);
                pnl.setBackground(isSel ? table.getSelectionBackground() : (r % 2 == 0 ? COLOR_SURFACE : new Color(250, 250, 252)));

                JButton btn = new JButton();
                btn.setIcon(new GearIcon(18, new Color(59, 130, 246)));
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(false);
                btn.setPreferredSize(new Dimension(36, 28));
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                pnl.add(btn);
                return pnl;
            }
        });

        tableProfiles.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            private JPanel pnl;
            private JButton btn;
            private int currentRow = -1;

            {
                pnl = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 18));
                pnl.setOpaque(true);
                btn = new JButton();
                btn.setIcon(new GearIcon(18, new Color(59, 130, 246)));
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(false);
                btn.setPreferredSize(new Dimension(36, 28));
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btn.addActionListener(e -> {
                    fireEditingStopped();
                    if (currentRow >= 0) {
                        HoSoLuongModel profile = listProfiles.get(currentRow);
                        SalaryProfileDialog dlg = new SalaryProfileDialog((Frame) SwingUtilities.getWindowAncestor(PayrollPanel.this), profile);
                        dlg.setVisible(true);
                        if (dlg.isSaved()) {
                            loadProfilesData();
                        }
                    }
                });
                pnl.add(btn);
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

        JScrollPane spProfiles = new JScrollPane(tableProfiles);
        spProfiles.setBorder(BorderFactory.createEmptyBorder());
        spProfiles.getViewport().setBackground(COLOR_SURFACE);
        pnlTableCard.add(spProfiles, BorderLayout.CENTER);
        pnl.add(pnlTableCard, BorderLayout.CENTER);

        return pnl;
    }

    private JPanel buildTaxesConfigPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 24));
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(10, 0, 10, 0));

        JPanel grid = new JPanel(new GridLayout(1, 2, 24, 0));
        grid.setOpaque(false);

        // ─── LEFT COLUMN: COMPULSORY INSURANCE ───
        JPanel pnlLeftCard = new JPanel(new BorderLayout(0, 20)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_SURFACE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(COLOR_BORDER);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        pnlLeftCard.setOpaque(false);
        pnlLeftCard.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Header Left
        JPanel pnlLeftHeader = new JPanel(new BorderLayout(12, 4));
        pnlLeftHeader.setOpaque(false);
        
        JLabel lblLeftIcon = new JLabel(new ShieldIcon(24, new Color(34, 197, 94)));
        pnlLeftHeader.add(lblLeftIcon, BorderLayout.WEST);

        JPanel pnlLeftTitleContainer = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlLeftTitleContainer.setOpaque(false);
        JLabel lblLeftTitle = new JLabel("Hệ số Bảo hiểm bắt buộc");
        lblLeftTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblLeftTitle.setForeground(COLOR_TEXT_PRI);
        JLabel lblLeftSub = new JLabel("Cấu hình các tỷ lệ trích đóng bảo hiểm xã hội bắt buộc đối với người lao động");
        lblLeftSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLeftSub.setForeground(COLOR_TEXT_SEC);
        pnlLeftTitleContainer.add(lblLeftTitle);
        pnlLeftTitleContainer.add(lblLeftSub);
        pnlLeftHeader.add(pnlLeftTitleContainer, BorderLayout.CENTER);
        pnlLeftCard.add(pnlLeftHeader, BorderLayout.NORTH);

        // Body Left (GridBagLayout for premium form styling)
        JPanel pnlLeftBody = new JPanel(new GridBagLayout());
        pnlLeftBody.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.weightx = 1.0;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 12);
        Color labelColor = new Color(100, 116, 139);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

        // Fetch current values
        double bhxhVal = payrollDAO.getConfigDouble("BHXH_RATE", 8.0);
        double bhytVal = payrollDAO.getConfigDouble("BHYT_RATE", 1.6);
        double bhtnVal = payrollDAO.getConfigDouble("BHTN_RATE", 1.0);
        long tranBHVal = payrollDAO.getConfigLong("TRAN_BAO_HIEM", 36000000L);

        // Row 1: BHXH
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblBhxh = new JLabel("TỶ LỆ ĐÓNG BHXH CỦA NLĐ (%)");
        lblBhxh.setFont(labelFont);
        lblBhxh.setForeground(labelColor);
        pnlLeftBody.add(lblBhxh, gbc);

        gbc.gridy = 1;
        txtBhxhRate = new JTextField(String.valueOf(bhxhVal));
        txtBhxhRate.setPreferredSize(new Dimension(0, 38));
        txtBhxhRate.setFont(fieldFont);
        txtBhxhRate.putClientProperty("FlatLaf.style", "arc: 8; focusColor: #22c55e");
        pnlLeftBody.add(txtBhxhRate, gbc);

        // Row 2: BHYT
        gbc.gridy = 2;
        JLabel lblBhyt = new JLabel("TỶ LỆ ĐÓNG BHYT CỦA NLĐ (%)");
        lblBhyt.setFont(labelFont);
        lblBhyt.setForeground(labelColor);
        pnlLeftBody.add(lblBhyt, gbc);

        gbc.gridy = 3;
        txtBhytRate = new JTextField(String.valueOf(bhytVal));
        txtBhytRate.setPreferredSize(new Dimension(0, 38));
        txtBhytRate.setFont(fieldFont);
        txtBhytRate.putClientProperty("FlatLaf.style", "arc: 8; focusColor: #22c55e");
        pnlLeftBody.add(txtBhytRate, gbc);

        // Row 3: BHTN
        gbc.gridy = 4;
        JLabel lblBhtn = new JLabel("TỶ LỆ ĐÓNG BHTN CỦA NLĐ (%)");
        lblBhtn.setFont(labelFont);
        lblBhtn.setForeground(labelColor);
        pnlLeftBody.add(lblBhtn, gbc);

        gbc.gridy = 5;
        txtBhtnRate = new JTextField(String.valueOf(bhtnVal));
        txtBhtnRate.setPreferredSize(new Dimension(0, 38));
        txtBhtnRate.setFont(fieldFont);
        txtBhtnRate.putClientProperty("FlatLaf.style", "arc: 8; focusColor: #22c55e");
        pnlLeftBody.add(txtBhtnRate, gbc);

        // Row 4: Total insurance display box
        gbc.gridy = 6;
        gbc.insets = new Insets(12, 0, 12, 0);
        JPanel pnlTotalBox = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(248, 250, 252)); // Slate 50
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(new Color(226, 232, 240)); // Slate 200
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
            }
        };
        pnlTotalBox.setOpaque(false);
        pnlTotalBox.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel lblTotalText = new JLabel("Tổng tỷ lệ khấu trừ lương:");
        lblTotalText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalText.setForeground(new Color(51, 65, 85)); // Slate 700
        pnlTotalBox.add(lblTotalText, BorderLayout.WEST);

        lblTotalVal = new JLabel("10.60 %");
        lblTotalVal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotalVal.setForeground(COLOR_DANGER_FG); // Red highlight
        pnlTotalBox.add(lblTotalVal, BorderLayout.EAST);
        pnlLeftBody.add(pnlTotalBox, gbc);

        // Document Listener to calculate dynamically
        javax.swing.event.DocumentListener insuranceCalcListener = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { calculateTotalInsurance(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { calculateTotalInsurance(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { calculateTotalInsurance(); }
        };
        txtBhxhRate.getDocument().addDocumentListener(insuranceCalcListener);
        txtBhytRate.getDocument().addDocumentListener(insuranceCalcListener);
        txtBhtnRate.getDocument().addDocumentListener(insuranceCalcListener);
        
        // Initial calculation
        calculateTotalInsurance();

        // Row 5: Trần bảo hiểm
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridy = 7;
        JLabel lblTran = new JLabel("MỨC TRẦN LƯƠNG ĐÓNG BẢO HIỂM (VND)");
        lblTran.setFont(labelFont);
        lblTran.setForeground(labelColor);
        pnlLeftBody.add(lblTran, gbc);

        gbc.gridy = 8;
        txtTranBaoHiem = new JTextField(String.valueOf(tranBHVal));
        txtTranBaoHiem.setPreferredSize(new Dimension(0, 38));
        txtTranBaoHiem.setFont(fieldFont);
        txtTranBaoHiem.putClientProperty("FlatLaf.style", "arc: 8; focusColor: #22c55e");
        pnlLeftBody.add(txtTranBaoHiem, gbc);

        gbc.gridy = 9;
        gbc.insets = new Insets(2, 0, 0, 0);
        JLabel lblTranSub = new JLabel("Giới hạn trần đóng bảo hiểm xã hội bắt buộc theo quy định nhà nước");
        lblTranSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTranSub.setForeground(new Color(148, 163, 184)); // Slate 400
        pnlLeftBody.add(lblTranSub, gbc);

        pnlLeftCard.add(pnlLeftBody, BorderLayout.CENTER);
        grid.add(pnlLeftCard);


        // ─── RIGHT COLUMN: PROGRESSIVE TAX ───
        JPanel pnlRightCard = new JPanel(new BorderLayout(0, 20)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_SURFACE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(COLOR_BORDER);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        pnlRightCard.setOpaque(false);
        pnlRightCard.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Header Right
        JPanel pnlRightHeader = new JPanel(new BorderLayout(12, 4));
        pnlRightHeader.setOpaque(false);

        JLabel lblRightIcon = new JLabel(new ScaleIcon(24, new Color(59, 130, 246)));
        pnlRightHeader.add(lblRightIcon, BorderLayout.WEST);

        JPanel pnlRightTitleContainer = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlRightTitleContainer.setOpaque(false);
        JLabel lblRightTitle = new JLabel("Biểu thuế lũy tiến TNCN");
        lblRightTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblRightTitle.setForeground(COLOR_TEXT_PRI);
        JLabel lblRightSub = new JLabel("Cấu hình các ngưỡng thu nhập tính thuế và thuế suất lũy tiến từng phần");
        lblRightSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRightSub.setForeground(COLOR_TEXT_SEC);
        pnlRightTitleContainer.add(lblRightTitle);
        pnlRightTitleContainer.add(lblRightSub);
        pnlRightHeader.add(pnlRightTitleContainer, BorderLayout.CENTER);
        pnlRightCard.add(pnlRightHeader, BorderLayout.NORTH);

        // Body Right (Progressive bracket rows list)
        JPanel pnlRightBody = new JPanel(new GridLayout(5, 1, 0, 10));
        pnlRightBody.setOpaque(false);

        // Fetch current values
        long limit1 = payrollDAO.getConfigLong("PIT_LIMIT_1", 10000000L);
        double rate1 = payrollDAO.getConfigDouble("PIT_RATE_1", 5.0);
        long limit2 = payrollDAO.getConfigLong("PIT_LIMIT_2", 30000000L);
        double rate2 = payrollDAO.getConfigDouble("PIT_RATE_2", 10.0);
        long limit3 = payrollDAO.getConfigLong("PIT_LIMIT_3", 60000000L);
        double rate3 = payrollDAO.getConfigDouble("PIT_RATE_3", 20.0);
        long limit4 = payrollDAO.getConfigLong("PIT_LIMIT_4", 100000000L);
        double rate4 = payrollDAO.getConfigDouble("PIT_RATE_4", 30.0);
        double rate5 = payrollDAO.getConfigDouble("PIT_RATE_5", 35.0);

        txtPitLimit1 = new JTextField(String.valueOf(limit1));
        txtPitRate1 = new JTextField(String.valueOf(rate1));
        txtPitLimit2 = new JTextField(String.valueOf(limit2));
        txtPitRate2 = new JTextField(String.valueOf(rate2));
        txtPitLimit3 = new JTextField(String.valueOf(limit3));
        txtPitRate3 = new JTextField(String.valueOf(rate3));
        txtPitLimit4 = new JTextField(String.valueOf(limit4));
        txtPitRate4 = new JTextField(String.valueOf(rate4));
        txtPitRate5 = new JTextField(String.valueOf(rate5));

        JTextField[] limitFields = { txtPitLimit1, txtPitLimit2, txtPitLimit3, txtPitLimit4, null };
        JTextField[] rateFields = { txtPitRate1, txtPitRate2, txtPitRate3, txtPitRate4, txtPitRate5 };

        for (int i = 0; i < 5; i++) {
            final int idx = i + 1;
            JPanel row = new JPanel(new BorderLayout(14, 0));
            row.setOpaque(false);

            // Circular badge indicator
            JLabel lblBadge = new JLabel(String.valueOf(idx), SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(239, 246, 255)); // Soft blue
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            lblBadge.setPreferredSize(new Dimension(30, 30));
            lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblBadge.setForeground(new Color(59, 130, 246));
            
            JPanel pnlBadgeContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
            pnlBadgeContainer.setOpaque(false);
            pnlBadgeContainer.add(lblBadge);
            row.add(pnlBadgeContainer, BorderLayout.WEST);

            // Inputs columns
            JPanel pnlInputs = new JPanel(new GridLayout(1, 2, 16, 0));
            pnlInputs.setOpaque(false);

            // Column 1: NGƯỠNG TRÊN (VND)
            JPanel pnlCol1 = new JPanel(new BorderLayout(0, 4));
            pnlCol1.setOpaque(false);
            JLabel lblCol1 = new JLabel("NGƯỠNG TRÊN (VND)");
            lblCol1.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblCol1.setForeground(new Color(148, 163, 184)); // Slate 400
            pnlCol1.add(lblCol1, BorderLayout.NORTH);

            JTextField tfLimit;
            if (idx == 5) {
                tfLimit = new JTextField("Vô cực");
                tfLimit.setEditable(false);
                tfLimit.setBackground(new Color(248, 250, 252));
                tfLimit.setForeground(new Color(100, 116, 139));
                tfLimit.putClientProperty("FlatLaf.style", "arc: 8;");
            } else {
                tfLimit = limitFields[i];
                tfLimit.setFont(fieldFont);
                tfLimit.putClientProperty("FlatLaf.style", "arc: 8; focusColor: #3b82f6");
            }
            tfLimit.setPreferredSize(new Dimension(0, 34));
            pnlCol1.add(tfLimit, BorderLayout.CENTER);
            pnlInputs.add(pnlCol1);

            // Column 2: THUẾ SUẤT (%)
            JPanel pnlCol2 = new JPanel(new BorderLayout(0, 4));
            pnlCol2.setOpaque(false);
            JLabel lblCol2 = new JLabel("THUẾ SUẤT (%)");
            lblCol2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblCol2.setForeground(new Color(148, 163, 184));
            pnlCol2.add(lblCol2, BorderLayout.NORTH);

            JTextField tfRate = rateFields[i];
            tfRate.setFont(fieldFont);
            tfRate.setPreferredSize(new Dimension(0, 34));
            tfRate.putClientProperty("FlatLaf.style", "arc: 8; focusColor: #3b82f6");
            pnlCol2.add(tfRate, BorderLayout.CENTER);
            pnlInputs.add(pnlCol2);

            row.add(pnlInputs, BorderLayout.CENTER);
            pnlRightBody.add(row);
        }
        pnlRightCard.add(pnlRightBody, BorderLayout.CENTER);

        // Footer Action Button
        JPanel pnlRightFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlRightFooter.setOpaque(false);

        JButton btnSaveConfig = createStyledButton("Lưu cấu hình hệ thống", new Color(37, 99, 235), Color.WHITE);
        btnSaveConfig.setIcon(new SaveIcon(14, Color.WHITE));
        btnSaveConfig.setIconTextGap(8);
        btnSaveConfig.setPreferredSize(new Dimension(210, 40));
        btnSaveConfig.addActionListener(e -> saveSystemConfigurations());
        btnSaveConfig.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseAdapter e) {
                btnSaveConfig.setBackground(new Color(29, 78, 216));
            }
            public void mouseExited(java.awt.event.MouseAdapter e) {
                btnSaveConfig.setBackground(new Color(37, 99, 235));
            }
        });
        pnlRightFooter.add(btnSaveConfig);
        pnlRightCard.add(pnlRightFooter, BorderLayout.SOUTH);

        grid.add(pnlRightCard);
        pnl.add(grid, BorderLayout.CENTER);

        return pnl;
    }

    private void calculateTotalInsurance() {
        if (txtBhxhRate == null || txtBhytRate == null || txtBhtnRate == null || lblTotalVal == null) return;
        try {
            double bhxh = Double.parseDouble(txtBhxhRate.getText().trim().replace(",", "."));
            double bhyt = Double.parseDouble(txtBhytRate.getText().trim().replace(",", "."));
            double bhtn = Double.parseDouble(txtBhtnRate.getText().trim().replace(",", "."));
            double total = bhxh + bhyt + bhtn;
            lblTotalVal.setText(String.format("%.2f %%", total));
        } catch (Exception ex) {
            lblTotalVal.setText("— %");
        }
    }

    private void saveSystemConfigurations() {
        try {
            // 1. Get and validate insurance fields
            String bhxhStr = txtBhxhRate.getText().trim().replace(",", ".");
            String bhytStr = txtBhytRate.getText().trim().replace(",", ".");
            String bhtnStr = txtBhtnRate.getText().trim().replace(",", ".");
            String tranBHStr = txtTranBaoHiem.getText().trim();

            double bhxh = Double.parseDouble(bhxhStr);
            double bhyt = Double.parseDouble(bhytStr);
            double bhtn = Double.parseDouble(bhtnStr);
            long tranBH = Long.parseLong(tranBHStr);

            if (bhxh < 0 || bhyt < 0 || bhtn < 0 || tranBH < 0) {
                throw new IllegalArgumentException("Hệ số bảo hiểm và mức trần phải là số dương!");
            }

            // 2. Get and validate PIT fields
            long p1 = Long.parseLong(txtPitLimit1.getText().trim());
            double r1 = Double.parseDouble(txtPitRate1.getText().trim().replace(",", "."));
            long p2 = Long.parseLong(txtPitLimit2.getText().trim());
            double r2 = Double.parseDouble(txtPitRate2.getText().trim().replace(",", "."));
            long p3 = Long.parseLong(txtPitLimit3.getText().trim());
            double r3 = Double.parseDouble(txtPitRate3.getText().trim().replace(",", "."));
            long p4 = Long.parseLong(txtPitLimit4.getText().trim());
            double r4 = Double.parseDouble(txtPitRate4.getText().trim().replace(",", "."));
            double r5 = Double.parseDouble(txtPitRate5.getText().trim().replace(",", "."));

            if (p1 < 0 || r1 < 0 || p2 < 0 || r2 < 0 || p3 < 0 || r3 < 0 || p4 < 0 || r4 < 0 || r5 < 0) {
                throw new IllegalArgumentException("Ngưỡng thuế và thuế suất phải là số dương!");
            }

            if (p1 >= p2 || p2 >= p3 || p3 >= p4) {
                throw new IllegalArgumentException("Các ngưỡng thuế suất lũy tiến phải sắp xếp tăng dần!");
            }

            // 3. Save to Database
            payrollDAO.updateConfig("BHXH_RATE", bhxhStr);
            payrollDAO.updateConfig("BHYT_RATE", bhytStr);
            payrollDAO.updateConfig("BHTN_RATE", bhtnStr);
            payrollDAO.updateConfig("TRAN_BAO_HIEM", tranBHStr);

            payrollDAO.updateConfig("PIT_LIMIT_1", String.valueOf(p1));
            payrollDAO.updateConfig("PIT_RATE_1", String.valueOf(r1));
            payrollDAO.updateConfig("PIT_LIMIT_2", String.valueOf(p2));
            payrollDAO.updateConfig("PIT_RATE_2", String.valueOf(r2));
            payrollDAO.updateConfig("PIT_LIMIT_3", String.valueOf(p3));
            payrollDAO.updateConfig("PIT_RATE_3", String.valueOf(r3));
            payrollDAO.updateConfig("PIT_LIMIT_4", String.valueOf(p4));
            payrollDAO.updateConfig("PIT_RATE_4", String.valueOf(r4));
            payrollDAO.updateConfig("PIT_RATE_5", String.valueOf(r5));

            JOptionPane.showMessageDialog(this, "Lưu cấu hình hệ thống thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập định dạng số hợp lệ cho tất cả các trường!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException iae) {
            JOptionPane.showMessageDialog(this, iae.getMessage(), "Lỗi nghiệp vụ", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi bất ngờ xảy ra khi lưu cấu hình. Vui lòng kiểm tra lại!", "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─── DATA LOAD CONTROLLER METHODS ────────────────────────────────────
    public void loadPayrollData() {
        if (cbMonth == null || cbYear == null) return;
        modelPayroll.setRowCount(0);
        
        // Construct MM/YYYY
        int monthIdx = cbMonth.getSelectedIndex() + 1;
        String yearStr = cbYear.getSelectedItem().toString();
        String thangNam = String.format("%02d/%s", monthIdx, yearStr);
        
        listPayroll = payrollDAO.getPayrollRecords(thangNam);
        DecimalFormat df = new DecimalFormat("###,###,###");

        for (PhieuLuongModel pl : listPayroll) {
            modelPayroll.addRow(new Object[]{
                    pl.getHoTen(),
                    pl.getLuong(),          // Gross
                    pl.getTongBaoHiemNv(),   // Insurance
                    pl.getTongThueTncn(),    // Tax
                    pl.getThucLinh(),        // Net
                    pl.getTrangThai(),       // Pill status
                    ""                       // Action button row
            });
        }
    }

    public void loadProfilesData() {
        modelProfiles.setRowCount(0);
        listProfiles = payrollDAO.getSalaryProfiles();
        for (HoSoLuongModel p : listProfiles) {
            modelProfiles.addRow(new Object[]{
                    p.getHoTen(),
                    p.getMucLuong(),
                    "Đang làm việc",
                    ""
            });
        }
    }

    private void calculatePayroll() {
        int monthIdx = cbMonth.getSelectedIndex() + 1;
        String yearStr = cbYear.getSelectedItem().toString();
        String thangNam = String.format("%02d/%s", monthIdx, yearStr);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Hệ thống sẽ tổng hợp toàn bộ ngày công và tính lương cho toàn bộ nhân viên trong tháng " + cbMonth.getSelectedItem() + " năm " + yearStr + ". Xác nhận tính?",
                "Tính lương tháng", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean success = payrollDAO.calculatePayroll(thangNam);
        if (success) {
            JOptionPane.showMessageDialog(this, "Đã tính toán bảng lương tháng " + cbMonth.getSelectedItem() + " thành công!");
            loadPayrollData();
        } else {
            JOptionPane.showMessageDialog(this, "Gặp lỗi khi tính lương. Vui lòng đảm bảo các thông tin ngày công đầy đủ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void approveAllPayroll() {
        int monthIdx = cbMonth.getSelectedIndex() + 1;
        String yearStr = cbYear.getSelectedItem().toString();
        String thangNam = String.format("%02d/%s", monthIdx, yearStr);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn muốn duyệt TOÀN BỘ phiếu lương còn đang ở trạng thái 'Chờ duyệt' của tháng " + cbMonth.getSelectedItem() + " năm " + yearStr + " không?",
                "Duyệt tất cả", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean success = payrollDAO.approveAllPayroll(thangNam);
        if (success) {
            JOptionPane.showMessageDialog(this, "Đã duyệt toàn bộ bảng lương tháng " + cbMonth.getSelectedItem() + "!");
            loadPayrollData();
        } else {
            JOptionPane.showMessageDialog(this, "Không có phiếu lương nào chờ duyệt hoặc có lỗi hệ thống!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updatePayrollStatus(String action) {
        int selected = tablePayroll.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu lương trong danh sách!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = tablePayroll.convertRowIndexToModel(selected);
        PhieuLuongModel pl = listPayroll.get(modelRow);

        if ("Đã thanh toán".equals(pl.getTrangThai())) {
            JOptionPane.showMessageDialog(this, "Phiếu lương này đã được thanh toán hoàn tất!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("Đã duyệt".equals(action) && !"Chờ duyệt".equals(pl.getTrangThai())) {
            JOptionPane.showMessageDialog(this, "Chỉ có thể duyệt phiếu lương đang ở trạng thái 'Chờ duyệt'!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("Đã thanh toán".equals(action) && !"Đã duyệt".equals(pl.getTrangThai())) {
            JOptionPane.showMessageDialog(this, "Chỉ có thể thanh toán phiếu lương đã được 'Đã duyệt' bởi quản lý!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn muốn xác nhận " + action.toLowerCase() + " phiếu lương của nhân viên " + pl.getHoTen() + " không?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean success = payrollDAO.updatePayrollStatus(pl.getMaPhieu(), action);
        if (success) {
            JOptionPane.showMessageDialog(this, "Thao tác thành công!");
            loadPayrollData();
        } else {
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra trong quá trình cập nhật trạng thái!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void configureSalaryProfile() {
        int selected = tableProfiles.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhân viên từ danh sách cấu hình hồ sơ!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = tableProfiles.convertRowIndexToModel(selected);
        HoSoLuongModel profile = listProfiles.get(modelRow);

        SalaryProfileDialog dlg = new SalaryProfileDialog((Frame) SwingUtilities.getWindowAncestor(this), profile);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadProfilesData();
        }
    }

    // ─── DETAIL POP-UP DIALOG ────────────────────────────────────────────
    private void showPayrollDetailDialog(PhieuLuongModel pl) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = (owner instanceof Frame)
                ? new JDialog((Frame) owner, "Chi tiết bảng lương nhân viên", true)
                : new JDialog((java.awt.Dialog) owner, "Chi tiết bảng lương nhân viên", true);
        dlg.setSize(500, 520);
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        // Header Sheet-style border line
        JPanel pnlSheetHeader = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new java.awt.GradientPaint(0, 0, COLOR_PRIMARY, 0, getHeight(), new Color(79, 65, 180)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        pnlSheetHeader.setLayout(new BorderLayout());
        pnlSheetHeader.setBorder(new EmptyBorder(16, 24, 16, 24));
        
        JLabel lblSheetTitle = new JLabel("PHIẾU LƯƠNG NHÂN VIÊN");
        lblSheetTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblSheetTitle.setForeground(Color.WHITE);
        
        JLabel lblSheetSub = new JLabel("Phân tích chi tiết tổng hợp thu nhập & Khấu trừ thuế tháng " + pl.getThangNam());
        lblSheetSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSheetSub.setForeground(new Color(210, 200, 255));
        
        JPanel pnlHeaderTexts = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlHeaderTexts.setOpaque(false);
        pnlHeaderTexts.add(lblSheetTitle);
        pnlHeaderTexts.add(lblSheetSub);
        
        pnlSheetHeader.add(pnlHeaderTexts, BorderLayout.CENTER);
        dlg.add(pnlSheetHeader, BorderLayout.NORTH);

        // Body Paper inside
        JPanel pnlBody = new JPanel();
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setBorder(new EmptyBorder(25, 30, 25, 30));

        DecimalFormat df = new DecimalFormat("###,###,###đ");

        pnlBody.add(buildDetailRow("HỌ TÊN NHÂN VIÊN:", pl.getHoTen(), true, COLOR_TEXT_PRI));
        pnlBody.add(buildDetailRow("THÁNG TÍNH LƯƠNG:", pl.getThangNam(), false, COLOR_TEXT_SEC));
        pnlBody.add(Box.createVerticalStrut(10));
        pnlBody.add(createDivider());
        pnlBody.add(Box.createVerticalStrut(10));

        pnlBody.add(buildDetailRow("💵 LƯƠNG GỘP (GROSS):", df.format(pl.getLuong()), true, COLOR_TEXT_PRI));
        pnlBody.add(buildDetailRow("🏥 TRỪ BẢO HIỂM BẮT BUỘC:", "-" + df.format(pl.getTongBaoHiemNv()), false, COLOR_DANGER_FG));
        pnlBody.add(buildDetailRow("💸 THUẾ THU NHẬP CÁ NHÂN (PIT):", "-" + df.format(pl.getTongThueTncn()), false, COLOR_DANGER_FG));
        
        pnlBody.add(Box.createVerticalStrut(10));
        pnlBody.add(createDivider());
        pnlBody.add(Box.createVerticalStrut(10));

        pnlBody.add(buildDetailRow("💰 THỰC LĨNH NHẬN ĐƯỢC (NET):", df.format(pl.getThucLinh()), true, COLOR_SUCCESS_FG));
        pnlBody.add(buildDetailRow("🏷 TRẠNG THÁI THANH TOÁN:", pl.getTrangThai(), true, COLOR_ORANGE_FG));

        dlg.add(pnlBody, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        footer.setBackground(new Color(248, 250, 252));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));

        JButton btnClose = createStyledButton("Đóng lại", COLOR_PRIMARY, Color.WHITE);
        btnClose.setPreferredSize(new Dimension(100, 36));
        btnClose.addActionListener(e -> dlg.dispose());
        footer.add(btnClose);
        dlg.add(footer, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private JPanel buildDetailRow(String label, String value, boolean isBold, Color valColor) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(6, 0, 6, 0));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(COLOR_TEXT_SEC);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", isBold ? Font.BOLD : Font.PLAIN, 14));
        val.setForeground(valColor);

        pnl.add(lbl, BorderLayout.WEST);
        pnl.add(val, BorderLayout.EAST);
        return pnl;
    }

    private JPanel createDivider() {
        JPanel div = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(COLOR_BORDER);
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        div.setPreferredSize(new Dimension(0, 1));
        div.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return div;
    }

    // ─── TAB SELECTION HELPERS ───────────────────────────────────────────
    private JButton createTabButton(String text, Icon icon) {
        JButton btn = new JButton(text, icon);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(COLOR_TEXT_SEC);
        btn.setBackground(COLOR_BG);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setIconTextGap(8);
        
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(0, 0, 0, 0)),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        return btn;
    }

    private void updateTabs(JButton activeBtn) {
        JButton[] buttons = {btnTabPayroll, btnTabProfiles, btnTabTaxes};
        for (JButton btn : buttons) {
            if (btn == activeBtn) {
                btn.setForeground(COLOR_PRIMARY);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 3, 0, COLOR_PRIMARY),
                        BorderFactory.createEmptyBorder(8, 16, 8, 16)
                ));
                if (btn == btnTabPayroll) btn.setIcon(new DollarIcon(16, COLOR_PRIMARY));
                else if (btn == btnTabProfiles) btn.setIcon(new ProfileIcon(16, COLOR_PRIMARY));
                else if (btn == btnTabTaxes) btn.setIcon(new TaxesIcon(16, COLOR_PRIMARY));
            } else {
                btn.setForeground(COLOR_TEXT_SEC);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(0, 0, 0, 0)),
                        BorderFactory.createEmptyBorder(8, 16, 8, 16)
                ));
                if (btn == btnTabPayroll) btn.setIcon(new DollarIcon(16, COLOR_TEXT_SEC));
                else if (btn == btnTabProfiles) btn.setIcon(new ProfileIcon(16, COLOR_TEXT_SEC));
                else if (btn == btnTabTaxes) btn.setIcon(new TaxesIcon(16, COLOR_TEXT_SEC));
            }
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

    // ─── CUSTOM TABLE RENDERERS ──────────────────────────────────────────
    private class EmployeeCellRenderer extends JPanel implements TableCellRenderer {
        private final JLabel lblAvatar = new JLabel("", SwingConstants.CENTER);
        private final JLabel lblName = new JLabel();

        public EmployeeCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.LEFT, 12, 14));
            setOpaque(true);

            lblAvatar.setPreferredSize(new Dimension(34, 34));
            lblAvatar.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblAvatar.setForeground(Color.WHITE);

            lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblName.setForeground(COLOR_TEXT_PRI);

            add(lblAvatar);
            add(lblName);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
            String name = val != null ? val.toString() : "";
            lblName.setText(name);

            String firstLetter = !name.isEmpty() ? name.substring(0, 1).toUpperCase() : "";
            lblAvatar.setText(firstLetter);

            setBackground(isSel ? table.getSelectionBackground() : (r % 2 == 0 ? COLOR_SURFACE : new Color(250, 250, 252)));
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            Rectangle bounds = lblAvatar.getBounds();
            int hash = lblName.getText().hashCode();
            Color avatarBg;
            if (hash % 3 == 0) {
                avatarBg = new Color(249, 115, 22); // Orange
            } else if (hash % 3 == 1) {
                avatarBg = new Color(139, 92, 246); // Purple
            } else {
                avatarBg = new Color(59, 130, 246); // Blue
            }
            
            g2.setColor(avatarBg);
            g2.fillOval(bounds.x, bounds.y, bounds.width, bounds.height);
            g2.dispose();
        }
    }

    private class MoneyCellRenderer extends DefaultTableCellRenderer {
        private final boolean isDeduction;
        private final boolean isBoldNet;
        private final DecimalFormat df = new DecimalFormat("###,###,###đ");

        public MoneyCellRenderer(boolean isDeduction, boolean isBoldNet) {
            this.isDeduction = isDeduction;
            this.isBoldNet = isBoldNet;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
            super.getTableCellRendererComponent(table, val, isSel, hasF, r, c);
            setHorizontalAlignment(SwingConstants.RIGHT);
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));

            if (val instanceof Number) {
                long num = ((Number) val).longValue();
                if (isDeduction) {
                    if (num > 0) {
                        setText("-" + df.format(num));
                        setForeground(COLOR_DANGER_FG);
                    } else {
                        setText("-0đ");
                        setForeground(COLOR_TEXT_SEC);
                    }
                } else {
                    setText(df.format(num));
                    if (isBoldNet) {
                        setFont(new Font("Segoe UI", Font.BOLD, 14));
                        setForeground(COLOR_SUCCESS_FG);
                    } else {
                        setFont(new Font("Segoe UI", Font.PLAIN, 13));
                        setForeground(COLOR_TEXT_PRI);
                    }
                }
            } else if (val != null) {
                // If it is already formatted string
                String valStr = val.toString();
                setText(valStr);
                if (isDeduction) {
                    if (valStr.equals("0đ") || valStr.equals("0") || valStr.equals("-0đ")) {
                        setText("-0đ");
                        setForeground(COLOR_TEXT_SEC);
                    } else {
                        setForeground(COLOR_DANGER_FG);
                    }
                } else if (isBoldNet) {
                    setFont(new Font("Segoe UI", Font.BOLD, 14));
                    setForeground(COLOR_SUCCESS_FG);
                } else {
                    setForeground(COLOR_TEXT_PRI);
                }
            }
            
            if (isSel) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(r % 2 == 0 ? COLOR_SURFACE : new Color(250, 250, 252));
            }
            return this;
        }
    }

    private class BadgeRenderer extends DefaultTableCellRenderer {
        private String badgeText = "";

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            
            setBackground(sel ? t.getSelectionBackground() : (row % 2 == 0 ? COLOR_SURFACE : new Color(250, 250, 252)));
            setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 8));
            badgeText = v != null ? v.toString() : "Chờ duyệt";
            setText("");
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (badgeText.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color pillBgColor;
            Color textColor;

            String lower = badgeText.toLowerCase();
            if (lower.contains("chờ duyệt")) {
                pillBgColor = COLOR_ORANGE_BG;
                textColor = COLOR_ORANGE_FG;
            } else if (lower.contains("đã duyệt")) {
                pillBgColor = COLOR_INFO_BG;
                textColor = COLOR_INFO_FG;
            } else {
                pillBgColor = COLOR_SUCCESS_BG;
                textColor = COLOR_SUCCESS_FG;
            }

            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(badgeText);
            int textHeight = fm.getHeight();

            int padX = 12;
            int padY = 4;
            int pillWidth = textWidth + 2 * padX;
            int pillHeight = textHeight + 2 * padY;

            int x = 16;
            int y = (getHeight() - pillHeight) / 2;

            g2.setColor(pillBgColor);
            g2.fillRoundRect(x, y, pillWidth, pillHeight, 12, 12);

            g2.setColor(textColor);
            g2.drawString(badgeText, x + padX, y + ((pillHeight - textHeight) / 2) + fm.getAscent());
            g2.dispose();
        }
    }

    private class EmployeeSalaryCellRenderer extends JPanel implements TableCellRenderer {
        private final JLabel lblAvatar = new JLabel("", SwingConstants.CENTER);
        private final JLabel lblName = new JLabel();
        private final JLabel lblEmail = new JLabel();

        public EmployeeSalaryCellRenderer() {
            setLayout(new GridBagLayout());
            setOpaque(true);

            lblAvatar.setPreferredSize(new Dimension(36, 36));
            lblAvatar.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblAvatar.setForeground(Color.WHITE);

            lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblName.setForeground(COLOR_TEXT_PRI);

            lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblEmail.setForeground(COLOR_TEXT_SEC);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 12, 4, 12);
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridheight = 2;
            gbc.fill = GridBagConstraints.VERTICAL;
            add(lblAvatar, gbc);

            gbc.gridx = 1; gbc.gridy = 0; gbc.gridheight = 1;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(4, 0, 0, 12);
            add(lblName, gbc);

            gbc.gridy = 1;
            gbc.insets = new Insets(0, 0, 4, 12);
            add(lblEmail, gbc);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasF, int r, int c) {
            if (r < 0 || r >= listProfiles.size()) {
                lblName.setText(val != null ? val.toString() : "");
                lblEmail.setText("");
                lblAvatar.setText("");
                return this;
            }
            HoSoLuongModel p = listProfiles.get(r);
            String name = p.getHoTen() != null ? p.getHoTen() : "";
            String email = p.getEmail() != null ? p.getEmail() : "";

            lblName.setText(name);
            lblEmail.setText(email.isEmpty() ? "—" : email);

            String firstLetter = !name.isEmpty() ? name.substring(0, 1).toUpperCase() : "";
            lblAvatar.setText(firstLetter);

            setBackground(isSel ? table.getSelectionBackground() : (r % 2 == 0 ? COLOR_SURFACE : new Color(250, 250, 252)));
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            Rectangle bounds = lblAvatar.getBounds();
            int hash = lblName.getText().hashCode();
            Color avatarBg;
            if (hash % 3 == 0) {
                avatarBg = new Color(249, 115, 22); // Orange
            } else if (hash % 3 == 1) {
                avatarBg = new Color(139, 92, 246); // Purple
            } else {
                avatarBg = new Color(59, 130, 246); // Blue
            }
            
            g2.setColor(avatarBg);
            g2.fillOval(bounds.x, bounds.y, bounds.width, bounds.height);
            g2.dispose();
        }
    }

    public static class DollarIcon implements Icon {
        private final int size;
        private final Color color;
        public DollarIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setFont(new Font("Segoe UI", Font.BOLD, (int)(size * 0.95)));
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (size - fm.stringWidth("$")) / 2;
            int ty = y + fm.getAscent() + (size - fm.getHeight()) / 2;
            g2.drawString("$", tx, ty);
            g2.dispose();
        }
        @Override
        public int getIconWidth() { return size; }
        @Override
        public int getIconHeight() { return size; }
    }

    public static class ProfileIcon implements Icon {
        private final int size;
        private final Color color;
        public ProfileIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            int pad = size / 6;
            int w = size - 2 * pad;
            int h = size - 2 * pad;
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(x + pad, y + pad, w, h, 4, 4);
            int headRadius = w / 4;
            int hcx = x + size / 2;
            int hcy = y + pad + h / 3;
            g2.fillOval(hcx - headRadius, hcy - headRadius, headRadius * 2, headRadius * 2);
            int shW = w * 7 / 10;
            int shH = h / 3;
            int shX = hcx - shW / 2;
            int shY = y + pad + h * 6 / 10;
            g2.fillArc(shX, shY, shW, shH * 2, 0, 180);
            g2.dispose();
        }
        @Override
        public int getIconWidth() { return size; }
        @Override
        public int getIconHeight() { return size; }
    }

    public static class TaxesIcon implements Icon {
        private final int size;
        private final Color color;
        public TaxesIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setFont(new Font("Segoe UI", Font.BOLD, (int)(size * 0.9)));
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (size - fm.stringWidth("%")) / 2;
            int ty = y + fm.getAscent() + (size - fm.getHeight()) / 2;
            g2.drawString("%", tx, ty);
            g2.dispose();
        }
        @Override
        public int getIconWidth() { return size; }
        @Override
        public int getIconHeight() { return size; }
    }

    public static class GearIcon implements Icon {
        private final int size;
        private final Color color;
        public GearIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            int cx = x + size / 2;
            int cy = y + size / 2;
            int rOuter = size * 4 / 10;
            int rInner = size * 2 / 10;
            int rHole = size * 1 / 10;
            int numTeeth = 8;
            int toothWidth = size * 15 / 100;
            int toothHeight = size * 20 / 100;
            for (int i = 0; i < numTeeth; i++) {
                g2.rotate(Math.PI * 2 / numTeeth, cx, cy);
                g2.fillRect(cx - toothWidth / 2, cy - rOuter - toothHeight / 2, toothWidth, toothHeight);
            }
            g2.fillOval(cx - rOuter, cy - rOuter, rOuter * 2, rOuter * 2);
            g2.setComposite(AlphaComposite.Clear);
            g2.fillOval(cx - rHole, cy - rHole, rHole * 2, rHole * 2);
            g2.dispose();
        }
        @Override
        public int getIconWidth() { return size; }
        @Override
        public int getIconHeight() { return size; }
    }

    public static class ShieldIcon implements Icon {
        private final int size;
        private final Color color;
        public ShieldIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            int pad = size / 6;
            int w = size - 2 * pad;
            int h = size - 2 * pad;
            java.awt.geom.Path2D path = new java.awt.geom.Path2D.Double();
            path.moveTo(x + size / 2.0, y + pad);
            path.lineTo(x + pad + w, y + pad + h / 4.0);
            path.quadTo(x + pad + w, y + pad + h * 0.7, x + size / 2.0, y + pad + h);
            path.quadTo(x + pad, y + pad + h * 0.7, x + pad, y + pad + h / 4.0);
            path.closePath();
            g2.fill(path);
            g2.dispose();
        }
        @Override
        public int getIconWidth() { return size; }
        @Override
        public int getIconHeight() { return size; }
    }

    public static class ScaleIcon implements Icon {
        private final int size;
        private final Color color;
        public ScaleIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            int pad = size / 6;
            int w = size - 2 * pad;
            int h = size - 2 * pad;
            g2.setStroke(new BasicStroke(1.8f));
            int cx = x + size / 2;
            g2.drawLine(cx, y + pad, cx, y + pad + h - 2);
            g2.drawLine(cx - w/4, y + pad + h - 2, cx + w/4, y + pad + h - 2);
            g2.drawLine(cx - w/2, y + pad + h/4, cx + w/2, y + pad + h/4);
            g2.drawLine(cx - w/2, y + pad + h/4, cx - w/2 - 2, y + pad + h*6/10);
            g2.drawLine(cx - w/2, y + pad + h/4, cx - w/2 + 2, y + pad + h*6/10);
            g2.drawArc(cx - w/2 - w/6, y + pad + h*6/10, w/3, h/6, 0, -180);
            g2.drawLine(cx + w/2, y + pad + h/4, cx + w/2 - 2, y + pad + h*6/10);
            g2.drawLine(cx + w/2, y + pad + h/4, cx + w/2 + 2, y + pad + h*6/10);
            g2.drawArc(cx + w/2 - w/6, y + pad + h*6/10, w/3, h/6, 0, -180);
            g2.dispose();
        }
        @Override
        public int getIconWidth() { return size; }
        @Override
        public int getIconHeight() { return size; }
    }

    public static class SaveIcon implements Icon {
        private final int size;
        private final Color color;
        public SaveIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            int pad = size / 6;
            int w = size - 2 * pad;
            int h = size - 2 * pad;
            g2.fillRect(x + pad, y + pad, w, h);
            g2.setColor(c.getBackground());
            g2.setColor(new Color(255, 255, 255, 180));
            g2.fillRect(x + pad + w/4, y + pad, w/2, h/3);
            g2.fillRect(x + pad + w/5, y + pad + h/2, w*3/5, h/2);
            g2.setColor(color);
            g2.fillRect(x + pad + w/3, y + pad + h*6/10, w/5, h*3/10);
            g2.dispose();
        }
        @Override
        public int getIconWidth() { return size; }
        @Override
        public int getIconHeight() { return size; }
    }
}
