package src.service;

import src.model.*;
import java.util.*;

public class AuctionService {

    private List<ArtPiece> inventory = new ArrayList<>();
    private TreeSet<ArtPiece> sortedCatalog = new TreeSet<>();
    private List<Client> clients = new ArrayList<>();
    private List<Bid> bidHistory = new ArrayList<>();

    public void addArtPiece(ArtPiece piece) {
        inventory.add(piece);
        sortedCatalog.add(piece);
    }

    public void registerClient(Client client) {
        clients.add(client);
    }

    public void displaySortedCatalog() {
        System.out.println("\n--- Current Catalog (Sorted by Price) ---");
        if (sortedCatalog.isEmpty()) {
            System.out.println("The catalog is empty.");
        } else {
            for (ArtPiece p : sortedCatalog) {
                System.out.println("[" + p.getId() + "] " + p.getTitle() + " - " + p.getCurrentPrice() + " EUR (" + p.getArtist() + ")");
            }
        }
    }

    public void startInteractiveAuction(int pieceId, Client user) {
        Random random = new Random();

        ArtPiece piece = inventory.stream()
                .filter(p -> p.getId() == pieceId)
                .findFirst()
                .orElse(null);

        if (piece == null) {
            System.out.println("Art piece with ID " + pieceId + " was not found!");
            return;
        }

        // Try-with-resources to prevent Scanner resource leaks
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("\n===========================================");
            System.out.println("THE AUCTION HAS STARTED: " + piece.getTitle());
            System.out.println("Artist: " + piece.getArtist());
            System.out.println("Current Starting Price: " + piece.getCurrentPrice() + " EUR");
            System.out.println("===========================================");

            boolean active = true;
            while (active) {
                System.out.print("\nYour bid (Enter a sum higher than " + piece.getCurrentPrice() + " or 0 to withdraw): ");
                
                // Input validation
                if (!scanner.hasNextDouble()) {
                    System.out.println("Please enter a valid numeric value!");
                    scanner.next(); 
                    continue;
                }

                double yourBid = scanner.nextDouble();

                // Check if user wants to withdraw or bid is too low
                if (yourBid <= piece.getCurrentPrice()) {
                    System.out.println("\nYou have withdrawn from the auction. The Bot wins the piece!");
                    active = false;
                    break;
                }

                piece.setCurrentPrice(yourBid);
                refreshSortedCatalog(piece);
                bidHistory.add(new Bid(user.getId(), piece.getId(), yourBid));

                System.out.println("Success! You bid " + yourBid + " EUR. Waiting for the bot...");
                
                try { 
                    Thread.sleep(1200); 
                } catch (InterruptedException e) { 
                    Thread.currentThread().interrupt(); 
                }

                if (random.nextInt(100) > 70) {
                    System.out.println("\n[BOT]: That's too rich for my blood. I'm out!");
                    System.out.println("CONGRATULATIONS! You won the piece: " + piece.getTitle());
                    active = false;
                } else {
                    double percentageExtra = 0.01 + (0.07 - 0.01) * random.nextDouble();
                    double extraSum = piece.getCurrentPrice() * percentageExtra;
                    
                    double botNewPrice = piece.getCurrentPrice() + extraSum;
                    
                    botNewPrice = Math.round(botNewPrice / 100.0) * 100.0;

                    piece.setCurrentPrice(botNewPrice);
                    refreshSortedCatalog(piece);
                    
                    System.out.println("\n[BOT]: I placed a bid of " + botNewPrice + " EUR!");
                }
            }
        } 
        
        System.out.println("\nAuction finalized.");
    }

    private void refreshSortedCatalog(ArtPiece p) {
        sortedCatalog.remove(p);
        sortedCatalog.add(p);
    }
}