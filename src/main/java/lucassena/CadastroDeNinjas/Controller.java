package lucassena.CadastroDeNinjas;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class Controller {
    @GetMapping("/mensagem")
    public String mensagem(){
        return "Api Cadastro de Ninjas";

    }
}
