package src.model;

public class Jewelry extends ArtPiece {
    private String material;
    private double carats;

    public Jewelry(int id, String title, String artist, double currentPrice, String material, double carats) {
        super(id, title, artist, currentPrice);
        this.material = material;
        this.carats = carats;
    }

    public String getMaterial() { 
        return material; 
    }

    public double getCarats() { 
        return carats; 
    }
}