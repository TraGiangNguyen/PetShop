package is216.petshop.Product;

import is216.petshop.Product.Product;
import is216.petshop.Product.ProductDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;

public class ProductDialog extends JDialog {
    private static final Color WHITE = Color.WHITE;
    private static final Color PRIMARY = new Color(79, 70, 229);
    private static final Color BORDER = new Color(229, 231, 235);
    private static final Color TXT2 = new Color(107, 114, 128);

    private final ProductDAO dao;
    private final Product product;
    private final boolean isEdit;
    private final Runnable onSuccess;
    private final boolean isServicePreset;

    private JLabel lblImagePreview;
    private File selectedImageFile = null;

    public ProductDialog(Frame parent, Product product, ProductDAO dao, Runnable onSuccess) {
        this(parent, product, dao, onSuccess, false);
    }

    public ProductDialog(Frame parent, Product product, ProductDAO dao, Runnable onSuccess, boolean isServicePreset) {
        super(parent, product != null ? "Sửa sản phẩm" : (isServicePreset ? "Thêm dịch vụ mới" : "Thêm sản phẩm mới"), true);
        this.dao = dao;
        this.product = product;
        this.isEdit = product != null;
        this.onSuccess = onSuccess;
        this.isServicePreset = isServicePreset;

        initUI();
    }

