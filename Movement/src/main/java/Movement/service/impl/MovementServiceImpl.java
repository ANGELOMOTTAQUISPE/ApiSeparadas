package Movement.service.impl;

import Movement.config.WebClientConfig;
import Movement.exception.ModelNotFoundException;
import Movement.model.Credit;
import Movement.model.Movement;
import Movement.repo.IMovementRepo;
import Movement.service.IMovementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@Service
public class MovementServiceImpl implements IMovementService {
    private static final Logger logger = LoggerFactory.getLogger(MovementServiceImpl.class);
    @Autowired
    private IMovementRepo repo;
    public Mono<Credit> findCreditBydocumentnumber(String id){
        WebClientConfig webconfig = new WebClientConfig();
        return webconfig.setUriData("http://localhost:8087").flatMap(
                d -> {
                    logger.info("URL: "+d );
                    Mono<Credit> clientMono = webconfig.getWebclient().get().uri("/api/credit/"+id).retrieve().bodyToMono(Credit.class);
                    logger.info("FIN URL: "+d );
                    return clientMono;
                }
        );
    }
    public Mono<Movement> register(Movement obj) {
        return repo.save(obj);
    }
    public Mono<Movement> registerAccount(Movement obj) {
        String typeMovement = "account";
        Mono<Movement> listmovementAccount = repo.findlastMovementbyAccount(obj.getAccount().getAccountNumber());
        logger.info("Lista de movimientos por cuenta: "+listmovementAccount);

        return listmovementAccount
                .switchIfEmpty( Mono.defer(() -> {
                    logger.info("No cuenta con ningun movimiento: ");
                    Double total = 0.0;
                    Boolean isEmpty = true;
                    return calculedMovement(total, obj, isEmpty, typeMovement, 0.0);
                }))
                .flatMap( listacc -> {
                    logger.info("Ultimo movimientos por número de cuenta: "+listacc.toString());
                    Double total = listacc.getBalance();
                    Boolean isEmpty = false;
                    return calculedMovement(total, obj, isEmpty, typeMovement, 0.0);
                });
    }

    public Mono<Movement> registerCredit(Movement obj) {
        String typeMovement = "credit";
        String creditcardNumber = obj.getCredit().getCreditCardNumber();
        String documentNumber = obj.getCredit().getIdCredit();

        Mono<Movement> listmovementCredit = repo.findlastMovementbyCredit(creditcardNumber);
        logger.info("Lista de movimientos por credito: "+listmovementCredit);

        return listmovementCredit
                .switchIfEmpty( Mono.defer(() -> {
                    logger.info("No cuenta con ningun movimiento: ");
                    Double total = 0.0;
                    Boolean isEmpty = true;

                    return calculedMovement(total, obj, isEmpty, typeMovement, 0.0);
                }))
                .flatMap( listacc -> {
                    logger.info("Ultimo movimientos por credito: "+listacc.toString());
                    Double total = listacc.getBalance();
                    Boolean isEmpty = false;
                    logger.info("documentNumber: "+documentNumber + " - creditcardNumber: " + creditcardNumber);

                    return findCreditBydocumentnumber(listacc.getCredit().getIdCredit())
                            .switchIfEmpty( Mono.defer(() -> {
                                logger.info("Vacio: ");
                                return Mono.empty();
                            }))
                            .flatMap( c -> {
                                logger.info("con datos: "+ c);
                                return calculedMovement(total, obj, isEmpty, typeMovement, c.getCreditLine());
                            });

                });
    }

    /*
     *El switchIfEmpty carga la informacion del movimiento (balance =0), y luego el flatmap, realiza el calculo y lo registra
     */
    private Mono<Movement> calculedMovement(Double total, Movement obj, Boolean isEmpty, String typeMovement, Double creditLine ){
        return Mono.just(total).flatMap( t -> {
            if(isEmpty.equals(false)){
                if (obj.getTypeMovement().equals("retiro") || obj.getTypeMovement().equals("pago")){
                    t = t - obj.getMovement();
                }else{
                    t = t + obj.getMovement();
                }
            }

            if(t < 0 ){
                throw new ModelNotFoundException(" No puede ser menor a cero: ");
            }
            else if ( isEmpty.equals(false) && typeMovement.equals("credit") && t > creditLine ){
                throw new ModelNotFoundException(" No puede superar la linea de crédito: ");
            }
            else{
                logger.info("total final: "+ t);
                Double totalfinal=t;
                return Mono.just(obj).flatMap( m ->{
                    logger.info("account data movement to register: "+ m.toString());
                    m.setBalance(totalfinal);
                    if(isEmpty.equals(true)){
                        return Mono.just(m);
                    }else{
                        return repo.save(m);
                    }
                });
            }

        });
    }

    public Mono<Movement> modify(Movement obj) {
        return repo.save(obj);
    }

    public Flux<Movement> list() {
        return repo.findAll();
    }

    public Mono<Movement> listofId(String id) {
        Mono<Movement> op = repo.findById(id);
        return op;
    }
    public Mono<Movement> delete(String id) {
        return repo.findById(id).flatMap(r-> repo.delete(r).then(Mono.just(r)));
    }
    public Flux<Movement> listmovementByAccount(String accountNumber) {
        return repo.findByAccount(accountNumber);
    }
    public Flux<Movement> listmovementByCredit( String creditCardNumber) {
        return repo.findByCredit(creditCardNumber);
    }
}
