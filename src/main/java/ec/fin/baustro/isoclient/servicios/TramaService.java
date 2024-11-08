package ec.fin.baustro.isoclient.servicios;

import ec.fin.baustro.isoclient.entidades.Trama;
import ec.fin.baustro.isoclient.respositorios.TramaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TramaService {

    @Autowired
    private TramaRepositorio tramaRepositorio;

    public Flux<Trama> findAll() {
        return tramaRepositorio.findAll();
    }

    public Flux<Trama> findByDefinicionId(Long definicionId) {
        return tramaRepositorio.findByDefinicionId(definicionId);
    }

    public Mono<Trama> findById(Long id) {
        return tramaRepositorio.findById(id);
    }

    public Mono<Trama> save(Trama trama) {
        return tramaRepositorio.save(trama);
    }

    public Mono<Void> deleteById(Long id) {
        return tramaRepositorio.deleteById(id);
    }
}