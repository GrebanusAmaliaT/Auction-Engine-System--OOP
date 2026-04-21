package src.main;

import src.model.*;
import src.service.AuctionService;

public class Main {
    public static void main(String[] args) {

        AuctionService auctionService = new AuctionService();

        auctionService.addArtPiece(new Painting(1, "Summer Garden", "Monet", 12000.0, "Oil"));
        
        auctionService.addArtPiece(new Jewelry(2, "Royal Tiara", "Unknown", 45000.0, "Gold/Diamonds", 24));
        
        auctionService.addArtPiece(new Painting(3, "Abstract Blue", "Pollock", 8000.0, "Acrylic"));

        Client user = new Client(777, "User_Boss", 500000.0);
        
        auctionService.registerClient(user);

        auctionService.displaySortedCatalog();

        auctionService.startInteractiveAuction(1, user);

        auctionService.displaySortedCatalog();
    }
}