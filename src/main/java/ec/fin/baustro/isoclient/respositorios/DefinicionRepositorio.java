package ec.fin.baustro.isoclient.respositorios;

import ec.fin.baustro.isoclient.entidades.Definicion;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;


public interface DefinicionRepositorio extends ReactiveCrudRepository<Definicion, Long> {
}
