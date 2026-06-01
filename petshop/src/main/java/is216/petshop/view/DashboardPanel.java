package is216.petshop.view;

import is216.petshop.dao.DashboardDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

public class DashboardPanel extends JPanel {
    private DashboardDAO dashboardDAO;

    private JLabel lblRevenue;
    private JLabel lblOrders;
    private JLabel lblLowStock;
    private JLabel lblCustomers;

    private JPanel panelTopProducts;
    private JPanel panelTopServices;

    // Styling constants
    private static final Color COLOR_SURFACE = Color.WHITE;
    private static final Color COLOR_BACKGROUND = new Color(244, 246, 249);
    private static final Color COLOR_BORDER = new Color(226, 232, 240);
    private static final Color COLOR_TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color COLOR_TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color COLOR_SELECTION = new Color(219, 234, 254);
    private static final Color COLOR_ALT_ROW = new Color(249, 250, 251);

    private static final Color COLOR_PRIMARY_GRAD_START = new Color(108, 93, 211);
    private static final Color COLOR_PRIMARY_GRAD_END = new Color(160, 140, 240);
    
    private static final Color COLOR_CARD_REVENUE = new Color(224, 231, 255); // Indigo
    private static final Color COLOR_CARD_ORDERS = new Color(254, 243, 199);  // Amber
    private static final Color COLOR_CARD_LOW = new Color(254, 226, 226);     // Rose
    private static final Color COLOR_CARD_CUST = new Color(220, 252, 231);    // Emerald

    public DashboardPanel() {
        this.dashboardDAO = new DashboardDAO();
        initComponents();
        loadData();
    }

