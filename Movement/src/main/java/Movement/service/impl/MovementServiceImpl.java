package Movement.service.impl;

import Movement.exception.ModelNotFoundException;
import Movement.model.Movement;
import Movement.repo.IMovementRepo;
import Movement.service.IMovementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@Service
public class MovementServiceImpl implements IMovementService {
    private static final Logger logger = LoggerFactory.getLogger(MovementServiceImpl.class);
    @Autowired
    private IMovementRepo repo;

    public Mono<Movement> register(Movement obj) {
        Mono<Movement> listmovementAccount = repo.findlastMovementbyAccount(obj.getAccount().getAccountNumber());

        return listmovementAccount.flatMap( listacc -> {
            logger.info("Lista de movimientos por cuenta: "+listacc.toString());
            Double total = listacc.getBalance();

            return Mono.just(total).map(t->{
                if (obj.getTypeMovement().equals("retiro")){
                    t = t - obj.getMovement();
                }else{
                    t = t + obj.getMovement();
                }
                logger.info("1 t: "+ t);
                return  t;
            }).flatMap( t -> {
                logger.info("2 t: "+ t);
                if(t < 0){
                    throw new ModelNotFoundException(" No puede ser menor a cero: ");
                }else{
                    logger.info("3 t: "+ t);
                    return Mono.just(obj).flatMap( m ->{
                        logger.info("4 m: "+ m.toString());
                        m.setBalance(t);
                        return repo.save(m);
                    });
                }
            });

        });
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
