package Movement.service.impl;

import Movement.config.WebClientConfig;
import Movement.exception.ModelNotFoundException;
import Movement.model.*;
import Movement.repo.IMovementRepo;
import Movement.service.IBanktransferetService;
import Movement.service.IMovementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Calendar;

@Service
public class MovementServiceImpl implements IMovementService {
    @Value("${my.property.ip}")
    private String ip;
    private static final Logger logger = LoggerFactory.getLogger(MovementServiceImpl.class);
    @Autowired
    private IMovementRepo repo;
    @Autowired
    private IBanktransferetService serviceBankTransferet;
    public Mono<Credit> findCreditByid(String id){
        WebClientConfig webconfig = new WebClientConfig();
        return webconfig.setUriData("http://"+ip+":8087").flatMap(
                d -> {
                    logger.info(" findCreditByid - INICIO URL: "+d );
                    Mono<Credit> creditMono = webconfig.getWebclient().get().uri("/api/credit/"+id).retrieve().bodyToMono(Credit.class);
                    logger.info("FIN URL: "+d +"/api/credit/");
                    return creditMono;
                }
        );
    }
    public Mono<Client> findClientbyPhoneNumber(String phoneNumber){
        WebClientConfig webconfig = new WebClientConfig();
        return webconfig.setUriData("http://"+ip+":8085").flatMap(
                d -> {
                    logger.info(" findClientbyPhoneNumber - INICIO URL: "+d );
                    Mono<Client> clientMono = webconfig.getWebclient().get().uri("/api/client/phoneNumber/"+phoneNumber).retrieve().bodyToMono(Client.class);
                    logger.info("FIN URL: "+d +"/api/client/phoneNumber/");
                    return clientMono;
                }
        );
    }
    public Flux<Account> findAccountbydocumentNumberandaccountype(String documentNumber,String accountType){
        WebClientConfig webconfig = new WebClientConfig();
        return webconfig.setUriData("http://"+ip+":8086").flatMapMany(
                d -> {
                    logger.info(" findAccountbydocumentNumberandaccountype - INICIO URL: "+d );
                    Flux<Account> accountFlux = webconfig.getWebclient().get().uri("/api/account/documentNumberandaccountType/"+documentNumber+"/"+accountType).retrieve().bodyToFlux(Account.class);
                    logger.info("FIN URL: "+d +"/api/client/phoneNumber/");
                    return accountFlux;
                }
        );
    }
    public Mono<Account> findAccountByid(String id){
        WebClientConfig webconfig = new WebClientConfig();
        return webconfig.setUriData("http://"+ip+":8086").flatMap(
                d -> {
                    logger.info("findAccountByid- INICIO URL: "+d );
                    Mono<Account> clientMono = webconfig.getWebclient().get().uri("/api/account/"+id).retrieve().bodyToMono(Account.class);
                    logger.info("FIN URL: "+d+"/api/account/" );
                    return clientMono;
                }
        );
    }
    public Flux<Account> listAccountByDebitCardNumber(String DebitCardNumber){
        WebClientConfig webconfig = new WebClientConfig();
        return webconfig.setUriData("http://"+ip+":8086").flatMapMany(
                d -> {
                    logger.info("listAccountByDebitCardNumber -INICIO URL: "+d );
                    Flux<Account> accountFlux = webconfig.getWebclient().get().uri("api/account/debitcardnumber/"+DebitCardNumber).retrieve().bodyToFlux(Account.class);
                    logger.info("FIN URL: "+d + "api/account/debitcardnumber/");
                    return accountFlux;
                }
        );
    }
    /*Registro sin validaciones del CRUD*/
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
        logger.info("registerAccount-INICIO : "+ obj.toString() );
        if(obj.getPhoneNumber() == null){
            return processTypeMovement(obj,typeMovement);
        }
        else{
            return findClientbyPhoneNumber(obj.getPhoneNumber()).flatMap(
                d->{
                    return findAccountbydocumentNumberandaccountype(d.getDocumentNumber(),"m")
                            .collectList()
                            .flatMap( c ->{
                                logger.info("1 ---------------> c " + c.toString());
                                Account accountMUser = c.get(0);
                                logger.info("2 ---------------> accountMUser " + accountMUser.toString());

                                Account account = Account.builder()
                                            .idAccount( (accountMUser.getIdAccount() != null ? accountMUser.getIdAccount() : "") )
                                            .accountNumber( (accountMUser.getAccountNumber() != null ? accountMUser.getAccountNumber() : "") )
                                            //SI LA TARJETA ASOCIADA SI NO ES NULL SE USA ESA TARJETA ASOCIADA EN EL MOVIMIENTO COMO TARJETA DE DEBITO
                                            .debitCardNumber( accountMUser.getAssociatedCard() )
                                            .associatedCard( accountMUser.getAssociatedCard() )
                                            .build();

                                obj.setAccount(account);

                                logger.info("---------------> Movimiento Monedero " + obj.toString());

                                return processTypeMovement(obj, typeMovement);

                            });
                } );

        }
        //Mono<Account> accountbyId = listAccountByDebitCardNumber(obj.getAccount().getDebitCardNumber());


    }
    //Para casos en que se tenga tarjeta de debito , para depostiar es necesario escoger a que cuenta se abona el dinero, pero al retirar si es automatico el cobro dependiendo del saldo en las cuentas
    //de la tarjeta
    public  Mono<Movement> processTypeMovement( Movement obj, String typeMovement){
        if ( obj.getAccount().getDebitCardNumber() != null && (obj.getTypeMovement().equals("retiro") || obj.getTypeMovement().equals("pago") || obj.getTypeMovement().equals("retirotransferencia")) ){

            Flux<Account> listaccountbyDebitCard = listAccountByDebitCardNumber(obj.getAccount().getDebitCardNumber());
            //listar las cuentas que posee una tarjeta de debito
            Flux<Movement> listmovement = listaccountbyDebitCard.flatMap(
                    p -> {

                        logger.info("cuentas de debito "+p.toString());
                        String accountId= p.getIdAccount();
                        Boolean isRegistro = false;
                        //balance de cuenta, id cuenta, idmovimiento
                        return findAndSaveMovements( p.getAccountNumber(), obj,  typeMovement,  accountId, isRegistro);
                    }
            );

            return listmovement.collectList().flatMap( cl -> {

                logger.info("----- Movimientos para procesar" + cl.toString());
                /*
                 *
                 * */
                Mono<Movement> mbObj = Mono.just(cl.stream()
                        .filter(mv ->  !mv.getIdMovement().equals("") )
                        .findFirst()
                        .orElse( new Movement() ));

                return mbObj.flatMap( mv ->{
                    logger.info("Movimiento con balnace encontrado: " + mv.toString() );

                    String accountId= mv.getAccount().getIdAccount();
                    Boolean isRegistro = true;
                    obj.setIdMovement(null);
                    Account account = Account.builder()
                            .idAccount( (mv.getAccount().getIdAccount() != null ? mv.getAccount().getIdAccount() : "") )
                            .accountNumber( (mv.getAccount().getAccountNumber() != null ? mv.getAccount().getAccountNumber() : "") )
                            .debitCardNumber(mv.getAccount().getDebitCardNumber())
                            .build();
                    obj.setAccount(account);

                    return findAndSaveMovements( mv.getAccount().getAccountNumber(), obj,  typeMovement,  accountId, isRegistro);

                });

            });
        }else{
            logger.info("cuentas de debito ----------- ");
            String accountId= obj.getAccount().getIdAccount();
            Boolean isRegistro = true;
            return findAndSaveMovements( obj.getAccount().getAccountNumber(),   obj,  typeMovement,  accountId, isRegistro);
        }
    }

    //Recorre N veces la cantidad de cuentas que posee una tarjeta, encontrando en cada cuenta su ultimo movimiento para poder verificar el saldo sufiente
    //para la operacion
    private Mono<Movement> findAndSaveMovements(String accountNumber,  Movement obj, String typeMovement, String accountId, Boolean isRegistro){
        Mono<Movement> listmovementAccount = repo.findlastMovementbyAccount(accountNumber);

        logger.info("--Lista de movimientos por cuenta : "+ accountNumber);
        return listmovementAccount
                .switchIfEmpty( Mono.defer(() -> {
                    logger.info("----No cuenta con ningun movimiento: ");
                    Double total = 0.0;
                    Boolean isEmpty = true;

                    return calculedMovement(total, obj, isEmpty, typeMovement, 0.0,0, true);
                }))
                .flatMap( listacc -> {
                    logger.info("----Ultimo movimientos por número de cuenta: "+listacc.toString());

                    if(isRegistro.equals(false)){
                        Account account = Account.builder()
                                .idAccount( (listacc.getAccount().getIdAccount() != null ? listacc.getAccount().getIdAccount() : "") )
                                .accountNumber( (listacc.getAccount().getAccountNumber() != null ? listacc.getAccount().getAccountNumber() : "") )
                                .debitCardNumber(obj.getAccount().getDebitCardNumber())
                                .build();
                        listacc.setAccount( account );
                    }

                    Double total = listacc.getBalance() != null ? listacc.getBalance(): 0.0;

                    Boolean isEmpty = false;
                    return findAccountByid(accountId)
                            .switchIfEmpty( Mono.defer(() -> {
                                logger.info("No cuentas cuentas bancarias: ");
                                return Mono.empty();
                            }))
                            .flatMap( c -> {
                                logger.info("Cuenta con cuenta bancaria 1: ");
                                Integer monthlyMovement = c.getFee() != null ? (c.getFee().getMonthlyMovement() != null ? c.getFee().getMonthlyMovement() : 0 ) : 0;
                                return calculedMovement(total, (isRegistro.equals(true) ? obj : listacc), isEmpty, typeMovement, 0.0, monthlyMovement , isRegistro );

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
                    Boolean isRegistro = true;
                    return calculedMovement(total, obj, isEmpty, typeMovement, 0.0,0, isRegistro);
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
                                Boolean isRegistro = true;
                                return calculedMovement(total, obj, isEmpty, typeMovement, c.getCreditLine(),0, isRegistro);
                            });

                });
    }

    /*
    * Método utilizado para realizar los montos finales de acuerdo al tipo de movimiento y se posee comisiones o no
    * El switchIfEmpty carga la informacion del movimiento (balance =0), y luego el flatmap, realiza el calculo y lo registra
    *  */
    private Mono<Movement> calculedMovement(Double total, Movement obj, Boolean isEmpty, String typeMovement, Double creditLine ,Integer monthlyMovement, Boolean isRegistro){

        logger.info("Inicio calculedMovement : "+ total );
        return Mono.just(total).flatMap( t -> {

            logger.info("2 Inicio calculedMovement : "+ isRegistro);

            if(isEmpty.equals(false)){

                logger.info("Cantidad de Movimientos: "+ monthlyMovement);
                if (obj.getTypeMovement().equals("retiro") || obj.getTypeMovement().equals("pago") || obj.getTypeMovement().equals("retirotransferencia") ){
                    t = t - obj.getMovement();
                }else{
                    t = t + obj.getMovement();
                }
            }


            if(t < 0 || isRegistro.equals(false)){
                if(typeMovement.equals("account")){
                    if(t < 0){
                        Movement mv = new Movement();
                        return Mono.just(mv);
                    }else{
                        return Mono.just(obj);
                    }
                }else{
                    throw new ModelNotFoundException(" No puede ser menor a cero: ");
                }
            }
            else if ( isEmpty.equals(false) && typeMovement.equals("credit") && t > creditLine ){
                throw new ModelNotFoundException(" No puede superar la linea de crédito: ");
            }
            else{
                logger.info("total final: "+ t);
                Double totalfinal=t;
                if (typeMovement.equals("account")){

                    if(obj.getPhoneNumber() != null && obj.getAccount().getAssociatedCard() == null){

                        return Mono.just(obj).flatMap( m ->{
                            logger.info("account data movement to register: "+ m.toString());
                            Double newtotal= totalfinal;
                            m.setBalance(newtotal);
                            m.setCommission(0.0);
                            if(isEmpty.equals(true)){
                                return Mono.just(m);
                            }else{
                                return repo.save(m);
                            }
                        });
                    }else{
                        obj.getAccount().setAssociatedCard(null);

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
                }else{

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
    /*
    * metodo para registrar transacciones
    * */
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
                    Boolean isRegistro = true;
                    return calculedMovement(total, obj, isEmpty, typeMovement, 0.0,0, isRegistro);
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
                                logger.info("Cuenta con cuenta bancaria 2: "+ c.getFee().getMonthlyMovement());
                                Boolean isRegistro = true;
                                return  calculedMovement(total, obj, isEmpty, typeMovement, 0.0,c.getFee().getMonthlyMovement(), isRegistro);
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
