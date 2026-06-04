package src.service;

import src.model.AuctionRecord;
import src.repository.AuctionRecordRepository;

import java.sql.SQLException;
import java.util.List;

public class AuctionRecordService implements CrudService<AuctionRecord> {
    private static AuctionRecordService instance;

    private final AuctionRecordRepository auctionRecordRepo = AuctionRecordRepository.getInstance();
    private final AuditService audit = AuditService.getInstance();

    private AuctionRecordService() {}

    public static AuctionRecordService getInstance() {
        if (instance == null) {
            instance = new AuctionRecordService();
        }
        return instance;
    }

    @Override
    public void create(AuctionRecord record) throws SQLException {
        auctionRecordRepo.insert(record);
        audit.logAction("CREATE_AUCTION_RECORD");
    }

    @Override
    public AuctionRecord getById(int id) throws SQLException {
        audit.logAction("READ_AUCTION_RECORD_BY_ID");
        return auctionRecordRepo.getById(id);
    }

    @Override
    public List<AuctionRecord> getAll() throws SQLException {
        audit.logAction("READ_ALL_AUCTION_RECORDS");
        return auctionRecordRepo.getAll();
    }

    @Override
    public void update(AuctionRecord record) throws SQLException {
        auctionRecordRepo.update(record);
        audit.logAction("UPDATE_AUCTION_RECORD");
    }

    @Override
    public void delete(int id) throws SQLException {
        auctionRecordRepo.delete(id);
        audit.logAction("DELETE_AUCTION_RECORD");
    }
}