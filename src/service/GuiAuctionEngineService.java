package src.service;

import src.model.*;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class GuiAuctionEngineService {
    private final AuditService audit = AuditService.getInstance();

    private final ClientService clientService = ClientService.getInstance();
    private final ArtPieceService artPieceService = ArtPieceService.getInstance();
    private final BidService bidService = BidService.getInstance();
    private final AuctionRecordService auctionRecordService = AuctionRecordService.getInstance();
    private final InventoryItemService inventoryItemService = InventoryItemService.getInstance();

    private final AuctionHouse auctionHouse =
            new AuctionHouse(1, "Elite Auction House", "Bucharest", 0.10);

    private AuctionState currentAuction;

    private enum RivalAction {
        BID,
        PASS,
        LEAVE
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

    private static class AuctionState {
        private ArtPiece piece;
        private List<Client> rivals;
        private Client currentWinner;
        private boolean active;
        private boolean userWithdrawn;
        private int quietRounds;
        private int totalRounds;

        public AuctionState(ArtPiece piece, List<Client> rivals) {
            this.piece = piece;
            this.rivals = rivals;
            this.currentWinner = null;
            this.active = true;
            this.userWithdrawn = false;
            this.quietRounds = 0;
            this.totalRounds = 0;
        }
    }

    public static class AuctionUpdate {
        private final List<String> messages;
        private final boolean active;
        private final String currentPieceTitle;
        private final double currentPrice;
        private final String currentWinnerName;

        public AuctionUpdate(
                List<String> messages,
                boolean active,
                String currentPieceTitle,
                double currentPrice,
                String currentWinnerName
        ) {
            this.messages = messages;
            this.active = active;
            this.currentPieceTitle = currentPieceTitle;
            this.currentPrice = currentPrice;
            this.currentWinnerName = currentWinnerName;
        }

        public List<String> getMessages() {
            return messages;
        }

        public boolean isActive() {
            return active;
        }

        public String getCurrentPieceTitle() {
            return currentPieceTitle;
        }

        public double getCurrentPrice() {
            return currentPrice;
        }

        public String getCurrentWinnerName() {
            return currentWinnerName;
        }
    }

    public static class DashboardData {
        private final String playerName;
        private final double cash;
        private final double assetsValue;
        private final double totalNetWorth;

        public DashboardData(String playerName, double cash, double assetsValue) {
            this.playerName = playerName;
            this.cash = cash;
            this.assetsValue = assetsValue;
            this.totalNetWorth = cash + assetsValue;
        }

        public String getPlayerName() {
            return playerName;
        }

        public double getCash() {
            return cash;
        }

        public double getAssetsValue() {
            return assetsValue;
        }

        public double getTotalNetWorth() {
            return totalNetWorth;
        }
    }

    public AuctionHouse getAuctionHouse() {
        return auctionHouse;
    }

    public boolean hasActiveAuction() {
        return currentAuction != null && currentAuction.active;
    }

    public Client getUser(int userId) throws SQLException {
        return clientService.getById(userId);
    }

    public DashboardData getDashboardData(int userId) throws SQLException {
        Client user = clientService.getById(userId);

        if (user == null) {
            return null;
        }

        double assets = inventoryItemService.getTotalValueForClient(userId);
        audit.logAction("GUI_DISPLAY_DASHBOARD");

        return new DashboardData(user.getName(), user.getBudget(), assets);
    }

    public List<String> getCatalogLines() throws SQLException {
        List<String> lines = new ArrayList<>();
        List<ArtPiece> pieces = artPieceService.getAll();

        audit.logAction("GUI_VIEW_CATALOG");

        if (pieces.isEmpty()) {
            lines.add("No available art pieces.");
            return lines;
        }

        for (ArtPiece piece : pieces) {
            lines.add("[" + piece.getId() + "] "
                    + piece.getTitle()
                    + " by " + piece.getArtist()
                    + " - " + piece.getCurrentPrice() + " EUR");
        }

        return lines;
    }

    public List<String> getSortedCatalogLines() throws SQLException {
        List<String> lines = new ArrayList<>();

        TreeSet<ArtPiece> sortedPieces = new TreeSet<>(
                Comparator.comparingDouble(ArtPiece::getCurrentPrice)
                        .thenComparingInt(ArtPiece::getId)
        );

        sortedPieces.addAll(artPieceService.getAll());

        audit.logAction("GUI_VIEW_SORTED_CATALOG");

        if (sortedPieces.isEmpty()) {
            lines.add("No available art pieces.");
            return lines;
        }

        for (ArtPiece piece : sortedPieces) {
            lines.add("[" + piece.getId() + "] "
                    + piece.getTitle()
                    + " by " + piece.getArtist()
                    + " - " + piece.getCurrentPrice() + " EUR");
        }

        return lines;
    }

    public List<String> getInventoryLines(int userId) throws SQLException {
        List<String> lines = new ArrayList<>();
        List<InventoryItem> inventory = inventoryItemService.getByClientId(userId);

        audit.logAction("GUI_VIEW_INVENTORY");

        if (inventory.isEmpty()) {
            lines.add("You do not own any art pieces yet.");
            return lines;
        }

        for (InventoryItem item : inventory) {
            ArtPiece piece = artPieceService.getById(item.getArtPieceId());

            if (piece != null) {
                lines.add("Inventory item #" + item.getId());
                lines.add("Piece: " + piece.getTitle() + " by " + piece.getArtist());
                lines.add("Current value: " + piece.getCurrentPrice() + " EUR");
                lines.add("Purchase price paid: " + item.getPurchasePrice() + " EUR");
                lines.add("Acquired at: " + item.getAcquiredAt());
                lines.add("-------------------------------------------");
            }
        }

        return lines;
    }

    public AuctionUpdate startRandomAuction(int userId) throws SQLException {
        List<String> messages = new ArrayList<>();

        Client user = clientService.getById(userId);

        if (user == null) {
            messages.add("User not found. Run the SQL script first.");
            return buildUpdate(messages, false);
        }

        List<ArtPiece> availablePieces = artPieceService.getAll();

        if (availablePieces.isEmpty()) {
            messages.add("No more pieces available for auction.");
            return buildUpdate(messages, false);
        }

        ArtPiece piece = availablePieces.get(new Random().nextInt(availablePieces.size()));

        List<Client> rivals = clientService.getAll()
                .stream()
                .filter(Client::isNpc)
                .collect(Collectors.toList());

        currentAuction = new AuctionState(piece, rivals);

        audit.logAction("GUI_START_AUCTION_PIECE_" + piece.getId());

        messages.add("===========================================");
        messages.add("WELCOME TO " + auctionHouse.getName().toUpperCase());
        messages.add("Location: " + auctionHouse.getLocation());
        messages.add("Buyer premium: " + (auctionHouse.getBuyerPremiumRate() * 100) + "%");
        messages.add("===========================================");
        messages.add("BIDDING FOR: " + piece.getTitle() + " by " + piece.getArtist());
        messages.add("Starting price: " + piece.getCurrentPrice() + " EUR");
        messages.add("Rivals in room: " + rivals.size());
        messages.add("Enter a bid, press Pass, or Leave Room.");

        return buildUpdate(messages, true);
    }

    public AuctionUpdate placeUserBid(int userId, double bidAmount) throws Exception {
        return processRound(userId, bidAmount, false, false);
    }

    public AuctionUpdate passRound(int userId) throws Exception {
        return processRound(userId, 0, true, false);
    }

    public AuctionUpdate leaveRoom(int userId) throws Exception {
        return processRound(userId, -1, false, true);
    }

    private AuctionUpdate processRound(
            int userId,
            double userBid,
            boolean pass,
            boolean leave
    ) throws Exception {
        List<String> messages = new ArrayList<>();

        if (currentAuction == null || !currentAuction.active) {
            messages.add("No active auction. Start a new auction first.");
            return buildUpdate(messages, false);
        }

        ExecutorService executor = Executors.newFixedThreadPool(4);

        try {
            Client user = clientService.getById(userId);

            if (user == null) {
                messages.add("User not found.");
                return buildUpdate(messages, false);
            }

            currentAuction.totalRounds++;

            boolean bidPlacedThisRound = false;

            messages.add("");
            messages.add("------------- ROUND " + currentAuction.totalRounds + " -------------");
            messages.add("Current price: " + currentAuction.piece.getCurrentPrice() + " EUR");

            if (!currentAuction.userWithdrawn) {
                if (leave) {
                    currentAuction.userWithdrawn = true;
                    audit.logAction("GUI_USER_LEFT_BIDDING_ROOM");

                    messages.add("You left the bidding room. You are no longer participating.");

                    if (currentAuction.currentWinner != null
                            && currentAuction.currentWinner.getId() == user.getId()) {
                        currentAuction.currentWinner = null;
                        messages.add("Your previous winning position was withdrawn.");
                    }

                } else if (pass) {
                    messages.add("You passed this round.");
                    audit.logAction("GUI_USER_PASS_ROUND");

                } else {
                    double totalPayment = auctionHouse.calculateTotalPrice(userBid);

                    if (userBid <= currentAuction.piece.getCurrentPrice()) {
                        messages.add("Invalid bid. It must be higher than the current price.");
                        return buildUpdate(messages, true);
                    }

                    if (totalPayment > user.getBudget()) {
                        messages.add("Invalid bid. You do not have enough budget.");
                        messages.add("Bid: " + userBid + " EUR");
                        messages.add("Total with buyer premium: " + totalPayment + " EUR");
                        messages.add("Your budget: " + user.getBudget() + " EUR");
                        return buildUpdate(messages, true);
                    }

                    currentAuction.piece.setCurrentPrice(userBid);
                    currentAuction.currentWinner = user;

                    bidService.create(new Bid(user.getId(), currentAuction.piece.getId(), userBid));
                    audit.logAction("GUI_USER_BID_" + userBid);

                    messages.add("You bid " + userBid + " EUR.");
                    messages.add("Buyer premium: "
                            + auctionHouse.calculateBuyerPremium(userBid) + " EUR");
                    messages.add("Total payment if you win: "
                            + totalPayment + " EUR");

                    bidPlacedThisRound = true;
                }
            } else {
                messages.add("You are outside the bidding room. Rivals continue bidding.");
            }

            List<RivalDecision> decisions = getRivalDecisionsMultithreaded(
                    executor,
                    currentAuction.rivals,
                    currentAuction.currentWinner,
                    currentAuction.piece.getCurrentPrice()
            );

            List<Client> rivalsToRemove = new ArrayList<>();
            RivalDecision bestBidDecision = null;

            for (RivalDecision decision : decisions) {
                messages.add(decision.getMessage());

                if (decision.getAction() == RivalAction.LEAVE) {
                    rivalsToRemove.add(decision.getRival());
                    audit.logAction("GUI_RIVAL_LEFT_AUCTION_" + decision.getRival().getName());
                }

                if (decision.getAction() == RivalAction.BID) {
                    if (bestBidDecision == null
                            || decision.getBidAmount() > bestBidDecision.getBidAmount()) {
                        bestBidDecision = decision;
                    }
                }
            }

            currentAuction.rivals.removeAll(rivalsToRemove);

            if (bestBidDecision != null
                    && bestBidDecision.getBidAmount() > currentAuction.piece.getCurrentPrice()) {
                Client rival = bestBidDecision.getRival();
                double rivalBid = bestBidDecision.getBidAmount();

                currentAuction.piece.setCurrentPrice(rivalBid);
                currentAuction.currentWinner = rival;

                bidService.create(new Bid(rival.getId(), currentAuction.piece.getId(), rivalBid));
                audit.logAction("GUI_RIVAL_BID_" + rival.getName());

                messages.add("[ACCEPTED BID] " + rival.getName()
                        + " is now winning with " + rivalBid + " EUR.");

                bidPlacedThisRound = true;
            }

            if (bidPlacedThisRound) {
                currentAuction.quietRounds = 0;
            } else {
                currentAuction.quietRounds++;
            }

            if (currentAuction.rivals.isEmpty()) {
                messages.add("No rivals left in the bidding room.");
                currentAuction.active = false;
            }

            if (currentAuction.quietRounds >= 2) {
                messages.add("No more bids are being placed.");
                currentAuction.active = false;
            }

            if (currentAuction.totalRounds >= 10) {
                messages.add("Auction time limit reached.");
                audit.logAction("GUI_AUCTION_TIME_LIMIT_REACHED");
                currentAuction.active = false;
            }

            if (!currentAuction.active) {
                messages.add("");
                messages.add("Going once... Going twice... SOLD!");
                messages.addAll(finalizeAuction(userId));

                AuctionUpdate finalUpdate = buildUpdate(messages, false);

                currentAuction = null;

                return finalUpdate;
            }

            return buildUpdate(messages, true);
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
                decisions.add(new RivalDecision(
                        null,
                        RivalAction.PASS,
                        0,
                        "A rival decision failed: " + e.getMessage()
                ));
            }
        }

        return decisions;
    }

    private RivalDecision decideRivalAction(Client rival, Client currentWinner, double currentPrice) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

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
                    "[THREAD-" + Thread.currentThread().getId() + "] "
                            + rival.getName() + " is already winning and waits."
            );
        }

        if (rival.getBudget() <= currentPrice) {
            return new RivalDecision(
                    rival,
                    RivalAction.LEAVE,
                    0,
                    "[THREAD-" + Thread.currentThread().getId() + "] "
                            + rival.getName() + " leaves. Price is too high."
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
                    "[THREAD-" + Thread.currentThread().getId() + "] "
                            + rival.getName() + " leaves the auction."
            );
        }

        int bidChance = 35;

        if (random.nextInt(100) < bidChance) {
            double incrementPercent = 0.05 + (0.12 - 0.05) * random.nextDouble();
            double rivalBid = currentPrice * (1 + incrementPercent);
            rivalBid = Math.round(rivalBid / 100.0) * 100.0;

            if (auctionHouse.calculateTotalPrice(rivalBid) <= rival.getBudget()
                    && rivalBid > currentPrice) {
                return new RivalDecision(
                        rival,
                        RivalAction.BID,
                        rivalBid,
                        "[THREAD-" + Thread.currentThread().getId() + "] "
                                + rival.getName() + " wants to bid "
                                + rivalBid + " EUR."
                );
            }
        }

        return new RivalDecision(
                rival,
                RivalAction.PASS,
                0,
                "[THREAD-" + Thread.currentThread().getId() + "] "
                        + rival.getName() + " passes this round."
        );
    }

    private List<String> finalizeAuction(int userId) throws SQLException {
        List<String> messages = new ArrayList<>();

        if (currentAuction == null) {
            messages.add("No auction to finalize.");
            return messages;
        }

        ArtPiece piece = currentAuction.piece;
        Client winner = currentAuction.currentWinner;

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

            messages.add("Auction House: " + auctionHouse.getName());
            messages.add("Hammer price: " + hammerPrice + " EUR");
            messages.add("Buyer premium: "
                    + auctionHouse.calculateBuyerPremium(hammerPrice) + " EUR");
            messages.add("Total payment: " + totalPayment + " EUR");

            if (winner.getId() == userId) {
                messages.add("CONGRATULATIONS! You won " + piece.getTitle() + ".");
                messages.add("The item was added to your inventory.");
                audit.logAction("GUI_AUCTION_WON_BY_USER");
            } else {
                messages.add("Auction ended. Winner: " + winner.getName());
                audit.logAction("GUI_AUCTION_LOST_BY_USER");
            }
        } else {
            auctionRecordService.create(
                    new AuctionRecord(piece.getId(), null, piece.getCurrentPrice())
            );

            messages.add("Auction ended. Nobody bought the piece.");
            audit.logAction("GUI_AUCTION_ENDED_WITHOUT_WINNER");
        }

        currentAuction.active = false;

        return messages;
    }

    private AuctionUpdate buildUpdate(List<String> messages, boolean active) {
        if (currentAuction == null) {
            return new AuctionUpdate(messages, false, "-", 0, "-");
        }

        String winnerName = currentAuction.currentWinner != null
                ? currentAuction.currentWinner.getName()
                : "No winner yet";

        return new AuctionUpdate(
                messages,
                active,
                currentAuction.piece.getTitle(),
                currentAuction.piece.getCurrentPrice(),
                winnerName
        );
    }
}