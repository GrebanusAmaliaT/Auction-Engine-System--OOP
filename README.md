# Art Auction Management System - Advanced OOP

Project developed for the **Advanced Object-Oriented Programming** course. The application simulates the management of a luxury auction house, allowing the administration of art pieces (paintings, jewelry) and interactive participation in auctions.

## Key Features (Stage I)
- **Management System**: Adding and organizing art pieces and clients.
- **OOP Hierarchy**: Utilizing inheritance and abstract classes to differentiate between object types.
- **Dynamic Catalog**: Automatic display of art pieces sorted by price using `TreeSet`.
- **Interactive Auction Engine**: Real-time console auction simulation against a bot with randomized behavior and rounded bidding increments.
- **Data Validation**: Handling user input to prevent system crashes and ensure logical bidding.

## Project Structure
- `model`: Contains system entities (`PiesaArta`, `Tablou`, `Bijuterie`, `Client`, `Oferta`).
- `service`: Includes business logic and the auction simulation engine.
- `main`: Entry point of the application.

## How to Run
1. Clone the repository.
2. Open the project in an IDE (IntelliJ IDEA / Eclipse / VS Code).
3. Run the `Main.java` class from the `main` package.
4. Follow the terminal instructions to place your bids

## 🛠️ Technologies Used
- Java 17+
- Java Collections API (List, TreeSet)
- Java Stream API