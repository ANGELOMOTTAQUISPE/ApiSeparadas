package Account.controller;

import Account.dto.Accountdto;
import Account.model.Account;
import Account.model.Client;
import Account.service.IAccountService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@RestController
@RequestMapping("/api/account")
public class AccountController {
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);
    @Autowired
    private IAccountService service;
    @GetMapping
    public ResponseEntity<Flux<Account>> listar(){
        logger.info("Inicio metodo list() de AccountController");
        Flux<Account> lista = null;
        try {
            lista = service.list();

        } catch (Exception e) {
            logger.info("Ocurrio un error " + e.getMessage());

        }finally {
            logger.info( "Fin metodo list() de AccountController");
        }
        return new ResponseEntity<>(lista, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Mono<Account>> register(@RequestBody Accountdto checkingdto){
        logger.info("Inicio metodo register() de AccountController");
        Mono<Account> p = null;

        Client client = Client.builder()
                .idClient(checkingdto.getIdClient())
                .documentNumber(checkingdto.getDocumentNumber())
                .build();

        Account account = Account.builder()
                .accountNumber(checkingdto.getAccountNumber())
                .accountType(checkingdto.getAccountType())
                .minimammount(checkingdto.getMinimammount())
                .debitCardNumber(checkingdto.getDebitCardNumber())
                .client(client)
                .build();
        Double ammountmovementInitial = checkingdto.getAmmountmovementInitial();
        try {
            p = service.register(account, ammountmovementInitial );

        } catch (Exception e) {
            logger.info("Ocurrio un error " + e.getMessage());

        }finally {
            logger.info( "Fin metodo register() de AccountController");
        }
        return new ResponseEntity<>(p, HttpStatus.CREATED);
    }
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable("id") String id) {
        logger.info("Inicio metodo delete() de AccountController");
        return service.delete(id).map(r->ResponseEntity.ok().<Void>build()).defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @PutMapping
    public ResponseEntity<Mono<Account>> update(@RequestBody Account account){
        logger.info("Inicio metodo update() de AccountController");
        Mono<Account> p = null;
        try {
            p = service.modify(account);

        } catch (Exception e) {
            logger.info("Ocurrio un error " + e.getMessage());
        }finally {
            logger.info( "Fin metodo update() de AccountController");
        }
        return new ResponseEntity<>(p, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @CircuitBreaker(name="account", fallbackMethod = "fallBackGetCreditbyId")
    public ResponseEntity<Mono<Account>> listCreditById(@PathVariable("id") String id){
        logger.info("Inicio metodo listCreditById() de AccountController");
        Mono<Account> account = service.listofId(id);
        logger.info("FIN metodo listCreditById() de AccountController");
        return new ResponseEntity<>(account, HttpStatus.OK);
    }
    @GetMapping("/debitcardnumber/{debitcardnumber}")
    @CircuitBreaker(name="account", fallbackMethod = "fallBackGetCreditbyId")
    public ResponseEntity<Flux<Account>> listAccountbyDebitCardNumber(@PathVariable("debitcardnumber") String debitcardnumber){
        logger.info("Inicio metodo listCreditById() de AccountController");
        Flux<Account> account = service.listByDebitCardNumber(debitcardnumber);
        logger.info("FIN metodo listCreditById() de AccountController");
        return new ResponseEntity<>(account, HttpStatus.OK);
    }
    @GetMapping("/documentNumberandaccountType/{documentNumber}/{accountType}")
    public ResponseEntity<Flux<Account>> findaccountbydocumentNumberandaccountype(@PathVariable("documentNumber") String documentNumber,@PathVariable("accountType") String accountType){
        logger.info("Inicio metodo findaccountbyphonenumber() de AccountController");
        Flux<Account> account = service.findaccountbydocumentNumberandaccountype(documentNumber,accountType);
        logger.info("FIN metodo findaccountbyphonenumber() de AccountController");
        return new ResponseEntity<Flux<Account>>(account, HttpStatus.OK);
    }
    public ResponseEntity<Mono<String>> fallBackGetCreditbyId(@PathVariable("id") String id, RuntimeException e){
        return new ResponseEntity("Microservicio Account no funciona",HttpStatus.OK);
    }
}
