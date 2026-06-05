package src.model;

public class AuctionHouse {
    private int id;
    private String name;
    private String location;
    private double buyerPremiumRate;

    public AuctionHouse(int id, String name, String location, double buyerPremiumRate) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.buyerPremiumRate = buyerPremiumRate;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public double getBuyerPremiumRate() {
        return buyerPremiumRate;
    }

    public double calculateBuyerPremium(double hammerPrice) {
        return hammerPrice * buyerPremiumRate;
    }

    public double calculateTotalPrice(double hammerPrice) {
        return hammerPrice + calculateBuyerPremium(hammerPrice);
    }

    public String getDisplayInfo() {
        return name + " - " + location + " | Buyer premium: " + (buyerPremiumRate * 100) + "%";
    }
}