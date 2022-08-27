package Movement.service.impl;

import Movement.model.Movement;
import Movement.repo.IMovementRepo;
import Movement.service.IMovementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@Service
public class MovementServiceImpl implements IMovementService {
    @Autowired
    private IMovementRepo repo;

    public Mono<Movement> register(Movement obj) {
        return repo.save(obj);
    }

    public Mono<Movement> modify(Movement obj) {
        return repo.save(obj);
    }

    public Flux<Movement> list() {
        return repo.findAll();
    }

    public Mono<Movement> listofId(String id) {
        Mono<Movement> op = repo.findById(id);
        return op;
    }
    public Mono<Movement> delete(String id) {
        return repo.findById(id).flatMap(r-> repo.delete(r).then(Mono.just(r)));
    }
    public Flux<Movement> listmovementByAccount(String accountNumber) {
        return repo.findByAccount(accountNumber);
    }
    public Flux<Movement> listmovementByCredit( String creditCardNumber) {
        return repo.findByCredit(creditCardNumber);
    }
}
