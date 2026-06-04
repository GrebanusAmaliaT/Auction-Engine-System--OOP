package src.service;

import src.model.ArtPiece;
import src.repository.ArtPieceRepository;

import java.sql.SQLException;
import java.util.List;

public class ArtPieceService implements CrudService<ArtPiece> {
    private static ArtPieceService instance;

    private final ArtPieceRepository artRepo = ArtPieceRepository.getInstance();
    private final AuditService audit = AuditService.getInstance();

    private ArtPieceService() {}

    public static ArtPieceService getInstance() {
        if (instance == null) {
            instance = new ArtPieceService();
        }
        return instance;
    }

    @Override
    public void create(ArtPiece piece) throws SQLException {
        artRepo.insert(piece);
        audit.logAction("CREATE_ART_PIECE");
    }

    @Override
    public ArtPiece getById(int id) throws SQLException {
        audit.logAction("READ_ART_PIECE_BY_ID");
        return artRepo.getById(id);
    }

    @Override
    public List<ArtPiece> getAll() throws SQLException {
        audit.logAction("READ_ALL_ART_PIECES");
        return artRepo.getAll();
    }

    @Override
    public void update(ArtPiece piece) throws SQLException {
        artRepo.update(piece);
        audit.logAction("UPDATE_ART_PIECE");
    }

    public void markAsSold(ArtPiece piece, int ownerId) throws SQLException {
        artRepo.markAsSold(piece, ownerId);
        audit.logAction("MARK_ART_PIECE_AS_SOLD");
    }

    @Override
    public void delete(int id) throws SQLException {
        artRepo.delete(id);
        audit.logAction("DELETE_ART_PIECE");
    }
}