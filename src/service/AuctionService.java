package src.service;

import src.model.*;
import src.repository.*;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class AuctionService {
    private final UserInventoryRepository inventoryRepo = UserInventoryRepository.getInstance();
    private final AuditService audit = AuditService.getInstance();
    private final ClientService clientService = ClientService.getInstance();
    private final ArtPieceService artPieceService = ArtPieceService.getInstance();
    private final BidService bidService = BidService.getInstance();
    private final AuctionRecordService auctionRecordService = AuctionRecordService.getInstance();

    public void startInteractiveAuction(int pieceId, int userId, Scanner scanner) {
    try {
        Client user = clientService.getById(userId);
        ArtPiece piece = artPieceService.getById(pieceId);

        List<Client> rivals = clientService.getAll()
                .stream()
                .filter(Client::isNpc)
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
        System.out.println("==  WELCOME TO THE ELITE AUCTION HOUSE   ==");
        System.out.println("==           PREMIUM MEMBERS ONLY        ==");
        System.out.println("===========================================");
        displayUserStats(user);
        System.out.println("BIDDING FOR: " + piece.getTitle() + " by " + piece.getArtist());
        System.out.println("Starting Price: " + piece.getCurrentPrice() + " EUR");
        System.out.println("===========================================");

        boolean active = true;
        boolean userWithdrawn = false;

        Client currentWinner = null;

        int quietRounds = 0;
        int totalRounds = 0;

        final int MAX_QUIET_ROUNDS = 2;
        final int MAX_TOTAL_ROUNDS = 15;

        while (active && totalRounds < MAX_TOTAL_ROUNDS) {
            totalRounds++;

            boolean bidPlacedThisRound = false;

            if (!userWithdrawn) {
                System.out.println("\nCurrent price: " + piece.getCurrentPrice() + " EUR");
                System.out.println("Your options:");
                System.out.println(" - enter a higher bid");
                System.out.println(" - 0 = pass this round");
                System.out.println(" - -1 = leave bidding room");
                System.out.print("Choice: ");

                String input = scanner.next();

                double yourBid;

                try {
                    yourBid = Double.parseDouble(input);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input! Please enter a number.");
                    continue;
                }

                if (yourBid == -1) {
                    userWithdrawn = true;
                    audit.logAction("USER_LEFT_BIDDING_ROOM");

                    System.out.println("\nYou left the bidding room. You are no longer participating in this auction.");

                    if (currentWinner != null && currentWinner.getId() == user.getId()) {
                        currentWinner = null;
                        System.out.println("Your previous bid was withdrawn.");
                    }
                } else if (yourBid == 0) {
                    System.out.println("You decided to wait this round...");
                } else if (yourBid > piece.getCurrentPrice() && yourBid <= user.getBudget()) {
                    piece.setCurrentPrice(yourBid);
                    currentWinner = user;

                    bidService.create(new Bid(user.getId(), piece.getId(), yourBid));
                    audit.logAction("USER_BID_" + yourBid);

                    System.out.println("You bid " + yourBid + " EUR!");

                    bidPlacedThisRound = true;
                } else {
                    System.out.println("Invalid bid! Must be higher than current price and within your budget.");
                    continue;
                }
            }

            Collections.shuffle(rivals);

            Iterator<Client> iterator = rivals.iterator();

            while (iterator.hasNext()) {
                Client rival = iterator.next();

                if (currentWinner != null && rival.getId() == currentWinner.getId()) {
                    continue;
                }

                if (rival.getBudget() <= piece.getCurrentPrice()) {
                    System.out.println("[RIVAL] " + rival.getName() + " leaves the auction. Price is too high.");
                    audit.logAction("RIVAL_LEFT_AUCTION_" + rival.getName());
                    iterator.remove();
                    continue;
                }

                double pricePressure = piece.getCurrentPrice() / rival.getBudget();

                int quitChance;

                if (pricePressure >= 0.80) {
                    quitChance = 70;
                } else if (pricePressure >= 0.60) {
                    quitChance = 50;
                } else {
                    quitChance = 30;
                }

                if (random.nextInt(100) < quitChance) {
                    System.out.println("[RIVAL] " + rival.getName() + " leaves the auction.");
                    audit.logAction("RIVAL_LEFT_AUCTION_" + rival.getName());
                    iterator.remove();
                    continue;
                }

                int bidChance = 35;

                if (random.nextInt(100) < bidChance) {
                    double incrementPercent = 0.05 + (0.12 - 0.05) * random.nextDouble();
                    double rivalBid = piece.getCurrentPrice() * (1 + incrementPercent);

                    rivalBid = Math.round(rivalBid / 100.0) * 100.0;

                    if (rivalBid <= rival.getBudget() && rivalBid > piece.getCurrentPrice()) {
                        Thread.sleep(800);

                        piece.setCurrentPrice(rivalBid);
                        currentWinner = rival;

                        bidService.create(new Bid(rival.getId(), piece.getId(), rivalBid));

                        System.out.println("[RIVAL] " + rival.getName() + " bids " + rivalBid + " EUR!");
                        audit.logAction("RIVAL_BID_" + rival.getName());

                        bidPlacedThisRound = true;

                        break;
                    } else {
                        System.out.println("[RIVAL] " + rival.getName() + " leaves the auction. Cannot afford next bid.");
                        audit.logAction("RIVAL_LEFT_AUCTION_" + rival.getName());
                        iterator.remove();
                    }
                }
            }

            if (bidPlacedThisRound) {
                quietRounds = 0;
            } else {
                quietRounds++;
            }

            if (rivals.isEmpty()) {
                System.out.println("\nNo rivals left in the bidding room.");
                active = false;
            }

            if (quietRounds >= MAX_QUIET_ROUNDS) {
                System.out.println("\nNo more bids are being placed.");
                active = false;
            }
        }

        if (totalRounds >= MAX_TOTAL_ROUNDS) {
            System.out.println("\nAuction time limit reached.");
            audit.logAction("AUCTION_TIME_LIMIT_REACHED");
        }

        System.out.println("\nGoing once... Going twice... SOLD!");
        finalizeAuction(piece, currentWinner, user);

    } catch (Exception e) {
        System.err.println("Auction Error: " + e.getMessage());
        e.printStackTrace();
    }
    }
    
    public void displayCatalogSortedByPrice() throws SQLException {
    TreeSet<ArtPiece> sortedPieces = new TreeSet<>(
            Comparator.comparingDouble(ArtPiece::getCurrentPrice)
                    .thenComparingInt(ArtPiece::getId)
    );

    sortedPieces.addAll(artPieceService.getAll());

    audit.logAction("DISPLAY_CATALOG_SORTED_BY_PRICE");

    System.out.println("\n--- CATALOG SORTED BY PRICE ---");

    for (ArtPiece piece : sortedPieces) {
        System.out.println("[" + piece.getId() + "] "
                + piece.getTitle()
                + " by " + piece.getArtist()
                + " - " + piece.getCurrentPrice() + " EUR");
        }
    }

    private void finalizeAuction(ArtPiece piece, Client winner, Client user) throws SQLException {
    if (winner != null) {
        winner.setBudget(winner.getBudget() - piece.getCurrentPrice());

        clientService.update(winner);
        artPieceService.markAsSold(piece, winner.getId());

        auctionRecordService.create(
                new AuctionRecord(piece.getId(), winner.getId(), piece.getCurrentPrice())
        );

        if (winner.getId() == user.getId()) {
            System.out.println("\nCONGRATULATIONS! You won " + piece.getTitle() + "!");
            audit.logAction("AUCTION_WON_BY_USER");
        } else {
            System.out.println("\nAuction ended. Winner: " + winner.getName());
            audit.logAction("AUCTION_LOST_BY_USER");
        }
    } else {
        auctionRecordService.create(
                new AuctionRecord(piece.getId(), null, piece.getCurrentPrice())
        );

        System.out.println("\nAuction ended. Nobody bought the piece.");
        audit.logAction("AUCTION_ENDED_WITHOUT_WINNER");
        }
    }

    public void displayUserStats(Client user) throws SQLException {
        audit.logAction("DISPLAY_USER_STATS");

        double assets = inventoryRepo.getTotalAssetsValue(user.getId());
        System.out.println(">>> PLAYER: " + user.getName());
        System.out.println(">>> CASH: " + user.getBudget() + " EUR");
        System.out.println(">>> ASSETS VALUE: " + assets + " EUR");
        System.out.println(">>> TOTAL NET WORTH: " + (user.getBudget() + assets) + " EUR");
        System.out.println("-------------------------------------------");
    }
}