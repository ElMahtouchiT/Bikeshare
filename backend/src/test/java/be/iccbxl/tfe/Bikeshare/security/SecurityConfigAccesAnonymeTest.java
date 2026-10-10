package be.iccbxl.tfe.Bikeshare.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comportement d'un visiteur non connecté, sur un vrai serveur : la documentation Swagger
 * n'envoie pas vers la connexion, l'API répond 401 (et non une redirection),
 * et les pages du site redirigent toujours vers /login.
 * Les redirections ne sont pas suivies, pour contrôler l'en-tête Location.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
                properties = "stripe.api.key=sk_test_factice")
class SecurityConfigAccesAnonymeTest {

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    private HttpResponse<Void> get(String chemin) throws IOException, InterruptedException {
        HttpRequest requete = HttpRequest.newBuilder(URI.create("http://localhost:" + port + chemin)).GET().build();
        return client.send(requete, HttpResponse.BodyHandlers.discarding());
    }

    @Test
    void swaggerHtml_sansConnexion_nEnvoiePasALaConnexion() throws Exception {
        HttpResponse<Void> reponse = get("/swagger-ui.html");

        assertThat(reponse.statusCode()).isBetween(300, 399);
        assertThat(reponse.headers().firstValue("Location").orElse("")).doesNotContain("/login");
    }

    @Test
    void apiAdmin_sansConnexion_repond401() throws Exception {
        assertThat(get("/api/admin/evaluations/dashboard").statusCode()).isEqualTo(401);
    }

    @Test
    void pageMembre_sansConnexion_redirigeVersLogin() throws Exception {
        HttpResponse<Void> reponse = get("/account/reservations");

        assertThat(reponse.statusCode()).isBetween(300, 399);
        assertThat(reponse.headers().firstValue("Location").orElse("")).contains("/login");
    }
}