    private void initComponents() {
        this.setBackground(COLOR_BACKGROUND);
        this.setLayout(new BorderLayout(20, 20));
        this.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // --- TOP PANEL: Welcome Header ---
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        topPanel.setBackground(COLOR_BACKGROUND);
        
        JLabel lblTitle = new JLabel("Tổng quan cửa hàng");
        lblTitle.setIcon(new VectorIcon(VectorIcon.Type.PAW, 26, COLOR_PRIMARY_GRAD_START));
        lblTitle.setIconTextGap(10);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(COLOR_TEXT_PRIMARY);

        JLabel lblSubtitle = new JLabel("Báo cáo số liệu kinh doanh, hàng tồn kho và các giao dịch gần đây");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(COLOR_TEXT_SECONDARY);

        topPanel.add(lblTitle);
        topPanel.add(lblSubtitle);

        // --- CENTRAL PANEL: Scrollable content container ---
        JPanel centralScrollContainer = new JPanel(new GridBagLayout());
        centralScrollContainer.setBackground(COLOR_BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 15, 0);

        // 1. KPI Cards Row (1x4)
        gbc.gridx = 0; gbc.gridy = 0; gbc.weighty = 0.12;
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        kpiPanel.setBackground(COLOR_BACKGROUND);
        kpiPanel.setPreferredSize(new Dimension(0, 90));

        lblRevenue = new JLabel("0đ");
        lblOrders = new JLabel("0");
        lblLowStock = new JLabel("0");
        lblCustomers = new JLabel("0");

        kpiPanel.add(createKPICard("TỔNG DOANH THU", lblRevenue, COLOR_CARD_REVENUE, new Color(55, 48, 163), new VectorIcon(VectorIcon.Type.REVENUE, 32, new Color(55, 48, 163))));
        kpiPanel.add(createKPICard("TỔNG ĐƠN HÀNG", lblOrders, COLOR_CARD_ORDERS, new Color(146, 64, 14), new VectorIcon(VectorIcon.Type.ORDERS, 32, new Color(146, 64, 14))));
        kpiPanel.add(createKPICard("SẢN PHẨM SẮP HẾT (< 15)", lblLowStock, COLOR_CARD_LOW, new Color(153, 27, 27), new VectorIcon(VectorIcon.Type.LOW_STOCK, 32, new Color(153, 27, 27))));
        kpiPanel.add(createKPICard("KHÁCH HÀNG ĐĂNG KÝ", lblCustomers, COLOR_CARD_CUST, new Color(21, 128, 61), new VectorIcon(VectorIcon.Type.CUSTOMERS, 32, new Color(21, 128, 61))));
        centralScrollContainer.add(kpiPanel, gbc);

        // 2. Revenue Chart Panel (Optimized compact sizing)
        gbc.gridy = 1; gbc.weighty = 0.50;
        JPanel chartContainer = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(COLOR_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        chartContainer.setOpaque(false);
        chartContainer.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        chartContainer.setPreferredSize(new Dimension(0, 300));

        JLabel lblChartTitle = new JLabel("Biểu đồ doanh thu 6 tháng gần nhất (VND)");
        lblChartTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblChartTitle.setForeground(COLOR_TEXT_PRIMARY);
        chartContainer.add(lblChartTitle, BorderLayout.NORTH);

        RevenueChartComponent chartComp = new RevenueChartComponent();
        chartContainer.add(chartComp, BorderLayout.CENTER);
        centralScrollContainer.add(chartContainer, gbc);

        // 3. Lower Column Split (Top products [Left] & Recent orders [Right])
        gbc.gridy = 2; gbc.weighty = 0.38; gbc.insets = new Insets(0, 0, 0, 0);
        JPanel bottomSplitPanel = new JPanel(new GridBagLayout());
        bottomSplitPanel.setPreferredSize(new Dimension(0, 200));
        bottomSplitPanel.setBackground(COLOR_BACKGROUND);
        GridBagConstraints gbcBottom = new GridBagConstraints();
        gbcBottom.fill = GridBagConstraints.BOTH;
        gbcBottom.weighty = 1.0;

        // Left Column: Top Selling Products
        gbcBottom.gridx = 0; gbcBottom.gridy = 0; gbcBottom.weightx = 0.50; // Equal 50/50 size
        gbcBottom.insets = new Insets(0, 0, 0, 10);
        JPanel topProductsPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(COLOR_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        topProductsPanel.setOpaque(false);
        topProductsPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel headerTopProducts = new JPanel(new BorderLayout());
        headerTopProducts.setOpaque(false);

        JLabel lblTopTitle = new JLabel("Sản phẩm bán chạy nhất");
        lblTopTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTopTitle.setForeground(COLOR_TEXT_PRIMARY);
        headerTopProducts.add(lblTopTitle, BorderLayout.WEST);

        JButton btnExpandProducts = new JButton("Chi tiết ↗");
        btnExpandProducts.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnExpandProducts.setForeground(COLOR_PRIMARY_GRAD_START);
        btnExpandProducts.setContentAreaFilled(false);
        btnExpandProducts.setBorderPainted(false);
        btnExpandProducts.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExpandProducts.setFocusPainted(false);
        btnExpandProducts.addActionListener(e -> showTopProductsDialog());
        headerTopProducts.add(btnExpandProducts, BorderLayout.EAST);

        topProductsPanel.add(headerTopProducts, BorderLayout.NORTH);

        panelTopProducts = new JPanel();
        panelTopProducts.setLayout(new BoxLayout(panelTopProducts, BoxLayout.Y_AXIS));
        panelTopProducts.setBackground(COLOR_SURFACE);
        
        JScrollPane scrollTopProducts = new JScrollPane(panelTopProducts);
        scrollTopProducts.getViewport().setBackground(COLOR_SURFACE);
        scrollTopProducts.setBorder(BorderFactory.createEmptyBorder());
        topProductsPanel.add(scrollTopProducts, BorderLayout.CENTER);
        bottomSplitPanel.add(topProductsPanel, gbcBottom);

        // Right Column: Top Services Panel
        gbcBottom.gridx = 1; gbcBottom.weightx = 0.50; // Equal 50/50 size
        gbcBottom.insets = new Insets(0, 10, 0, 0);
        JPanel topServicesPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(COLOR_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        topServicesPanel.setOpaque(false);
        topServicesPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel headerTopServices = new JPanel(new BorderLayout());
        headerTopServices.setOpaque(false);

        JLabel lblServiceTitle = new JLabel("Dịch vụ sử dụng nhiều");
        lblServiceTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblServiceTitle.setForeground(COLOR_TEXT_PRIMARY);
        headerTopServices.add(lblServiceTitle, BorderLayout.WEST);

        JButton btnExpandServices = new JButton("Chi tiết ↗");
        btnExpandServices.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnExpandServices.setForeground(COLOR_PRIMARY_GRAD_START);
        btnExpandServices.setContentAreaFilled(false);
        btnExpandServices.setBorderPainted(false);
        btnExpandServices.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExpandServices.setFocusPainted(false);
        btnExpandServices.addActionListener(e -> showTopServicesDialog());
        headerTopServices.add(btnExpandServices, BorderLayout.EAST);

        topServicesPanel.add(headerTopServices, BorderLayout.NORTH);

        panelTopServices = new JPanel();
        panelTopServices.setLayout(new BoxLayout(panelTopServices, BoxLayout.Y_AXIS));
        panelTopServices.setBackground(COLOR_SURFACE);

        JScrollPane scrollTopServices = new JScrollPane(panelTopServices);
        scrollTopServices.getViewport().setBackground(COLOR_SURFACE);
        scrollTopServices.setBorder(BorderFactory.createEmptyBorder());
        topServicesPanel.add(scrollTopServices, BorderLayout.CENTER);
        
        bottomSplitPanel.add(topServicesPanel, gbcBottom);
        centralScrollContainer.add(bottomSplitPanel, gbc);

        JScrollPane mainScrollPane = new JScrollPane(centralScrollContainer);
        mainScrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainScrollPane.setBackground(COLOR_BACKGROUND);
        mainScrollPane.getViewport().setBackground(COLOR_BACKGROUND);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smooth scrolling!

        this.add(topPanel, BorderLayout.NORTH);
        this.add(mainScrollPane, BorderLayout.CENTER);
    }

    private JPanel createKPICard(String title, JLabel lblVal, Color bg, Color fgText, Icon icon) {
        JPanel card = new JPanel(new BorderLayout(10, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(COLOR_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        leftPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(COLOR_TEXT_SECONDARY);

        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblVal.setForeground(fgText);

        leftPanel.add(lblTitle);
        leftPanel.add(lblVal);

        card.add(leftPanel, BorderLayout.CENTER);

        if (icon != null) {
            JLabel lblIcon = new JLabel(icon);
            lblIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
            card.add(lblIcon, BorderLayout.EAST);
        }

        return card;
    }

    private void loadData() {
        // Load KPIs
        DashboardDAO.KPIStats stats = dashboardDAO.getKPIStats();
        DecimalFormat df = new DecimalFormat("###,###,###đ");
        lblRevenue.setText(df.format(stats.totalRevenue));
        lblOrders.setText(String.valueOf(stats.totalOrders));
        lblLowStock.setText(String.valueOf(stats.lowStockCount));
        lblCustomers.setText(String.valueOf(stats.totalCustomers));

        // Load Top Products
        panelTopProducts.removeAll();
        java.util.List<DashboardDAO.TopProduct> tops = dashboardDAO.getTopProducts();
        for (int i = 0; i < tops.size(); i++) {
            DashboardDAO.TopProduct p = tops.get(i);
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setBackground(COLOR_SURFACE);
            row.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

            JLabel lblRankName = new JLabel((i + 1) + ". " + p.productName);
            lblRankName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblRankName.setForeground(COLOR_TEXT_PRIMARY);

            JLabel lblBadge = new JLabel(p.totalSold + " đã bán");
            lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblBadge.setForeground(COLOR_PRIMARY_GRAD_START);

            row.add(lblRankName, BorderLayout.CENTER);
            row.add(lblBadge, BorderLayout.EAST);
            panelTopProducts.add(row);
        }

        // Load Top Services
        panelTopServices.removeAll();
        java.util.List<DashboardDAO.TopService> services = dashboardDAO.getTopServices();
        for (int i = 0; i < services.size(); i++) {
            DashboardDAO.TopService s = services.get(i);
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setBackground(COLOR_SURFACE);
            row.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

            JLabel lblRankName = new JLabel((i + 1) + ". " + s.serviceName);
            lblRankName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblRankName.setForeground(COLOR_TEXT_PRIMARY);

            JLabel lblBadge = new JLabel(s.useCount + " lượt dùng");
            lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblBadge.setForeground(COLOR_PRIMARY_GRAD_START);

            row.add(lblRankName, BorderLayout.CENTER);
            row.add(lblBadge, BorderLayout.EAST);
            panelTopServices.add(row);
        }
    }

    // --- Custom painted Graphics2D bar chart component ---
    private class RevenueChartComponent extends JComponent {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            int paddingLeft = 60;
            int paddingBottom = 40;
            int paddingTop = 20;
            int paddingRight = 20;

            int chartWidth = width - paddingLeft - paddingRight;
            int chartHeight = height - paddingTop - paddingBottom;

            // Draw Background Grid Lines & Y-axis labels
            g2.setColor(COLOR_BORDER);
            g2.setStroke(new BasicStroke(1.0f));
            
            // X-axis baseline
            g2.drawLine(paddingLeft, height - paddingBottom, width - paddingRight, height - paddingBottom);

            // Fetch revenue chart data
            Map<String, Double> chartData = dashboardDAO.getMonthlyRevenue();
            java.util.List<String> months = new ArrayList<>(chartData.keySet());
            java.util.List<Double> revenues = new ArrayList<>(chartData.values());

            double maxRevenue = 0;
            for (double rev : revenues) {
                if (rev > maxRevenue) maxRevenue = rev;
            }
            if (maxRevenue == 0) maxRevenue = 1000000;
            // Round maxRevenue up to nice steps
            maxRevenue = Math.ceil(maxRevenue / 10000000.0) * 10000000.0;

            // Draw Y Grid lines (4 steps)
            int steps = 4;
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();
            for (int i = 0; i <= steps; i++) {
                double val = (maxRevenue / steps) * i;
                int y = height - paddingBottom - (int) ((chartHeight * val) / maxRevenue);
                
                if (i > 0) {
                    g2.setColor(new Color(240, 242, 245));
                    g2.drawLine(paddingLeft, y, width - paddingRight, y);
                }
                
                g2.setColor(COLOR_TEXT_SECONDARY);
                String labelVal = new DecimalFormat("###,###k").format(val / 1000.0);
                if (val == 0) labelVal = "0đ";
                g2.drawString(labelVal, paddingLeft - fm.stringWidth(labelVal) - 10, y + fm.getAscent() / 2);
            }

            // Draw Line Chart & X-axis labels
            if (months.isEmpty()) {
                g2.dispose();
                return;
            }

            int numPoints = months.size();
            // Calculate gap between horizontal data points
            int gap = chartWidth / (numPoints > 1 ? (numPoints - 1) : 1);
            
            int[] xPoints = new int[numPoints];
            int[] yPoints = new int[numPoints];

            for (int i = 0; i < numPoints; i++) {
                double rev = revenues.get(i);
                int pointHeight = (int) ((chartHeight * rev) / maxRevenue);
                xPoints[i] = paddingLeft + i * gap;
                if (numPoints == 1) {
                    xPoints[i] = paddingLeft + chartWidth / 2;
                }
                yPoints[i] = height - paddingBottom - pointHeight;
            }

            // 1. Draw smooth gradient fill under the line (Area Chart effect)
            if (numPoints > 1) {
                Path2D areaPath = new Path2D.Float();
                areaPath.moveTo(xPoints[0], height - paddingBottom);
                for (int i = 0; i < numPoints; i++) {
                    areaPath.lineTo(xPoints[i], yPoints[i]);
                }
                areaPath.lineTo(xPoints[numPoints - 1], height - paddingBottom);
                areaPath.closePath();

                Color startGradColor = new Color(COLOR_PRIMARY_GRAD_START.getRed(), COLOR_PRIMARY_GRAD_START.getGreen(), COLOR_PRIMARY_GRAD_START.getBlue(), 70); 
                Color endGradColor = new Color(COLOR_PRIMARY_GRAD_END.getRed(), COLOR_PRIMARY_GRAD_END.getGreen(), COLOR_PRIMARY_GRAD_END.getBlue(), 0); 
                GradientPaint areaGrad = new GradientPaint(0, paddingTop, startGradColor, 0, height - paddingBottom, endGradColor);
                g2.setPaint(areaGrad);
                g2.fill(areaPath);
            }

            // 2. Draw the main connecting line with a thick premium stroke
            g2.setColor(COLOR_PRIMARY_GRAD_START);
            g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < numPoints - 1; i++) {
                g2.drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
            }

            // 3. Draw beautiful dots and labels at each data point
            for (int i = 0; i < numPoints; i++) {
                int x = xPoints[i];
                int y = yPoints[i];
                double rev = revenues.get(i);
                String monthLabel = months.get(i);

                // Draw solid outer white dot with primary colored core
                g2.setColor(Color.WHITE);
                g2.fillOval(x - 6, y - 6, 12, 12);
                g2.setColor(COLOR_PRIMARY_GRAD_START);
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawOval(x - 6, y - 6, 12, 12);
                g2.fillOval(x - 3, y - 3, 6, 6);

                // Draw amount above the dot
                g2.setColor(COLOR_TEXT_PRIMARY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                String revText = new DecimalFormat("#,###đ").format(rev);
                int revTextWidth = g2.getFontMetrics().stringWidth(revText);
                g2.drawString(revText, x - revTextWidth / 2, y - 10);

                // Draw X label (month name) below the baseline
                g2.setColor(COLOR_TEXT_SECONDARY);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                int labelWidth = g2.getFontMetrics().stringWidth(monthLabel);
                g2.drawString(monthLabel, x - labelWidth / 2, height - paddingBottom + fm.getAscent() + 8);
            }

            g2.dispose();
        }
    }

    private class BadgeRenderer extends DefaultTableCellRenderer {
        private String badgeText = "";

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            if (!sel)
                setBackground(row % 2 == 0 ? COLOR_SURFACE : COLOR_ALT_ROW);
            else
                setBackground(COLOR_SELECTION);

            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 8));
            badgeText = v != null ? v.toString() : "";
            setText("");
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (badgeText.isEmpty())
                return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color pillBgColor;
            Color textColor;

            String lower = badgeText.toLowerCase();
            if (lower.contains("chờ thanh toán")) {
                pillBgColor = new Color(254, 243, 199); // Vàng
                textColor = new Color(180, 83, 9);
            } else if (lower.contains("đã thanh toán") || lower.contains("hoàn thành") || lower.contains("daxacnhan")) {
                pillBgColor = new Color(220, 252, 231); // Xanh lá
                textColor = new Color(22, 163, 74);
            } else {
                pillBgColor = new Color(254, 226, 226); // Đỏ
                textColor = new Color(220, 38, 38);
            }

            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(badgeText);
            int textHeight = fm.getHeight();

            int padX = 12;
            int padY = 4;
            int pillWidth = textWidth + 2 * padX;
            int pillHeight = textHeight + 2 * padY;

            int x = 12;
            int y = (getHeight() - pillHeight) / 2;

            g2.setColor(pillBgColor);
            g2.fillRoundRect(x, y, pillWidth, pillHeight, pillHeight, pillHeight);

            g2.setColor(textColor);
            g2.drawString(badgeText, x + padX, y + ((pillHeight - textHeight) / 2) + fm.getAscent());

            g2.dispose();
        }
    }

    private void showTopProductsDialog() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "Chi tiết sản phẩm bán chạy nhất", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(550, 400);
        dialog.setLocationRelativeTo(parentWindow);
        dialog.setLayout(new BorderLayout());
        
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        contentPanel.setBackground(COLOR_SURFACE);
        
        JLabel lblTitle = new JLabel("Danh sách sản phẩm bán chạy nhất");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(COLOR_TEXT_PRIMARY);
        contentPanel.add(lblTitle, BorderLayout.NORTH);
        
        String[] cols = { "STT", "Tên Sản Phẩm", "Số Lượng Đã Bán" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        JTable table = new JTable(model);
        table.setRowHeight(36);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(COLOR_BORDER);
        table.setSelectionBackground(COLOR_SELECTION);
        table.setFocusable(false);
        
        // Renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        
        // Load data
        java.util.List<DashboardDAO.TopProduct> tops = dashboardDAO.getTopProducts();
        for (int i = 0; i < tops.size(); i++) {
            DashboardDAO.TopProduct p = tops.get(i);
            model.addRow(new Object[]{ (i + 1), p.productName, p.totalSold });
        }
        
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(COLOR_SURFACE);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        contentPanel.add(scroll, BorderLayout.CENTER);
        
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void showTopServicesDialog() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "Chi tiết dịch vụ sử dụng nhiều", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(550, 400);
        dialog.setLocationRelativeTo(parentWindow);
        dialog.setLayout(new BorderLayout());
        
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        contentPanel.setBackground(COLOR_SURFACE);
        
        JLabel lblTitle = new JLabel("Danh sách dịch vụ sử dụng nhiều nhất");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(COLOR_TEXT_PRIMARY);
        contentPanel.add(lblTitle, BorderLayout.NORTH);
        
        String[] cols = { "STT", "Tên Dịch Vụ", "Lượt Sử Dụng" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        JTable table = new JTable(model);
        table.setRowHeight(36);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(COLOR_BORDER);
        table.setSelectionBackground(COLOR_SELECTION);
        table.setFocusable(false);
        
        // Renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        
        // Load data
        java.util.List<DashboardDAO.TopService> services = dashboardDAO.getTopServices();
        for (int i = 0; i < services.size(); i++) {
            DashboardDAO.TopService s = services.get(i);
            model.addRow(new Object[]{ (i + 1), s.serviceName, s.useCount });
        }
        
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(COLOR_SURFACE);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        contentPanel.add(scroll, BorderLayout.CENTER);
        
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    public static class VectorIcon implements Icon {
        public enum Type { PAW, REVENUE, ORDERS, LOW_STOCK, CUSTOMERS }
        private final Type type;
        private final int size;
        private final Color color;

        public VectorIcon(Type type, int size, Color color) {
            this.type = type;
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.translate(x, y);

            double scale = size / 100.0;
            g2.scale(scale, scale);

            switch (type) {
                case PAW:
                    drawPaw(g2);
                    break;
                case REVENUE:
                    drawRevenue(g2);
                    break;
                case ORDERS:
                    drawOrders(g2);
                    break;
                case LOW_STOCK:
                    drawLowStock(g2);
                    break;
                case CUSTOMERS:
                    drawCustomers(g2);
                    break;
            }

            g2.dispose();
        }

        @Override
        public int getIconWidth() { return size; }
        @Override
        public int getIconHeight() { return size; }

        private void drawPaw(Graphics2D g2) {
            // Main pad (rounded bottom part of the paw)
            g2.fillRoundRect(22, 45, 56, 40, 36, 26);
            // 4 toes
            g2.fillOval(12, 28, 16, 24);
            g2.fillOval(31, 12, 17, 27);
            g2.fillOval(52, 12, 17, 27);
            g2.fillOval(72, 28, 16, 24);
        }

        private void drawRevenue(Graphics2D g2) {
            g2.setStroke(new BasicStroke(7.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Coin circle
            g2.drawOval(10, 10, 80, 80);
            // Vertical dollar lines
            g2.drawLine(50, 20, 50, 80);
            // S curves for dollar sign
            Path2D path = new Path2D.Float();
            path.moveTo(65, 34);
            path.curveTo(55, 23, 35, 29, 50, 48);
            path.curveTo(65, 67, 45, 77, 35, 66);
            g2.draw(path);
        }

        private void drawOrders(Graphics2D g2) {
            g2.setStroke(new BasicStroke(7.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Shopping bag body
            g2.drawRoundRect(15, 35, 70, 52, 12, 12);
            // Shopping bag handle (arc)
            g2.drawArc(33, 13, 34, 44, 0, 180);
        }

        private void drawLowStock(Graphics2D g2) {
            g2.setStroke(new BasicStroke(7.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Warning triangle
            Path2D tri = new Path2D.Float();
            tri.moveTo(50, 12);
            tri.lineTo(12, 82);
            tri.lineTo(88, 82);
            tri.closePath();
            g2.draw(tri);
            // Exclamation mark
            g2.drawLine(50, 36, 50, 58);
            g2.fillOval(46, 68, 8, 8);
        }

        private void drawCustomers(Graphics2D g2) {
            g2.setStroke(new BasicStroke(7.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // User head
            g2.drawOval(35, 14, 30, 30);
            // User body shoulders
            g2.drawArc(15, 56, 70, 50, 0, 180);
            g2.drawLine(15, 81, 85, 81);
        }
    }
}
