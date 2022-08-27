package Movement.repo;

import Movement.model.Movement;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
@Repository
public interface IMovementRepo extends ReactiveMongoRepository<Movement, String> {
    @Query(value = "{ 'account.accountNumber': ?0 } ")
    Flux<Movement> findByAccount(String accountNumber);
    @Query(value = "{ 'credit.creditCardNumber': ?0 } ")
    Flux<Movement> findByCredit(String creditCardNumber);
}
