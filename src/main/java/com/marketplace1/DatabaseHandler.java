package com.marketplace1;

import java.sql.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class DatabaseHandler {
    private static final String DB_URL = "jdbc:sqlite:marketplace.db";
    private static final String ADMIN_EMAIL = "admin01@email.com";
    private static final int PASSWORD_ITERATIONS = 120_000;
    private static final SecureRandom PASSWORD_RANDOM = new SecureRandom();
    private static boolean legacyTitleColumn;

    static {
        createTables();
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private static void createTables() {
        String usersTable = "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT, "
                + "student_id TEXT UNIQUE, "
                + "email TEXT UNIQUE, "
                + "password TEXT, "
                + "role TEXT, is_disabled INTEGER DEFAULT 0);";

        String productsTable = "CREATE TABLE IF NOT EXISTS products ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT, "
                + "description TEXT, "
                + "price REAL, "
                + "category TEXT DEFAULT 'General', "
                + "seller_email TEXT, "
                + "is_sold INTEGER DEFAULT 0, "
                + "buyer_email TEXT, "
                + "image_path TEXT, payment_method TEXT, sold_at TEXT);";
        String notificationsTable = "CREATE TABLE IF NOT EXISTS notifications ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "recipient_email TEXT NOT NULL, "
                + "type TEXT NOT NULL, "
                + "product_id INTEGER, "
                + "product_name TEXT, "
                + "message TEXT NOT NULL, "
                + "created_at TEXT DEFAULT CURRENT_TIMESTAMP, "
                + "is_read INTEGER DEFAULT 0);";
        String blocksTable = "CREATE TABLE IF NOT EXISTS user_blocks ("
                + "blocker_email TEXT NOT NULL, blocked_email TEXT NOT NULL, created_at TEXT DEFAULT CURRENT_TIMESTAMP, "
                + "PRIMARY KEY(blocker_email, blocked_email));";
        String reportsTable = "CREATE TABLE IF NOT EXISTS user_reports ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, reporter_email TEXT NOT NULL, reported_email TEXT NOT NULL, "
                + "product_id INTEGER, reason TEXT NOT NULL, details TEXT, status TEXT DEFAULT 'OPEN', created_at TEXT DEFAULT CURRENT_TIMESTAMP);";
        String reviewsTable = "CREATE TABLE IF NOT EXISTS seller_reviews ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, product_id INTEGER NOT NULL, reviewer_email TEXT NOT NULL, "
                + "rating INTEGER NOT NULL CHECK(rating BETWEEN 1 AND 5), comment TEXT, created_at TEXT DEFAULT CURRENT_TIMESTAMP, "
                + "UNIQUE(product_id, reviewer_email));";
        String messagesTable = "CREATE TABLE IF NOT EXISTS chat_messages ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, product_id INTEGER NOT NULL, sender_email TEXT NOT NULL, "
                + "recipient_email TEXT NOT NULL, message TEXT NOT NULL, created_at TEXT DEFAULT CURRENT_TIMESTAMP, is_read INTEGER DEFAULT 0);";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(usersTable);
            stmt.execute(productsTable);
            stmt.execute(notificationsTable);
            stmt.execute(blocksTable);
            stmt.execute(reportsTable);
            stmt.execute(reviewsTable);
            stmt.execute(messagesTable);
            ensureColumn(conn, "users", "is_disabled", "INTEGER DEFAULT 0");
            ensureColumn(conn, "user_reports", "status", "TEXT DEFAULT 'OPEN'");
            ensureColumn(conn, "products", "payment_method", "TEXT");
            ensureColumn(conn, "products", "sold_at", "TEXT");
            migrateLegacyPasswords(conn);
            migrateProducts(conn);
            migrateSoldNotifications(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void ensureColumn(Connection conn, String table, String column, String type) throws SQLException {
        boolean found = false;
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) if (column.equalsIgnoreCase(rs.getString("name"))) found = true;
        }
        if (!found) try (Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
        }
    }

    public static boolean isAdmin(String email) {
        return email != null && ADMIN_EMAIL.equalsIgnoreCase(email.trim());
    }

    private static boolean adminAllowed(String email) {
        if (isAdmin(email)) return true;
        System.err.println("Blocked an unauthorized admin operation.");
        return false;
    }

    public static int[] getAdminCounts(String adminEmail) {
        if (!adminAllowed(adminEmail)) return new int[]{0, 0, 0};
        int[] counts = new int[3];
        String[] queries = {
                "SELECT COUNT(*) FROM users WHERE COALESCE(is_disabled,0)=0",
                "SELECT COUNT(*) FROM products WHERE is_sold=0",
                "SELECT COUNT(*) FROM user_reports WHERE status='OPEN'"
        };
        try (Connection conn = connect()) {
            for (int i = 0; i < queries.length; i++)
                try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(queries[i])) {
                    if (rs.next()) counts[i] = rs.getInt(1);
                }
        } catch (SQLException e) { e.printStackTrace(); }
        return counts;
    }

    public static class AdminUser {
        public final String name, email, studentId, role;
        public final boolean disabled;
        AdminUser(String name, String email, String studentId, String role, boolean disabled) {
            this.name=name; this.email=email; this.studentId=studentId; this.role=role; this.disabled=disabled;
        }
        @Override public String toString() { return name + " · " + email + " · " + (disabled ? "SUSPENDED" : "ACTIVE"); }
    }

    public static List<AdminUser> getAdminUsers(String adminEmail) {
        List<AdminUser> users = new ArrayList<>();
        if (!adminAllowed(adminEmail)) return users;
        try (Connection conn = connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name,email,student_id,role,COALESCE(is_disabled,0) disabled FROM users ORDER BY name COLLATE NOCASE")) {
            while (rs.next()) users.add(new AdminUser(rs.getString("name"), rs.getString("email"),
                    rs.getString("student_id"), rs.getString("role"), rs.getInt("disabled") != 0));
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }

    public static boolean setUserDisabled(String adminEmail, String targetEmail, boolean disabled) {
        if (!adminAllowed(adminEmail) || targetEmail == null || isAdmin(targetEmail)) return false;
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement("UPDATE users SET is_disabled=? WHERE lower(email)=lower(?)")) {
            stmt.setInt(1, disabled ? 1 : 0); stmt.setString(2, targetEmail); return stmt.executeUpdate() == 1;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static class AdminReport {
        public final int id;
        public final String reason, details, status, summary;
        AdminReport(int id, String reason, String details, String status, String summary) {
            this.id=id; this.reason=reason; this.details=details; this.status=status; this.summary=summary;
        }
        @Override public String toString() { return "#" + id + " · " + reason + " · " + status; }
    }

    public static class AdminOrder {
        public final int productId;
        public final String productName, category, paymentMethod, purchasedAt;
        public final double price;
        public final String buyerName, buyerEmail, buyerStudentId, sellerName, sellerEmail, sellerStudentId;
        AdminOrder(int productId, String productName, String category, double price, String paymentMethod, String purchasedAt,
                   String buyerName, String buyerEmail, String buyerStudentId, String sellerName, String sellerEmail, String sellerStudentId) {
            this.productId=productId; this.productName=productName; this.category=category; this.price=price;
            this.paymentMethod=paymentMethod; this.purchasedAt=purchasedAt; this.buyerName=buyerName; this.buyerEmail=buyerEmail;
            this.buyerStudentId=buyerStudentId; this.sellerName=sellerName; this.sellerEmail=sellerEmail; this.sellerStudentId=sellerStudentId;
        }
        @Override public String toString() { return productName + " · " + buyerName + " · BDT " + String.format(java.util.Locale.ROOT, "%.2f", price); }
    }

    public static class AdminOrderMessage {
        public final String senderName, senderEmail, senderRole, message, sentAt;
        AdminOrderMessage(String senderName, String senderEmail, String senderRole, String message, String sentAt) {
            this.senderName=senderName; this.senderEmail=senderEmail; this.senderRole=senderRole; this.message=message; this.sentAt=sentAt;
        }
    }

    public static List<AdminOrder> getAdminOrders(String adminEmail) {
        List<AdminOrder> orders = new ArrayList<>();
        if (!adminAllowed(adminEmail)) return orders;
        String sql = "SELECT p.id,p.name,p.category,p.price,COALESCE(NULLIF(p.payment_method,''),'Not recorded') payment_method,"
                + "COALESCE(p.sold_at,(SELECT MIN(n.created_at) FROM notifications n WHERE n.product_id=p.id AND n.type='SOLD'),'Earlier purchase') purchased_at,"
                + "COALESCE(NULLIF(b.name,''),'Student') buyer_name,b.email buyer_email,b.student_id buyer_student_id,"
                + "COALESCE(NULLIF(s.name,''),'Student') seller_name,s.email seller_email,s.student_id seller_student_id "
                + "FROM products p LEFT JOIN users b ON lower(b.email)=lower(p.buyer_email) "
                + "LEFT JOIN users s ON lower(s.email)=lower(p.seller_email) "
                + "WHERE p.is_sold=1 AND p.buyer_email IS NOT NULL ORDER BY p.sold_at DESC,p.id DESC";
        try (Connection conn=connect(); Statement stmt=conn.createStatement(); ResultSet rs=stmt.executeQuery(sql)) {
            while (rs.next()) orders.add(new AdminOrder(rs.getInt("id"), rs.getString("name"), rs.getString("category"),
                    rs.getDouble("price"), rs.getString("payment_method"), rs.getString("purchased_at"), rs.getString("buyer_name"),
                    rs.getString("buyer_email"), rs.getString("buyer_student_id"), rs.getString("seller_name"), rs.getString("seller_email"), rs.getString("seller_student_id")));
        } catch (SQLException e) { e.printStackTrace(); }
        return orders;
    }

    public static List<AdminOrderMessage> getAdminOrderMessages(String adminEmail, int productId) {
        List<AdminOrderMessage> messages = new ArrayList<>();
        if (!adminAllowed(adminEmail)) return messages;
        String sql = "SELECT COALESCE(NULLIF(u.name,''),'Student') sender_name,u.email sender_email,"
                + "CASE WHEN lower(m.sender_email)=lower(p.buyer_email) THEN 'Buyer' ELSE 'Seller' END sender_role,"
                + "m.message,m.created_at FROM products p JOIN chat_messages m ON m.product_id=p.id "
                + "JOIN users u ON lower(u.email)=lower(m.sender_email) "
                + "WHERE p.id=? AND p.is_sold=1 AND lower(m.sender_email) IN (lower(p.buyer_email),lower(p.seller_email)) "
                + "AND lower(m.recipient_email) IN (lower(p.buyer_email),lower(p.seller_email)) ORDER BY m.id ASC";
        try (Connection conn=connect(); PreparedStatement stmt=conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            try (ResultSet rs=stmt.executeQuery()) {
                while (rs.next()) messages.add(new AdminOrderMessage(rs.getString("sender_name"), rs.getString("sender_email"),
                        rs.getString("sender_role"), rs.getString("message"), rs.getString("created_at")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return messages;
    }

    public static List<AdminReport> getAdminReports(String adminEmail) {
        List<AdminReport> reports = new ArrayList<>();
        if (!adminAllowed(adminEmail)) return reports;
        String sql = "SELECT r.id,r.reason,r.details,COALESCE(r.status,'OPEN') status,r.created_at, "
                + "COALESCE(NULLIF(reporter.name,''),'Student') reporter,COALESCE(NULLIF(target.name,''),'Former student') target, "
                + "COALESCE(p.name,'Removed listing') product FROM user_reports r "
                + "LEFT JOIN users reporter ON lower(reporter.email)=lower(r.reporter_email) "
                + "LEFT JOIN users target ON lower(target.email)=lower(r.reported_email) LEFT JOIN products p ON p.id=r.product_id ORDER BY r.id DESC";
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String summary = "Listing: " + rs.getString("product") + "\nReported seller: " + rs.getString("target")
                        + "\nReported by: " + rs.getString("reporter") + "\nReason: " + rs.getString("reason")
                        + "\nDetails: " + (rs.getString("details") == null ? "(none)" : rs.getString("details"))
                        + "\nSubmitted: " + rs.getString("created_at");
                reports.add(new AdminReport(rs.getInt("id"), rs.getString("reason"), rs.getString("details"), rs.getString("status"), summary));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return reports;
    }

    public static boolean reviewReport(String adminEmail, int reportId, String status) {
        if (!adminAllowed(adminEmail) || !("RESOLVED".equals(status) || "DISMISSED".equals(status))) return false;
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement("UPDATE user_reports SET status=? WHERE id=?")) {
            stmt.setString(1,status); stmt.setInt(2,reportId); return stmt.executeUpdate()==1;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean deleteProductAsAdmin(String adminEmail, int productId) {
        if (!adminAllowed(adminEmail)) return false;
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement("DELETE FROM products WHERE id=? AND is_sold=0")) {
            stmt.setInt(1,productId); return stmt.executeUpdate()==1;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static List<String> getAdminProducts(String adminEmail) {
        if (!adminAllowed(adminEmail)) return new ArrayList<>();
        return getProductRows("SELECT p.*, COALESCE(NULLIF(u.name,''),'Student seller') AS seller_name FROM products p "
                + "LEFT JOIN users u ON lower(u.email)=lower(p.seller_email) WHERE p.is_sold=0 ORDER BY p.id DESC");
    }

    /** Upgrade older plaintext credentials when opening an existing local database. */
    private static void migrateLegacyPasswords(Connection conn) throws SQLException {
        List<String[]> legacy = new ArrayList<>();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT id, password FROM users")) {
            while (rs.next()) {
                String stored = rs.getString("password");
                if (stored != null && !stored.startsWith("pbkdf2$")) legacy.add(new String[]{rs.getString("id"), stored});
            }
        }
        try (PreparedStatement update = conn.prepareStatement("UPDATE users SET password = ? WHERE id = ?")) {
            for (String[] row : legacy) {
                update.setString(1, hashPassword(row[1]));
                update.setString(2, row[0]);
                update.addBatch();
            }
            if (!legacy.isEmpty()) update.executeBatch();
        }
    }

    private static String hashPassword(String password) {
        byte[] salt = new byte[16];
        PASSWORD_RANDOM.nextBytes(salt);
        byte[] hash = derivePassword(password, salt, PASSWORD_ITERATIONS);
        return "pbkdf2$" + PASSWORD_ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(hash);
    }

    private static boolean verifyPassword(String password, String stored) {
        if (password == null || stored == null) return false;
        try {
            String[] parts = stored.split("\\$", -1);
            if (parts.length != 4 || !"pbkdf2".equals(parts[0])) return false;
            int iterations = Integer.parseInt(parts[1]);
            if (iterations < 1 || iterations > 1_000_000) return false;
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            return MessageDigest.isEqual(expected, derivePassword(password, salt, iterations));
        } catch (IllegalArgumentException e) { return false; }
    }

    private static byte[] derivePassword(String password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Password hashing is unavailable", e);
        } finally { spec.clearPassword(); }
    }

    /** Keeps databases created by earlier project versions usable. */
    private static void migrateProducts(Connection conn) throws SQLException {
        java.util.Set<String> columns = new java.util.HashSet<>();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("PRAGMA table_info(products)")) {
            while (rs.next()) columns.add(rs.getString("name").toLowerCase());
        }
        legacyTitleColumn = columns.contains("title");
        boolean needsSoldMigration = !columns.contains("is_sold");

        String[][] additions = {
                {"name", "TEXT"}, {"description", "TEXT"}, {"price", "REAL DEFAULT 0"},
                {"category", "TEXT DEFAULT 'General'"}, {"seller_email", "TEXT"},
                {"is_sold", "INTEGER DEFAULT 0"}, {"buyer_email", "TEXT"}, {"image_path", "TEXT"},
                {"payment_method", "TEXT"}, {"sold_at", "TEXT"}
        };
        try (Statement stmt = conn.createStatement()) {
            for (String[] addition : additions) {
                if (!columns.contains(addition[0])) stmt.execute("ALTER TABLE products ADD COLUMN " + addition[0] + " " + addition[1]);
            }
            if (columns.contains("title")) stmt.executeUpdate("UPDATE products SET name = title WHERE name IS NULL OR name = ''");
            if (needsSoldMigration && columns.contains("status")) {
                stmt.executeUpdate("UPDATE products SET is_sold = CASE WHEN lower(status) = 'available' THEN 0 ELSE 1 END");
            }
            if (columns.contains("seller_id")) {
                stmt.executeUpdate("UPDATE products SET seller_email = (SELECT email FROM users WHERE users.id = products.seller_id) WHERE seller_email IS NULL OR seller_email = ''");
            }
        }
    }

    /** Adds read-only history entries for purchases made before notifications existed. */
    private static void migrateSoldNotifications(Connection conn) throws SQLException {
        String sellerSql = "INSERT INTO notifications(recipient_email, type, product_id, product_name, message, is_read) "
                + "SELECT p.seller_email, 'SOLD', p.id, p.name, 'Your listing was purchased by ' || COALESCE(NULLIF(buyer.name,''),'another student') || '.', 1 "
                + "FROM products p LEFT JOIN users buyer ON lower(buyer.email) = lower(p.buyer_email) "
                + "WHERE p.is_sold = 1 AND p.seller_email IS NOT NULL AND p.seller_email <> '' "
                + "AND buyer.id IS NOT NULL "
                + "AND p.buyer_email IS NOT NULL AND NOT EXISTS (SELECT 1 FROM notifications n "
                + "WHERE n.recipient_email = p.seller_email AND n.type = 'SOLD' AND n.product_id = p.id)";
        String buyerSql = "INSERT INTO notifications(recipient_email, type, product_id, product_name, message, is_read) "
                + "SELECT p.buyer_email, 'PURCHASED', p.id, p.name, 'You purchased this item from ' || COALESCE(NULLIF(seller.name,''),'another student') || '.', 1 "
                + "FROM products p LEFT JOIN users seller ON lower(seller.email) = lower(p.seller_email) "
                + "WHERE p.is_sold = 1 AND p.buyer_email IS NOT NULL AND p.buyer_email <> '' "
                + "AND seller.id IS NOT NULL "
                + "AND NOT EXISTS (SELECT 1 FROM notifications n "
                + "WHERE n.recipient_email = p.buyer_email AND n.type = 'PURCHASED' AND n.product_id = p.id)";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sellerSql);
            stmt.executeUpdate(buyerSql);
        }
    }

    public static boolean registerUser(String name, String studentId, String email, String password, String role) {
        String registrationError = validateRegistration(name, studentId, email, password);
        if (!registrationError.isEmpty()) {
            System.err.println("Registration rejected: " + registrationError);
            return false;
        }
        String duplicateSql = "SELECT 1 FROM users WHERE lower(email)=lower(?) OR lower(student_id)=lower(?)";
        String sql = "INSERT INTO users(name, student_id, email, password, role) VALUES(?,?,?,?,?)";
        try (Connection conn = connect()) {
            try (PreparedStatement duplicate = conn.prepareStatement(duplicateSql)) {
                duplicate.setString(1, email.trim());
                duplicate.setString(2, studentId.trim());
                try (ResultSet rs = duplicate.executeQuery()) { if (rs.next()) return false; }
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name.trim());
            pstmt.setString(2, studentId.trim());
            pstmt.setString(3, email.trim().toLowerCase(java.util.Locale.ROOT));
            pstmt.setString(4, hashPassword(password));
            pstmt.setString(5, "Student");
            pstmt.executeUpdate();
            return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Shared registration rules used by both JavaFX registration screens and the database layer. */
    public static String validateRegistration(String name, String studentId, String email, String password) {
        if (name == null || name.trim().isEmpty()) return "Enter your full name.";
        if (name.trim().length() > 80) return "Name must be 80 characters or fewer.";
        if (studentId == null || studentId.trim().isEmpty()) return "Enter your student ID.";
        if (studentId.trim().length() > 50) return "Student ID must be 50 characters or fewer.";
        if (!isValidEmail(email)) return "Enter a valid email address, for example name@example.com.";
        if (!isStrongPassword(password)) return "Password must be at least 10 characters and include uppercase, lowercase, a number, and a symbol; spaces are not allowed.";
        return "";
    }

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        String value = email.trim();
        if (value.length() > 254 || value.contains("..")) return false;
        return value.matches("(?i)^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$");
    }

    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 10 || password.length() > 128 || password.matches(".*\\s.*")) return false;
        boolean upper = false, lower = false, digit = false, symbol = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) upper = true;
            else if (Character.isLowerCase(c)) lower = true;
            else if (Character.isDigit(c)) digit = true;
            else symbol = true;
        }
        return upper && lower && digit && symbol;
    }

    public static boolean loginUser(String email, String password) {
        String sql = "SELECT password FROM users WHERE lower(email) = lower(?) AND COALESCE(is_disabled,0)=0";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && verifyPassword(password, rs.getString("password"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean addProduct(String name, String description, double price, String category, String sellerEmail) {
        return addProduct(name, description, price, category, sellerEmail, null);
    }

    public static boolean addProduct(String name, String description, double price, String category, String sellerEmail, String imagePath) {
        String sql = legacyTitleColumn
                ? "INSERT INTO products(title, name, description, price, category, seller_email, is_sold, image_path) VALUES(?,?,?,?,?,?,0,?)"
                : "INSERT INTO products(name, description, price, category, seller_email, is_sold, image_path) VALUES(?,?,?,?,?,0,?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int parameter = 1;
            if (legacyTitleColumn) pstmt.setString(parameter++, name);
            pstmt.setString(parameter++, name);
            pstmt.setString(parameter++, description);
            pstmt.setDouble(parameter++, price);
            pstmt.setString(parameter++, category);
            pstmt.setString(parameter++, sellerEmail);
            pstmt.setString(parameter, imagePath);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateProduct(int productId, String name, String description, double price,
                                        String category, String sellerEmail, String imagePath) {
        String sql = legacyTitleColumn
                ? "UPDATE products SET title=?, name=?, description=?, price=?, category=?, image_path=? WHERE id=? AND lower(seller_email)=lower(?) AND is_sold=0"
                : "UPDATE products SET name=?, description=?, price=?, category=?, image_path=? WHERE id=? AND lower(seller_email)=lower(?) AND is_sold=0";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            int parameter = 1;
            if (legacyTitleColumn) stmt.setString(parameter++, name);
            stmt.setString(parameter++, name);
            stmt.setString(parameter++, description);
            stmt.setDouble(parameter++, price);
            stmt.setString(parameter++, category);
            stmt.setString(parameter++, imagePath);
            stmt.setInt(parameter++, productId);
            stmt.setString(parameter, sellerEmail);
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> getAllProducts() {
        String sql = "SELECT p.*, COALESCE(NULLIF(u.name,''),'Student seller') AS seller_name "
                + "FROM products p LEFT JOIN users u ON lower(u.email) = lower(p.seller_email) WHERE p.is_sold = 0 ORDER BY p.id DESC";
        return getProductRows(sql);
    }

    public static List<String> getAllProducts(String viewerEmail) {
        String sql = "SELECT p.*, COALESCE(NULLIF(u.name,''),'Student seller') AS seller_name "
                + "FROM products p LEFT JOIN users u ON lower(u.email) = lower(p.seller_email) "
                + "WHERE p.is_sold = 0 AND COALESCE(u.is_disabled,0)=0 AND NOT EXISTS (SELECT 1 FROM user_blocks b "
                + "WHERE lower(b.blocker_email) = lower(?) AND lower(b.blocked_email) = lower(p.seller_email)) "
                + "ORDER BY p.id DESC";
        return getProductRows(sql, viewerEmail);
    }

    public static boolean isProductAvailableTo(int productId, String viewerEmail) {
        String sql = "SELECT 1 FROM products p WHERE p.id = ? AND p.is_sold = 0 AND NOT EXISTS "
                + "(SELECT 1 FROM user_blocks b WHERE lower(b.blocker_email) = lower(?) AND lower(b.blocked_email) = lower(p.seller_email))";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.setString(2, viewerEmail);
            try (ResultSet rs = stmt.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean blockSeller(String blockerEmail, int productId) {
        String sql = "INSERT OR IGNORE INTO user_blocks(blocker_email, blocked_email) "
                + "SELECT ?, seller_email FROM products WHERE id = ? AND lower(seller_email) <> lower(?)";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, blockerEmail);
            stmt.setInt(2, productId);
            stmt.setString(3, blockerEmail);
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean unblockSeller(String blockerEmail, String blockedEmail) {
        String sql = "DELETE FROM user_blocks WHERE lower(blocker_email) = lower(?) AND lower(blocked_email) = lower(?)";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, blockerEmail);
            stmt.setString(2, blockedEmail);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static class BlockedUser {
        private final String email;
        private final String name;
        public BlockedUser(String email, String name) { this.email = email; this.name = name; }
        public String getEmail() { return email; }
        public String getName() { return name; }
        @Override public String toString() { return name; }
    }

    public static List<BlockedUser> getBlockedUsers(String blockerEmail) {
        List<BlockedUser> users = new ArrayList<>();
        String sql = "SELECT b.blocked_email, COALESCE(NULLIF(u.name,''),'Student') AS display_name "
                + "FROM user_blocks b LEFT JOIN users u ON lower(u.email) = lower(b.blocked_email) "
                + "WHERE lower(b.blocker_email) = lower(?) ORDER BY display_name COLLATE NOCASE";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, blockerEmail);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) users.add(new BlockedUser(rs.getString("blocked_email"), rs.getString("display_name")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }

    public static boolean reportSeller(String reporterEmail, int productId, String reason, String details) {
        String sql = "INSERT INTO user_reports(reporter_email, reported_email, product_id, reason, details) "
                + "SELECT ?, seller_email, id, ?, ? FROM products WHERE id = ? AND lower(seller_email) <> lower(?)";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reporterEmail);
            stmt.setString(2, reason);
            stmt.setString(3, details == null ? "" : details.trim());
            stmt.setInt(4, productId);
            stmt.setString(5, reporterEmail);
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean deleteAccount(String email, String password) {
        String verifySql = "SELECT password FROM users WHERE lower(email) = lower(?)";
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try (PreparedStatement verify = conn.prepareStatement(verifySql)) {
                verify.setString(1, email);
                verify.setString(2, password);
                try (ResultSet rs = verify.executeQuery()) {
                    if (!rs.next() || !verifyPassword(password, rs.getString("password"))) { conn.rollback(); return false; }
                }
            }
            List<String> imagePaths = new ArrayList<>();
            String imageSql = "SELECT image_path FROM products WHERE (lower(seller_email) = lower(?) OR lower(buyer_email) = lower(?)) AND image_path IS NOT NULL";
            try (PreparedStatement images = conn.prepareStatement(imageSql)) {
                images.setString(1, email);
                images.setString(2, email);
                try (ResultSet rs = images.executeQuery()) {
                    while (rs.next()) imagePaths.add(rs.getString("image_path"));
                }
            }
            String[] deletes = {
                    "DELETE FROM notifications WHERE lower(recipient_email) = lower(?) OR product_id IN (SELECT id FROM products WHERE lower(seller_email) = lower(?) OR lower(buyer_email) = lower(?))",
                    "DELETE FROM user_reports WHERE lower(reporter_email) = lower(?) OR lower(reported_email) = lower(?)",
                    "DELETE FROM user_blocks WHERE lower(blocker_email) = lower(?) OR lower(blocked_email) = lower(?)",
                    "DELETE FROM products WHERE lower(seller_email) = lower(?) OR lower(buyer_email) = lower(?)",
                    "DELETE FROM users WHERE lower(email) = lower(?)"
            };
            for (int i = 0; i < deletes.length; i++) {
                try (PreparedStatement stmt = conn.prepareStatement(deletes[i])) {
                    if (i == 4) stmt.setString(1, email);
                    else if (i == 0) { stmt.setString(1, email); stmt.setString(2, email); stmt.setString(3, email); }
                    else { stmt.setString(1, email); stmt.setString(2, email); }
                    stmt.executeUpdate();
                }
            }
            conn.commit();
            Path imageDirectory = Paths.get("product-images").toAbsolutePath().normalize();
            for (String imagePath : imagePaths) {
                try {
                    Path candidate = Paths.get(imagePath).toAbsolutePath().normalize();
                    if (candidate.startsWith(imageDirectory)) Files.deleteIfExists(candidate);
                } catch (Exception ignored) { /* Keep account removal successful if an old photo is already missing. */ }
            }
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static List<String> getMyProducts(String sellerEmail) {
        String sql = "SELECT p.*, COALESCE(NULLIF(u.name,''),'Student seller') AS seller_name "
                + "FROM products p LEFT JOIN users u ON lower(u.email) = lower(p.seller_email) "
                + "WHERE p.is_sold = 0 AND lower(p.seller_email) = lower(?) ORDER BY p.id DESC";
        return getProductRows(sql, sellerEmail);
    }

    private static List<String> getProductRows(String sql) {
        List<String> products = new ArrayList<>();
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(productRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    private static List<String> getProductRows(String sql, String sellerEmail) {
        List<String> products = new ArrayList<>();
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sellerEmail);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) products.add(productRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    private static String productRow(ResultSet rs) throws SQLException {
        String category = rs.getString("category");
        if (category == null || category.isEmpty()) category = "General";
        return "ID: " + rs.getInt("id") +
                " | Title: " + clean(rs.getString("name")) +
                " | Desc: " + clean(rs.getString("description")) +
                " | Price: " + rs.getDouble("price") +
                " | Category: " + clean(category) +
                " | Seller: " + clean(rs.getString("seller_name")) +
                " | Image: " + clean(rs.getString("image_path"));
    }

    public static boolean isSeller(int productId, String email) {
        String sql = "SELECT 1 FROM products WHERE id = ? AND lower(seller_email) = lower(?)";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.setString(2, email);
            try (ResultSet rs = stmt.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // '|' চিহ্ন আমাদের string format ভেঙে দেয়, তাই সরিয়ে দেওয়া হচ্ছে
    private static String clean(String s) {
        return s == null ? "" : s.replace("|", "/");
    }

    public static boolean buyProduct(int productId, String buyerEmail) {
        return buyProduct(productId, buyerEmail, "Cash at meetup");
    }

    public static boolean buyProduct(int productId, String buyerEmail, String paymentMethod) {
        if (paymentMethod == null || !(paymentMethod.equals("Cash at meetup")
                || paymentMethod.equals("bKash at meetup") || paymentMethod.equals("Nagad at meetup"))) return false;
        String findSql = "SELECT p.name, p.seller_email, "
                + "COALESCE(NULLIF(seller.name,''),'another student') AS seller_name, "
                + "COALESCE(NULLIF(buyer.name,''),'another student') AS buyer_name "
                + "FROM products p LEFT JOIN users seller ON lower(seller.email) = lower(p.seller_email) "
                + "LEFT JOIN users buyer ON lower(buyer.email) = lower(?) WHERE p.id = ? AND p.is_sold = 0 "
                + "AND COALESCE(seller.is_disabled,0)=0 "
                + "AND NOT EXISTS (SELECT 1 FROM user_blocks b WHERE lower(b.blocker_email) = lower(?) AND lower(b.blocked_email) = lower(p.seller_email))";
        String updateSql = "UPDATE products SET is_sold = 1, buyer_email = ?, payment_method=?, sold_at=CURRENT_TIMESTAMP WHERE id = ? AND is_sold = 0";
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            String productName;
            String sellerEmail;
            String sellerName;
            String buyerName;
            try (PreparedStatement find = conn.prepareStatement(findSql)) {
                find.setString(1, buyerEmail);
                find.setInt(2, productId);
                find.setString(3, buyerEmail);
                try (ResultSet rs = find.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    productName = rs.getString("name");
                    sellerEmail = rs.getString("seller_email");
                    sellerName = rs.getString("seller_name");
                    buyerName = rs.getString("buyer_name");
                }
            }
            if (sellerEmail == null || sellerEmail.equalsIgnoreCase(buyerEmail)) {
                conn.rollback();
                return false;
            }
            try (PreparedStatement update = conn.prepareStatement(updateSql)) {
                update.setString(1, buyerEmail);
                update.setString(2, paymentMethod);
                update.setInt(3, productId);
                if (update.executeUpdate() != 1) {
                    conn.rollback();
                    return false;
                }
            }
            createNotification(conn, sellerEmail, "SOLD", productId, productName,
                    "Your listing was reserved by " + buyerName + ". Payment preference: " + paymentMethod + ". Arrange payment and handover directly with the buyer.");
            createNotification(conn, buyerEmail, "PURCHASED", productId, productName,
                    "You reserved this item from " + sellerName + ". Payment preference: " + paymentMethod + ". Arrange payment and handover directly with the seller.");
            String paymentWording = paymentMethod.startsWith("Cash") ? "cash" : "online via " + paymentMethod.substring(0, paymentMethod.indexOf(' '));
            String openingMessage = "Hi " + sellerName + "! I want to buy “" + productName + "” and pay "
                    + paymentWording + " at our campus meetup. Please message me so we can agree on a time and place.";
            try (PreparedStatement message = conn.prepareStatement(
                    "INSERT INTO chat_messages(product_id,sender_email,recipient_email,message) VALUES(?,?,?,?)")) {
                message.setInt(1, productId);
                message.setString(2, buyerEmail);
                message.setString(3, sellerEmail);
                message.setString(4, openingMessage);
                message.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void createNotification(Connection conn, String recipient, String type, int productId,
                                           String productName, String message) throws SQLException {
        String sql = "INSERT INTO notifications(recipient_email, type, product_id, product_name, message) VALUES(?,?,?,?,?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, recipient);
            stmt.setString(2, type);
            stmt.setInt(3, productId);
            stmt.setString(4, productName);
            stmt.setString(5, message);
            stmt.executeUpdate();
        }
    }

    public static class Notification {
        private final int id;
        private final String type;
        private final int productId;
        private final String productName;
        private final String message;
        private final String createdAt;
        private final boolean read;

        public Notification(int id, String type, int productId, String productName, String message, String createdAt, boolean read) {
            this.id = id;
            this.type = type;
            this.productId = productId;
            this.productName = productName;
            this.message = message;
            this.createdAt = createdAt;
            this.read = read;
        }
        public int getId() { return id; }
        public String getType() { return type; }
        public int getProductId() { return productId; }
        public String getProductName() { return productName; }
        public String getMessage() { return message; }
        public String getCreatedAt() { return createdAt; }
        public boolean isRead() { return read; }
    }

    public static List<Notification> getNotifications(String email) {
        List<Notification> result = new ArrayList<>();
        String sql = "SELECT n.id, n.type, n.product_id, n.product_name, "
                + "CASE WHEN n.type = 'SOLD' THEN 'Buyer: ' || COALESCE(NULLIF(buyer.name,''),'another student') "
                + "ELSE 'Seller: ' || COALESCE(NULLIF(seller.name,''),'another student') END AS safe_message, "
                + "n.created_at, n.is_read FROM notifications n "
                + "LEFT JOIN products p ON p.id = n.product_id "
                + "LEFT JOIN users buyer ON lower(buyer.email) = lower(p.buyer_email) "
                + "LEFT JOIN users seller ON lower(seller.email) = lower(p.seller_email) "
                + "WHERE n.recipient_email = ? ORDER BY n.id DESC";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) result.add(new Notification(rs.getInt("id"), rs.getString("type"),
                        rs.getInt("product_id"), rs.getString("product_name"), rs.getString("safe_message"),
                        rs.getString("created_at"), rs.getInt("is_read") != 0));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static int countUnreadNotifications(String email) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE recipient_email = ? AND is_read = 0";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void markNotificationRead(int notificationId, String email) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE id = ? AND recipient_email = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notificationId);
            stmt.setString(2, email);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void markAllNotificationsRead(String email) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE recipient_email = ? AND is_read = 0";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static boolean deleteProduct(int productId, String sellerEmail) {
        String sql = "DELETE FROM products WHERE id = ? AND seller_email = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            pstmt.setString(2, sellerEmail);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> getSoldNotifications(String sellerEmail) {
        List<String> notifications = new ArrayList<>();
        String sql = "SELECT p.name, p.price, p.image_path, COALESCE(NULLIF(u.name,''),'another student') AS buyer_name "
                + "FROM products p LEFT JOIN users u ON lower(u.email) = lower(p.buyer_email) "
                + "WHERE lower(p.seller_email) = lower(?) AND p.is_sold = 1";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sellerEmail);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String notif = "SOLD: " + rs.getString("name") +
                        " | Price: BDT " + rs.getDouble("price") +
                        " | Buyer: " + rs.getString("buyer_name") +
                        " | Photo: " + clean(rs.getString("image_path"));
                notifications.add(notif);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }

    public static class ChatMessage {
        public final int id;
        public final String senderName, message, createdAt;
        public final boolean mine;
        ChatMessage(int id, String senderName, String message, String createdAt, boolean mine) {
            this.id=id; this.senderName=senderName; this.message=message; this.createdAt=createdAt; this.mine=mine;
        }
        @Override public String toString() { return (mine ? "You" : senderName) + "  ·  " + createdAt + "\n" + message; }
    }

    public static class ChatConversation {
        public final int productId;
        public final String productName, otherName, otherEmail, lastMessage, createdAt;
        public final int unread;
        ChatConversation(int productId, String productName, String otherName, String otherEmail, String lastMessage, String createdAt, int unread) {
            this.productId=productId; this.productName=productName; this.otherName=otherName;
            this.otherEmail=otherEmail; this.lastMessage=lastMessage; this.createdAt=createdAt; this.unread=unread;
        }
        @Override public String toString() {
            return otherName + "  ·  " + productName + (unread > 0 ? "  ·  " + unread + " new" : "") + "\n" + lastMessage;
        }
    }

    private static String chatPartner(Connection conn, String userEmail, int productId, String otherEmail) throws SQLException {
        String sql = "SELECT p.seller_email,p.buyer_email,p.is_sold,COALESCE(s.is_disabled,0) seller_disabled,COALESCE(b.is_disabled,0) buyer_disabled "
                + "FROM products p LEFT JOIN users s ON lower(s.email)=lower(p.seller_email) "
                + "LEFT JOIN users b ON lower(b.email)=lower(p.buyer_email) WHERE p.id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;
                String seller = rs.getString("seller_email"), buyer = rs.getString("buyer_email");
                boolean sold = rs.getInt("is_sold") != 0;
                if (seller == null || otherEmail == null || otherEmail.equalsIgnoreCase(userEmail)) return null;
                if (seller.equalsIgnoreCase(userEmail)) {
                    if (rs.getInt("seller_disabled") != 0 || otherEmail.equalsIgnoreCase(seller)) return null;
                    if (sold) return buyer != null && otherEmail.equalsIgnoreCase(buyer) && rs.getInt("buyer_disabled") == 0 ? buyer : null;
                    try (PreparedStatement prior = conn.prepareStatement("SELECT 1 FROM chat_messages WHERE product_id=? AND ((lower(sender_email)=lower(?) AND lower(recipient_email)=lower(?)) OR (lower(sender_email)=lower(?) AND lower(recipient_email)=lower(?))) LIMIT 1")) {
                        prior.setInt(1,productId); prior.setString(2,seller); prior.setString(3,otherEmail); prior.setString(4,otherEmail); prior.setString(5,seller);
                        try (ResultSet hasChat=prior.executeQuery()) { return hasChat.next() ? otherEmail : null; }
                    }
                }
                if (!otherEmail.equalsIgnoreCase(seller) || rs.getInt("seller_disabled") != 0) return null;
                if (!sold) return seller;
                return buyer != null && buyer.equalsIgnoreCase(userEmail) && rs.getInt("buyer_disabled") == 0 ? seller : null;
            }
        }
    }

    private static boolean isBlocked(Connection conn, String first, String second) throws SQLException {
        String sql = "SELECT 1 FROM user_blocks WHERE (lower(blocker_email)=lower(?) AND lower(blocked_email)=lower(?)) "
                + "OR (lower(blocker_email)=lower(?) AND lower(blocked_email)=lower(?)) LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, first); stmt.setString(2, second); stmt.setString(3, second); stmt.setString(4, first);
            try (ResultSet rs = stmt.executeQuery()) { return rs.next(); }
        }
    }

    public static String getChatCounterpart(String userEmail, int productId) {
        try (Connection conn=connect()) {
            String sql="SELECT seller_email,buyer_email,is_sold FROM products WHERE id=?";
            try (PreparedStatement stmt=conn.prepareStatement(sql)) {
                stmt.setInt(1,productId);
                try(ResultSet rs=stmt.executeQuery()) {
                    if(!rs.next()) return null;
                    String seller=rs.getString("seller_email"),buyer=rs.getString("buyer_email");
                    if(seller==null || seller.equalsIgnoreCase(userEmail)) return null;
                    if(rs.getInt("is_sold")!=0 && (buyer==null || !buyer.equalsIgnoreCase(userEmail))) return null;
                    return seller;
                }
            }
        } catch(SQLException e){e.printStackTrace();return null;}
    }

    public static boolean sendChatMessage(String senderEmail, int productId, String otherEmail, String message) {
        if (senderEmail == null || message == null || message.trim().isEmpty() || message.trim().length() > 1500) return false;
        String sql = "INSERT INTO chat_messages(product_id,sender_email,recipient_email,message) VALUES(?,?,?,?)";
        try (Connection conn = connect()) {
            String recipient = chatPartner(conn, senderEmail, productId, otherEmail);
            if (recipient == null || isBlocked(conn, senderEmail, recipient)) return false;
            try (PreparedStatement status = conn.prepareStatement("SELECT COALESCE(is_disabled,0) FROM users WHERE lower(email)=lower(?)")) {
                status.setString(1, senderEmail);
                try (ResultSet rs = status.executeQuery()) { if (!rs.next() || rs.getInt(1) != 0) return false; }
            }
            try (PreparedStatement status = conn.prepareStatement("SELECT COALESCE(is_disabled,0) FROM users WHERE lower(email)=lower(?)")) {
                status.setString(1, recipient);
                try (ResultSet rs = status.executeQuery()) { if (!rs.next() || rs.getInt(1) != 0) return false; }
            }
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, productId); stmt.setString(2, senderEmail); stmt.setString(3, recipient); stmt.setString(4, message.trim());
                return stmt.executeUpdate() == 1;
            }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static List<ChatMessage> getChatMessages(String userEmail, int productId, String otherEmail) {
        List<ChatMessage> messages = new ArrayList<>();
        String sql = "SELECT m.id,m.message,m.created_at,m.sender_email,COALESCE(NULLIF(u.name,''),'Student') sender_name "
                + "FROM chat_messages m LEFT JOIN users u ON lower(u.email)=lower(m.sender_email) "
                + "WHERE m.product_id=? AND ((lower(m.sender_email)=lower(?) AND lower(m.recipient_email)=lower(?)) "
                + "OR (lower(m.sender_email)=lower(?) AND lower(m.recipient_email)=lower(?))) "
                + "ORDER BY m.id ASC";
        try (Connection conn = connect()) {
            String partner = chatPartner(conn, userEmail, productId, otherEmail);
            if (partner == null || isBlocked(conn, userEmail, partner)) return messages;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1,productId); stmt.setString(2,userEmail); stmt.setString(3,partner);
                stmt.setString(4,partner); stmt.setString(5,userEmail);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) messages.add(new ChatMessage(rs.getInt("id"), rs.getString("sender_name"),
                            rs.getString("message"), rs.getString("created_at"), userEmail.equalsIgnoreCase(rs.getString("sender_email"))));
                }
            }
            try (PreparedStatement read = conn.prepareStatement("UPDATE chat_messages SET is_read=1 WHERE product_id=? AND lower(recipient_email)=lower(?) AND lower(sender_email)=lower(?)")) {
                read.setInt(1,productId); read.setString(2,userEmail); read.setString(3,partner); read.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return messages;
    }

    public static List<ChatConversation> getChatConversations(String userEmail) {
        List<ChatConversation> conversations = new ArrayList<>();
        String pairsSql = "SELECT DISTINCT product_id, CASE WHEN lower(sender_email)=lower(?) THEN recipient_email ELSE sender_email END partner "
                + "FROM chat_messages WHERE lower(sender_email)=lower(?) OR lower(recipient_email)=lower(?)";
        try (Connection conn = connect(); PreparedStatement pairs = conn.prepareStatement(pairsSql)) {
            pairs.setString(1,userEmail); pairs.setString(2,userEmail); pairs.setString(3,userEmail);
            try (ResultSet rs = pairs.executeQuery()) {
                while (rs.next()) {
                    int productId = rs.getInt("product_id"); String partner = rs.getString("partner");
                    if (partner == null || isBlocked(conn,userEmail,partner)) continue;
                    String partnerName = "Student", productName = "Listing", last = ""; String time = ""; int unread = 0;
                    try (PreparedStatement info = conn.prepareStatement("SELECT COALESCE(NULLIF(u.name,''),'Student'),COALESCE(p.name,'Listing') FROM users u,products p WHERE lower(u.email)=lower(?) AND p.id=?")) {
                        info.setString(1,partner); info.setInt(2,productId);
                        try (ResultSet data = info.executeQuery()) { if (data.next()) { partnerName=data.getString(1); productName=data.getString(2); } }
                    }
                    try (PreparedStatement latest = conn.prepareStatement("SELECT message,created_at FROM chat_messages WHERE product_id=? AND ((lower(sender_email)=lower(?) AND lower(recipient_email)=lower(?)) OR (lower(sender_email)=lower(?) AND lower(recipient_email)=lower(?))) ORDER BY id DESC LIMIT 1")) {
                        latest.setInt(1,productId); latest.setString(2,userEmail); latest.setString(3,partner); latest.setString(4,partner); latest.setString(5,userEmail);
                        try (ResultSet data = latest.executeQuery()) { if (data.next()) { last=data.getString(1); time=data.getString(2); } }
                    }
                    try (PreparedStatement unreadStmt = conn.prepareStatement("SELECT COUNT(*) FROM chat_messages WHERE product_id=? AND lower(recipient_email)=lower(?) AND lower(sender_email)=lower(?) AND is_read=0")) {
                        unreadStmt.setInt(1,productId); unreadStmt.setString(2,userEmail); unreadStmt.setString(3,partner);
                        try (ResultSet data=unreadStmt.executeQuery()) { if (data.next()) unread=data.getInt(1); }
                    }
                    conversations.add(new ChatConversation(productId,productName,partnerName,partner,last,time,unread));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        conversations.sort((a,b) -> b.createdAt.compareTo(a.createdAt));
        return conversations;
    }

    public static boolean hasPurchasedProduct(String buyerEmail, int productId) {
        try (Connection conn=connect(); PreparedStatement stmt=conn.prepareStatement("SELECT 1 FROM products WHERE id=? AND is_sold=1 AND lower(buyer_email)=lower(?)")) {
            stmt.setInt(1,productId); stmt.setString(2,buyerEmail); try (ResultSet rs=stmt.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean addSellerReview(String reviewerEmail, int productId, int rating, String comment) {
        if (rating < 1 || rating > 5 || comment == null || comment.trim().length() > 600 || !hasPurchasedProduct(reviewerEmail,productId)) return false;
        String sql = "INSERT INTO seller_reviews(product_id,reviewer_email,rating,comment) VALUES(?,?,?,?) "
                + "ON CONFLICT(product_id,reviewer_email) DO UPDATE SET rating=excluded.rating,comment=excluded.comment,created_at=CURRENT_TIMESTAMP";
        try (Connection conn=connect(); PreparedStatement stmt=conn.prepareStatement(sql)) {
            stmt.setInt(1,productId); stmt.setString(2,reviewerEmail); stmt.setInt(3,rating); stmt.setString(4,comment.trim());
            return stmt.executeUpdate()==1;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static String getSellerRatingForProduct(int productId) {
        String sql = "SELECT AVG(r.rating) avg_rating,COUNT(r.id) total FROM products p LEFT JOIN seller_reviews r "
                + "ON lower(r.reviewer_email)<>lower(p.seller_email) AND r.product_id IN (SELECT id FROM products WHERE lower(seller_email)=lower(p.seller_email)) WHERE p.id=? GROUP BY p.id";
        try (Connection conn=connect(); PreparedStatement stmt=conn.prepareStatement(sql)) {
            stmt.setInt(1,productId); try (ResultSet rs=stmt.executeQuery()) {
                if (rs.next() && rs.getInt("total")>0) return String.format(java.util.Locale.ROOT,"%.1f (%d reviews)",rs.getDouble("avg_rating"),rs.getInt("total"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return "No reviews yet";
    }

    public static List<String> getSellerReviewsForProduct(int productId) {
        List<String> reviews=new ArrayList<>();
        String sql="SELECT r.rating,r.comment,COALESCE(NULLIF(u.name,''),'Student') reviewer,r.created_at FROM products p "
                + "JOIN products seller_products ON lower(seller_products.seller_email)=lower(p.seller_email) "
                + "JOIN seller_reviews r ON r.product_id=seller_products.id LEFT JOIN users u ON lower(u.email)=lower(r.reviewer_email) "
                + "WHERE p.id=? ORDER BY r.id DESC LIMIT 8";
        try(Connection conn=connect();PreparedStatement stmt=conn.prepareStatement(sql)) {
            stmt.setInt(1,productId); try(ResultSet rs=stmt.executeQuery()) {
                while(rs.next()) reviews.add(repeatCharacter('★', rs.getInt("rating"))+"  ·  "+rs.getString("reviewer")+"\n"+(rs.getString("comment")==null?"":rs.getString("comment")));
            }
        } catch(SQLException e){e.printStackTrace();}
        return reviews;
    }

    private static String repeatCharacter(char character, int count) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) result.append(character);
        return result.toString();
    }

    public static List<String> getMyPurchases(String buyerEmail) {
        List<String> purchases = new ArrayList<>();
        String sql = "SELECT p.id, p.name, p.description, p.price, p.category, "
                + "COALESCE(NULLIF(u.name,''),'Student seller') AS seller_name, p.image_path "
                + "FROM products p LEFT JOIN users u ON lower(u.email) = lower(p.seller_email) "
                + "WHERE lower(p.buyer_email) = lower(?) AND p.is_sold = 1 ORDER BY p.id DESC";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, buyerEmail);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String purchase = "ID: " + rs.getInt("id") +
                        " | Title: " + clean(rs.getString("name")) +
                        " | Desc: " + clean(rs.getString("description")) +
                        " | Price: " + rs.getDouble("price") +
                        " | Category: " + clean(rs.getString("category")) +
                        " | Seller: " + clean(rs.getString("seller_name")) +
                        " | Image: " + clean(rs.getString("image_path"));
                purchases.add(purchase);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return purchases;
    }
}
