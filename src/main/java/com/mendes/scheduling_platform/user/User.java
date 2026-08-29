package com.mendes.scheduling_platform.user;
import com.fasterxml.jackson.annotation.JsonIgnore; import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="users",uniqueConstraints=@UniqueConstraint(columnNames={"tenant_id","email"})) @Getter @Setter @NoArgsConstructor
public class User { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(nullable=false) private Long tenantId; @Column(nullable=false) private String name; @Column(nullable=false) private String email; @JsonIgnore @Column(nullable=false) private String password; @Enumerated(EnumType.STRING) private Role role; public enum Role { OWNER, MANAGER, EMPLOYEE } }
