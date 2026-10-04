package lucassena.CadastroDeNinjas.ninjas;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lucassena.CadastroDeNinjas.missoes.MissoesModel;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NinjaDTO {

    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    private String email;

    @NotNull(message = "Idade é obrigatória")
    @Positive(message = "Idade deve ser maior que zero")
    private Integer idade;

    @NotBlank(message = "Rank é obrigatório")
    private String rank;

    private Long missaoId;
}