# Auction-Engine-System-OOP

A professional Java-based simulation engine for high-end art auctions. This project focuses on clean **Object-Oriented Programming (OOP)** architecture, demonstrating advanced concepts like polymorphism, custom sorting, and interactive process simulation.

## Key Features (Stage I)
- **Management System**: Adding and organizing art pieces and clients.
- **OOP Hierarchy**: Utilizing inheritance and abstract classes to differentiate between object types.
- **Dynamic Catalog**: Automatic display of art pieces sorted by price using `TreeSet`.
- **Interactive Auction Engine**: Real-time console auction simulation against a bot with randomized behavior and rounded bidding increments.
- **Data Validation**: Handling user input to prevent system crashes and ensure logical bidding.

---

## System Specifications

### 1. System Objects (8 Types)
*Where to find them in the code:*

1. **ArtPiece (Abstract)**: Found in `model/ArtPiece.java`. The base for all items.
2. **Painting**: Found in `model/Painting.java`. Extends `ArtPiece` with technique details.
3. **Jewelry**: Found in `model/Jewelry.java`. Extends `ArtPiece` with carat details.
4. **Client**: Found in `model/Client.java`. Manages user identity and budget.
5. **Bid**: Found in `model/Bid.java`. Records the "who, when, and how much" of an offer.
6. **AuctionService**: Found in `service/AuctionService.java`. The main controller.
7. **BiddingBot**: Simulated logic within `AuctionService.java` using `java.util.Random`.
8. **InventoryCatalog**: Represented by the `TreeSet<ArtPiece>` collection in the service layer.

### 2. Actions & Queries (10 Actions)
*How they are implemented:*

1. **Add Art Piece**: Handled by `addArtPiece(ArtPiece piece)` in the service.
2. **Register Client**: Handled by `registerClient(Client client)`.
3. **Place Bid**: Interactive loop in `startInteractiveAuction` where user input is processed.
4. **Automated Counter-Bid**: The "Bot Logic" section using randomized probability (30% exit chance).
5. **Display Sorted Catalog**: The `displaySortedCatalog()` query using the `TreeSet` iterator.
6. **Get Random Piece**: The `getRandomPiece()` method using `Random.nextInt()`.
7. **Search Piece by ID**: Implemented via **Java Stream API** (filter/findFirst) in the service.
8. **Update Current Price**: Handled by `piece.setCurrentPrice(yourBid)` to maintain object state.
9. **Refresh Catalog State**: The `refreshSortedCatalog()` method which forces `TreeSet` re-sorting.
10. **Validate User Input**: `scanner.hasNextDouble()` checks to prevent crashes on invalid input.

---


## Project Structure
- `model`: Contains system entities (`ArtPiece`, `Painting`, `Jewelry`, `Client`, `Bid`).
- `service`: Includes business logic and the auction simulation engine (`AuctionService`).
- `main`: Entry point of the application.

## How to Run
1. Clone the repository.
2. Open the project in an IDE (IntelliJ IDEA / Eclipse / VS Code).
3. Run the `Main.java` class from the `main` package.
4. Follow the terminal instructions to place your bids.

## Technologies Used
- **Java 17+**
- **Java Collections API** (List, TreeSet)
- **Java Stream API**