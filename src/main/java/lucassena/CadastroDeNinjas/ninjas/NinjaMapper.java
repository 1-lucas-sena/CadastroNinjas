
package lucassena.CadastroDeNinjas.ninjas;

import org.springframework.stereotype.Component;

@Component
public class NinjaMapper {

    public NinjaModel map(NinjaRequestDTO ninjaDTO) {

        NinjaModel ninjaModel = new NinjaModel();

        ninjaModel.setNome(ninjaDTO.getNome());
        ninjaModel.setEmail(ninjaDTO.getEmail());
        ninjaModel.setIdade(ninjaDTO.getIdade());
        ninjaModel.setRank(ninjaDTO.getRank());

        return ninjaModel;
    }

    public NinjaResponseDTO map(NinjaModel ninjaModel) {

        NinjaResponseDTO ninjaDTO = new NinjaResponseDTO();

        ninjaDTO.setId(ninjaModel.getId());
        ninjaDTO.setNome(ninjaModel.getNome());
        ninjaDTO.setEmail(ninjaModel.getEmail());
        ninjaDTO.setIdade(ninjaModel.getIdade());
        ninjaDTO.setRank(ninjaModel.getRank());

        ninjaDTO.setMissaoId(
                ninjaModel.getMissoes() != null
                        ? ninjaModel.getMissoes().getId()
                        : null
        );

        return ninjaDTO;
    }
}
