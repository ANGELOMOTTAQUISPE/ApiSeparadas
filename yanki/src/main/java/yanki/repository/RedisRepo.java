package yanki.repository;

import yanki.model.Client;

import java.util.Map;

public interface RedisRepo {
    Map<String, Client> findAll();
    Client findById(String id);
    void save(Client client);
    void delete(String id);
}
