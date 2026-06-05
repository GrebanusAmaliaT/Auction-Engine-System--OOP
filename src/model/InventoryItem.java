package src.model;

import java.time.LocalDateTime;

public class InventoryItem {
    private int id;
    private int clientId;
    private int artPieceId;
    private double purchasePrice;
    private LocalDateTime acquiredAt;

    public InventoryItem(int id, int clientId, int artPieceId, double purchasePrice, LocalDateTime acquiredAt) {
        this.id = id;
        this.clientId = clientId;
        this.artPieceId = artPieceId;
        this.purchasePrice = purchasePrice;
        this.acquiredAt = acquiredAt;
    }

    public InventoryItem(int clientId, int artPieceId, double purchasePrice) {
        this.clientId = clientId;
        this.artPieceId = artPieceId;
        this.purchasePrice = purchasePrice;
        this.acquiredAt = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public int getClientId() {
        return clientId;
    }

    public int getArtPieceId() {
        return artPieceId;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public LocalDateTime getAcquiredAt() {
        return acquiredAt;
    }
}