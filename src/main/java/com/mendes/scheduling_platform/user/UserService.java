package com.mendes.scheduling_platform.user;

import com.mendes.scheduling_platform.exception.BusinessException;
import com.mendes.scheduling_platform.plan.PlanService;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service public class UserService { private final UserRepository users; private final PasswordEncoder encoder; private final PlanService plans; public UserService(UserRepository users,PasswordEncoder encoder,PlanService plans){this.users=users;this.encoder=encoder;this.plans=plans;} public User create(Long tenantId,String name,String email,String password,User.Role role,Authentication actor){boolean owner=actor.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_OWNER"));boolean manager=actor.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_MANAGER"));if(!owner&&!manager)throw new BusinessException("Usuário sem permissão para criar contas");if(manager&&role!=User.Role.EMPLOYEE)throw new BusinessException("MANAGER só pode criar usuários EMPLOYEE");if(users.existsByTenantIdAndEmailIgnoreCase(tenantId,email))throw new BusinessException("E-mail já cadastrado");plans.assertCanCreateUser(tenantId);User u=new User();u.setTenantId(tenantId);u.setName(name);u.setEmail(email.toLowerCase());u.setPassword(encoder.encode(password));u.setRole(role);return users.save(u);} }
