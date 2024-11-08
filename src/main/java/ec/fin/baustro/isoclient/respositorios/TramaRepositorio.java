package ec.fin.baustro.isoclient.respositorios;

import ec.fin.baustro.isoclient.entidades.Trama;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface TramaRepositorio  extends ReactiveCrudRepository<Trama, Long> {
    Flux<Trama> findByDefinicionId(Long definicionId);
}
