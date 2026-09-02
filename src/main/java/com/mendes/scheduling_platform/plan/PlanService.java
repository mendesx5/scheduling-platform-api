package com.mendes.scheduling_platform.plan;

import com.mendes.scheduling_platform.exception.*;
import com.mendes.scheduling_platform.subscription.*;
import com.mendes.scheduling_platform.user.UserRepository;
import com.mendes.scheduling_platform.venue.VenueRepository;
import com.mendes.scheduling_platform.addon.AddonRepository;
import com.mendes.scheduling_platform.venuepackage.VenuePackageRepository;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class PlanService {
    public enum Feature { ADVANCED_PRICING, REMOVE_BRANDING, EMPLOYEE_ROLE }
    public record Limits(int maxVenues,int maxUsers,int maxAddons,int maxPackages,int maxGalleryImages){}
    public record Features(boolean advancedPricing,boolean removeBranding,boolean employeeRole){}
    private final SubscriptionRepository subscriptions; private final VenueRepository venues; private final UserRepository users;
    private final AddonRepository addons; private final VenuePackageRepository packages;

    public PlanService(SubscriptionRepository s,VenueRepository v,UserRepository u,AddonRepository addons,VenuePackageRepository packages){
        subscriptions=s;venues=v;users=u;this.addons=addons;this.packages=packages;
    }

    public String currentPlan(Long tenantId){return subscriptions.findByTenantId(tenantId).map(Subscription::getPlan).orElse("STARTER");}
    public Limits limits(Long tenantId){return limitsFor(currentPlan(tenantId));}
    public Limits limitsFor(String plan){return switch(normalize(plan)){
        case "BUSINESS" -> new Limits(5,10,50,20,Integer.MAX_VALUE);
        case "PRO" -> new Limits(3,3,15,8,5);
        default -> new Limits(1,1,5,2,1);
    };}

    public Features features(Long tenantId){return featuresFor(currentPlan(tenantId));}
    public Features featuresFor(String plan){
        return switch(normalize(plan)){
            case "PRO", "BUSINESS" -> new Features(true,true,true);
            default -> new Features(false,false,false);
        };
    }
    public boolean hasFeature(Long tenantId, Feature feature){
        Features f=features(tenantId);
        return switch(feature){
            case ADVANCED_PRICING -> f.advancedPricing();
            case REMOVE_BRANDING -> f.removeBranding();
            case EMPLOYEE_ROLE -> f.employeeRole();
        };
    }
    public void assertFeature(Long tenantId, Feature feature){
        if(!hasFeature(tenantId,feature))
            throw new BusinessException("PLAN_FEATURE_LOCKED: recurso disponível a partir do plano Pro");
    }

    public void assertCanCreateVenue(Long tenantId){Limits l=limits(tenantId);if(venues.countByTenantId(tenantId)>=l.maxVenues())throw new BusinessException("PLAN_LIMIT_REACHED: seu plano permite até "+l.maxVenues()+" espaço(s)");}
    public void assertCanCreateUser(Long tenantId){Limits l=limits(tenantId);if(users.countByTenantId(tenantId)>=l.maxUsers())throw new BusinessException("PLAN_LIMIT_REACHED: seu plano permite até "+l.maxUsers()+" usuário(s)");}
    public void assertCanCreateAddon(Long tenantId,Long venueId){Limits l=limits(tenantId);if(addons.countByTenantIdAndVenueId(tenantId,venueId)>=l.maxAddons())throw new BusinessException("PLAN_LIMIT_REACHED: seu plano permite até "+l.maxAddons()+" adicional(is) por espaço");}
    public void assertCanCreatePackage(Long tenantId,Long venueId){Limits l=limits(tenantId);if(packages.countByTenantIdAndVenueId(tenantId,venueId)>=l.maxPackages())throw new BusinessException("PLAN_LIMIT_REACHED: seu plano permite até "+l.maxPackages()+" pacote(s) por espaço");}
    public void assertGalleryLimit(Long tenantId,int requestedCount){
        int max=limits(tenantId).maxGalleryImages();
        if(requestedCount>max) throw new BusinessException("PLAN_LIMIT_REACHED: seu plano permite até "+max+" foto(s) na galeria");
    }
    private String normalize(String plan){return plan==null?"STARTER":plan.trim().toUpperCase();}
}