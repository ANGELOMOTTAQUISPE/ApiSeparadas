package Client.repo;

import Client.model.Client;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
@Repository
public interface IClientRepo extends ReactiveMongoRepository<Client, String> {
    public Mono<Client> findByDocumentNumber(String documentNumber);
    public Mono<Client> findByPhoneNumber(String phoneNumber);
}
