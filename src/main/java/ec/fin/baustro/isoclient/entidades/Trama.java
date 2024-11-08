package ec.fin.baustro.isoclient.entidades;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("tramas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trama {

    @Id
    private Long id;
    private String nombre;
    private String contenido;
    private Long definicionId;
}