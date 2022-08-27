package Credit.service.impl;

import Credit.model.Client;
import Credit.model.Credit;
import Credit.repo.ICreditRepo;
import Credit.service.ICreditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@Service
public class CreditServiceImpl implements ICreditService {
    private static final Logger logger = LoggerFactory.getLogger(CreditServiceImpl.class);
    @Autowired
    private ICreditRepo repo;

    public Mono<Client> findByApiClient(Client client){
        String documentNumber =client.getDocumentNumber();
        String Uri ="http://localhost:8085/api/client/documentNumber/"+documentNumber;
        RestTemplate resTemplate= new RestTemplate();
        Client clien = resTemplate.getForObject(Uri,Client.class);
        return Mono.just(clien);
    }
    public Mono<Credit> register(Credit obj) {
        //Mono<Credit> p = service.register(credit);
        String documentNumber =obj.getClient().getDocumentNumber();
        return findByApiClient(obj.getClient())
                .flatMap( cl -> {
                    if(cl.getClientType().equals("personal")){
                        logger.info("personal");
                        Flux<Credit> lista = repo.findByClient(documentNumber);
                        Mono<Long> count = lista.count();
                        return count
                                .flatMap( c->{
                                    //Mono<Long> cant = null;
                                    logger.info("-- : "+c);
                                    if(c>0){
                                        logger.info("1: "+c);
                                        throw new UserAlreadyPresentException(" El cliente ya tiene un credito: "+c);
                                        //return Mono.just("El cliente ya tiene un credito");
                                    }else{
                                        logger.info("2: "+c);
                                        return repo.save(obj);
                                    }
                                });
                    }else if(cl.getClientType().equals("empresarial")){
                        logger.info("empresarial: " + obj.getIdCredit() + " - " +  obj.getCreditCardNumber());
                        return repo.save(obj);
                    }else{
                        return Mono.just(obj);
                    }
                })
                .then(  Mono.just(obj) );
    }

    public Mono<Credit> modify(Credit obj) {
        return repo.save(obj);
    }

    public Flux<Credit> list() {
        return repo.findAll();
    }

    public Mono<Credit> listofId(String id) {
        Mono<Credit> op = repo.findById(id);
        return op;
    }
    public Mono<Credit> delete(String id) {
        return repo.findById(id).flatMap(r-> repo.delete(r).then(Mono.just(r)));
    }
    public Flux<Credit> listCreditByDocumentNumberClient(String documentNumber) {
        return repo.findByClient(documentNumber);
    }

    public Mono<Long> countCreditByDocumentNumberClient(String documentNumber) {
        return repo.CountByDocumentNumber(documentNumber);
    }


    class UserAlreadyPresentException extends RuntimeException {

        public UserAlreadyPresentException(String email) {
            super("User already present with email " + email);
        }
    }
}
