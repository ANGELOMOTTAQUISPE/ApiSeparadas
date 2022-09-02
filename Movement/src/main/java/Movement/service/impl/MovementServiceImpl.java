package Movement.service.impl;

import Movement.config.WebClientConfig;
import Movement.exception.ModelNotFoundException;
import Movement.model.Account;
import Movement.model.BankTransferet;
import Movement.model.Credit;
import Movement.model.Movement;
import Movement.repo.IMovementRepo;
import Movement.service.IBanktransferetService;
import Movement.service.IMovementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Calendar;

@Service
public class MovementServiceImpl implements IMovementService {
    private static final Logger logger = LoggerFactory.getLogger(MovementServiceImpl.class);
    @Autowired
    private IMovementRepo repo;
    @Autowired
    private IBanktransferetService serviceBankTransferet;
    public Mono<Credit> findCreditByid(String id){
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
    public Mono<Account> findAccountByid(String id){
        WebClientConfig webconfig = new WebClientConfig();
        return webconfig.setUriData("http://localhost:8086").flatMap(
                d -> {
                    logger.info("URL: "+d );
                    Mono<Account> clientMono = webconfig.getWebclient().get().uri("/api/account/"+id).retrieve().bodyToMono(Account.class);
                    logger.info("FIN URL: "+d );
                    return clientMono;
                }
        );
    }
    public Mono<Movement> register(Movement obj) {
        return repo.save(obj);
    }
    /*
     * Registros de Movimientos de cuentas:
     *  "typeMovement": "retiro" -> para retirar dinero de la cuenta
     *  "typeMovement": "deposito" -> para depositar dinero de la cuenta
     * */
    public Mono<Movement> registerAccount(Movement obj) {
        String typeMovement = "account";
        String accountId= obj.getAccount().getIdAccount();

        Mono<Movement> listmovementAccount = repo.findlastMovementbyAccount(obj.getAccount().getAccountNumber());
        logger.info("Lista de movimientos por cuenta: "+listmovementAccount);

        return listmovementAccount
                .switchIfEmpty( Mono.defer(() -> {
                    logger.info("No cuenta con ningun movimiento: ");
                    Double total = 0.0;
                    Boolean isEmpty = true;
                    return calculedMovement(total, obj, isEmpty, typeMovement, 0.0,0);
                }))
                .flatMap( listacc -> {
                    logger.info("Ultimo movimientos por número de cuenta: "+listacc.toString());
                    Double total = listacc.getBalance();
                    Boolean isEmpty = false;
                    return findAccountByid(accountId)
                            .switchIfEmpty( Mono.defer(() -> {
                                logger.info("No cuentas cuentas bancarias: ");
                                return Mono.empty();
                            }))
                            .flatMap( c -> {
                                logger.info("Cuenta con cuenta bancaria: "+ c.getFee().getMonthlyMovement());
                                return  calculedMovement(total, obj, isEmpty, typeMovement, 0.0,c.getFee().getMonthlyMovement());
                            });

                });
    }


    /*
    * Registros de Movimientos de  credito:
    *  "typeMovement": "pago" -> para realizar el pago de la tarjeta de credito
    *  "typeMovement": "credito" -> para realizar una compra con la tarjeta de credito
    * */
    public Mono<Movement> registerCredit(Movement obj) {
        String typeMovement = "credit";
        String creditcardNumber = obj.getCredit().getCreditCardNumber();
        String creditId = obj.getCredit().getIdCredit();

        Mono<Movement> listmovementCredit = repo.findlastMovementbyCredit(creditcardNumber);
        logger.info("Lista de movimientos por credito: "+listmovementCredit);

        return listmovementCredit
                .switchIfEmpty( Mono.defer(() -> {
                    logger.info("No cuenta con ningun movimiento: ");
                    Double total = 0.0;
                    Boolean isEmpty = true;

                    return calculedMovement(total, obj, isEmpty, typeMovement, 0.0,0);
                }))
                .flatMap( listacc -> {
                    logger.info("Ultimo movimientos por credito: "+listacc.toString());
                    Double total = listacc.getBalance();
                    Boolean isEmpty = false;
                    logger.info("creditId: "+creditId + " - creditcardNumber: " + creditcardNumber);

                    return findCreditByid(creditId)
                            .switchIfEmpty( Mono.defer(() -> {
                                logger.info("No cuentas credito: ");
                                return Mono.empty();
                            }))
                            .flatMap( c -> {
                                logger.info("con una tarjeta de credito: "+ c);
                                return calculedMovement(total, obj, isEmpty, typeMovement, c.getCreditLine(),0);
                            });

                });
    }

    /*
    * Método utilizado para realizar los montos finales de acuerdo al tipo de movimiento y se posee comisiones o no
    * El switchIfEmpty carga la informacion del movimiento (balance =0), y luego el flatmap, realiza el calculo y lo registra
    *  */
    private Mono<Movement> calculedMovement(Double total, Movement obj, Boolean isEmpty, String typeMovement, Double creditLine ,Integer monthlyMovement){
        return Mono.just(total).flatMap( t -> {
            if(isEmpty.equals(false)){

                logger.info("Cantidad de Movimientos: "+ monthlyMovement);
                if (obj.getTypeMovement().equals("retiro") || obj.getTypeMovement().equals("pago") || obj.getTypeMovement().equals("retirotransferencia") ){
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
                if (typeMovement.equals("account")){
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
                    LocalDateTime fecha = LocalDateTime.now();
                    String valormes="";
                    if(fecha.getMonthValue()<10) {
                        valormes= "0"+fecha.getMonthValue();
                    }
                    String fechaFin= fecha.getYear()+"-"+valormes+"-"+calendar.getActualMaximum(Calendar.DAY_OF_MONTH)+"T23:59:59.999Z";
                    String fechaIni= fecha.getYear()+"-"+valormes+"-0"+1+"T00:00:00.000Z";
                    logger.info("Fecha Inicio "+ fechaIni);
                    logger.info("Fecha Fin "+ fechaFin);
                    logger.info("Numero de cuenta "+ obj.getAccount().getAccountNumber());
                    Mono<Long> countMovement = repo.findmovementsbyDates(fechaIni,fechaFin,obj.getAccount().getAccountNumber()).count();

                    return countMovement
                            .flatMap( c->{
                                logger.info("cantidad de movimientos: "+c);
                                    logger.info("Posee mas movimientos: "+c);
                                    return Mono.just(obj).flatMap( m ->{
                                        logger.info("account data movement to register: "+ m.toString());
                                        Double commision= totalfinal* (c>monthlyMovement ? 0.10 : 0);//ternaria
                                        Double newtotal= totalfinal-commision;
                                        m.setBalance(newtotal);
                                        m.setCommission(commision);
                                        if(isEmpty.equals(true)){
                                            return Mono.just(m);
                                        }else{
                                            return repo.save(m);
                                        }
                                    });

                            });
                }

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
    /*
    * Metodo utilizado para registrar las transferencias bancarias, para registrar cada movimiento
    *
    * */
    public Mono<BankTransferet> registerTransferet(BankTransferet obj) {
        Movement objSource = obj.getSourceAccount();
        Movement objDestination = obj.getDestinationAccount();
        String typeMovement = "account";

        Mono<BankTransferet> BankTransferetAccount = Mono.just(obj);
        return BankTransferetAccount.flatMap( b -> {
            logger.info("save objSource: ");
            return registerMovenemtAccount(objSource, typeMovement).flatMap( x-> {
                logger.info("set objSource: "+x.getIdMovement());
                b.setSourceAccount(x);
                logger.info("save objDestination: ");
                return registerMovenemtAccount(objDestination, typeMovement).flatMap( x2 -> {
                    logger.info("set objDestination: "+x2.getIdMovement());
                    b.setDestinationAccount(x2);
                    logger.info("save BankTransferet: ");
                    return serviceBankTransferet.register(b);
                });
            });
        });

    }

    public Mono<Movement> registerMovenemtAccount(Movement obj, String typeMovement) {
        // String typeMovement = "account";
        String accountId= obj.getAccount().getIdAccount();

        Mono<Movement> listmovementAccount = repo.findlastMovementbyAccount(obj.getAccount().getAccountNumber());
        logger.info("Lista de movimientos por cuenta: "+listmovementAccount);

        return listmovementAccount
                .switchIfEmpty( Mono.defer(() -> {
                    logger.info("No cuenta con ningun movimiento: ");
                    Double total = 0.0;
                    Boolean isEmpty = true;
                    return calculedMovement(total, obj, isEmpty, typeMovement, 0.0,0);
                }))
                .flatMap( listacc -> {
                    logger.info("Ultimo movimientos por número de cuenta: "+listacc.toString());
                    Double total = listacc.getBalance();
                    Boolean isEmpty = false;
                    return findAccountByid(accountId)
                            .switchIfEmpty( Mono.defer(() -> {
                                logger.info("No cuentas cuentas bancarias: ");
                                return Mono.empty();
                            }))
                            .flatMap( c -> {
                                logger.info("Cuenta con cuenta bancaria: "+ c.getFee().getMonthlyMovement());
                                return  calculedMovement(total, obj, isEmpty, typeMovement, 0.0,c.getFee().getMonthlyMovement());
                            });

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
    public Flux<Movement> listmovementByDate( String iniDate,String finDate,String accountNumber) {
        iniDate= iniDate+"T00:00:00.000Z";
        finDate= finDate+"T23:59:59.999Z";
        return repo.findmovementsbyDates(iniDate,finDate,accountNumber);
    }

}
