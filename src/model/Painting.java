package src.model;

public class Painting extends ArtPiece {
    private String technique; 

    public Painting(int id, String title, String artist, double currentPrice, String technique) {
        super(id, title, artist, currentPrice);
        this.technique = technique;
    }

    public String getTechnique() { 
        return technique; 
    }
}