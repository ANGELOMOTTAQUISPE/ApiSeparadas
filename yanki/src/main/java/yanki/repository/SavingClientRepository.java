package yanki.repository;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import yanki.model.Client;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.UUID;

@Repository
public class SavingClientRepository implements RedisRepo{
    private static final String KEY = "bank";
    private RedisTemplate<String, Client> redisTemplate;
    private HashOperations hashOperations;

    public SavingClientRepository(RedisTemplate<String, Client> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    private void init() {
        hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public Map<String, Client> findAll() {
        return hashOperations.entries(KEY);
    }

    @Override
    public Client findById(String id) {
        return (Client) hashOperations.get(KEY, id);
    }

    @Override
    public void save(Client savingAccount) {
        hashOperations.put(KEY, UUID.randomUUID().toString(), savingAccount);
    }

    @Override
    public void delete(String id) {
        hashOperations.delete(KEY, id);
    }

}
