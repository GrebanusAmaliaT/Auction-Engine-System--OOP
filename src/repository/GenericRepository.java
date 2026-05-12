package src.repository;

import java.sql.SQLException;
import java.util.List;

public interface GenericRepository<T> {
    void insert(T object) throws SQLException;
    T getById(int id) throws SQLException;
    List<T> getAll() throws SQLException;
    void update(T object) throws SQLException;
    void delete(int id) throws SQLException;
}