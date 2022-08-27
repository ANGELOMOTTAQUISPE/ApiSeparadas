package Movement.service;

import Movement.model.Movement;
import reactor.core.publisher.Flux;

public interface IMovementService extends ICRUD<Movement, String>{
    Flux<Movement> listmovementByAccount(String accountNumber);
    Flux<Movement> listmovementByCredit(String creditCardNumber);
}
