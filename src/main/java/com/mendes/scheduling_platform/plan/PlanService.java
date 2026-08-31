package com.mendes.scheduling_platform.plan;

import com.mendes.scheduling_platform.exception.*;
import com.mendes.scheduling_platform.subscription.*;
import com.mendes.scheduling_platform.user.UserRepository;
import com.mendes.scheduling_platform.venue.VenueRepository;
import org.springframework.stereotype.Service;

@Service
public class PlanService {
    public enum Feature { ADDONS, PACKAGES }
    public record Limits(int maxVenues,int maxUsers,boolean addonsEnabled,boolean packagesEnabled){}
    private final SubscriptionRepository subscriptions; private final VenueRepository venues; private final UserRepository users;
    public PlanService(SubscriptionRepository s,VenueRepository v,UserRepository u){subscriptions=s;venues=v;users=u;}
    public String currentPlan(Long tenantId){return subscriptions.findByTenantId(tenantId).map(Subscription::getPlan).orElse("STARTER");}
    public Limits limits(Long tenantId){return limitsFor(currentPlan(tenantId));}
    public Limits limitsFor(String plan){return switch(normalize(plan)){case "BUSINESS" -> new Limits(5,10,true,true);case "PRO" -> new Limits(3,3,true,true);default -> new Limits(1,1,false,false);};}
    public void assertCanCreateVenue(Long tenantId){Limits l=limits(tenantId);if(venues.countByTenantId(tenantId)>=l.maxVenues())throw new BusinessException("PLAN_LIMIT_REACHED: seu plano permite até "+l.maxVenues()+" espaço(s)");}
    public void assertCanCreateUser(Long tenantId){Limits l=limits(tenantId);if(users.countByTenantId(tenantId)>=l.maxUsers())throw new BusinessException("PLAN_LIMIT_REACHED: seu plano permite até "+l.maxUsers()+" usuário(s)");}
    public void assertFeatureEnabled(Long tenantId,Feature feature){Limits l=limits(tenantId);boolean enabled=feature==Feature.ADDONS?l.addonsEnabled():l.packagesEnabled();if(!enabled)throw new BusinessException("FEATURE_NOT_AVAILABLE: recurso não disponível no seu plano");}
    private String normalize(String plan){return plan==null?"STARTER":plan.trim().toUpperCase();}
}
