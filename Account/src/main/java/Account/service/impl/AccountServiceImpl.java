package Account.service.impl;

import Account.exception.ModelNotFoundException;
import Account.model.Account;
import Account.model.Client;
import Account.model.Fee;
import Account.repo.IAccountRepo;
import Account.service.IAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
@Service
public class AccountServiceImpl  implements IAccountService {
    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);
    @Autowired
    private IAccountRepo repo;
    public Mono<Client> findByApiClient(Client client){
        String documentNumber =client.getDocumentNumber();
        String Uri ="http://localhost:8085/api/client/documentNumber/"+documentNumber;
        RestTemplate resTemplate= new RestTemplate();
        Client clien = resTemplate.getForObject(Uri,Client.class);
        return Mono.just(clien);
    }
    public Mono<Account> register(Account obj) {
        String documentNumber =obj.getClient().getDocumentNumber();

        return findByApiClient(obj.getClient())
                .flatMap( cl -> {
                    Fee fee = new Fee();
                    String AccountType=obj.getAccountType().toString();
                    if (AccountType.equals("a")){
                        fee.setMonthlyMovement(5);
                        obj.setFee(fee);
                    } else if (AccountType.equals("cc")) {
                        fee.setMaintenanceCommission(200.0);
                        obj.setFee(fee);
                    } else if (AccountType.equals("pf")) {
                        fee.setDate(LocalDateTime.now());
                        fee.setMonthlyMovement(1);
                        obj.setFee(fee);
                    }
                    logger.info("Entra condicional " +cl.getClientType());
                    if(cl.getClientType().equals("personal")){
                        logger.info("personal");
                        Flux<Account> lista = repo.findByAccountClient(documentNumber, AccountType);
                        Mono<Long> count = lista.count();
                        return count
                                .flatMap( c->{
                                    logger.info("-- : "+c);
                                    if(c > 0){
                                        logger.info(" El cliente personal ya tiene una cuenta: "+c);
                                        throw new ModelNotFoundException(" El cliente personal ya tiene una cuenta: "+c);
                                        //return Mono.just("El cliente ya tiene un credito");
                                    }else{
                                        logger.info("El cliente puede registrar la cuenta: "+c);
                                        return repo.save(obj);
                                    }
                                });
                    }else if(cl.getClientType().equals("empresarial")){
                        if( AccountType.equals("a") || AccountType.equals("pf") ){
                            throw new ModelNotFoundException(" El cliente empresarial ya tiene una cuenta: ");
                        }else{
                            //logger.info("empresarial: " + obj.getIdCredit() + " - " +  obj.getCreditCardNumber());
                            return repo.save(obj);
                        }
                    }else{
                        return Mono.just(obj);
                    }
                })
                .then(  Mono.just(obj) );
    }

    public Mono<Account> modify(Account obj) {
        return repo.save(obj);
    }

    public Flux<Account> list() {
        return repo.findAll();
    }

    public Mono<Account> listofId(String id) {
        Mono<Account> op = repo.findById(id);
        return op;
    }
    public Mono<Account> delete(String id) {
        return repo.findById(id).flatMap(r-> repo.delete(r).then(Mono.just(r)));
    }
}
