package br.com.yourpethealth.form;

import br.com.yourpethealth.dto.request.RegisterRequest;
import br.com.yourpethealth.entity.enums.Perfil;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CadastroForm {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nome;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Size(max = 150, message = "E-mail deve ter no máximo 150 caracteres")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, max = 100, message = "Senha deve ter no mínimo 8 caracteres")
    private String senha;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String telefone;

    @NotNull(message = "Selecione o tipo de conta")
    private Perfil perfil;

    private String crmv;
    private String especialidade;

    public RegisterRequest paraRequest() {
        return new RegisterRequest(nome, email, senha, telefone, perfil, crmv, especialidade);
    }
}