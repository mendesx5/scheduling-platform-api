package com.mendes.scheduling_platform.booking; import com.mendes.scheduling_platform.security.TenantContext; import org.springframework.web.bind.annotation.*; import java.util.*; import java.time.*;
@RestController @RequestMapping("/bookings")
public class BookingController {

    private final BookingService service;

    public BookingController(BookingService s){
        service=s;
    }
    record StatusRequest(Booking.Status status,Booking.PaymentStatus paymentStatus){

    }

    @GetMapping List<Booking> list(){
        return service.list(TenantContext.getRequired());
    }

    //@GetMapping("/calendar") List<Booking> calendar(@RequestParam OffsetDateTime start,@RequestParam OffsetDateTime end){
    //    return service.calendar(TenantContext.getRequired(),start,end);
    //}
    @PatchMapping("/{id}") Booking update(@PathVariable Long id,@RequestBody StatusRequest r){
        return service.status(TenantContext.getRequired(),id,r.status(),r.paymentStatus());
    }
}
