package src.model;

import java.time.LocalDateTime;

public class AuctionRecord {
    private int id;
    private int pieceId;
    private Integer winnerId;
    private double finalPrice;
    private LocalDateTime timestamp;

    public AuctionRecord(int id, int pieceId, Integer winnerId, double finalPrice, LocalDateTime timestamp) {
        this.id = id;
        this.pieceId = pieceId;
        this.winnerId = winnerId;
        this.finalPrice = finalPrice;
        this.timestamp = timestamp;
    }

    public AuctionRecord(int pieceId, Integer winnerId, double finalPrice) {
        this.pieceId = pieceId;
        this.winnerId = winnerId;
        this.finalPrice = finalPrice;
        this.timestamp = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public int getPieceId() {
        return pieceId;
    }

    public Integer getWinnerId() {
        return winnerId;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}