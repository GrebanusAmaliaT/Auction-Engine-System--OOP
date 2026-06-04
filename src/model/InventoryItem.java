package src.model;

import java.time.LocalDateTime;

public class InventoryItem {
    private int id;
    private int clientId;
    private int artPieceId;
    private LocalDateTime acquiredAt;

    public InventoryItem(int id, int clientId, int artPieceId, LocalDateTime acquiredAt) {
        this.id = id;
        this.clientId = clientId;
        this.artPieceId = artPieceId;
        this.acquiredAt = acquiredAt;
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

    public LocalDateTime getAcquiredAt() {
        return acquiredAt;
    }
}