package Movement.repo;

import Movement.model.Movement;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Repository
public interface IMovementRepo extends ReactiveMongoRepository<Movement, String> {
    @Aggregation(pipeline = {"{ '$match': { 'account.accountNumber' : ?0 } }","{ '$sort' : { 'movementDate' : -1 } }","{'$limit': 1}"})
    Mono<Movement> findlastMovementbyAccount(String accountNumber) ;
    @Aggregation(pipeline = {"{ '$match': { 'credit.creditCardNumber' : ?0 } }","{ '$sort' : { 'movementDate' : -1 } }","{'$limit': 1}"})
    Mono<Movement> findlastMovementbyCredit(String creditNumber);
    @Query(value = "{$and:[{'movementDate':{$gte:  { '$date' : ?0} }},{'movementDate': {$lte:  { '$date' : ?1} }}],'account.accountNumber':?2}")
    Flux<Movement> findmovementsbyDates(String iniDate,String finalDate,String accountNumber);
    @Query(value = "{ 'credit.creditCardNumber': ?0 } ")
    Flux<Movement> findByCredit(String creditCardNumber);

    @Query(value = "{ 'account.accountNumber': ?0 } ")
    Flux<Movement> findByAccount(String accountNumber);
}
