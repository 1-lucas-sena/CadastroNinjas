
package lucassena.CadastroDeNinjas.missoes;

import org.springframework.stereotype.Component;

@Component
public class MissoesMapper {

    public MissoesModel map(MissoesRequestDTO missoesDTO) {

        MissoesModel missoesModel = new MissoesModel();

        missoesModel.setNome(missoesDTO.getNome());
        missoesModel.setRank(missoesDTO.getRank());

        return missoesModel;
    }

    public MissoesResponseDTO map(MissoesModel missoesModel) {

        MissoesResponseDTO missoesDTO = new MissoesResponseDTO();

        missoesDTO.setId(missoesModel.getId());
        missoesDTO.setNome(missoesModel.getNome());
        missoesDTO.setRank(missoesModel.getRank());
        missoesDTO.setStatus(missoesModel.getStatus());

        return missoesDTO;
    }
}
