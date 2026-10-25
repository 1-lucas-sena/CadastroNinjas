package lucassena.CadastroDeNinjas.ninjas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lucassena.CadastroDeNinjas.util.Rank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NinjaResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private Integer idade;
    private Rank rank;
    private Long missaoId;
}
