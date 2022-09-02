package Movement.repo;

import Movement.model.BankTransferet;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IBankTransferetRepo extends ReactiveMongoRepository<BankTransferet, String> {
}
