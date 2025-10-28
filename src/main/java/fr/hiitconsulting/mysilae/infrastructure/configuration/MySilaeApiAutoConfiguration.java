package fr.hiitconsulting.mysilae.infrastructure.configuration;

import fr.hiitconsulting.mysilae.api.client.MysilaeApi;
import fr.hiitconsulting.mysilae.auth.client.MysilaeAuthApi;
import fr.hiitconsulting.mysilae.invoker.ApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class MySilaeApiAutoConfiguration {

    private List<String> cookie;

    @Bean
    public MysilaeAuthApi mysilaeAuthApi() {
        return new MysilaeAuthApi(new ApiClient());
    }

    @Bean
    public MysilaeApi mysilaeApi() {
        return new MysilaeApi(new ApiClient(getSpecificTemplate()));
    }

    private RestTemplate getSpecificTemplate() {
        RestTemplate restTemplate = new RestTemplate(
                new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));

        // Méthode pour intercepter le changement d'authentifcation de l'API qui passe de bearer à set et check les cookies pour valider la requête
        restTemplate.getInterceptors().add((request, body, execution) -> {
            if (cookie != null && request.getURI().toString().contains("companies")) {
                request.getHeaders().addAll(HttpHeaders.COOKIE, cookie);
            }
            // Perform the actual request and capture the response
            ClientHttpResponse response = execution.execute(request, body);

            // Log or process the response headers
            if (request.getURI().toString().contains("/authenticate/bearer")) {
                cookie = response.getHeaders().get(HttpHeaders.SET_COOKIE);
            }

            return response;
        });

        return restTemplate;
    }
}
