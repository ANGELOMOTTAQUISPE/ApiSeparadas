package Account.service.impl;

import Account.config.WebClientConfig;
import Account.dto.AccountMovementdto;
import Account.exception.ModelNotFoundException;
import Account.model.*;
import Account.repo.IAccountRepo;
import Account.service.IAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class AccountServiceImpl  implements IAccountService {
    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);
    @Autowired
    private IAccountRepo repo;
    public Mono<Client> findClientByDni(String documentNumber){
        WebClientConfig webconfig = new WebClientConfig();
        logger.info("documentNumber: "+ documentNumber );
        return webconfig.setUriData("http://localhost:8085").flatMap(
                d -> {
                    logger.info("URL: "+d );
                    Mono<Client> clientMono = webconfig.getWebclient().get().uri("/api/client/documentNumber/"+documentNumber).retrieve().bodyToMono(Client.class);

                    return clientMono.flatMap( clientflatmap -> {
                                logger.info("Id del cliente buscado: "+clientflatmap.getIdClient() );
                                return Mono.just(clientflatmap);
                            }
                            );
                }
        );
    }
    public Flux<Credit> findCreditBydocumentnumber(String documentNumber){
        WebClientConfig webconfig = new WebClientConfig();
        return webconfig.setUriData("http://localhost:8087").flatMapMany(
                d -> {
                    logger.info("URL: "+d );
                    Flux<Credit> clientMono = webconfig.getWebclient().get().uri("/api/credit/documentNumber/"+documentNumber).retrieve().bodyToFlux(Credit.class);
                    return clientMono;
                }
        );
    }
    public Mono<Movement> registerMovementBydocumentnumber(AccountMovementdto movement){
        WebClientConfig webconfigregister = new WebClientConfig();

        return webconfigregister.setUriData("http://localhost:8088").flatMap(
                d -> {
                    logger.info("URL: "+d );
                    Mono<Movement> clientMono = webconfigregister
                            .getWebclient()
                            .post()
                            .uri("/api/movement/accountmovement/")
                            .accept(MediaType.APPLICATION_JSON)
                            .body(Mono.just(movement),AccountMovementdto.class)
                            .retrieve()
                            .bodyToMono(Movement.class);
                    return clientMono;
                }
        );
    }
    public Mono<Account> register(Account obj, Double ammountmovementInitial) {
        String documentNumber =obj.getClient().getDocumentNumber();
        logger.info(" Número de documento del cliente: "+documentNumber);
        return findClientByDni(documentNumber)
                .flatMap( cl -> {
                    Fee fee = new Fee();
                    logger.info(" Account Type ");
                    String AccountType=obj.getAccountType();
                    logger.info(" Profile ");
                    logger.info(" Profile 2 " + cl.getTypeClient().getProfile());
                    String profileTypeCLient=cl.getTypeClient().getProfile();

                    if(profileTypeCLient==null){
                        logger.info(" Profile null ");
                        cl.getTypeClient().setProfile("");
                    }
                    else{
                        logger.info(" Profile not null ");
                        profileTypeCLient=cl.getTypeClient().getProfile();
                    }
                    logger.info(" Account Type: "+AccountType);
                    logger.info(" Profile Type Client   : "+profileTypeCLient);
                    if (AccountType.equals("a")){
                        fee.setMonthlyMovement(5);
                        obj.setFee(fee);
                    } else if (AccountType.equals("cc")) {
                        if (profileTypeCLient.equals("PYME")){
                            fee.setMaintenanceCommission(0.0);
                        }else{
                            fee.setMaintenanceCommission(200.0);
                        }
                        fee.setMonthlyMovement(3);
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

                                    if(c > 0){
                                        logger.info(" El cliente personal ya tiene una cuenta: "+c);
                                        throw new ModelNotFoundException(" El cliente personal ya tiene una cuenta: ");
                                    }else{

                                        logger.info("El cliente puede registrar la cuenta: "+c);
                                        if (AccountType.equals("a") && cl.getTypeClient().getProfile().equals("VIP")) {
                                            logger.info("El cliente es VIP y registrara una cuenta de ahorros");
                                            Flux<Credit> countCredit = findCreditBydocumentnumber(documentNumber);

                                            return countCredit.count()
                                                .flatMap( ca -> {
                                                    logger.info("Cantidad de creditos: "+ca);
                                                    if(ca > 0) {
                                                        if(obj.getMinimammount() <= ammountmovementInitial){
                                                            logger.info("El monto minimo es suficiente");
                                                            return repo.save(obj).flatMap(doAc->{
                                                                logger.info("Datos registrados de Account: ID:"+doAc.getIdAccount()+ " ACCOUNT NUMBER"+doAc.getAccountNumber());
                                                                AccountMovementdto movement = AccountMovementdto.builder()
                                                                        .movement(ammountmovementInitial)
                                                                        .typeMovement("deposito")
                                                                        .idAccount(doAc.getIdAccount())
                                                                        .accountNumber(doAc.getAccountNumber())
                                                                        .build();

                                                                return registerMovementBydocumentnumber(movement); //Mono.just(doAc);
                                                            });
                                                        }else{
                                                            throw new ModelNotFoundException(" Monto mínimo no suficiente: ");
                                                        }
                                                    }else {
                                                        throw new ModelNotFoundException(" El cliente personal no tiene tarjeta de crédito: ");
                                                    }

                                                });
                                        }else {
                                            logger.info("No es cuenta de ahorro o no es cliente VIP " );
                                            return repo.save(obj);
                                        }
                                    }
                                });
                    }else if(cl.getTypeClient().getClientType().equals("empresarial")){
                        if( AccountType.equals("a") || AccountType.equals("pf") ){
                            throw new ModelNotFoundException(" El cliente empresarial ya tiene una cuenta: ");
                        }else{
                            if (AccountType.equals("cc") && cl.getTypeClient().getProfile().equals("PYME")) {
                                logger.info("El cliente empresarial es PYME y registrara una cuenta corriente");
                                Flux<Credit> countCredit = findCreditBydocumentnumber(documentNumber);
                                return countCredit.count()
                                        .flatMap( ca -> {
                                            logger.info("Cantidad de creditos: "+ca);
                                            if(ca > 0) {
                                                logger.info("El cliente cuenta con al menos una tarjeta de crédito: ");
                                                return repo.save(obj);
                                            }else {
                                                throw new ModelNotFoundException(" El cliente Empresarial no tiene tarjeta de crédito: ");
                                            }
                                        });
                            }
                            return repo.save(obj);
                        }
                    }else{
                        logger.info("Es otro tipo de cliente " );
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
