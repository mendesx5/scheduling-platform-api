package com.mendes.scheduling_platform.platform;
import com.mendes.scheduling_platform.password.PasswordResetService; import jakarta.validation.Valid; import jakarta.validation.constraints.*; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/platform/password")
public class PlatformPasswordResetController {
 private final PasswordResetService service; public PlatformPasswordResetController(PasswordResetService s){service=s;}
 public record Email(@Email @NotBlank String email){} public record Reset(@NotBlank String token,@Size(min=8) @NotBlank String password){}
 @PostMapping("/forgot") void forgot(@Valid @RequestBody Email r){service.requestPlatform(r.email());}
 @PostMapping("/reset") void reset(@Valid @RequestBody Reset r){service.reset(r.token(),r.password());}
}
