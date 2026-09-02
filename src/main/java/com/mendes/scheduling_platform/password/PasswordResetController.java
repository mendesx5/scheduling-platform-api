package com.mendes.scheduling_platform.password;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/password")
public class PasswordResetController {
 private final PasswordResetService service; public PasswordResetController(PasswordResetService s){service=s;}
 public record Email(@Email @NotBlank String email){} public record Reset(@NotBlank String token,@Size(min=8) @NotBlank String password){}
 @PostMapping("/forgot") void forgot(@Valid @RequestBody Email r){service.requestUser(r.email());}
 @PostMapping("/reset") void reset(@Valid @RequestBody Reset r){service.reset(r.token(),r.password());}
}
