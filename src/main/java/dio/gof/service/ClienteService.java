package dio.gof.service;

import dio.gof.model.Cliente;

public interface ClienteService {

    Iterable<Cliente> buscarTodos();

    Cliente buscarPorCpf(Long cpf) throws Exception;

    void inserir(Cliente cliente) throws Exception;

    void atualizar(Long cpf, Cliente cliente);

    void deletar(Long cpf);

}