    private void initUI() {
        if (isServicePreset) {
            setSize(480, 580);
        } else {
            setSize(480, 780);
        }
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        getContentPane().setBackground(WHITE);

        JPanel dTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        dTop.setBackground(PRIMARY);
        JLabel dLbl = new JLabel(isEdit ? "Sửa thông tin" : (isServicePreset ? "Thêm dịch vụ mới" : "Thêm sản phẩm mới"));
        dLbl.setFont(new Font("Helvetica Neue", Font.BOLD, 18));
        dLbl.setForeground(WHITE);
        dTop.add(dLbl);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE);
        form.setBorder(new EmptyBorder(15, 25, 15, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.weightx = 1.0;

        // --- IMAGE UPLOAD CONTAINER ---
        JPanel pnlImageContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlImageContainer.setBackground(WHITE);
        
        JPanel pnlImageFrame = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BORDER);
                float[] dash = {4.0f};
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
            }
        };
        pnlImageFrame.setPreferredSize(new Dimension(120, 120));
        pnlImageFrame.setBackground(new Color(249, 250, 251));
        pnlImageFrame.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        lblImagePreview = new JLabel("📷 Chọn ảnh", SwingConstants.CENTER);
        lblImagePreview.setFont(new Font("Helvetica Neue", Font.BOLD, 12));
        lblImagePreview.setForeground(TXT2);
        pnlImageFrame.add(lblImagePreview, BorderLayout.CENTER);
        
        pnlImageFrame.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Chọn hình ảnh sản phẩm");
                fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                    @Override
                    public boolean accept(java.io.File f) {
                        return f.isDirectory() || f.getName().toLowerCase().endsWith(".png") 
                            || f.getName().toLowerCase().endsWith(".jpg") || f.getName().toLowerCase().endsWith(".jpeg");
                    }
                    @Override
                    public String getDescription() {
                        return "Hình ảnh (*.png, *.jpg, *.jpeg)";
                    }
                });
                
                int result = fileChooser.showOpenDialog(ProductDialog.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedImageFile = fileChooser.getSelectedFile();
                    updateImagePreview(selectedImageFile);
                }
            }
        });
        pnlImageContainer.add(pnlImageFrame);

        if (isEdit) {
            ImageIcon existing = getProductImageByName(product.getName());
            if (existing != null) {
                updateImagePreview(existing);
            } else {
                updateImagePreview(null);
            }
        } else {
            updateImagePreview(null);
        }

        // Add fields
        JTextField tfName = mkFld(isEdit ? product.getName() : "");
        JTextField tfBrand = mkFld(isEdit ? product.getCategory() : (isServicePreset ? "Dịch vụ" : ""));
        JTextField tfUnit = mkFld(isEdit ? product.getUnit() : (isServicePreset ? "Lần" : "Cái"));
        JTextField tfPrice = mkFld(isEdit ? String.valueOf(product.getPrice()) : "0");
        JTextField tfTax = mkFld(isEdit ? String.valueOf(product.getTax()) : "0.0");
        JTextField tfBarcode = mkFld(isEdit ? product.getBarcode() : "");
        JTextField tfOrigin = mkFld(isEdit ? product.getOrigin() : "");
        JTextField tfSuitable = mkFld(isEdit ? product.getSuitableFor() : "");
        JTextField tfStock = mkFld(isEdit ? String.valueOf(product.getStock()) : "0");

        JCheckBox chkBuy = new JCheckBox("Có thể mua", isEdit ? product.getActiveBuy() == 1 : (isServicePreset ? false : true));
        chkBuy.setBackground(WHITE);
        chkBuy.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        
        JCheckBox chkSell = new JCheckBox("Có thể bán", isEdit ? product.getActiveSell() == 1 : true);
        chkSell.setBackground(WHITE);
        chkSell.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        
        JCheckBox chkPos = new JCheckBox("POS", isEdit ? product.getActivePos() == 1 : true);
        chkPos.setBackground(WHITE);
        chkPos.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));

        int row = 0;
        gbc.gridy = row++; gbc.insets = new Insets(5, 0, 10, 0);
        form.add(pnlImageContainer, gbc);
        gbc.insets = new Insets(4, 0, 4, 0);
        
        if (isServicePreset) {
            gbc.gridy = row++; form.add(new JLabel("Tên dịch vụ:"), gbc);
            gbc.gridy = row++; form.add(tfName, gbc);
            gbc.gridy = row++; form.add(new JLabel("Đơn vị tính:"), gbc);
            gbc.gridy = row++; form.add(tfUnit, gbc);
            gbc.gridy = row++; form.add(new JLabel("Giá dịch vụ niêm yết (VNĐ):"), gbc);
            gbc.gridy = row++; form.add(tfPrice, gbc);
            gbc.gridy = row++; form.add(new JLabel("Thuế suất (%):"), gbc);
            gbc.gridy = row++; form.add(tfTax, gbc);
            gbc.gridy = row++; form.add(new JLabel("Mã vạch / Mã dịch vụ (Barcode):"), gbc);
            gbc.gridy = row++; form.add(tfBarcode, gbc);
        } else {
            gbc.gridy = row++; form.add(new JLabel("Tên sản phẩm:"), gbc);
            gbc.gridy = row++; form.add(tfName, gbc);
            gbc.gridy = row++; form.add(new JLabel("Nhãn hiệu / Thương hiệu:"), gbc);
            gbc.gridy = row++; form.add(tfBrand, gbc);
            gbc.gridy = row++; form.add(new JLabel("Đơn vị tính:"), gbc);
            gbc.gridy = row++; form.add(tfUnit, gbc);
            gbc.gridy = row++; form.add(new JLabel("Giá bán niêm yết (VNĐ):"), gbc);
            gbc.gridy = row++; form.add(tfPrice, gbc);
            gbc.gridy = row++; form.add(new JLabel("Thuế suất (%):"), gbc);
            gbc.gridy = row++; form.add(tfTax, gbc);
            gbc.gridy = row++; form.add(new JLabel("Mã vạch (Barcode):"), gbc);
            gbc.gridy = row++; form.add(tfBarcode, gbc);
            gbc.gridy = row++; form.add(new JLabel("Xuất xứ:"), gbc);
            gbc.gridy = row++; form.add(tfOrigin, gbc);
            gbc.gridy = row++; form.add(new JLabel("Phù hợp cho:"), gbc);
            gbc.gridy = row++; form.add(tfSuitable, gbc);
            gbc.gridy = row++; form.add(new JLabel("Số lượng tồn kho:"), gbc);
            gbc.gridy = row++; form.add(tfStock, gbc);

            JPanel pnlChecks = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
            pnlChecks.setBackground(WHITE);
            pnlChecks.add(chkBuy);
            pnlChecks.add(chkSell);
            pnlChecks.add(chkPos);
            gbc.gridy = row++; gbc.insets = new Insets(10, 0, 5, 0);
            form.add(pnlChecks, gbc);
        }

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(WHITE);

        FlatBtn bSave = new FlatBtn(isEdit ? "Cập nhật" : "Lưu lại");
        bSave.setPreferredSize(new Dimension(0, 50));
        bSave.addActionListener(e -> {
            String name = tfName.getText().trim();
            String brand = tfBrand.getText().trim();
            String unit = tfUnit.getText().trim();
            String priceStr = tfPrice.getText().trim();
            String taxStr = tfTax.getText().trim();
            String barcode = tfBarcode.getText().trim();
            String origin = tfOrigin.getText().trim();
            String suitable = tfSuitable.getText().trim();
            String stockStr = tfStock.getText().trim();

            if (isServicePreset) {
                brand = "Dịch vụ";
                origin = "Việt Nam";
                suitable = "Tất cả";
                stockStr = "0";
            }

            if (name.isEmpty()) { showError(isServicePreset ? "Tên dịch vụ không được để trống" : "Tên sản phẩm không được để trống"); return; }
            if (brand.isEmpty()) { showError("Nhãn hiệu không được để trống"); return; }

            long price = 0;
            try {
                price = Long.parseLong(priceStr);
                if (price < 0) { showError("Giá bán không được âm"); return; }
            } catch (NumberFormatException ex) {
                showError("Giá bán phải là số nguyên"); return;
            }

            double tax = 0.0;
            try {
                tax = Double.parseDouble(taxStr);
                if (tax < 0 || tax > 100) { showError("Thuế suất phải từ 0% đến 100%"); return; }
            } catch (NumberFormatException ex) {
                showError("Thuế suất phải là số thực"); return;
            }

            int stock = 0;
            try {
                stock = Integer.parseInt(stockStr);
                if (stock < 0) { showError("Số lượng tồn kho không được âm"); return; }
            } catch (NumberFormatException ex) {
                showError("Số lượng tồn kho phải là số nguyên"); return;
            }

            Product p = isEdit ? product : new Product();
            p.setName(name);
            p.setCategory(brand);
            p.setUnit(unit.isEmpty() ? "Cái" : unit);
            p.setPrice(price);
            p.setTax(tax);
            p.setBarcode(barcode);
            p.setOrigin(origin);
            p.setSuitableFor(suitable);
            p.setStock(stock);
            p.setActiveBuy(chkBuy.isSelected() ? 1 : 0);
            p.setActiveSell(chkSell.isSelected() ? 1 : 0);
            p.setActivePos(chkPos.isSelected() ? 1 : 0);

            boolean success = isEdit ? dao.updateProduct(p) : dao.insertProduct(p);
            if (success) {
                if (selectedImageFile != null) {
                    saveProductImage(selectedImageFile, name);
                }
                if (onSuccess != null) onSuccess.run();
                dispose();
            } else {
                showError("Không thể lưu sản phẩm vào database. Vui lòng kiểm tra lại.");
            }
        });

        add(dTop, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bSave, BorderLayout.SOUTH);
    }

    private void updateImagePreview(Object source) {
        if (source instanceof File) {
            try {
                ImageIcon icon = new ImageIcon(((File) source).getAbsolutePath());
                Image img = icon.getImage().getScaledInstance(110, 110, Image.SCALE_SMOOTH);
                lblImagePreview.setIcon(new ImageIcon(img));
                lblImagePreview.setText("");
            } catch (Exception e) {
                lblImagePreview.setIcon(null);
                lblImagePreview.setText("Lỗi hiển thị");
            }
        } else if (source instanceof ImageIcon) {
            Image img = ((ImageIcon) source).getImage().getScaledInstance(110, 110, Image.SCALE_SMOOTH);
            lblImagePreview.setIcon(new ImageIcon(img));
            lblImagePreview.setText("");
        } else {
            lblImagePreview.setIcon(null);
            lblImagePreview.setText("📷 Chọn ảnh");
        }
    }

    private void saveProductImage(File selectedFile, String productName) {
        try {
            String slug = getProductImageSlug(productName);
            File destSrc = new File("src/main/resources/images/" + slug);
            File destTarget = new File("target/classes/images/" + slug);
            
            destSrc.getParentFile().mkdirs();
            destTarget.getParentFile().mkdirs();
            
            java.nio.file.Files.copy(
                selectedFile.toPath(), 
                destSrc.toPath(), 
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
            
            java.nio.file.Files.copy(
                selectedFile.toPath(), 
                destTarget.toPath(), 
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
        }
        
        if (filename != null) {
            try {
                java.net.URL imgURL = getClass().getClassLoader().getResource("images/" + filename);
                if (imgURL != null) {
                    return new ImageIcon(imgURL);
                }
            } catch (Exception e) {}
        }
        return null;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private JTextField mkFld(String v) {
        JTextField tf = new JTextField(v);
        tf.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(8, 12, 8, 12)));
        return tf;
    }

    static class FlatBtn extends JButton {
        FlatBtn(String text) {
            super(text);
            setFont(new Font("Helvetica Neue", Font.BOLD, 15));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isPressed() ? PRIMARY.darker() : getModel().isRollover() ? PRIMARY.brighter() : PRIMARY);
            g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 18, 18));
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
