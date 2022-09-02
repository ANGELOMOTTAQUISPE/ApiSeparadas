package Movement.controller;

import Movement.model.BankTransferet;
import Movement.service.IBanktransferetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@RestController
@RequestMapping("/api/banktrasferet")
public class BankTransferController {
    private static final Logger logger = LoggerFactory.getLogger(BankTransferController.class);
    @Autowired
    private IBanktransferetService service;
    @GetMapping
    public ResponseEntity<Flux<BankTransferet>> list(){
        logger.info("Inicio metodo list() de MovementController");
        Flux<BankTransferet> lista = null;
        try {
            lista = service.list();
        } catch (Exception e) {
            logger.info("Ocurrio un error " + e.getMessage());

        }finally {
            logger.info( "Fin metodo list() de MovementController");
        }
        return new ResponseEntity<>(lista, HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Mono<BankTransferet>> listId(@PathVariable("id") String id){
        logger.info("Inicio metodo listId() de requestcontroller");
        Mono<BankTransferet> p = null;
        try {
            p = service.listofId(id);

        } catch (Exception e) {
            logger.info("Ocurrio un error " + e.getMessage());

        }finally {
            logger.info( "Fin metodo listId() de requestcontroller");
        }
        return new ResponseEntity<>(p, HttpStatus.OK);

    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable("id") String id) {
        logger.info("Inicio metodo delete() de MovementController");
        return service.delete(id).map(r->ResponseEntity.ok().<Void>build()).defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @PutMapping
    public ResponseEntity<Mono<BankTransferet>> update(@RequestBody BankTransferet banktransfer){
        logger.info("Inicio metodo update() de MovementController");
        Mono<BankTransferet> p =Mono.empty();
        try {
            p = service.modify(banktransfer);

        } catch (Exception e) {
            logger.info("Ocurrio un error " + e.getMessage());

        }finally {
            logger.info( "Fin metodo update() de MovementController");
        }
        return new ResponseEntity<>(p, HttpStatus.OK);
    }

}
