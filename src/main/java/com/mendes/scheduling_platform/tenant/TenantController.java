package com.mendes.scheduling_platform.tenant;
import com.mendes.scheduling_platform.exception.NotFoundException; import com.mendes.scheduling_platform.security.TenantContext;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/tenants")
public class TenantController {
 private final TenantService service; private final TenantRepository tenants;
 public TenantController(TenantService service,TenantRepository tenants){this.service=service;this.tenants=tenants;}
 public record Register(@NotBlank String name,@NotBlank @Pattern(regexp="[a-z0-9-]+") String slug,@NotBlank String ownerName,@Email @NotBlank String email,@Size(min=8) String password,String phone,String address,@NotBlank String plan,String billingCycle){}
 @PostMapping("/register") public TenantService.RegistrationResult register(@Valid @RequestBody Register r){return service.register(new TenantService.Registration(r.name(),r.slug(),r.ownerName(),r.email(),r.password(),r.phone(),r.address(),r.plan(),r.billingCycle()));}
 @GetMapping("/me") Tenant me(){return tenants.findById(TenantContext.getRequired()).orElseThrow(()->new NotFoundException("Tenant não encontrado"));}
 public record Settings(String timezone,String dateFormat,String timeFormat,String weekStartsOn,boolean notifyNewBooking,boolean notifyCancellation,boolean notifyBookingReminder,boolean notifyEmail,boolean notifyWhatsapp){}
 @PutMapping("/me") @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
 Tenant update(@RequestBody Tenant in){
   Tenant t=me(); t.setName(in.getName()); t.setLogoUrl(in.getLogoUrl()); t.setCoverUrl(in.getCoverUrl()); t.setPrimaryColor(in.getPrimaryColor()); t.setPhone(in.getPhone()); t.setInstagram(in.getInstagram()); t.setAddress(in.getAddress());
   t.setTimezone(in.getTimezone()==null||in.getTimezone().isBlank()?t.getTimezone():in.getTimezone());
   t.setDateFormat(in.getDateFormat()==null?t.getDateFormat():in.getDateFormat()); t.setTimeFormat(in.getTimeFormat()==null?t.getTimeFormat():in.getTimeFormat());
   t.setWeekStartsOn(in.getWeekStartsOn()==null?t.getWeekStartsOn():in.getWeekStartsOn());
   t.setNotifyNewBooking(in.isNotifyNewBooking());t.setNotifyCancellation(in.isNotifyCancellation());t.setNotifyBookingReminder(in.isNotifyBookingReminder());t.setNotifyEmail(in.isNotifyEmail());t.setNotifyWhatsapp(in.isNotifyWhatsapp());
   return tenants.save(t);
 }
}
