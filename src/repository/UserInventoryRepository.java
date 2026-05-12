package src.repository;
import src.config.DatabaseConfig;

import java.sql.*;

public class UserInventoryRepository {
    private static UserInventoryRepository instance;

    private UserInventoryRepository() {}

    public static UserInventoryRepository getInstance() {
        if (instance == null) instance = new UserInventoryRepository();
        return instance;
    }
    
    public double getTotalAssetsValue(int userId) throws SQLException {
        String sql = "SELECT SUM(current_price) as total FROM art_pieces WHERE owner_id = ? AND is_sold = true";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        return 0;
    }
}