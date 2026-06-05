package src.repository;

import src.config.DatabaseConfig;
import src.model.Client;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientRepository implements GenericRepository<Client> {
    private static ClientRepository instance;

    private ClientRepository() {}

    public static ClientRepository getInstance() {
        if (instance == null) instance = new ClientRepository();
        return instance;
    }

    @Override
    public void insert(Client client) throws SQLException {
        String sql = "INSERT INTO clients (name, budget, is_npc) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, client.getName());
            pstmt.setDouble(2, client.getBudget());
            pstmt.setBoolean(3, client.isNpc()); 
            pstmt.executeUpdate();
        }
    }

    @Override
    public Client getById(int id) throws SQLException {
        String sql = "SELECT * FROM clients WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Client client = new Client(rs.getInt("id"), rs.getString("name"), rs.getDouble("budget"));
                    client.setNpc(rs.getBoolean("is_npc"));
                    return client;
                }
            }
        }
        return null;
    }

    @Override
    public List<Client> getAll() throws SQLException {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM clients";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Client client = new Client(rs.getInt("id"), rs.getString("name"), rs.getDouble("budget"));
                client.setNpc(rs.getBoolean("is_npc"));
                clients.add(client);
            }
        }
        return clients;
    }

    @Override
    public void update(Client client) throws SQLException {
        String sql = "UPDATE clients SET budget = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, client.getBudget());
            pstmt.setInt(2, client.getId());
            pstmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM clients WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
}