package Credit.service;

import Credit.model.Client;
import Credit.model.Credit;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ICreditService extends ICRUD<Credit, String>{
    Flux<Credit> listCreditByDocumentNumberClient(String documentNumber);
    public Mono<Client> findByApiClient(Client client);

}
