package lucassena.CadastroDeNinjas.ninjas;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NinjaService {


    private NinjaRepository ninjaRepository;

    public NinjaService(NinjaRepository ninjaRepository) {

        this.ninjaRepository = ninjaRepository;
    }

    public List<NinjaModel> listarNinjas() {
        return ninjaRepository.findAll();
    }

    public NinjaModel listarNinjasPorId(Long ninjaID) {
        Optional<NinjaModel> ninjaPorId = ninjaRepository.findById(ninjaID);
       return ninjaPorId.orElse(null);
    }
}
