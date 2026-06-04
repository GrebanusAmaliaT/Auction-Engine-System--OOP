package src.repository;

import src.config.DatabaseConfig;
import src.model.AuctionRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuctionRecordRepository implements GenericRepository<AuctionRecord> {
    private static AuctionRecordRepository instance;

    private AuctionRecordRepository() {}

    public static AuctionRecordRepository getInstance() {
        if (instance == null) {
            instance = new AuctionRecordRepository();
        }
        return instance;
    }

    @Override
    public void insert(AuctionRecord record) throws SQLException {
        String sql = "INSERT INTO auction_records (piece_id, winner_id, final_price) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, record.getPieceId());

            if (record.getWinnerId() == null) {
                pstmt.setNull(2, Types.INTEGER);
            } else {
                pstmt.setInt(2, record.getWinnerId());
            }

            pstmt.setDouble(3, record.getFinalPrice());

            pstmt.executeUpdate();
        }
    }

    @Override
    public AuctionRecord getById(int id) throws SQLException {
        String sql = "SELECT * FROM auction_records WHERE id = ?";

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
    public List<AuctionRecord> getAll() throws SQLException {
        List<AuctionRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM auction_records ORDER BY auction_time DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                records.add(mapResultSetToEntity(rs));
            }
        }

        return records;
    }

    @Override
    public void update(AuctionRecord record) throws SQLException {
        String sql = "UPDATE auction_records SET piece_id = ?, winner_id = ?, final_price = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, record.getPieceId());

            if (record.getWinnerId() == null) {
                pstmt.setNull(2, Types.INTEGER);
            } else {
                pstmt.setInt(2, record.getWinnerId());
            }

            pstmt.setDouble(3, record.getFinalPrice());
            pstmt.setInt(4, record.getId());

            pstmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM auction_records WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    private AuctionRecord mapResultSetToEntity(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("auction_time");

        Integer winnerId = rs.getObject("winner_id") != null
                ? rs.getInt("winner_id")
                : null;

        return new AuctionRecord(
                rs.getInt("id"),
                rs.getInt("piece_id"),
                winnerId,
                rs.getDouble("final_price"),
                timestamp != null ? timestamp.toLocalDateTime() : null
        );
    }
}