package Movement.service.impl;

import Movement.model.BankTransferet;
import Movement.repo.IBankTransferetRepo;
import Movement.service.IBanktransferetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@Service
public class BankTransferetServiceImpl implements IBanktransferetService {
    private static final Logger logger = LoggerFactory.getLogger(BankTransferetServiceImpl.class);
    @Autowired
    private IBankTransferetRepo repo;
    public Mono<BankTransferet> register(BankTransferet obj) {
        return repo.save(obj);
    }
    public Mono<BankTransferet> modify(BankTransferet obj) {
        return repo.save(obj);
    }

    public Flux<BankTransferet> list() {

        return repo.findAll();
    }

    public Mono<BankTransferet> listofId(String id) {
        Mono<BankTransferet> op = repo.findById(id);
        return op;
    }
    public Mono<BankTransferet> delete(String id) {
        return repo.findById(id).flatMap(r-> repo.delete(r).then(Mono.just(r)));
    }
}
