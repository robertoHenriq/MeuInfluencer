package app.pagamento.config;

import com.mercadopago.MercadoPagoConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MercadoPagoConfiguracao {

    @Value("${mercadopago.access.token}")
    private String accessToken;

    @PostConstruct
    public void init() {
        // Isso equivale a passar o Header 'Authorization: Bearer' no curl
        MercadoPagoConfig.setAccessToken(accessToken);
        System.out.println("SDK do Mercado Pago configurado com sucesso!");
    }
}