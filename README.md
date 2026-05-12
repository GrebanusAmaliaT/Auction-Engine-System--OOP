# Auction-Engine-System-OOP (Stage II)

A professional Java-based simulation engine for high-end art auctions, now upgraded with **Database Persistence** and **Audit Logging**. This project demonstrates a complete transition from in-memory storage to a persistent architecture using **PostgreSQL** and **JDBC**.

## Key Features (Stage II Upgrades)
- **Database Persistence**: All data (Clients, Art Pieces, Bids) is stored in a **PostgreSQL** database.
- **Singleton Repositories**: Implementation of the **Repository Pattern** with Singleton access to ensure efficient database connections and resource management.
- **Audit Logging**: Automatic logging of all system actions (bid placement, auction wins, loss events) into a **CSV file** with precise timestamps.
- **Financial Dashboard**: Real-time calculation of **User Budget**, **Assets Value** (portfolio), and **Total Net Worth** (Cash + Assets).
- **Real-World Rivals**: Auction simulation against real historical figures and billionaires (NPCs) loaded dynamically from the database.

---

## System Specifications (Stage II)

### 1. Persistence Layer (CRUD Operations)
*Implemented via JDBC in the `src.repository` package:*

* **ClientRepository**: Manages player and NPC profiles. Handles budget updates after purchases and identifies rivals.
* **ArtPieceRepository**: Handles polymorphic storage of `Painting` and `Jewelry` using a discriminator column (`type`).
* **BidRepository**: Maintains a permanent record of all bidding history for transparency.
* **UserInventoryRepository**: A specialized service that calculates the real-time value of the pieces currently owned by the user.

### 2. Service Layer & Logic
* **DatabaseConfig**: Singleton class for managing the **PostgreSQL Connection pool** and credentials.
* **AuditService**: Writes to `audit_log.csv` for every significant system event, ensuring a trail of operations.
* **AuctionService 2.0**: Enhanced engine that synchronizes the interactive console loop with the SQL database.

---

## System Objects & Hierarchy
1.  **ArtPiece (Abstract)**: Base entity for all collectables.
2.  **Painting**: Inherits `ArtPiece`, adds `technique` (e.g., Oil, Tempera).
3.  **Jewelry**: Inherits `ArtPiece`, adds `material` and `carats`.
4.  **Client**: Represents both the User and NPCs (distinguished by the `isNpc` flag).
5.  **Bid**: Persistent record containing `clientId`, `pieceId`, and `value`.

---

## Project Structure
- `src.model`: System entities and domain objects.
- `src.repository`: Data Access Objects (DAO) using JDBC and SQL queries.
- `src.service`: Business logic, Auction Engine, and Audit Logging.
- `src.config`: Database connection settings and environment setup.
- `src.main`: Console-based interactive menu and application entry point.

## How to Run
1.  **Database Setup**: 
    - Create a PostgreSQL database.
    - Run the SQL setup scripts to create tables (`clients`, `art_pieces`, `bids`).
2.  **Configuration**: Update `src/config/DatabaseConfig.java` with your local database URL, username, and password.
3.  **Drivers**: Ensure the **PostgreSQL JDBC Driver** (`.jar`) is added to the project's **Referenced Libraries**.
4.  **Run**: Execute `Main.java` and follow the on-screen instructions to bid against rivals and build your empire.

## Technologies Used
- **Java 17+** (or JRE 1.8 compatible)
- **JDBC** (Java Database Connectivity)
- **PostgreSQL 16**
- **Java Stream API**
- **CSV I/O** (Audit Logging)