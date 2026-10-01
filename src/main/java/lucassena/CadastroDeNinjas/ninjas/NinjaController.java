package lucassena.CadastroDeNinjas.ninjas;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ninjas")
public class NinjaController {
    private NinjaService ninjaService;
    public NinjaController(NinjaService ninjaService) {
        this.ninjaService = ninjaService;
    }

    @GetMapping("/listar")
    public List<NinjaModel> listarNinjas() {
        return ninjaService.listarNinjas() ;
    }

    @GetMapping("/listar/{id}")
    public NinjaModel listarNinjasPorId(@PathVariable Long id) {
        return ninjaService.listarNinjasPorId(id);
    }

    @PostMapping("/criar")
    public String criarNinja() {
        return "Ninja Criada com sucesso";
    }





    @PutMapping("/alterar")
    public String alterar(){
        return "Ninja Alterada com sucesso";
    }

    @DeleteMapping("/deletar")
    public String deletar(){
        return "Ninja Deletada com sucesso";
    }
}
