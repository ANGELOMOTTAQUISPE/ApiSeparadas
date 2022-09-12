package Client.Service.impl;

import Client.model.Client;
import Client.model.TypeClient;
import Client.repo.IClientRepo;
import Client.service.impl.ClientServiceImpl;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
class ClientServiceImplTest {
    private static final Logger logger = LoggerFactory.getLogger(ClientServiceImplTest.class);
    @Mock
    IClientRepo RepoClient;

    @InjectMocks
    ClientServiceImpl clientService;

    @Autowired
    private Mono<Client> clientMono;
    @Autowired
    private Flux<Client> clientflux;
    @Autowired
    private Client client ;
    @Autowired
    private TypeClient typeclient;

    @BeforeEach
    void ini(){
        clientMono = Mono.just(new Client("1", "PEPITO SAC", "21458963256", "RUC", new TypeClient( "empresarial", "pyme") ));
        clientflux= Flux.just(new Client("1", "PEPITO SAC", "21458963256", "RUC", new TypeClient( "empresarial", "pyme") ));
        typeclient= TypeClient.builder().clientType("empresarial").profile("pyme").build();
        client =Client.builder().idClient("1").name("PEPITO SAC").documentNumber("21458963256").documentType("RUC").typeClient(typeclient).build(); ;
    }

    @Test
    void clientbydocumentNumber() {
        Mockito.when(RepoClient.findByDocumentNumber("21458963256")).thenReturn(clientMono);
        Mono<Client> obj = clientService.clientbydocumentNumber("21458963256");
        assertEquals(clientMono, obj);
        clientMono.subscribe(x -> assertEquals("PEPITO SAC", x.getName()));
        clientMono.subscribe(y -> assertEquals("21458963256", y.getDocumentNumber()));
        clientMono.subscribe(z -> assertEquals("RUC", z.getDocumentType()));
        clientMono.subscribe(w -> assertEquals(new TypeClient("empresarial", "pyme"), w.getTypeClient()));
    }
    @Test
    void  listofId(){
        Mockito.when(clientService.listofId("1")).thenReturn(clientMono);
        Mono<Client> obj = clientService.listofId("1");
        assertEquals(clientMono, obj);
        clientMono.subscribe(x -> assertEquals("PEPITO SAC", x.getName()));
        clientMono.subscribe(y -> assertEquals("21458963256", y.getDocumentNumber()));
        clientMono.subscribe(z -> assertEquals("RUC", z.getDocumentType()));
        clientMono.subscribe(w -> assertEquals(new TypeClient("empresarial", "pyme"), w.getTypeClient()));
    }
    @Test
    void  list(){
        Mockito.when(clientService.list()).thenReturn(clientflux);
        Flux<Client> obj = clientService.list();
        assertEquals(clientflux, obj);
    }
    @Test
    void  register(){
        //Se realiza un mock de la respuesta del register()
        Mockito.when(clientService.register(client)).thenReturn(clientMono);
        //Se recrea la accion
        Mono<Client> clientregister = clientService.register(client);
        //Verificamos que la respuesta coincite y no es vacio alguno de los campos
        StepVerifier
                .create(clientregister)
                .assertNext(a -> assertNotNull(a.getDocumentNumber()))
                .expectComplete()
                .verify();
        clientregister.subscribe(x -> assertEquals("PEPITO SAC", x.getName()));
    }
    @Test
    public void modify(){
        logger.info("client: "+client.toString());

        //Se realiza un mock de la respuesta del register()
        Mockito.when(clientService.modify(client)).thenReturn(clientMono);
        //Se recrea la accion
        Mono<Client> clientregister = clientService.modify(client);
        clientregister.subscribe(System.out::println);
        //Verificamos que la respuesta coincite y no es vacio alguno de los campos
        StepVerifier
                .create(clientregister)
                .assertNext(a -> assertNotNull(a.getDocumentNumber()))
                .expectComplete()
                .verify();
        clientregister.subscribe(x -> assertEquals("PEPITO SAC", x.getName()));
    }




}