package com.mendes.scheduling_platform.publicpage;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="tenant_page_highlights") @Getter @Setter @NoArgsConstructor
public class TenantPageHighlight {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private Long tenantId;
    @Column(nullable=false) private String title;
    @Column(columnDefinition="TEXT") private String description;
    private String icon;
    @Column(nullable=false) private Integer sortOrder=0;
}
