package app;

import com.mercadopago.MercadoPagoConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    // O Spring chama isso AUTOMATICAMENTE após ligar, pegando o token do properties
    @org.springframework.beans.factory.annotation.Value("${mercadopago.access.token}")
    private String token;

    @jakarta.annotation.PostConstruct
    public void init() {
        com.mercadopago.MercadoPagoConfig.setAccessToken(token);
    }
}