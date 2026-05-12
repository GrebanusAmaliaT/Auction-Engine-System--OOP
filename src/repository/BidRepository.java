package src.repository;

import src.model.Bid;
import src.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BidRepository implements GenericRepository<Bid> {
    private static BidRepository instance;

    private BidRepository() {}

    public static BidRepository getInstance() {
        if (instance == null) instance = new BidRepository();
        return instance;
    }

    @Override
    public void insert(Bid bid) throws SQLException {
        String sql = "INSERT INTO bids (client_id, piece_id, amount) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bid.getClientId());
            pstmt.setInt(2, bid.getPieceId());
            pstmt.setDouble(3, bid.getValue());
            pstmt.executeUpdate();
        }
    }

    @Override
    public Bid getById(int id) throws SQLException {
        String sql = "SELECT * FROM bids WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Bid(rs.getInt("client_id"), rs.getInt("piece_id"), rs.getDouble("amount"));
            }
        }
        return null;
    }

    @Override
    public List<Bid> getAll() throws SQLException {
        List<Bid> bids = new ArrayList<>();
        String sql = "SELECT * FROM bids ORDER BY bid_time DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                bids.add(new Bid(rs.getInt("client_id"), rs.getInt("piece_id"), rs.getDouble("amount")));
            }
        }
        return bids;
    }

    @Override
    public void update(Bid bid) throws SQLException {
        String sql = "UPDATE bids SET amount = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, bid.getValue());
            pstmt.setInt(2, bid.getClientId()); 
            
            pstmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM bids WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
}