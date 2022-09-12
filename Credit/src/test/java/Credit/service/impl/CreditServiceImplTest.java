package Credit.service.impl;

import Credit.model.Client;
import Credit.model.Credit;
import Credit.model.TypeClient;
import Credit.repo.ICreditRepo;
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
class CreditServiceImplTest {
    private static final Logger logger = LoggerFactory.getLogger(CreditServiceImplTest.class);
    @Mock
    ICreditRepo RepoCredit;
    @InjectMocks
    CreditServiceImpl creditService;
    @Autowired
    private Mono<Credit> creditMono;
    @Autowired
    private Flux<Credit> creditflux;
    @Autowired
    private Credit credit ;
    @Autowired
    private Client client ;
    @Autowired
    private TypeClient typeclient;
    @Autowired
    private Mono<Client> clientMono;
    @BeforeEach
    void ini(){
        typeclient= TypeClient.builder().clientType("empresarial").profile("pyme").build();
        client =Client.builder().idClient("1").name("PEPITO SAC").documentNumber("21458963256").documentType("RUC").typeClient(typeclient).build();
        creditMono = Mono.just(new Credit("1","4567-7894-7894-7894",200.00,client));
        creditflux = Flux.just(new Credit("1","4567-7894-7894-7894",200.00,client));
        credit = Credit.builder().idCredit("1").creditCardNumber("4567-7894-7894-7894").creditLine(200.00).client(client).build();
        clientMono = Mono.just(new Client("1", "PEPITO SAC", "21458963256", "RUC", new TypeClient( "empresarial", "pyme") ));
    }
    @Test
    void findByApiClient() {
        CreditServiceImpl mock = org.mockito.Mockito.mock(CreditServiceImpl.class);
        Mockito.when(mock.findByApiClient(client)).thenReturn(clientMono);
        Mono<Client> clientfindapi = mock.findByApiClient(client);
        logger.info("client: "+client.toString());
        clientfindapi.subscribe(System.out::println);
        assertEquals(clientMono, clientfindapi);
        clientMono.subscribe(x -> assertEquals("PEPITO SAC", x.getName()));
        clientMono.subscribe(y -> assertEquals("21458963256", y.getDocumentNumber()));
    }

    @Test
    void register() {
        CreditServiceImpl mock = org.mockito.Mockito.mock(CreditServiceImpl.class);
        Mockito.when(mock.register(credit)).thenReturn(creditMono);
        Mono<Credit> creditregister = mock.register(credit);
        StepVerifier
                .create(creditregister)
                .assertNext(a -> assertNotNull(a.getCreditCardNumber()))
                .expectComplete()
                .verify();
        creditregister.subscribe(x -> assertEquals("4567-7894-7894-7894", x.getCreditCardNumber()));
    }

    @Test
    void modify() {

        Mockito.when(creditService.modify(credit)).thenReturn(creditMono);
        Mono<Credit> creditregister = creditService.modify(credit);
        StepVerifier
                .create(creditregister)
                .assertNext(a -> assertNotNull(a.getCreditCardNumber()))
                .expectComplete()
                .verify();
        creditregister.subscribe(x -> assertEquals("4567-7894-7894-7894", x.getCreditCardNumber()));
    }

    @Test
    void list() {
        Mockito.when(creditService.list()).thenReturn(creditflux);
        Flux<Credit> obj = creditService.list();
        assertEquals(creditflux, obj);

    }

    @Test
    void listofId() {
        Mockito.when(creditService.listofId("1")).thenReturn(creditMono);
        Mono<Credit> obj = creditService.listofId("1");
        assertEquals(creditMono, obj);
        creditMono.subscribe(x -> assertEquals("4567-7894-7894-7894", x.getCreditCardNumber()));
        creditMono.subscribe(y -> assertEquals(200.00, y.getCreditLine()));
    }

    @Test
    void listCreditByDocumentNumberClient() {
        Mockito.when(creditService.listCreditByDocumentNumberClient("21458963256")).thenReturn(creditflux);
        Flux<Credit> obj = RepoCredit.findByClient("21458963256");
        assertEquals(creditflux, obj);
        creditMono.subscribe(x -> assertEquals("4567-7894-7894-7894", x.getCreditCardNumber()));
        creditMono.subscribe(y -> assertEquals(200.00, y.getCreditLine()));
    }

    @Test
    void countCreditByDocumentNumberClient() {
        Mono<Long> count=creditflux.count();
        Mockito.when(creditService.countCreditByDocumentNumberClient("21458963256")).thenReturn(count);
        Mono<Long> obj = RepoCredit.CountByDocumentNumber("21458963256");
        assertEquals(count, obj);
        creditMono.subscribe(x -> assertEquals("4567-7894-7894-7894", x.getCreditCardNumber()));
        creditMono.subscribe(y -> assertEquals(200.00, y.getCreditLine()));
    }
}