package com.mendes.scheduling_platform.subscription;
import com.mendes.scheduling_platform.billing.AsaasBillingService; import com.mendes.scheduling_platform.exception.NotFoundException; import com.mendes.scheduling_platform.security.TenantContext; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/subscription")
public class SubscriptionController {
 private final SubscriptionRepository repo; private final AsaasBillingService billing;
 public SubscriptionController(SubscriptionRepository r,AsaasBillingService billing){repo=r;this.billing=billing;}
 @GetMapping Subscription current(){return repo.findByTenantId(TenantContext.getRequired()).orElseThrow(()->new NotFoundException("Assinatura não encontrada"));}
 public record ChangePlan(String plan,String billingCycle){}
 @PostMapping("/change") AsaasBillingService.Checkout change(@RequestBody ChangePlan r){return billing.requestPlanChange(TenantContext.getRequired(),r.plan(),r.billingCycle());}
}
