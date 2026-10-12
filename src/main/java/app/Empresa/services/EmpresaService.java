package app.Empresa.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.Empresa.models.Empresa;
import app.Empresa.repository.EmpresaRepository;

@Service 
public class EmpresaService {

    @Autowired 
    private EmpresaRepository empresaRepository;
    
    public EmpresaService() {}

    public void adicionarEmpresa(Empresa empresa){
        empresaRepository.save(empresa);
    }

    public void removerEmpresa(Integer id){
        empresaRepository.deleteById(id);
    }

    public Empresa editarEmpresa(Empresa e){
        if(empresaRepository.existsById(e.getId())){
            empresaRepository.deleteById(e.getId());
            adicionarEmpresa(e);
        }
        return e;
    }

    public List<Empresa> listarEmpresas(){
        return empresaRepository.findAll();
    }
}
