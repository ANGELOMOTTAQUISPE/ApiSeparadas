package pe.com.nttdata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class PrjApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrjApiGatewayApplication.class, args);
	}

}
