package ec.fin.baustro.isoclient.servicios;

import ec.fin.baustro.isoclient.entidades.Definicion;
import ec.fin.baustro.isoclient.respositorios.DefinicionRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DefinicionService {

    private final DefinicionRepositorio definicionRepositorio;

    @Autowired
    public DefinicionService(DefinicionRepositorio definicionRepositorio) {
        this.definicionRepositorio = definicionRepositorio;
    }

    public Flux<Definicion> findAll() {
        return definicionRepositorio.findAll();
    }

    public Mono<Definicion> findById(Long id) {
        return definicionRepositorio.findById(id);
    }

    public Mono<Definicion> save(Definicion definicion) {
        return definicionRepositorio.save(definicion);
    }

    public Mono<Void> deleteById(Long id) {
        return definicionRepositorio.deleteById(id);
    }
}