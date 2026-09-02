package com.mendes.scheduling_platform.user;

import com.mendes.scheduling_platform.exception.*;
import com.mendes.scheduling_platform.security.TenantContext; import com.mendes.scheduling_platform.security.JwtService;
import jakarta.validation.Valid; import jakarta.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/users")
public class UserController {
 private final UserRepository users; private final UserService service; private final PasswordEncoder encoder; private final JwtService jwt;
 public UserController(UserRepository users,UserService service,PasswordEncoder encoder,JwtService jwt){this.users=users;this.service=service;this.encoder=encoder;this.jwt=jwt;}
 record Request(@NotBlank String name,@Email @NotBlank String email,@Size(min=8) String password,@NotNull User.Role role){}
 record Profile(String name,String email,String username,String language){} record ProfileResult(Profile profile,String token){}
 record UpdateProfile(@NotBlank String name,@Email @NotBlank String email,String username,String language){}
 record ChangePassword(@NotBlank String currentPassword,@Size(min=8) @NotBlank String newPassword){}
 @GetMapping @PreAuthorize("hasAnyRole('OWNER','MANAGER')") List<User> list(){return users.findAllByTenantId(TenantContext.getRequired());}
 @PostMapping @PreAuthorize("hasAnyRole('OWNER','MANAGER')") User create(@Valid @RequestBody Request r,Authentication a){return service.create(TenantContext.getRequired(),r.name(),r.email(),r.password(),r.role(),a);}
 @GetMapping("/me") Profile me(Authentication a){User u=users.findByTenantIdAndEmailIgnoreCase(TenantContext.getRequired(),a.getName()).orElseThrow(()->new NotFoundException("Usuário não encontrado"));return new Profile(u.getName(),u.getEmail(),u.getUsername(),u.getLanguage());}
 @PutMapping("/me") ProfileResult updateMe(@Valid @RequestBody UpdateProfile r,Authentication a){
   Long t=TenantContext.getRequired(); User u=users.findByTenantIdAndEmailIgnoreCase(t,a.getName()).orElseThrow(()->new NotFoundException("Usuário não encontrado"));
   if(!r.email().equalsIgnoreCase(u.getEmail()) && users.findFirstByEmailIgnoreCase(r.email()).isPresent()) throw new BusinessException("E-mail já cadastrado");
   if(r.username()!=null&&!r.username().isBlank()&&!r.username().equalsIgnoreCase((u.getUsername()==null?"":u.getUsername()))&&users.existsByTenantIdAndUsernameIgnoreCase(t,r.username())) throw new BusinessException("Nome de usuário já cadastrado");
   u.setName(r.name());u.setEmail(r.email().toLowerCase());u.setUsername(r.username()==null?null:r.username().trim().toLowerCase());u.setLanguage(validLanguage(r.language()));users.save(u);
   return new ProfileResult(new Profile(u.getName(),u.getEmail(),u.getUsername(),u.getLanguage()),jwt.issue(u.getEmail(),u.getTenantId(),u.getRole().name(),false));
 }
 @PutMapping("/me/password") void changePassword(@Valid @RequestBody ChangePassword r,Authentication a){
   User u=users.findByTenantIdAndEmailIgnoreCase(TenantContext.getRequired(),a.getName()).orElseThrow(()->new NotFoundException("Usuário não encontrado"));
   if(!encoder.matches(r.currentPassword(),u.getPassword())) throw new BusinessException("Senha atual inválida");
   u.setPassword(encoder.encode(r.newPassword()));users.save(u);
 }
 @PatchMapping("/{id}/active") @PreAuthorize("hasRole('OWNER')") User active(@PathVariable Long id,@RequestParam boolean active){
   User u=users.findByIdAndTenantId(id,TenantContext.getRequired()).orElseThrow(()->new NotFoundException("Usuário não encontrado"));
   if(u.getRole()==User.Role.OWNER && !active) throw new BusinessException("O proprietário não pode ser desativado");
   u.setActive(active);return users.save(u);
 }
 @DeleteMapping("/{id}") @PreAuthorize("hasRole('OWNER')") void delete(@PathVariable Long id){
   User u=users.findByIdAndTenantId(id,TenantContext.getRequired()).orElseThrow(()->new NotFoundException("Usuário não encontrado"));
   if(u.getRole()==User.Role.OWNER) throw new BusinessException("O proprietário não pode ser excluído");
   users.delete(u);
 }
 private String validLanguage(String language){if(language==null||language.isBlank())return "pt-BR"; if(!List.of("pt-BR","en-US","es-ES").contains(language))throw new BusinessException("Idioma inválido");return language;}
}
