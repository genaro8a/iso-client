package ec.fin.baustro.isoclient.entidades;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("definiciones")
public class Definicion {
    @Id
    private long id;

    @Length(max = 100)
    private String nombre;

    @Length(max = 255)
    private String descripcion;

    @Length(max = 50)
    private String headertpdu;

    @Length(max = 200)
    private String path;
}
