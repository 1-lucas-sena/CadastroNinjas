package lucassena.CadastroDeNinjas.ninjas;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NinjaRepository extends JpaRepository<NinjaModel, Long> {

    List<NinjaModel> findByMissoes_Id(Long missaoId);
}