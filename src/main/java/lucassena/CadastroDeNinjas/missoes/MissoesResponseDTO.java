
package lucassena.CadastroDeNinjas.missoes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lucassena.CadastroDeNinjas.util.Rank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissoesResponseDTO {

    private Long id;
    private String nome;
    private Rank rank;
    private StatusMissao status;
}
