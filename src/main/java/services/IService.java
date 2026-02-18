package services;

import java.sql.SQLException;
import java.util.List;

public interface IService<T> {

    // ➕ Ajouter
    void create(T t) throws SQLException;

    // ✏️ Modifier
    void update(T t) throws SQLException;

    // ❌ Supprimer
    void delete(int id) throws SQLException;

    // 📋 Afficher tout
    List<T> getAll() throws SQLException;

    // 🔍 Chercher par ID
    T getById(int id) throws SQLException;
}
