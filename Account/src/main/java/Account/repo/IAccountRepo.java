package Account.repo;

import Account.model.Account;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface IAccountRepo extends ReactiveMongoRepository<Account, String> {
    @Query(value = "{'client.documentNumber' : ?0, accountType: ?1 }")
    Flux<Account> findByAccountClient(String documentNumber, String accountType);

}
