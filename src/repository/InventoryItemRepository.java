package src.repository;

import src.config.DatabaseConfig;
import src.model.InventoryItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryItemRepository implements GenericRepository<InventoryItem> {
    private static InventoryItemRepository instance;

    private InventoryItemRepository() {}

    public static InventoryItemRepository getInstance() {
        if (instance == null) {
            instance = new InventoryItemRepository();
        }
        return instance;
    }

    @Override
    public void insert(InventoryItem item) throws SQLException {
        String sql = "INSERT INTO inventory_items (client_id, art_piece_id, purchase_price) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, item.getClientId());
            pstmt.setInt(2, item.getArtPieceId());
            pstmt.setDouble(3, item.getPurchasePrice());

            pstmt.executeUpdate();
        }
    }

    @Override
    public InventoryItem getById(int id) throws SQLException {
        String sql = "SELECT * FROM inventory_items WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        }

        return null;
    }

    @Override
    public List<InventoryItem> getAll() throws SQLException {
        List<InventoryItem> items = new ArrayList<>();
        String sql = "SELECT * FROM inventory_items ORDER BY acquired_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                items.add(mapResultSetToEntity(rs));
            }
        }

        return items;
    }

    public List<InventoryItem> getByClientId(int clientId) throws SQLException {
        List<InventoryItem> items = new ArrayList<>();
        String sql = "SELECT * FROM inventory_items WHERE client_id = ? ORDER BY acquired_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, clientId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToEntity(rs));
                }
            }
        }

        return items;
    }

    public double getTotalValueForClient(int clientId) throws SQLException {
       String sql = "SELECT COALESCE(SUM(ap.current_price), 0) AS total " +
             "FROM inventory_items ii " +
             "JOIN art_pieces ap ON ii.art_piece_id = ap.id " +
             "WHERE ii.client_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, clientId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }

        return 0;
    }

    @Override
    public void update(InventoryItem item) throws SQLException {
        String sql = "UPDATE inventory_items SET client_id = ?, art_piece_id = ?, purchase_price = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, item.getClientId());
            pstmt.setInt(2, item.getArtPieceId());
            pstmt.setDouble(3, item.getPurchasePrice());
            pstmt.setInt(4, item.getId());

            pstmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM inventory_items WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    private InventoryItem mapResultSetToEntity(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("acquired_at");

        return new InventoryItem(
                rs.getInt("id"),
                rs.getInt("client_id"),
                rs.getInt("art_piece_id"),
                rs.getDouble("purchase_price"),
                timestamp != null ? timestamp.toLocalDateTime() : null
        );
    }
}