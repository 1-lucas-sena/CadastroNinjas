package lucassena.CadastroDeNinjas.ninjas;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NinjaRepository extends JpaRepository<NinjaModel, Long> {
    Long id(long id);
}
