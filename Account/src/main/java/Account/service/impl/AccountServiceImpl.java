package Account.service.impl;

import Account.dto.CreditServiceList;
import Account.exception.ModelNotFoundException;
import Account.model.*;
import Account.repo.IAccountRepo;
import Account.service.IAccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class AccountServiceImpl  implements IAccountService {
    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private IAccountRepo repo;
    public Mono<Client> findByApiClient(String documentNumber){
        String Uri ="http://localhost:8085/api/client/documentNumber/"+documentNumber;
        RestTemplate resTemplate= new RestTemplate();
        Client clien = resTemplate.getForObject(Uri,Client.class);
        return Mono.just(clien);
    }
    public Flux<Credit> findCreditBydocumentnumber(String documentNumber){
        //String documentNumber =credit.getClient().getDocumentNumber();
        String Uri ="http://localhost:8087/api/credit/documentNumber/"+documentNumber;
        RestTemplate resTemplate= new RestTemplate();
        CreditServiceList credit = resTemplate.getForObject(Uri, CreditServiceList.class);
        return Flux.fromIterable(credit.getCreditServices());
    }
    public Mono<Movement> registerMovementBydocumentnumber(Movement movement){
        //String documentNumber =credit.getClient().getDocumentNumber();
        String Uri ="http://localhost:8088/api/movement/accountmovement";
        RestTemplate resTemplate= new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Movement> httpEntity = new HttpEntity<>(movement, headers);
        Movement creditResultAsJson =
                resTemplate.postForObject(Uri, httpEntity, Movement.class);
        logger.info("Recibí el id del movimiento registrado " +creditResultAsJson.getIdMovement());
        return Mono.just(creditResultAsJson);
    }
    public Mono<Account> register(Account obj, Double ammountmovementInitial) {
        String documentNumber =obj.getClient().getDocumentNumber();

        return findByApiClient(documentNumber)
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
                    /* Personal VIP
                    * Cuenta de ahorro que requiere un monto mínimo de promedio diario cada mes. Adicionalmente,
                    *  para solicitar este producto el cliente debe tener una tarjeta de crédito con el banco al
                    *  momento de la creación de la cuenta.
                    * */
                    logger.info("Entra condicional " +cl.getTypeClient().getClientType());


                    if(cl.getTypeClient().getClientType().equals("personal")){
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
                                        if (AccountType.equals("a") && cl.getTypeClient().getProfile().equals("VIP")) {
                                            logger.info("El cliente es VIP y registrara una cuenta de ahorros");
                                        Flux<Credit> countCredit = findCreditBydocumentnumber(documentNumber);
                                        return countCredit.count()
                                                .flatMap( ca -> {
                                                    if(ca > 0) {
                                                        if(obj.getMinimammount() <= ammountmovementInitial){
                                                            Mono<Account> account = repo.save(obj);
                                                            logger.info("El monto minimo es suficiente");
                                                            account.doOnNext(doAc->{
                                                                Movement movement = Movement.builder()
                                                                        .movement(ammountmovementInitial)
                                                                        .typeMovement("deposito")
                                                                        .account(doAc)
                                                                        .build();

                                                                registerMovementBydocumentnumber(movement);

                                                            }).subscribe();

                                                            return account;

                                                        }else{
                                                            throw new ModelNotFoundException(" Monto mínimo no suficiente: ");
                                                        }
                                                    }else {
                                                        throw new ModelNotFoundException(" El cliente personal no tiene tarjeta de crédito: ");
                                                    }

                                                });
                                        }else {
                                            return repo.save(obj);
                                        }
                                    }
                                });
                    }else if(cl.getTypeClient().getClientType().equals("empresarial")){
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
