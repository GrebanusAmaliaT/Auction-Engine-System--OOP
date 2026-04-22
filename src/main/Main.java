package src.main;

import src.model.*;
import src.service.AuctionService;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        AuctionService auctionService = new AuctionService();
        Scanner mainScanner = new Scanner(System.in);

        auctionService.addArtPiece(new Painting(1, "Summer Garden", "Monet", 12000.0, "Oil"));
        auctionService.addArtPiece(new Jewelry(2, "Royal Tiara", "Unknown", 45000.0, "Gold/Diamonds", 24));
        auctionService.addArtPiece(new Painting(3, "Abstract Blue", "Pollock", 8000.0, "Acrylic"));
        auctionService.addArtPiece(new Jewelry(4, "Diamond Necklace", "Cartier", 150000.0, "Platinum", 15.5));

        Client user = new Client(777, "User_Boss", 500000.0);
        auctionService.registerClient(user);

        System.out.println("Welcome to the Elite Auction System, " + user.getName() + "!");

        String choice;
        do {
            ArtPiece randomPiece = auctionService.getRandomPiece();

            if (randomPiece != null) {
                auctionService.startInteractiveAuction(randomPiece.getId(), user, mainScanner);
            }

            System.out.print("\nWould you like to participate in another auction? (yes/no): ");
            choice = mainScanner.next().toLowerCase();

        } while (choice.equals("yes") || choice.equals("y"));

        System.out.println("\nThank you for participating! Final Catalog Status:");
        auctionService.displaySortedCatalog();
        
        mainScanner.close();
    }
}