# Auction-Engine-System-OOP

A professional Java-based simulation engine for high-end art auctions. This project focuses on clean **Object-Oriented Programming (OOP)** architecture, demonstrating advanced concepts like polymorphism, custom sorting, and interactive process simulation.

## Key Features (Stage I)
- **Management System**: Adding and organizing art pieces and clients.
- **OOP Hierarchy**: Utilizing inheritance and abstract classes to differentiate between object types.
- **Dynamic Catalog**: Automatic display of art pieces sorted by price using `TreeSet`.
- **Interactive Auction Engine**: Real-time console auction simulation against a bot with randomized behavior and rounded bidding increments.
- **Data Validation**: Handling user input to prevent system crashes and ensure logical bidding.

---

## System Specifications (Project Requirements)

### 1. System Objects (8 Types)
The following entities define the core structure of the application:
1. **ArtPiece (Abstract)**: The base class for all auctionable items.
2. **Painting**: Specific art type inheriting from `ArtPiece` (with technique attributes).
3. **Jewelry**: Specific art type inheriting from `ArtPiece` (with carat attributes).
4. **Client**: The entity representing the user with a specific budget.
5. **Bid**: A model representing a financial offer made by a client.
6. **AuctionService**: The logic engine managing the business flow.
7. **BiddingBot**: An automated entity designed to compete against the user.
8. **InventoryCatalog**: A specialized collection (`TreeSet`) for real-time price-based sorting.

### 2. Actions & Queries (10 Actions)
Operations available within the current system:
1. **Add Art Piece**: Insert a new item into the inventory.
2. **Register Client**: Add a new participant to the system.
3. **Place Bid**: Submit a new financial offer for a specific item.
4. **Automated Counter-Bid**: Bot-generated response based on random logic.
5. **Display Sorted Catalog**: Query the system for items ordered by price ($O(\log n)$).
6. **Get Random Piece**: Retrieve an item at random to initiate an auction round.
7. **Search Piece by ID**: Efficiently find an item using Java Streams.
8. **Update Current Price**: Real-time state change of an `ArtPiece` object.
9. **Refresh Catalog State**: Re-sorting the `TreeSet` after a price update.
10. **Validate User Input**: Ensuring numeric and logical integrity of terminal data.

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