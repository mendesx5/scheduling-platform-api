package com.mendes.scheduling_platform.publicpage;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="tenant_page_settings")
@Getter @Setter @NoArgsConstructor
public class TenantPageSettings {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true) private Long tenantId;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Template template=Template.MODERN;
    private String backgroundColor, surfaceColor, textColor, primaryColor, secondaryColor, accentColor;
    private String heroTitle;
    @Column(columnDefinition="TEXT") private String heroSubtitle;
    @Column(columnDefinition="TEXT") private String heroImageUrl;
    private String heroCtaText;
    private String aboutTitle;
    @Column(columnDefinition="TEXT") private String aboutText;
    @Column(columnDefinition="TEXT") private String aboutImageUrl;
    private String inclusionsTitle;
    @Column(columnDefinition="TEXT") private String inclusionsSubtitle;
    private String galleryTitle;
    @Column(columnDefinition="TEXT") private String gallerySubtitle;
    private boolean showAbout=true, showVenues=true, showInclusions=true, showGallery=true, showLocation=true;
    public enum Template { MODERN, ELEGANT, NATURE }
}
