package src.model;

import java.time.LocalDateTime;

public class Bid {
    private int id;
    private int clientId;
    private int pieceId;
    private double value;
    private LocalDateTime timestamp;

    public Bid(int id, int clientId, int pieceId, double value, LocalDateTime timestamp) {
        this.id = id;
        this.clientId = clientId;
        this.pieceId = pieceId;
        this.value = value;
        this.timestamp = timestamp;
    }

    public Bid(int clientId, int pieceId, double value) {
        this.clientId = clientId;
        this.pieceId = pieceId;
        this.value = value;
        this.timestamp = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public int getClientId() {
        return clientId;
    }

    public int getPieceId() {
        return pieceId;
    }

    public double getValue() {
        return value;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}