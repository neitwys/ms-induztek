package cl.induztek.clientes.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import cl.induztek.clientes.model.Empresa;
import cl.induztek.clientes.repository.EmpresaRepository;

@Service
public class EmpresaService {
    private EmpresaRepository repository;

    public EmpresaService(EmpresaRepository repository) {
        this.repository = repository;
    }

    public List<Empresa> listarTodas() {
        return repository.findAll();
    }

    public Empresa obtenerPorId(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Optional<Empresa> obtenerPorRut(String rut) {
        return Optional.ofNullable(repository.findByRut(rut).orElse(null));
    }

    public Empresa guardar(Empresa empresa) {
        if (empresa.getContactos() != null) {
            empresa.getContactos().forEach(c -> c.setEmpresa(empresa));
        }
        if (empresa.getInstalaciones() != null) {
            empresa.getInstalaciones().forEach(i -> i.setEmpresa(empresa));
        }
        return repository.save(empresa);
    }

    public void eliminarEmpresa(Long id) {
        repository.deleteById(id);
    }
}
