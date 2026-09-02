package com.mendes.scheduling_platform.billing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mendes.scheduling_platform.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class AsaasClient {
    private final RestClient client;
    private final String frontendUrl;
    private final ObjectMapper mapper = new ObjectMapper();

    public AsaasClient(RestClient.Builder builder,
                       @Value("${app.asaas.base-url:https://api-sandbox.asaas.com/v3}") String baseUrl,
                       @Value("${app.asaas.api-key:}") String apiKey,
                       @Value("${app.asaas.frontend-url:http://localhost:5173}") String frontendUrl) {
        this.frontendUrl=frontendUrl;
        this.client=builder.baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("access_token", apiKey)
            .build();
    }

    public String frontendUrl(){return frontendUrl;}

    public JsonNode createRecurringCheckout(Map<String,Object> body){
        String raw = client.post().uri("/checkouts").contentType(MediaType.APPLICATION_JSON).body(body)
            .retrieve()
            .onStatus(status -> status.isError(), (req, res) -> {
                String detail = new String(res.getBody().readAllBytes());
                throw new BusinessException("Falha ao criar checkout no Asaas: " + detail);
            })
            .body(String.class);
        try {
            return mapper.readTree(raw);
        } catch (Exception e) {
            throw new BusinessException("Resposta inválida do Asaas ao criar checkout");
        }
    }

    public void updateSubscription(String id, Map<String,Object> body){
        client.put().uri("/subscriptions/{id}",id).contentType(MediaType.APPLICATION_JSON).body(body).retrieve()
            .onStatus(status->status.isError(),(req,res)->{throw new BusinessException("Falha ao atualizar assinatura no Asaas: "+new String(res.getBody().readAllBytes()));})
            .toBodilessEntity();
    }

    public void cancelSubscription(String id){
        client.delete().uri("/subscriptions/{id}",id).retrieve().toBodilessEntity();
    }
}
