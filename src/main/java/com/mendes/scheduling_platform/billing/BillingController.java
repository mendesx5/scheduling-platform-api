package com.mendes.scheduling_platform.billing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mendes.scheduling_platform.exception.BusinessException;
import com.mendes.scheduling_platform.security.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/billing")
public class BillingController {
    private final AsaasBillingService billing;
    private final String webhookToken;
    private final ObjectMapper mapper = new ObjectMapper();

    public BillingController(AsaasBillingService billing,
                              @org.springframework.beans.factory.annotation.Value("${app.asaas.webhook-token:}") String webhookToken) {
        this.billing=billing; this.webhookToken=webhookToken;
    }

    @PostMapping("/webhooks/asaas")
    public ResponseEntity<Void> webhook(@RequestHeader(value="asaas-access-token", required=false) String token,
                                        @RequestBody String rawPayload) {
        if(webhookToken.isBlank() || token==null || !java.security.MessageDigest.isEqual(token.getBytes(), webhookToken.getBytes()))
            return ResponseEntity.status(401).build();
        JsonNode payload;
        try {
            payload = mapper.readTree(rawPayload);
        } catch (Exception e) {
            throw new BusinessException("Payload de webhook inválido");
        }
        billing.handleWebhook(payload.path("event").asText(""), payload);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/checkouts/{checkoutId}")
    public AsaasBillingService.CheckoutStatus checkoutStatus(@PathVariable String checkoutId){
        return billing.checkoutStatus(checkoutId);
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancel(){
        billing.cancel(TenantContext.getRequired());
        return ResponseEntity.noContent().build();
    }
}
