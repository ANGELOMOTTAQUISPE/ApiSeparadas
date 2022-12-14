package Credit.service.impl;

import Credit.config.WebClientConfig;
import Credit.exception.ModelNotFoundException;
import Credit.model.Client;
import Credit.model.Credit;
import Credit.repo.ICreditRepo;
import Credit.service.ICreditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class CreditServiceImpl implements ICreditService {
    @Value("${my.property.ip}")
    private String ip;
    private static final Logger logger = LoggerFactory.getLogger(CreditServiceImpl.class);
    @Autowired
    private ICreditRepo repo;



    public Mono<Client> findByApiClient(Client client){
        String documentNumber =client.getDocumentNumber();

        WebClientConfig webconfig = new WebClientConfig();
        return webconfig.setUriData("http://"+ip+":8085").flatMap(
                d -> {
                    System.out.println("URL :" +d);
                    Mono<Client> clientMono = webconfig.getWebclient().get().uri("/api/client/documentNumber/"+documentNumber).retrieve().bodyToMono(Client.class);
                    return clientMono;
                }
        );
    }
    public Mono<Credit> register(Credit obj) {
        //Mono<Credit> p = service.register(credit);
        String documentNumber =obj.getClient().getDocumentNumber();
        return findByApiClient(obj.getClient())
                .flatMap( cl -> {
                    List<String> listtypeclient=cl.getTypeClient().getClientType();
                    logger.info("Tipo client" +listtypeclient);
                    //if(cl.getTypeClient().getClientType().equals("personal")){
                    if(listtypeclient.contains("personal") ){
                        logger.info("personal");
                        Flux<Credit> lista = repo.findByClient(documentNumber);
                        Mono<Long> count = lista.count();
                        return count
                                .flatMap( c->{
                                    logger.info("Contamos la lista de creditos: "+c);
                                    if(c>0){
                                        logger.info("Posee mas de un credito: "+c);
                                        throw new ModelNotFoundException(" El cliente ya tiene un credito: "+c);
                                        //return Mono.just("El cliente ya tiene un credito");
                                    }else{
                                        logger.info("No posee credito: "+c);
                                        return repo.save(obj);
                                    }
                                });
                    }else if(listtypeclient.contains("empresarial")){
                    //}else if(cl.getTypeClient().getClientType().equals("empresarial")){
                        logger.info("empresarial: " + obj.getIdCredit() + " - " +  obj.getCreditCardNumber());
                        return repo.save(obj);
                    }else{
                        logger.info("Es otro tipo de cliente no valido" );
                        return Mono.just(obj);
                    }
                })
                .then(  Mono.just(obj) );
    }

    public Mono<Credit> modify(Credit obj) {
        return repo.save(obj);
    }

    public Flux<Credit> list() {
        return repo.findAll();
    }

    public Mono<Credit> listofId(String id) {
        Mono<Credit> op = repo.findById(id);
        return op;
    }
    public Mono<Credit> delete(String id) {
        return repo.findById(id).flatMap(r-> repo.delete(r).then(Mono.just(r)));
    }
    public Flux<Credit> listCreditByDocumentNumberClient(String documentNumber) {
        return repo.findByClient(documentNumber);
    }

    public Mono<Long> countCreditByDocumentNumberClient(String documentNumber) {
        return repo.CountByDocumentNumber(documentNumber);
    }


}
