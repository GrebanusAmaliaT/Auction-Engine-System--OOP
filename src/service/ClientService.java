package src.service;

import src.model.Client;
import src.repository.ClientRepository;

import java.sql.SQLException;
import java.util.List;

public class ClientService implements CrudService<Client> {
    private static ClientService instance;

    private final ClientRepository clientRepo = ClientRepository.getInstance();
    private final AuditService audit = AuditService.getInstance();

    private ClientService() {}

    public static ClientService getInstance() {
        if (instance == null) {
            instance = new ClientService();
        }
        return instance;
    }

    @Override
    public void create(Client client) throws SQLException {
        clientRepo.insert(client);
        audit.logAction("CREATE_CLIENT");
    }

    @Override
    public Client getById(int id) throws SQLException {
        audit.logAction("READ_CLIENT_BY_ID");
        return clientRepo.getById(id);
    }

    @Override
    public List<Client> getAll() throws SQLException {
        audit.logAction("READ_ALL_CLIENTS");
        return clientRepo.getAll();
    }

    @Override
    public void update(Client client) throws SQLException {
        clientRepo.update(client);
        audit.logAction("UPDATE_CLIENT");
    }

    @Override
    public void delete(int id) throws SQLException {
        clientRepo.delete(id);
        audit.logAction("DELETE_CLIENT");
    }
}