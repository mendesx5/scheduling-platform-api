package com.mendes.scheduling_platform.exception; import org.springframework.http.*; import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.*; import org.springframework.web.multipart.MaxUploadSizeExceededException; import java.time.*; import java.util.*;
@RestControllerAdvice public class ApiExceptionHandler {
    record ErrorResponse(Instant timestamp,int status,String error,Object details){
    }

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ErrorResponse> notFound(NotFoundException e){
        return response(HttpStatus.NOT_FOUND,e.getMessage());
    }
    @ExceptionHandler({BusinessException.class,IllegalArgumentException.class})
    ResponseEntity<ErrorResponse> bad(RuntimeException e){
        return response(HttpStatus.UNPROCESSABLE_ENTITY,e.getMessage());
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ErrorResponse> tooLarge(MaxUploadSizeExceededException e){
        return response(HttpStatus.PAYLOAD_TOO_LARGE,"Arquivo excede o tamanho máximo permitido");
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException e){
        Map<String,String> errors=new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(x->errors.put(x.getField(),x.getDefaultMessage()));
    return new ResponseEntity<>(new ErrorResponse(Instant.now(),400,"Validation failed",errors),HttpStatus.BAD_REQUEST);}

    private ResponseEntity<ErrorResponse> response(HttpStatus s,String m){
        return new ResponseEntity<>(new ErrorResponse(Instant.now(),s.value(),m,null),s);
    }
}