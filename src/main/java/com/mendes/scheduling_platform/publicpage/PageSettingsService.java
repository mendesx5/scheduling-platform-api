package com.mendes.scheduling_platform.publicpage;

import com.mendes.scheduling_platform.tenant.*;
import com.mendes.scheduling_platform.venue.*;
import com.mendes.scheduling_platform.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class PageSettingsService {
    private final TenantPageSettingsRepository settings;
    private final TenantPageGalleryImageRepository gallery;
    private final TenantPageHighlightRepository highlights;
    private final TenantRepository tenants;
    private final VenueRepository venues;

    public PageSettingsService(TenantPageSettingsRepository s,TenantPageGalleryImageRepository g,TenantPageHighlightRepository h,TenantRepository t,VenueRepository v){
        settings=s;gallery=g;highlights=h;tenants=t;venues=v;
    }

    public TenantPageSettings get(Long tenantId){ return settings.findByTenantId(tenantId).orElseGet(()->defaults(tenantId)); }
    @Transactional public TenantPageSettings save(Long tenantId,TenantPageSettings input){
        TenantPageSettings current=get(tenantId); current.setTenantId(tenantId);
        current.setTemplate(input.getTemplate()); current.setBackgroundColor(input.getBackgroundColor()); current.setSurfaceColor(input.getSurfaceColor());
        current.setTextColor(input.getTextColor()); current.setPrimaryColor(input.getPrimaryColor()); current.setSecondaryColor(input.getSecondaryColor()); current.setAccentColor(input.getAccentColor());
        current.setHeroTitle(input.getHeroTitle()); current.setHeroSubtitle(input.getHeroSubtitle()); current.setHeroImageUrl(input.getHeroImageUrl()); current.setHeroCtaText(input.getHeroCtaText());
        current.setAboutTitle(input.getAboutTitle()); current.setAboutText(input.getAboutText()); current.setAboutImageUrl(input.getAboutImageUrl());
        current.setInclusionsTitle(input.getInclusionsTitle()); current.setInclusionsSubtitle(input.getInclusionsSubtitle()); current.setGalleryTitle(input.getGalleryTitle()); current.setGallerySubtitle(input.getGallerySubtitle());
        current.setShowAbout(input.isShowAbout()); current.setShowVenues(input.isShowVenues()); current.setShowInclusions(input.isShowInclusions()); current.setShowGallery(input.isShowGallery()); current.setShowLocation(input.isShowLocation());
        return settings.save(current);
    }
    public List<TenantPageGalleryImage> gallery(Long tenantId){return gallery.findAllByTenantIdOrderBySortOrderAsc(tenantId);}
    @Transactional public List<TenantPageGalleryImage> saveGallery(Long tenantId,List<TenantPageGalleryImage> items){
        gallery.deleteByTenantId(tenantId); if(items==null)return List.of(); int order=0;
        for(TenantPageGalleryImage item:items){ if(item.getImageUrl()==null||item.getImageUrl().isBlank())continue; item.setId(null);item.setTenantId(tenantId);item.setSortOrder(order++);gallery.save(item); }
        return gallery(tenantId);
    }
    public List<TenantPageHighlight> highlights(Long tenantId){return highlights.findAllByTenantIdOrderBySortOrderAsc(tenantId);}
    @Transactional public List<TenantPageHighlight> saveHighlights(Long tenantId,List<TenantPageHighlight> items){
        highlights.deleteByTenantId(tenantId); if(items==null)return List.of(); int order=0;
        for(TenantPageHighlight item:items){if(item.getTitle()==null||item.getTitle().isBlank())continue;item.setId(null);item.setTenantId(tenantId);item.setSortOrder(order++);highlights.save(item);}
        return highlights(tenantId);
    }
    public PublicPageData publicPage(String slug){
        Tenant tenant=tenants.findBySlug(slug).filter(t->t.getStatus()==Tenant.TenantStatus.ACTIVE).orElseThrow(()->new NotFoundException("Estabelecimento não encontrado"));
        return new PublicPageData(tenant,get(tenant.getId()),gallery(tenant.getId()),highlights(tenant.getId()),venues.findAllByTenantIdAndActiveTrue(tenant.getId()));
    }
    private TenantPageSettings defaults(Long tenantId){
        TenantPageSettings p=new TenantPageSettings();p.setTenantId(tenantId);p.setTemplate(TenantPageSettings.Template.MODERN);
        p.setBackgroundColor("#fffdf8");p.setSurfaceColor("#ffffff");p.setTextColor("#172018");p.setPrimaryColor("#244b36");p.setSecondaryColor("#dbe8c9");p.setAccentColor("#d6a85f");
        p.setHeroTitle("Seu próximo momento começa aqui.");p.setHeroSubtitle("Escolha seu espaço, veja a disponibilidade e reserve online.");p.setHeroCtaText("Reservar agora");
        p.setAboutTitle("Um espaço pensado para bons momentos.");p.setInclusionsTitle("O que você encontra aqui");p.setGalleryTitle("Conheça cada detalhe");
        return p;
    }
    public record PublicPageData(Tenant tenant,TenantPageSettings settings,List<TenantPageGalleryImage> gallery,List<TenantPageHighlight> highlights,List<Venue> venues){}
}
