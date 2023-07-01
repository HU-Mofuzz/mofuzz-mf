package de.hub.mse.client.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import de.hub.mse.client.experiment.DescriptorResponse;
import de.hub.mse.client.health.HealthReport;
import de.hub.mse.client.result.ExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static de.hub.mse.client.MofuzzDocumentClientApplication.CONFIG;

@Slf4j
public class BackendConnector {

    private static final ObjectMapper OBJECT_MAPPER = new JsonMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private static URI buildUri(String path) throws URISyntaxException {
        return new URIBuilder(CONFIG.getBackendHost())
                .setPath(path)
                .build();
    }

    private static HttpRequest createGetBuilder(String path) throws URISyntaxException {
        return HttpRequest.newBuilder()
                .GET()
                .uri(buildUri(path))
                .build();
    }

    private static <T> HttpRequest createPostBuilder(String path, T body) throws JsonProcessingException, URISyntaxException {
        String bodyString = OBJECT_MAPPER.writeValueAsString(body);
        return HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(bodyString))
                .uri(buildUri(path))
                .headers(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .build();
    }

    public void validateClientId() {
        try {
            var request = createGetBuilder("/api/v1/clients/" + CONFIG.getClientId());
            var response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() == HttpStatus.SC_NOT_FOUND) {
                throw new IllegalStateException("Invalid client id: " + CONFIG.getClientId());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to validate client id!", e);
        }
    }

    public void reportHealth(HealthReport report) {
        try {
            var request = createPostBuilder("/api/v1/report/health/" + CONFIG.getClientId(), report);
            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException | URISyntaxException e) {
            log.error("Exception while reporting health!", e);
        }
    }

    public void reportResult(ExecutionResult result) {
        try {
            var request = createPostBuilder("/api/v1/report/result", result);
            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException | URISyntaxException e) {
            log.error("Exception while reporting health!", e);
        }
    }

    public DescriptorResponse getNextFileDescriptor() {
        try {
            var request = createGetBuilder("/api/v1/execution/" + CONFIG.getClientId());
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.body() != null && !response.body().isEmpty()) {
                return OBJECT_MAPPER.readValue(response.body(), DescriptorResponse.class);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException | URISyntaxException e) {
            log.error("Exception while reporting health!", e);
        }
        return null;
    }
}
