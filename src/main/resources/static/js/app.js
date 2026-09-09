/**
 * Confirmação de remoção.
 * O nome vem por data-nome porque o Thymeleaf não permite interpolar
 * strings dentro de atributos de evento (proteção contra injeção).
 */
function confirmarRemocao(form) {
    const nome = form.dataset.nome;
    return confirm('Remover ' + nome + '? Esta ação não pode ser desfeita.');
}

/**
 * Cadastro: mostra CRMV e especialidade só quando o perfil é veterinário.
 */
function alternarCamposVeterinario() {
    const perfil = document.getElementById('perfil');
    const campos = document.getElementById('camposVeterinario');
    if (!perfil || !campos) return;

    campos.style.display = perfil.value === 'VETERINARIO' ? 'block' : 'none';
}

document.addEventListener('DOMContentLoaded', function () {
    const perfil = document.getElementById('perfil');
    if (perfil) {
        perfil.addEventListener('change', alternarCamposVeterinario);
        alternarCamposVeterinario();   // mantém o estado ao voltar com erro
    }
});