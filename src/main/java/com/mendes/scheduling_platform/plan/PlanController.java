package com.mendes.scheduling_platform.plan;
import com.mendes.scheduling_platform.security.TenantContext; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/plan") public class PlanController { private final PlanService service; public PlanController(PlanService service){this.service=service;} public record Current(String plan,PlanService.Limits limits){} @GetMapping Current current(){Long tenant=TenantContext.getRequired();return new Current(service.currentPlan(tenant),service.limits(tenant));} }
