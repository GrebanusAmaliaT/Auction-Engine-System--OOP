package src.main;

import src.model.*;
import src.repository.*;
import src.service.AuctionService;
import java.sql.SQLException;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        AuctionService auctionService = new AuctionService();
        ClientRepository clientRepo = ClientRepository.getInstance();
        ArtPieceRepository artRepo = ArtPieceRepository.getInstance();

        try {
            Client user = clientRepo.getById(1);
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
                System.out.println("3. Exit");
                System.out.print("Choice: ");
                
                int choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        List<ArtPiece> available = artRepo.getAll();
                        if (!available.isEmpty()) {
                            ArtPiece randomPiece = available.get(new Random().nextInt(available.size()));
                            auctionService.startInteractiveAuction(randomPiece.getId(), user.getId(), scanner);
                            user = clientRepo.getById(user.getId());
                        } else {
                            System.out.println("No more pieces available for auction!");
                        }
                        break;
                    case 2:
                        artRepo.getAll().forEach(p -> 
                            System.out.println("[" + p.getId() + "] " + p.getTitle() + " - " + p.getCurrentPrice() + " EUR"));
                        break;
                    case 3:
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