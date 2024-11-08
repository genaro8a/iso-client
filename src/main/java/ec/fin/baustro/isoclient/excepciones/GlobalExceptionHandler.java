package ec.fin.baustro.isoclient.excepciones;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<Void> manejarExcepcionEstado(ServerWebExchange exchange, ResponseStatusException ex) {
        log.error("Error: {}", ex.getMessage(), ex);
        exchange.getResponse().setStatusCode(ex.getStatusCode());
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String mensaje = "{\"error\": \"" + ex.getReason() + "\"}";
        byte[] bytes = mensaje.getBytes();
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    @ExceptionHandler(Exception.class)
    public Mono<Void> manejarExcepcionGeneral(ServerWebExchange exchange, Exception ex) {
        log.error("Error: {}", ex.getMessage(), ex);
        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String mensaje = "{\"error\": \"Error interno del servidor: " + ex.getMessage() + "\"}";
        byte[] bytes = mensaje.getBytes();
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    @ExceptionHandler(ExcepcionPersonalizada.class)
    public Mono<Void> handleSaldoNoDisponibleException(ServerWebExchange exchange, ExcepcionPersonalizada ex) {
        log.error("Error: {}", ex.getMessage(), ex);
        exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String mensaje = "{\"error\": \"" + ex.getMessage() + "\"}";
        byte[] bytes = mensaje.getBytes();
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }
}
