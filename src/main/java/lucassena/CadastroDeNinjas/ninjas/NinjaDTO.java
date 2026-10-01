package lucassena.CadastroDeNinjas.ninjas;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lucassena.CadastroDeNinjas.missoes.MissoesModel;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NinjaDTO {

    private Long  id;
    private String nome;
    private String email;
    private int idade;
    private String rank;
    private MissoesModel missoes;
}
