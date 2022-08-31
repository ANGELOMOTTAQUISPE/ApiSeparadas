package Account.service;

import Account.dto.AccountMovementdto;
import Account.model.Account;
import Account.model.Client;
import Account.model.Credit;
import Account.model.Movement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IAccountService extends ICRUD<Account, String>{
    public Mono<Client> findClientByDni(String documentNumber);
    public Flux<Credit> findCreditBydocumentnumber(String documentNumber);
    public Mono<Movement> registerMovementBydocumentnumber(AccountMovementdto movement);

}
