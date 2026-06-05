package src.service;

import src.model.*;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class AuctionService {
    private final AuditService audit = AuditService.getInstance();
    private final ClientService clientService = ClientService.getInstance();
    private final ArtPieceService artPieceService = ArtPieceService.getInstance();
    private final BidService bidService = BidService.getInstance();
    private final AuctionRecordService auctionRecordService = AuctionRecordService.getInstance();
    private final InventoryItemService inventoryItemService = InventoryItemService.getInstance();

private final AuctionHouse auctionHouse =
        new AuctionHouse(1, "Elite Auction House", "Bucharest", 0.10);

    private enum RivalAction {
        BID,
        PASS,
        LEAVE
    }

    public AuctionHouse getAuctionHouse() {
    return auctionHouse;
}

    private static class RivalDecision {
        private final Client rival;
        private final RivalAction action;
        private final double bidAmount;
        private final String message;

        public RivalDecision(Client rival, RivalAction action, double bidAmount, String message) {
            this.rival = rival;
            this.action = action;
            this.bidAmount = bidAmount;
            this.message = message;
        }

        public Client getRival() {
            return rival;
        }

        public RivalAction getAction() {
            return action;
        }

        public double getBidAmount() {
            return bidAmount;
        }

        public String getMessage() {
            return message;
        }
    }

    public void startInteractiveAuction(int pieceId, int userId, Scanner scanner) {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        try {
            Client user = clientService.getById(userId);
            ArtPiece piece = artPieceService.getById(pieceId);

            if (piece == null || user == null) {
                System.out.println("Eroare: Piesa sau Clientul nu exista in DB!");
                return;
            }

            List<Client> rivals = clientService.getAll()
                    .stream()
                    .filter(Client::isNpc)
                    .collect(Collectors.toList());

            if (rivals.isEmpty()) {
                System.out.println("Avertisment: Nu exista rivali NPC in baza de date!");
            }

            audit.logAction("START_AUCTION_PIECE_" + pieceId);

            System.out.println("\n===========================================");
            System.out.println("    WELCOME TO THE ELITE AUCTION HOUSE     ");
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
            final int MAX_TOTAL_ROUNDS = 10;

            while (active && totalRounds < MAX_TOTAL_ROUNDS) {
                totalRounds++;

                boolean bidPlacedThisRound = false;

                System.out.println("\n------------- ROUND " + totalRounds + " -------------");
                System.out.println("Current price: " + piece.getCurrentPrice() + " EUR");

                if (!userWithdrawn) {
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
                            System.out.println("Your previous winning position was withdrawn.");
                        }

                    } else if (yourBid == 0) {
                        System.out.println("You passed this round.");

                    } else if (yourBid > piece.getCurrentPrice()
        && auctionHouse.calculateTotalPrice(yourBid) <= user.getBudget()) {
                        piece.setCurrentPrice(yourBid);
                        currentWinner = user;

                        bidService.create(new Bid(user.getId(), piece.getId(), yourBid));
                        audit.logAction("USER_BID_" + yourBid);

                        System.out.println("You bid " + yourBid + " EUR!");
                        bidPlacedThisRound = true;

                    } else {
                        System.out.println("Invalid bid! It must be higher than the current price and within your budget.");
                        continue;
                    }
                } else {
                    System.out.println("You are outside the bidding room. Rivals continue bidding...");
                }

                List<RivalDecision> decisions = getRivalDecisionsMultithreaded(
                        executor,
                        rivals,
                        currentWinner,
                        piece.getCurrentPrice()
                );

                List<Client> rivalsToRemove = new ArrayList<>();
                RivalDecision bestBidDecision = null;

                for (RivalDecision decision : decisions) {
                    System.out.println(decision.getMessage());

                    if (decision.getAction() == RivalAction.LEAVE) {
                        rivalsToRemove.add(decision.getRival());
                        audit.logAction("RIVAL_LEFT_AUCTION_" + decision.getRival().getName());
                    }

                    if (decision.getAction() == RivalAction.BID) {
                        if (bestBidDecision == null ||
                                decision.getBidAmount() > bestBidDecision.getBidAmount()) {
                            bestBidDecision = decision;
                        }
                    }
                }

                rivals.removeAll(rivalsToRemove);

                if (bestBidDecision != null && bestBidDecision.getBidAmount() > piece.getCurrentPrice()) {
                    Client rival = bestBidDecision.getRival();
                    double rivalBid = bestBidDecision.getBidAmount();

                    piece.setCurrentPrice(rivalBid);
                    currentWinner = rival;

                    bidService.create(new Bid(rival.getId(), piece.getId(), rivalBid));
                    audit.logAction("RIVAL_BID_" + rival.getName());

                    System.out.println("[ACCEPTED BID] " + rival.getName() + " is now winning with "
                            + rivalBid + " EUR!");

                    bidPlacedThisRound = true;
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
        } finally {
            executor.shutdown();
        }
    }

    private List<RivalDecision> getRivalDecisionsMultithreaded(
            ExecutorService executor,
            List<Client> rivals,
            Client currentWinner,
            double currentPrice
    ) throws InterruptedException {

        List<Callable<RivalDecision>> tasks = new ArrayList<>();

        for (Client rival : rivals) {
            tasks.add(() -> decideRivalAction(rival, currentWinner, currentPrice));
        }

        List<Future<RivalDecision>> futures = executor.invokeAll(tasks);

        List<RivalDecision> decisions = new ArrayList<>();

        for (Future<RivalDecision> future : futures) {
            try {
                decisions.add(future.get());
            } catch (ExecutionException e) {
                System.err.println("Rival decision error: " + e.getMessage());
            }
        }

        return decisions;
    }

    private RivalDecision decideRivalAction(Client rival, Client currentWinner, double currentPrice) {
        Random random = new Random();

        try {
            Thread.sleep(300 + random.nextInt(700));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (currentWinner != null && currentWinner.getId() == rival.getId()) {
            return new RivalDecision(
                    rival,
                    RivalAction.PASS,
                    0,
                    "[THREAD-" + Thread.currentThread().getId() + "] " + rival.getName()
                            + " is already winning and waits."
            );
        }

        if (rival.getBudget() <= currentPrice) {
            return new RivalDecision(
                    rival,
                    RivalAction.LEAVE,
                    0,
                    "[THREAD-" + Thread.currentThread().getId() + "] " + rival.getName()
                            + " leaves. Price is too high."
            );
        }

        double pricePressure = currentPrice / rival.getBudget();

        int leaveChance;
        if (pricePressure >= 0.80) {
            leaveChance = 75;
        } else if (pricePressure >= 0.60) {
            leaveChance = 55;
        } else {
            leaveChance = 35;
        }

        if (random.nextInt(100) < leaveChance) {
            return new RivalDecision(
                    rival,
                    RivalAction.LEAVE,
                    0,
                    "[THREAD-" + Thread.currentThread().getId() + "] " + rival.getName()
                            + " decides to leave the auction."
            );
        }

        int bidChance = 35;

        if (random.nextInt(100) < bidChance) {
            double incrementPercent = 0.05 + (0.12 - 0.05) * random.nextDouble();
            double rivalBid = currentPrice * (1 + incrementPercent);
            rivalBid = Math.round(rivalBid / 100.0) * 100.0;

            if (auctionHouse.calculateTotalPrice(rivalBid) <= rival.getBudget() && rivalBid > currentPrice) {
                return new RivalDecision(
                        rival,
                        RivalAction.BID,
                        rivalBid,
                        "[THREAD-" + Thread.currentThread().getId() + "] " + rival.getName()
                                + " wants to bid " + rivalBid + " EUR."
                );
            }
        }

        return new RivalDecision(
                rival,
                RivalAction.PASS,
                0,
                "[THREAD-" + Thread.currentThread().getId() + "] " + rival.getName()
                        + " passes this round."
        );
    }

    public void displayUserInventory(int userId) throws SQLException {
        List<InventoryItem> inventory = inventoryItemService.getByClientId(userId);

        audit.logAction("DISPLAY_USER_INVENTORY");

        System.out.println("\n--- MY INVENTORY ---");

        if (inventory.isEmpty()) {
            System.out.println("You do not own any art pieces yet.");
            return;
        }

        for (InventoryItem item : inventory) {
            ArtPiece piece = artPieceService.getById(item.getArtPieceId());

            if (piece != null) {
                System.out.println("Inventory item #" + item.getId());
                System.out.println("Piece: " + piece.getTitle() + " by " + piece.getArtist());
                System.out.println("Current value: " + piece.getCurrentPrice() + " EUR");
                System.out.println("Purchase price paid: " + item.getPurchasePrice() + " EUR");
                System.out.println("Acquired at: " + item.getAcquiredAt());
                System.out.println("-------------------------------------------");
            }
        }
    }


    private void finalizeAuction(ArtPiece piece, Client winner, Client user) throws SQLException {
    if (winner != null) {
        double hammerPrice = piece.getCurrentPrice();
        double totalPayment = auctionHouse.calculateTotalPrice(hammerPrice);

        winner.setBudget(winner.getBudget() - totalPayment);

        clientService.update(winner);
        artPieceService.markAsSold(piece, winner.getId());

        inventoryItemService.create(
                new InventoryItem(winner.getId(), piece.getId(), totalPayment)
        );

        auctionRecordService.create(
                new AuctionRecord(piece.getId(), winner.getId(), hammerPrice)
        );

        System.out.println("\nAuction House: " + auctionHouse.getName());
        System.out.println("Hammer price: " + hammerPrice + " EUR");
        System.out.println("Buyer premium: " + auctionHouse.calculateBuyerPremium(hammerPrice) + " EUR");
        System.out.println("Total payment: " + totalPayment + " EUR");

        if (winner.getId() == user.getId()) {
            System.out.println("\nCONGRATULATIONS! You won " + piece.getTitle() + "!");
            System.out.println("The item was added to your inventory.");
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

        double assets = inventoryItemService.getTotalValueForClient(user.getId());

        System.out.println(">>> PLAYER: " + user.getName());
        System.out.println(">>> CASH: " + user.getBudget() + " EUR");
        System.out.println(">>> ASSETS VALUE: " + assets + " EUR");
        System.out.println(">>> TOTAL NET WORTH: " + (user.getBudget() + assets) + " EUR");
        System.out.println("-------------------------------------------");
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
}