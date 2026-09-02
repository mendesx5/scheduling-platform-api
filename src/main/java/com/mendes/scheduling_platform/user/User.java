package com.mendes.scheduling_platform.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="users",uniqueConstraints=@UniqueConstraint(columnNames={"tenant_id","email"}))
@Getter @Setter @NoArgsConstructor
public class User {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private Long tenantId;
    @Column(nullable=false) private String name;
    @Column(nullable=false) private String email;
    @Column(length=80) private String username;
    @Column(nullable=false) private String language = "pt-BR";
    @Column(nullable=false) private boolean active = true;
    @JsonIgnore @Column(nullable=false) private String password;
    @Enumerated(EnumType.STRING) private Role role;
    public enum Role { OWNER, MANAGER, EMPLOYEE }
}
