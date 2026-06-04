package src.main;

import src.model.*;
import src.service.ArtPieceService;
import src.service.AuctionService;
import src.service.ClientService;

import java.sql.SQLException;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        AuctionService auctionService = new AuctionService();
        ClientService clientService = ClientService.getInstance();
        ArtPieceService artPieceService = ArtPieceService.getInstance();

        try {
            Client user = clientService.getById(1);
            if (user == null) {
                System.out.println("Eroare: Ruleaza scriptul SQL pentru a crea userul!");
                return;
            }

            boolean running = true;
            while (running) {
                System.out.println("\n--- ART TYCOON DASHBOARD ---");
                auctionService.displayUserStats(user); 
                
                System.out.println("1. Start Random Auction");
                System.out.println("2. View Available Catalog");
                System.out.println("3. View Catalog Sorted By Price");
                System.out.println("4. Exit");
                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        List<ArtPiece> available = artPieceService.getAll();
                        if (!available.isEmpty()) {
                            ArtPiece randomPiece = available.get(new Random().nextInt(available.size()));
                            auctionService.startInteractiveAuction(randomPiece.getId(), user.getId(), scanner);
                            user = clientService.getById(user.getId());
                        } else {
                            System.out.println("No more pieces available for auction!");
                        }
                        break;
                    case 2:
                        artPieceService.getAll().forEach(p ->
                                System.out.println("[" + p.getId() + "] "
                                        + p.getTitle()
                                        + " - " + p.getCurrentPrice() + " EUR"));
                        break;
                    case 3:
                        auctionService.displayCatalogSortedByPrice();
                        break;

                    case 4:
                        running = false;
                        break;
                }
            }
        } catch (SQLException e) {
            System.err.println("Database Error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}