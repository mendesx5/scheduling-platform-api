package com.mendes.scheduling_platform.plan;

import com.mendes.scheduling_platform.exception.*;
import com.mendes.scheduling_platform.subscription.*;
import com.mendes.scheduling_platform.user.UserRepository;
import com.mendes.scheduling_platform.venue.VenueRepository;
import com.mendes.scheduling_platform.addon.AddonRepository;
import com.mendes.scheduling_platform.venuepackage.VenuePackageRepository;
import org.springframework.stereotype.Service;

@Service
public class PlanService {
    public enum Feature { ADDONS, PACKAGES }
    // maxAddons/maxPackages are per-venue limits. Integer.MAX_VALUE is used to mean "unlimited".
    public record Limits(int maxVenues,int maxUsers,int maxAddons,int maxPackages){}
    private final SubscriptionRepository subscriptions; private final VenueRepository venues; private final UserRepository users;
    private final AddonRepository addons; private final VenuePackageRepository packages;

    public PlanService(SubscriptionRepository s,VenueRepository v,UserRepository u,AddonRepository addons,VenuePackageRepository packages){
        subscriptions=s;venues=v;users=u;this.addons=addons;this.packages=packages;
    }

    public String currentPlan(Long tenantId){return subscriptions.findByTenantId(tenantId).map(Subscription::getPlan).orElse("STARTER");}
    public Limits limits(Long tenantId){return limitsFor(currentPlan(tenantId));}
    public Limits limitsFor(String plan){return switch(normalize(plan)){
        case "BUSINESS" -> new Limits(5,10,50,20);
        case "PRO" -> new Limits(3,3,15,8);
        default -> new Limits(1,1,5,2); // Básico/STARTER
    };}

    public void assertCanCreateVenue(Long tenantId){Limits l=limits(tenantId);if(venues.countByTenantId(tenantId)>=l.maxVenues())throw new BusinessException("PLAN_LIMIT_REACHED: seu plano permite até "+l.maxVenues()+" espaço(s)");}
    public void assertCanCreateUser(Long tenantId){Limits l=limits(tenantId);if(users.countByTenantId(tenantId)>=l.maxUsers())throw new BusinessException("PLAN_LIMIT_REACHED: seu plano permite até "+l.maxUsers()+" usuário(s)");}

    public void assertCanCreateAddon(Long tenantId,Long venueId){
        Limits l=limits(tenantId);
        if(addons.countByTenantIdAndVenueId(tenantId,venueId)>=l.maxAddons())
            throw new BusinessException("PLAN_LIMIT_REACHED: seu plano permite até "+l.maxAddons()+" adicional(is) por espaço");
    }
    public void assertCanCreatePackage(Long tenantId,Long venueId){
        Limits l=limits(tenantId);
        if(packages.countByTenantIdAndVenueId(tenantId,venueId)>=l.maxPackages())
            throw new BusinessException("PLAN_LIMIT_REACHED: seu plano permite até "+l.maxPackages()+" pacote(s) por espaço");
    }

    private String normalize(String plan){return plan==null?"STARTER":plan.trim().toUpperCase();}
}