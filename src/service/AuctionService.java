package src.service;

import src.model.*;
import src.repository.*;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class AuctionService {
    private final ClientRepository clientRepo = ClientRepository.getInstance();
    private final ArtPieceRepository artRepo = ArtPieceRepository.getInstance();
    private final BidRepository bidRepo = BidRepository.getInstance();
    private final UserInventoryRepository inventoryRepo = UserInventoryRepository.getInstance();
    private final AuditService audit = AuditService.getInstance();

    public void startInteractiveAuction(int pieceId, int userId, Scanner scanner) {
        try {
            Client user = clientRepo.getById(userId);
            ArtPiece piece = artRepo.getById(pieceId);
            List<Client> rivals = clientRepo.getAll().stream()
                    .filter(c -> c.isNpc())
                    .collect(Collectors.toList());

            if (piece == null || user == null) {
                System.out.println("Eroare: Piesa sau Clientul nu exista in DB!");
                return;
            }

            if (rivals.isEmpty()) {
                System.out.println("Avertisment: Nu exista rivali (NPCs) in baza de date!");
            }

            audit.logAction("START_AUCTION_PIECE_" + pieceId);
            Random random = new Random();

            System.out.println("\n===========================================");
            System.out.println("    WELCOME TO THE ELITE AUCTION HOUSE     ");
            System.out.println("===========================================");
            displayUserStats(user);
            System.out.println("BIDDING FOR: " + piece.getTitle() + " by " + piece.getArtist());
            System.out.println("Starting Price: " + piece.getCurrentPrice() + " EUR");
            System.out.println("===========================================");

            boolean active = true;
            Client currentWinner = null;
            int passesInARow = 0;

            while (active) {
                System.out.print("\nYour bid (current: " + piece.getCurrentPrice() + " EUR) or 0 to pass: ");
                
                double yourBid = 0;
                if (scanner.hasNextDouble()) {
                    yourBid = scanner.nextDouble();
                } else {
                    scanner.next(); 
                }

                if (yourBid > piece.getCurrentPrice() && yourBid <= user.getBudget()) {
                    piece.setCurrentPrice(yourBid);
                    currentWinner = user;
                    bidRepo.insert(new Bid(user.getId(), piece.getId(), yourBid));
                    audit.logAction("USER_BID_" + yourBid);
                    passesInARow = 0; 
                } else if (yourBid == 0) {
                    System.out.println("You decided to wait...");
                    passesInARow++;
                } else {
                    System.out.println("Invalid bid! Must be higher than current price and within budget.");
                    continue;
                }

                boolean rivalLicitatedInThisRound = false;

                Collections.shuffle(rivals); 
                for (Client rival : rivals) {
                    if (currentWinner == null || rival.getId() != currentWinner.getId()) {
                        
                        if (random.nextInt(100) < 70 && rival.getBudget() > piece.getCurrentPrice()) {
                            
                            double incrementPercent = 0.05 + (0.15 - 0.05) * random.nextDouble();
                            double rivalBid = piece.getCurrentPrice() * (1 + incrementPercent);
                            rivalBid = Math.round(rivalBid / 100.0) * 100.0; 

                            if (rivalBid <= rival.getBudget()) {
                                Thread.sleep(800); 
                                piece.setCurrentPrice(rivalBid);
                                currentWinner = rival;
                                bidRepo.insert(new Bid(rival.getId(), piece.getId(), rivalBid));
                                
                                System.out.println("[RIVAL] " + rival.getName() + " bids " + rivalBid + " EUR!");
                                audit.logAction("RIVAL_BID_" + rival.getName());
                                rivalLicitatedInThisRound = true;
                                passesInARow = 0;

                                break; 
                            }
                        }
                    }
                }

                if (!rivalLicitatedInThisRound && yourBid == 0) {
                    if (passesInARow >= 1) {
                        System.out.println("\nGoing once... Going twice... SOLD!");
                        active = false;
                    }
                } else if (!rivalLicitatedInThisRound) {
                    System.out.println("Nobody else wants to bid right now.");
                }
            }

            finalizeAuction(piece, currentWinner, user);

        } catch (Exception e) {
            System.err.println("Auction Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void finalizeAuction(ArtPiece piece, Client winner, Client user) throws SQLException {
        if (winner != null && winner.getId() == user.getId()) {
            System.out.println("\n🏆 CONGRATULATIONS! You won " + piece.getTitle() + "!");
            
            user.setBudget(user.getBudget() - piece.getCurrentPrice());
            clientRepo.update(user);
            artRepo.update(piece, user.getId()); 
            
            audit.logAction("AUCTION_WON_BY_USER");
        } else {
            String winnerName = (winner != null) ? winner.getName() : "Nobody";
            System.out.println("\n Auction ended. Winner: " + winnerName);
            audit.logAction("AUCTION_LOST_BY_USER");
        }
    }

    public void displayUserStats(Client user) throws SQLException {
        double assets = inventoryRepo.getTotalAssetsValue(user.getId());
        System.out.println(">>> PLAYER: " + user.getName());
        System.out.println(">>> CASH: " + user.getBudget() + " EUR");
        System.out.println(">>> ASSETS VALUE: " + assets + " EUR");
        System.out.println(">>> TOTAL NET WORTH: " + (user.getBudget() + assets) + " EUR");
        System.out.println("-------------------------------------------");
    }
}