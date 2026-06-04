package src.service;

import src.model.Bid;
import src.repository.BidRepository;

import java.sql.SQLException;
import java.util.List;

public class BidService implements CrudService<Bid> {
    private static BidService instance;

    private final BidRepository bidRepo = BidRepository.getInstance();
    private final AuditService audit = AuditService.getInstance();

    private BidService() {}

    public static BidService getInstance() {
        if (instance == null) {
            instance = new BidService();
        }
        return instance;
    }

    @Override
    public void create(Bid bid) throws SQLException {
        bidRepo.insert(bid);
        audit.logAction("CREATE_BID");
    }

    @Override
    public Bid getById(int id) throws SQLException {
        audit.logAction("READ_BID_BY_ID");
        return bidRepo.getById(id);
    }

    @Override
    public List<Bid> getAll() throws SQLException {
        audit.logAction("READ_ALL_BIDS");
        return bidRepo.getAll();
    }

    @Override
    public void update(Bid bid) throws SQLException {
        bidRepo.update(bid);
        audit.logAction("UPDATE_BID");
    }

    @Override
    public void delete(int id) throws SQLException {
        bidRepo.delete(id);
        audit.logAction("DELETE_BID");
    }
}