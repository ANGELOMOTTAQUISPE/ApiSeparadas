package Client.service.impl;

import Client.model.Client;
import Client.model.TypeClient;
import Client.repo.IClientRepo;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
class ClientServiceImplTest {

    @Mock
    IClientRepo RepoClient;

    @InjectMocks
    ClientServiceImpl clientService;

    @Autowired
    private Mono<Client> client;

    @BeforeEach
    void ini(){
        client = Mono.just(new Client("1", "PEPITO SAC", "21458963256", "RUC", new TypeClient( "empresarial", "pyme") ));
    }

    @Test
    void clientbydocumentNumber() {
        Mockito.when(RepoClient.findByDocumentNumber("21458963256")).thenReturn(client);
        Mono<Client> obj = clientService.clientbydocumentNumber("21458963256");
        assertEquals(client, obj);
        client.subscribe(x -> assertEquals("PEPITO SAC", x.getName()));
        client.subscribe(y -> assertEquals("21458963256", y.getDocumentNumber()));
        client.subscribe(z -> assertEquals("RUC", z.getDocumentType()));
        client.subscribe(w -> assertEquals(new TypeClient("empresarial", "pyme"), w.getTypeClient()));
    }


    @Test
    void  listofId(){
        Mockito.when(clientService.listofId("1")).thenReturn(client);
        Mono<Client> obj = clientService.listofId("1");
        assertEquals(client, obj);
        System.out.println("Evaluacion listofId");
        client.subscribe(x -> assertEquals("PEPITO SAC", x.getName()));
        client.subscribe(y -> assertEquals("21458963256", y.getDocumentNumber()));
        client.subscribe(z -> assertEquals("RUC", z.getDocumentType()));
        client.subscribe(w -> assertEquals(new TypeClient("empresarial", "pyme"), w.getTypeClient()));
    }





}