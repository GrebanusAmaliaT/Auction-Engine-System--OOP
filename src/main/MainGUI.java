package src.main;

import src.service.GuiAuctionEngineService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainGUI extends JFrame {
    private static final int USER_ID = 1;

    private final GuiAuctionEngineService engine = new GuiAuctionEngineService();

    private JLabel houseLabel;
    private JLabel playerLabel;
    private JLabel cashLabel;
    private JLabel assetsLabel;
    private JLabel netWorthLabel;
    private JLabel currentAuctionLabel;
    private JLabel currentWinnerLabel;

    private JTextArea outputArea;
    private JTextField bidField;

    private JButton startButton;
    private JButton bidButton;
    private JButton passButton;
    private JButton leaveButton;
    private JButton catalogButton;
    private JButton sortedCatalogButton;
    private JButton inventoryButton;
    private JButton refreshButton;

    public MainGUI() {
        setTitle("Auction Engine System");
        setSize(1050, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        buildInterface();
        setAuctionControlsEnabled(false);
        refreshDashboard();
    }

    private void buildInterface() {
        setLayout(new BorderLayout());

        JPanel dashboardPanel = new JPanel(new GridLayout(6, 1));
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        houseLabel = new JLabel();
        playerLabel = new JLabel();
        cashLabel = new JLabel();
        assetsLabel = new JLabel();
        netWorthLabel = new JLabel();
        currentAuctionLabel = new JLabel("Current auction: -");
        currentWinnerLabel = new JLabel("Current winner: -");

        houseLabel.setFont(new Font("Serif", Font.BOLD, 22));
        playerLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        cashLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        assetsLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        netWorthLabel.setFont(new Font("SansSerif", Font.BOLD, 15));

        dashboardPanel.add(houseLabel);
        dashboardPanel.add(playerLabel);
        dashboardPanel.add(cashLabel);
        dashboardPanel.add(assetsLabel);
        dashboardPanel.add(netWorthLabel);
        dashboardPanel.add(currentAuctionLabel);

        add(dashboardPanel, BorderLayout.NORTH);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(outputArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomContainer = new JPanel(new BorderLayout());

        JPanel bidPanel = new JPanel(new BorderLayout(8, 8));
        bidPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        bidField = new JTextField();
        bidButton = new JButton("Place Bid");
        passButton = new JButton("Pass Round");
        leaveButton = new JButton("Leave Room");

        JPanel bidButtonsPanel = new JPanel(new GridLayout(1, 3, 8, 8));
        bidButtonsPanel.add(bidButton);
        bidButtonsPanel.add(passButton);
        bidButtonsPanel.add(leaveButton);

        bidPanel.add(new JLabel("Bid amount:"), BorderLayout.WEST);
        bidPanel.add(bidField, BorderLayout.CENTER);
        bidPanel.add(bidButtonsPanel, BorderLayout.EAST);

        JPanel menuPanel = new JPanel(new GridLayout(1, 5, 8, 8));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        startButton = new JButton("Start Random Auction");
        catalogButton = new JButton("View Catalog");
        sortedCatalogButton = new JButton("Sorted Catalog");
        inventoryButton = new JButton("My Inventory");
        refreshButton = new JButton("Refresh");

        menuPanel.add(startButton);
        menuPanel.add(catalogButton);
        menuPanel.add(sortedCatalogButton);
        menuPanel.add(inventoryButton);
        menuPanel.add(refreshButton);

        bottomContainer.add(bidPanel, BorderLayout.NORTH);
        bottomContainer.add(menuPanel, BorderLayout.SOUTH);

        add(bottomContainer, BorderLayout.SOUTH);

        startButton.addActionListener(e -> startAuction());
        bidButton.addActionListener(e -> placeBid());
        passButton.addActionListener(e -> passRound());
        leaveButton.addActionListener(e -> leaveRoom());
        catalogButton.addActionListener(e -> showCatalog());
        sortedCatalogButton.addActionListener(e -> showSortedCatalog());
        inventoryButton.addActionListener(e -> showInventory());
        refreshButton.addActionListener(e -> refreshDashboard());
    }

    private void refreshDashboard() {
        new SwingWorker<GuiAuctionEngineService.DashboardData, Void>() {
            @Override
            protected GuiAuctionEngineService.DashboardData doInBackground() throws Exception {
                return engine.getDashboardData(USER_ID);
            }

            @Override
            protected void done() {
                try {
                    GuiAuctionEngineService.DashboardData data = get();

                    if (data == null) {
                        showError("User not found. Run the SQL script first.");
                        return;
                    }

                    houseLabel.setText(engine.getAuctionHouse().getDisplayInfo());
                    playerLabel.setText("Player: " + data.getPlayerName());
                    cashLabel.setText("Cash: " + data.getCash() + " EUR");
                    assetsLabel.setText("Assets value: " + data.getAssetsValue() + " EUR");
                    netWorthLabel.setText("Total net worth: " + data.getTotalNetWorth() + " EUR");

                } catch (Exception e) {
                    showError(e.getMessage());
                }
            }
        }.execute();
    }

    private void startAuction() {
        outputArea.setText("");
        appendOutput("Starting auction...");

        setMainButtonsEnabled(false);

        new SwingWorker<GuiAuctionEngineService.AuctionUpdate, Void>() {
            @Override
            protected GuiAuctionEngineService.AuctionUpdate doInBackground() throws Exception {
                return engine.startRandomAuction(USER_ID);
            }

            @Override
            protected void done() {
                try {
                    GuiAuctionEngineService.AuctionUpdate update = get();
                    outputArea.setText("");
                    appendMessages(update.getMessages());
                    updateAuctionLabels(update);
                    setAuctionControlsEnabled(update.isActive());
                    setMainButtonsEnabled(true);
                    refreshDashboard();
                } catch (Exception e) {
                    showError(e.getMessage());
                    setMainButtonsEnabled(true);
                }
            }
        }.execute();
    }

    private void placeBid() {
        String bidText = bidField.getText().trim();

        if (bidText.isEmpty()) {
            showError("Enter a bid amount first.");
            return;
        }

        double bidAmount;

        try {
            bidAmount = Double.parseDouble(bidText);
        } catch (NumberFormatException e) {
            showError("Invalid bid amount.");
            return;
        }

        processAuctionAction("bid", bidAmount);
    }

    private void passRound() {
        processAuctionAction("pass", 0);
    }

    private void leaveRoom() {
        processAuctionAction("leave", -1);
    }

    private void processAuctionAction(String action, double bidAmount) {
        setAuctionControlsEnabled(false);

        new SwingWorker<GuiAuctionEngineService.AuctionUpdate, Void>() {
            @Override
            protected GuiAuctionEngineService.AuctionUpdate doInBackground() throws Exception {
                if ("bid".equals(action)) {
                    return engine.placeUserBid(USER_ID, bidAmount);
                } else if ("pass".equals(action)) {
                    return engine.passRound(USER_ID);
                } else {
                    return engine.leaveRoom(USER_ID);
                }
            }

            @Override
            protected void done() {
                try {
                    GuiAuctionEngineService.AuctionUpdate update = get();

                    appendMessages(update.getMessages());
                    updateAuctionLabels(update);

                    setAuctionControlsEnabled(update.isActive());

                    if (!update.isActive()) {
                        bidField.setText("");
                        refreshDashboard();
                    }

                } catch (Exception e) {
                    showError(e.getMessage());
                    setAuctionControlsEnabled(engine.hasActiveAuction());
                }
            }
        }.execute();
    }

    private void showCatalog() {
        outputArea.setText("");
        appendOutput("--- AVAILABLE CATALOG ---");

        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return engine.getCatalogLines();
            }

            @Override
            protected void done() {
                try {
                    appendMessages(get());
                } catch (Exception e) {
                    showError(e.getMessage());
                }
            }
        }.execute();
    }

    private void showSortedCatalog() {
        outputArea.setText("");
        appendOutput("--- CATALOG SORTED BY PRICE ---");

        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return engine.getSortedCatalogLines();
            }

            @Override
            protected void done() {
                try {
                    appendMessages(get());
                } catch (Exception e) {
                    showError(e.getMessage());
                }
            }
        }.execute();
    }

    private void showInventory() {
        outputArea.setText("");
        appendOutput("--- MY INVENTORY ---");

        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return engine.getInventoryLines(USER_ID);
            }

            @Override
            protected void done() {
                try {
                    appendMessages(get());
                } catch (Exception e) {
                    showError(e.getMessage());
                }
            }
        }.execute();
    }

    private void updateAuctionLabels(GuiAuctionEngineService.AuctionUpdate update) {
        currentAuctionLabel.setText("Current auction: "
                + update.getCurrentPieceTitle()
                + " | Current price: "
                + update.getCurrentPrice()
                + " EUR");

        currentWinnerLabel.setText("Current winner: " + update.getCurrentWinnerName());
    }

    private void setAuctionControlsEnabled(boolean enabled) {
        bidField.setEnabled(enabled);
        bidButton.setEnabled(enabled);
        passButton.setEnabled(enabled);
        leaveButton.setEnabled(enabled);
    }

    private void setMainButtonsEnabled(boolean enabled) {
        startButton.setEnabled(enabled);
        catalogButton.setEnabled(enabled);
        sortedCatalogButton.setEnabled(enabled);
        inventoryButton.setEnabled(enabled);
        refreshButton.setEnabled(enabled);
    }

    private void appendMessages(List<String> messages) {
        for (String message : messages) {
            appendOutput(message);
        }
    }

    private void appendOutput(String text) {
        outputArea.append(text + "\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainGUI gui = new MainGUI();
            gui.setVisible(true);
        });
    }
}