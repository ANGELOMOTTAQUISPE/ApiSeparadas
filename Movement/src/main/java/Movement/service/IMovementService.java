package Movement.service;

import Movement.model.BankTransferet;
import Movement.model.Movement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IMovementService extends ICRUD<Movement, String>{
    Flux<Movement> listmovementByAccount(String accountNumber);
    Flux<Movement> listmovementByCredit(String creditCardNumber);
    Flux<Movement> listmovementByDate(String iniDate,String finDate,String accountNumber);
    Mono<Movement> registerAccount(Movement obj);
    Mono<Movement> registerCredit(Movement obj);
    Mono<BankTransferet> registerTransferet(BankTransferet obj);
}
