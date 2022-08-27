package Account.service;

import Account.model.Account;
import Account.model.Client;
import reactor.core.publisher.Mono;

public interface IAccountService extends ICRUD<Account, String>{
    public Mono<Client> findByApiClient(Client client);
}
