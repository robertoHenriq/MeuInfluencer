package app.Empresa.service;

import app.Empresa.model.Empresa;
import app.Empresa.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmpresaService{

    @Autowired
    private EmpresaRepository empresaRepository;

    public void adicionarEmpresa(Empresa empresa){
        empresaRepository.save(empresa);
    }

    public List<Empresa> listarTodasAsEmpresas(){
        return empresaRepository.findAll();
    }

    public void removerEmpresa(Integer id){
        empresaRepository.deleteById(id);
    }

}
