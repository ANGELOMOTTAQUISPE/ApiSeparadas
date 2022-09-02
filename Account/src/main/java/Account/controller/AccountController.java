package Account.controller;

import Account.dto.Accountdto;
import Account.model.Account;
import Account.model.Client;
import Account.service.IAccountService;
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
        return new ResponseEntity<Flux<Account>>(lista, HttpStatus.OK);
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
        return new ResponseEntity<Mono<Account>>(p, HttpStatus.CREATED);
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
        return new ResponseEntity<Mono<Account>>(p, HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Mono<Account>> listCreditById(@PathVariable("id") String id){
        logger.info("Inicio metodo listCreditById() de AccountController");
        Mono<Account> account = service.listofId(id);
        logger.info("FIN metodo listCreditById() de AccountController");
        return new ResponseEntity<Mono<Account>>(account, HttpStatus.OK);
    }
}
