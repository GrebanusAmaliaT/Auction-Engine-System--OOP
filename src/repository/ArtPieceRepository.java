package src.repository;

import src.config.DatabaseConfig;
import src.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArtPieceRepository implements GenericRepository<ArtPiece> {
    private static ArtPieceRepository instance;

    private ArtPieceRepository() {}

    public static ArtPieceRepository getInstance() {
        if (instance == null) instance = new ArtPieceRepository();
        return instance;
    }

    @Override
    public void insert(ArtPiece piece) throws SQLException {
        String sql = "INSERT INTO art_pieces (title, artist, current_price, type, technique, material, carats, is_sold) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, piece.getTitle());
            pstmt.setString(2, piece.getArtist());
            pstmt.setDouble(3, piece.getCurrentPrice());
            pstmt.setBoolean(8, false); 

            if (piece instanceof Painting) {
                pstmt.setString(4, "Painting");
                pstmt.setString(5, ((Painting) piece).getTechnique());
                pstmt.setNull(6, Types.VARCHAR); // material e null pt painting
                pstmt.setNull(7, Types.DOUBLE);  // carats e null pt painting
            } else if (piece instanceof Jewelry) {
                pstmt.setString(4, "Jewelry");
                pstmt.setNull(5, Types.VARCHAR); // technique e null pt jewelry
                pstmt.setString(6, ((Jewelry) piece).getMaterial());
                pstmt.setDouble(7, ((Jewelry) piece).getCarats());
            }
            
            pstmt.executeUpdate();
        }
    }

    @Override
    public ArtPiece getById(int id) throws SQLException {
        String sql = "SELECT * FROM art_pieces WHERE id = ?";
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
    public List<ArtPiece> getAll() throws SQLException {
        List<ArtPiece> pieces = new ArrayList<>();
        String sql = "SELECT * FROM art_pieces WHERE is_sold = false";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                pieces.add(mapResultSetToEntity(rs));
            }
        }
        return pieces;
    }

    public void markAsSold(ArtPiece piece, int ownerId) throws SQLException {
    String sql = "UPDATE art_pieces SET current_price = ?, is_sold = ?, owner_id = ? WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setDouble(1, piece.getCurrentPrice());
        pstmt.setBoolean(2, true);
        pstmt.setInt(3, ownerId);
        pstmt.setInt(4, piece.getId());

        pstmt.executeUpdate();
    }
    }

    @Override
    public void update(ArtPiece piece) throws SQLException {
    String sql = "UPDATE art_pieces SET title = ?, artist = ?, current_price = ?, type = ?, technique = ?, material = ?, carats = ? WHERE id = ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, piece.getTitle());
        pstmt.setString(2, piece.getArtist());
        pstmt.setDouble(3, piece.getCurrentPrice());

        if (piece instanceof Painting) {
            pstmt.setString(4, "Painting");
            pstmt.setString(5, ((Painting) piece).getTechnique());
            pstmt.setNull(6, Types.VARCHAR);
            pstmt.setNull(7, Types.DOUBLE);
        } else if (piece instanceof Jewelry) {
            pstmt.setString(4, "Jewelry");
            pstmt.setNull(5, Types.VARCHAR);
            pstmt.setString(6, ((Jewelry) piece).getMaterial());
            pstmt.setDouble(7, ((Jewelry) piece).getCarats());
        }

        pstmt.setInt(8, piece.getId());

        pstmt.executeUpdate();
    }
}

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM art_pieces WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    private ArtPiece mapResultSetToEntity(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("title");
        String artist = rs.getString("artist");
        double price = rs.getDouble("current_price");
        String type = rs.getString("type");

        if ("Painting".equals(type)) {
            return new Painting(id, title, artist, price, rs.getString("technique"));
        } else {
            return new Jewelry(id, title, artist, price, rs.getString("material"), rs.getDouble("carats"));
        }
    }
}