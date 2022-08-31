package Movement.config;

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
        logger.info("Inicio Set uri data Mono " + getUrl );

        return Mono.just(getUrl).flatMap(
                u ->{
                    logger.info("Valor URL 1: "+Url );
                    logger.info("Valor URL 2: "+getUrl );
                    if(!Url.equals(getUrl)){
                        logger.info("Ingreso y le dio el valor a la URL: "+Url );
                        this.webclient =  WebClient.builder().baseUrl(u).build();
                        setUrl(getUrl);
                    }
                    return Mono.just(u);
                }
        );
    }
    public Mono<String> setUriData2(String getUrl){
        logger.info("Inicio 2 Set uri data Mono " + getUrl );
        return Mono.just(getUrl).flatMap(
                u ->{
                    if(!Url.equals(getUrl)){
                        logger.info("Ingreso y le dio el valor a la URL: "+Url );
                        this.webclient =  WebClient.builder().baseUrl(u).build();
                        setUrl(getUrl);
                    }
                    return Mono.just(u);
                }
        );
    }
}
