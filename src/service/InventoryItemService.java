package src.service;

import src.model.InventoryItem;
import src.repository.InventoryItemRepository;

import java.sql.SQLException;
import java.util.List;

public class InventoryItemService implements CrudService<InventoryItem> {
    private static InventoryItemService instance;

    private final InventoryItemRepository inventoryRepo = InventoryItemRepository.getInstance();
    private final AuditService audit = AuditService.getInstance();

    private InventoryItemService() {}

    public static InventoryItemService getInstance() {
        if (instance == null) {
            instance = new InventoryItemService();
        }
        return instance;
    }

    @Override
    public void create(InventoryItem item) throws SQLException {
        inventoryRepo.insert(item);
        audit.logAction("CREATE_INVENTORY_ITEM");
    }

    @Override
    public InventoryItem getById(int id) throws SQLException {
        audit.logAction("READ_INVENTORY_ITEM_BY_ID");
        return inventoryRepo.getById(id);
    }

    @Override
    public List<InventoryItem> getAll() throws SQLException {
        audit.logAction("READ_ALL_INVENTORY_ITEMS");
        return inventoryRepo.getAll();
    }

    public List<InventoryItem> getByClientId(int clientId) throws SQLException {
        audit.logAction("READ_CLIENT_INVENTORY");
        return inventoryRepo.getByClientId(clientId);
    }

    public double getTotalValueForClient(int clientId) throws SQLException {
        audit.logAction("CALCULATE_CLIENT_INVENTORY_VALUE");
        return inventoryRepo.getTotalValueForClient(clientId);
    }

    @Override
    public void update(InventoryItem item) throws SQLException {
        inventoryRepo.update(item);
        audit.logAction("UPDATE_INVENTORY_ITEM");
    }

    @Override
    public void delete(int id) throws SQLException {
        inventoryRepo.delete(id);
        audit.logAction("DELETE_INVENTORY_ITEM");
    }
}