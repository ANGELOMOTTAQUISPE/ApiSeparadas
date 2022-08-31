package Credit.controller;

import Credit.dto.Creditdto;
import Credit.model.Client;
import Credit.model.Credit;
import Credit.service.ICreditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/credit")
public class CreditController {
    private static final Logger logger = LoggerFactory.getLogger(CreditController.class);
    @Autowired
    private ICreditService service;
    @GetMapping
    public ResponseEntity<Flux<Credit>> list(){
        logger.info("Inicio metodo list() de CreditController");
        Flux<Credit> lista = null;
        String documentNumberClient ="75399757";
        Flux<Credit> credito =service.list().filter(a->a.equals(a.getClient().getDocumentNumber().equals(documentNumberClient)));
        credito.subscribe(System.out::println);
        try {
            lista = service.list();

        } catch (Exception e) {
            logger.info("Ocurrio un error " + e.getMessage());

        }finally {
            logger.info( "Fin metodo list() de CreditController");
        }
        return new ResponseEntity<Flux<Credit>>(lista, HttpStatus.OK);
    }


    @PostMapping
    public ResponseEntity<Mono<Credit>> register(@RequestBody Creditdto creditdto){
        logger.info("Inicio metodo register() de CreditController");
        Client client = Client.builder()
                .idClient(creditdto.getIdClient())
                .documentNumber(creditdto.getDocumentNumber())
                .build();
        Credit credit = Credit.builder()
                .creditCardNumber(creditdto.getCreditCardNumber())
                .creditLine(creditdto.getCreditLine())
                .client(client)
                .build();
        Mono<Credit> p = service.register(credit);

        return new ResponseEntity<Mono<Credit>>(p, HttpStatus.CREATED);
    }
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable("id") String id) {
        logger.info("Inicio metodo delete() de CreditController");
        return service.delete(id).map(r->ResponseEntity.ok().<Void>build()).defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @PutMapping
    public ResponseEntity<Mono<Credit>> update(@RequestBody Credit credit){
        logger.info("Inicio metodo update() de CreditController");
        Mono<Credit> p = null;
        try {
            p = service.modify(credit);

        } catch (Exception e) {
            logger.info("Ocurrio un error " + e.getMessage());

        }finally {
            logger.info( "Fin metodo update() de CreditController");
        }
        return new ResponseEntity<Mono<Credit>>(p, HttpStatus.OK);
    }

    @GetMapping("/documentNumber/{documentNumber}")
    public ResponseEntity<Flux<Credit>> listCreditByDocumentNumberClient(@PathVariable("documentNumber") String documentNumber){
        logger.info("Inicio metodo listCreditByDocumentNumberClient() de CreditController");
        Flux<Credit> credit = service.listCreditByDocumentNumberClient(documentNumber);

        return new ResponseEntity<Flux<Credit>>(credit, HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Mono<Credit>> listCreditById(@PathVariable("id") String id){
        logger.info("Inicio metodo listCreditById() de CreditController");
        Mono<Credit> credit = service.listofId(id);
        logger.info("FIN metodo listCreditById() de CreditController");
        return new ResponseEntity<Mono<Credit>>(credit, HttpStatus.OK);
    }
}
