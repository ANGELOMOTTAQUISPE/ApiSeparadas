package Credit.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Data
@NoArgsConstructor
public  class WebClientConfig {
    private static final Logger logger = LoggerFactory.getLogger(WebClientConfig.class);
    private WebClient webclient;
    private String Url = "";

    public Mono<String> setUriData(String getUrl){
        logger.info("Inicio Set uri data" );
        return Mono.just(getUrl).flatMap(
                u ->{
                    if(!Url.equals(getUrl)){
                        logger.info("Ingreso y le dio el valor a la URL: "+Url );
                        this.webclient =  WebClient.builder().baseUrl(u).build();
                    }
                    return Mono.just(u);
                }
        );
    }

}
