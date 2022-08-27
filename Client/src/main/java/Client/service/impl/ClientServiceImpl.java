package Client.service.impl;

import Client.model.Client;
import Client.repo.IClientRepo;
import Client.service.IClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ClientServiceImpl implements IClientService {
    @Autowired
    private IClientRepo repo;

    public Mono<Client> register(Client obj) {
        return repo.save(obj);
    }

    public Mono<Client> modify(Client obj) {
        return repo.save(obj);
    }

    public Flux<Client> list() {
        return repo.findAll();
    }

    public Mono<Client> listofId(String id) {
        Mono<Client> op = repo.findById(id);
        return op;
    }

    public Mono<Client> delete(String id) {
        return repo.findById(id).flatMap(r-> repo.delete(r).then(Mono.just(r)));
    }

    public Mono<Client> clientbydocumentNumber (String documentNumber){
        return repo.findByDocumentNumber(documentNumber);
    }
}
