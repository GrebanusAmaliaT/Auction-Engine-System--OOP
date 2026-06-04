# Auction Engine System OOP

Java console application for simulating high-end art auctions.  
The project uses OOP principles, JDBC persistence with PostgreSQL, and CSV audit logging.

---

## Stage I - OOP System

### System Actions / Queries

The system supports the following actions:

1. Display user statistics.
2. Display available art catalog.
3. Display catalog sorted by price.
4. Start a random auction.
5. Place a bid.
6. Pass a bidding round.
7. Leave the bidding room.
8. Simulate NPC rival bids.
9. Simulate NPC rival withdrawal.
10. Finalize an auction.
11. Mark an art piece as sold.
12. Save bid history.
13. Save auction result.
14. Calculate user assets value.
15. Calculate total net worth.

### Object Types

The project defines at least 8 object types:

1. `ArtPiece`
2. `Painting`
3. `Jewelry`
4. `Client`
5. `Bid`
6. `AuctionRecord`
7. `AuctionHouse`
8. `InventoryItem`

### OOP Concepts

The project uses:

- encapsulation through private attributes and getters/setters;
- inheritance through `Painting` and `Jewelry`, which extend `ArtPiece`;
- polymorphism by storing both paintings and jewelry as `ArtPiece`;
- service classes for exposing system operations.

### Collections

The project uses multiple collections:

- `List<Client>`
- `List<ArtPiece>`
- `List<Bid>`
- `TreeSet<ArtPiece>` for displaying the catalog sorted by price

---

## Stage II - Persistence and Audit

The project was extended with database persistence using **PostgreSQL** and **JDBC**.

### CRUD Classes

CRUD operations are implemented for at least 4 classes:

1. `Client`
2. `ArtPiece`
3. `Bid`
4. `AuctionRecord`

### Repository Layer

The project uses a generic repository interface: GenericRepository<T>

Implemented repositories:

- ClientRepository
- ArtPieceRepository
- BidRepository
- AuctionRecordRepository
- Service Layer

The project uses a generic CRUD service interface: CrudService<T>

Implemented services:

- ClientService
- ArtPieceService
- BidService
- AuctionRecordService
- AuctionService
- AuditService

The services are implemented using the Singleton pattern.

### Audit

AuditService writes every important system action to a CSV file:

action_name,timestamp

Example:

CREATE_BID,2026-06-04 21:32:20
START_AUCTION_PIECE_3,2026-06-04 21:33:10
AUCTION_WON_BY_USER,2026-06-04 21:35:44
Database Tables

### Main tables used by the application:

- clients
- art_pieces
- bids
- auction_records

### Project Structure

```text
src
├── config
│   └── DatabaseConfig.java
├── main
│   └── Main.java
├── model
│   ├── ArtPiece.java
│   ├── Painting.java
│   ├── Jewelry.java
│   ├── Client.java
│   ├── Bid.java
│   ├── AuctionRecord.java
│   ├── AuctionHouse.java
│   └── InventoryItem.java
├── repository
│   ├── GenericRepository.java
│   ├── ClientRepository.java
│   ├── ArtPieceRepository.java
│   ├── BidRepository.java
│   ├── AuctionRecordRepository.java
│   └── UserInventoryRepository.java
└── service
    ├── CrudService.java
    ├── AuctionService.java
    ├── ClientService.java
    ├── ArtPieceService.java
    ├── BidService.java
    ├── AuctionRecordService.java
    └── AuditService.java
```


### How to Run
1. Set up PostgreSQL

Create the database and run the SQL scripts for:
- clients
- art_pieces
- bids
- auction_records

2. Set environment variables

PowerShell example:

$env:DB_URL="jdbc:postgresql://localhost:5432/postgres"
$env:DB_USER="postgres"
$env:DB_PASS="your_password_here"

3. Add PostgreSQL JDBC Driver

Add the PostgreSQL JDBC .jar file to the project referenced libraries.

4. Run the app

Run:

Main.java
Technologies Used
Java
OOP
JDBC
PostgreSQL
Java Collections
Java Stream API
CSV File I/O