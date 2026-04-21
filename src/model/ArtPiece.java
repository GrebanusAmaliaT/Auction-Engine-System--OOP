package src.model;

public abstract class ArtPiece implements Comparable<ArtPiece> {
    private int id;
    private String title;
    private String artist;
    private double currentPrice;

    public ArtPiece(int id, String title, String artist, double currentPrice) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.currentPrice = currentPrice;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public double getCurrentPrice() { return currentPrice; }
    
    public void setCurrentPrice(double currentPrice) { 
        this.currentPrice = currentPrice; 
    }

    @Override
    public int compareTo(ArtPiece other) {
        return Double.compare(this.currentPrice, other.currentPrice);
    }
}