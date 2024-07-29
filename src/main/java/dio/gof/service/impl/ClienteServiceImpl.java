package dio.gof.service.impl;

import dio.gof.model.Cliente;
import dio.gof.model.ClienteRepository;
import dio.gof.model.Endereco;
import dio.gof.model.EnderecoRepository;
import dio.gof.service.ClienteService;
import dio.gof.service.ViaCepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private EnderecoRepository enderecoRepository;
    @Autowired
    private ViaCepService viaCepService;

    @Override
    public Iterable<Cliente> buscarTodos() {
        return clienteRepository.findAll();
    }

    @Override
    public Cliente buscarPorCpf(Long cpf) throws Exception {
        Optional<Cliente> cliente = clienteRepository.findById(cpf);
        if (cliente.isPresent()) {
            return cliente.get();
        }
        throw new RuntimeException("Cliente não encontrado.");
    }

    @Override
    public void inserir(Cliente cliente) throws Exception {
        if (!clienteRepository.existsById(cliente.getCpf())) {
            salvarClienteComCep(cliente);
        } else {
            throw new RuntimeException("Cliente já cadastrado.");
        }
    }

    @Override
    public void atualizar(Long cpf, Cliente cliente) {
        Optional<Cliente> clienteBd = clienteRepository.findById(cpf);
        if (clienteBd.isPresent()) {
            Cliente clienteEncontrado = clienteBd.get();
            if (cliente.getNome() != null) {
                clienteEncontrado.setNome(cliente.getNome());
            }
            if (cliente.getEndereco() != null && cliente.getEndereco().getCep() != null) {
                try {
                    salvarClienteComCep(cliente);
                } catch (RuntimeException e) {
                    throw new RuntimeException("Falha ao atualizar o endereço com o CEP fornecido.", e);
                }
            }
            clienteRepository.save(clienteEncontrado);
        } else {
            throw new RuntimeException("Cliente não encontrado com o Cpf: " + cpf);
        }
    }

    @Override
    public void deletar(Long cpf) {
        clienteRepository.deleteById(cpf);
    }

    private void salvarClienteComCep(Cliente cliente) {
        String cep = cliente.getEndereco().getCep();
        try {
            Endereco endereco = enderecoRepository.findById(cep).orElseGet(() -> {
                Endereco novoEndereco = viaCepService.consultarCep(cep);

                if (novoEndereco == null || novoEndereco.getCep() == null || novoEndereco.getCep().isEmpty()) {
                    throw new RuntimeException("Endereço não encontrado para o CEP: " + cep);
                }
                enderecoRepository.save(novoEndereco);
                return novoEndereco;
            });
            cliente.setEndereco(endereco);
            clienteRepository.save(cliente);
        } catch (RuntimeException e){
            throw new RuntimeException("Erro ao buscar o CEP: " + cep, e);
        }
    }
}
