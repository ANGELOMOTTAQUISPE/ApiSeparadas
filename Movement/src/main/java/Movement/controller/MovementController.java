package Movement.controller;

import Movement.dto.AccountMovementdto;
import Movement.dto.BankTransferetdto;
import Movement.dto.CreditMovementdto;
import Movement.model.Account;
import Movement.model.BankTransferet;
import Movement.model.Credit;
import Movement.model.Movement;
import Movement.service.IMovementService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/movement")
public class MovementController {
    private static final Logger logger = LoggerFactory.getLogger(MovementController.class);
    @Autowired
    private IMovementService service;
    @GetMapping
    public ResponseEntity<Flux<Movement>> list(){
        logger.info("Inicio metodo list() de MovementController");
        Flux<Movement> lista = null;
        try {
            lista = service.list();
        } catch (Exception e) {
            logger.info("Ocurrio un error " + e.getMessage());

        }finally {
            logger.info( "Fin metodo list() de MovementController");
        }
        return new ResponseEntity<Flux<Movement>>(lista, HttpStatus.OK);
    }
    @CircuitBreaker(name="movement", fallbackMethod = "fallBackGetCredit")
    @GetMapping("/{id}")
    public ResponseEntity<Mono<Movement>> listId(@PathVariable("id") String id){
        logger.info("Inicio metodo listId() de requestcontroller");
        Mono<Movement> p = null;
        try {
            p = service.listofId(id);

        } catch (Exception e) {
            logger.info("Ocurrio un error " + e.getMessage());

        }finally {
            logger.info( "Fin metodo listId() de requestcontroller");
        }
        return new ResponseEntity<Mono<Movement>>(p, HttpStatus.OK);

    }
    @PostMapping("/accountmovement")
    public ResponseEntity<Mono<Movement>> registeraccountmovement(@RequestBody AccountMovementdto accountmovement){
        logger.info("Inicio metodo register() de MovementController");
        Mono<Movement> p = Mono.empty();
        Account account = Account.builder()
                .idAccount( (accountmovement.getIdAccount() != null ? accountmovement.getIdAccount() : "") )
                .accountNumber( (accountmovement.getAccountNumber() != null ? accountmovement.getAccountNumber() : "") )
                .debitCardNumber( accountmovement.getDebitcardnumber())
                .build();
        logger.info("account--- " + account.toString());
        Movement movement = Movement.builder()
                .movement(accountmovement.getMovement())
                .typeMovement(accountmovement.getTypeMovement())
                .account(account)
                .movementDate(LocalDateTime.now())
                .phoneNumber(accountmovement.getPhoneNumber())
                .build();


        logger.info("movement--- " + movement.toString());

        try {
            p = service.registerAccount(movement);

        } catch (Exception e) {
            logger.info("Ocurrio un error " + e.getMessage());

        }finally {
            logger.info( "Fin metodo register() de MovementController");
        }
        return new ResponseEntity<Mono<Movement>>(p, HttpStatus.CREATED);
    }
    @PostMapping("/creditmovement")
    public ResponseEntity<Mono<Movement>> registercreditmovement(@RequestBody CreditMovementdto creditmovement){
        logger.info("Inicio metodo register() de MovementController");
        Mono<Movement> p = Mono.empty();
        Credit credit = Credit.builder()
                .idCredit(creditmovement.getIdCredit())
                .creditCardNumber(creditmovement.getCreditCardNumber())
                .build();
        Movement movement = Movement.builder()
                .movement(creditmovement.getMovement())
                .typeMovement(creditmovement.getTypeMovement())
                .credit(credit)
                .movementDate(LocalDateTime.now())
                .build();
        try {
            p = service.registerCredit(movement);

        } catch (Exception e) {
            logger.info("Ocurrio un error " + e.getMessage());

        }finally {
            logger.info( "Fin metodo register() de MovementController");
        }
        return new ResponseEntity<Mono<Movement>>(p, HttpStatus.CREATED);
    }
    @PostMapping("/transferetmovement")
    public ResponseEntity<Mono<BankTransferet>> registertransfermovement(@RequestBody BankTransferetdto transferetmovement){
        logger.info("Inicio metodo registertransfermovement() de MovementController");
        Mono<BankTransferet> p = Mono.empty();
        Account accountSource = Account.builder()
                .idAccount(transferetmovement.getIdAccountasource())
                .accountNumber(transferetmovement.getNumberAccountSource())
                .build();
        Movement movementSource = Movement.builder()
                .movement(transferetmovement.getAmount())
                .typeMovement("retirotransferencia")
                .account(accountSource)
                .movementDate(LocalDateTime.now())
                .build();
        Account accountDestination = Account.builder()
                .idAccount(transferetmovement.getIdAccountaDestination())
                .accountNumber(transferetmovement.getNumberAccountdestination())
                .build();
        Movement movementDestination = Movement.builder()
                .movement(transferetmovement.getAmount())
                .typeMovement("depositotransferencia")
                .account(accountDestination)
                .movementDate(LocalDateTime.now())
                .build();
        BankTransferet transferet = BankTransferet.builder()
                .sourceAccount(movementSource)
                .destinationAccount(movementDestination)
                .build();
        try {
            p = service.registerTransferet(transferet);

        } catch (Exception e) {
            logger.info("Ocurrio un error " + e.getMessage());

        }finally {
            logger.info( "Fin metodo register() de MovementController");
        }
        return new ResponseEntity<Mono<BankTransferet>>(p, HttpStatus.CREATED);
    }
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable("id") String id) {
        logger.info("Inicio metodo delete() de MovementController");
        return service.delete(id).map(r->ResponseEntity.ok().<Void>build()).defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @PutMapping
    public ResponseEntity<Mono<Movement>> update(@RequestBody Movement movement){
        logger.info("Inicio metodo update() de MovementController");
        Mono<Movement> p = null;
        try {
            p = service.modify(movement);

        } catch (Exception e) {
            logger.info("Ocurrio un error " + e.getMessage());

        }finally {
            logger.info( "Fin metodo update() de MovementController");
        }
        return new ResponseEntity<Mono<Movement>>(p, HttpStatus.OK);
    }
    @CircuitBreaker(name="movement", fallbackMethod = "fallBackGetCredit")
    @GetMapping("/listMovementAccount/{accountNumber}")
    public ResponseEntity<Flux<Movement>> listmovementByAccount(@PathVariable("accountNumber") String accountNumber){
        logger.info("Inicio metodo listmovementByAccount() de MovementController");
        Flux<Movement> movement = service.listmovementByAccount(accountNumber);

        return new ResponseEntity<Flux<Movement>>(movement, HttpStatus.OK);
    }
    @CircuitBreaker(name="movement", fallbackMethod = "fallBackGetCredit")
    @GetMapping("/listMovementCredit/{creditNumber}")
    public ResponseEntity<Flux<Movement>> listmovementByCredit(@PathVariable("creditNumber") String creditNumber){
        logger.info("Inicio metodo listmovementByCredit() de MovementController");
        Flux<Movement> movement = service.listmovementByCredit(creditNumber);

        return new ResponseEntity<Flux<Movement>>(movement, HttpStatus.OK);
    }
    @CircuitBreaker(name="movement", fallbackMethod = "fallBackGetCredit")
    @GetMapping("/listmovementsbydates/{iniDate}/{finDate}/{accountNumber}")
    public ResponseEntity<Flux<Movement>> listmovementByDates(@PathVariable("iniDate") String iniDate,@PathVariable("finDate") String finDate,@PathVariable("accountNumber") String accountNumber){
        logger.info("Inicio metodo listmovementByDates() de MovementController");

        Flux<Movement> movement = service.listmovementByDate(iniDate,finDate,accountNumber);

        return new ResponseEntity<Flux<Movement>>(movement, HttpStatus.OK);
    }
    public ResponseEntity<Mono<Movement>> fallBackGetCredit(String documentNumber, RuntimeException runtimeException){
        return new ResponseEntity("Microservicio Movement no funciona",HttpStatus.OK);
    }
}
