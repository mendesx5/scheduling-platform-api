package com.mendes.scheduling_platform.billing;

import com.fasterxml.jackson.databind.JsonNode;
import com.mendes.scheduling_platform.security.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/billing")
public class BillingController {
    private final AsaasBillingService billing;
    private final String webhookToken;

    public BillingController(AsaasBillingService billing,
                              @org.springframework.beans.factory.annotation.Value("${app.asaas.webhook-token:}") String webhookToken) {
        this.billing=billing; this.webhookToken=webhookToken;
    }

    @PostMapping("/webhooks/asaas")
    public ResponseEntity<Void> webhook(@RequestHeader(value="asaas-access-token", required=false) String token,
                                        @RequestBody JsonNode payload) {
        if(webhookToken.isBlank() || token==null || !java.security.MessageDigest.isEqual(token.getBytes(), webhookToken.getBytes()))
            return ResponseEntity.status(401).build();
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
