package com.mendes.scheduling_platform.publicpage;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="tenant_page_gallery_images") @Getter @Setter @NoArgsConstructor
public class TenantPageGalleryImage {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private Long tenantId;
    @Column(nullable=false,columnDefinition="TEXT") private String imageUrl;
    private String altText;
    @Column(nullable=false) private Integer sortOrder=0;
}
