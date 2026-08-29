package com.mendes.scheduling_platform.user;

import com.mendes.scheduling_platform.security.TenantContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserRepository users;
    private final UserService service;

    public UserController(UserRepository users, UserService service) {
        this.users = users;
        this.service = service;
    }

    record Request(@NotBlank String name, @Email @NotBlank String email,
                   @Size(min = 8) String password, @NotNull User.Role role) {}

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    List<User> list() {
        return users.findAllByTenantId(TenantContext.getRequired());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    User create(@Valid @RequestBody Request request, Authentication authentication) {
        return service.create(TenantContext.getRequired(), request.name(), request.email(),
                request.password(), request.role(), authentication);
    }
}
