package app.security;

import app.Empresa.models.Empresa;
import app.Influencer.models.Influencer;
import app.Empresa.repository.EmpresaRepository;
import app.Influencer.repository.InfluencerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private InfluencerRepository influencerRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Empresa> empresaOptional = empresaRepository.findByEmail(email);
        if (empresaOptional.isPresent()) {
            Empresa empresa = empresaOptional.get();
            return User.withUsername(empresa.getEmail())
                    .password(empresa.getSenha())
                    .roles("EMPRESA")
                    .build();
        }

        List<Influencer> influencers = influencerRepository.findByEmail(email);
        if (!influencers.isEmpty()) {
            Influencer influencer = influencers.get(0); // Assuming email is unique for influencers
            return User.withUsername(influencer.getEmail())
                    .password(influencer.getSenha())
                    .roles("INFLUENCER")
                    .build();
        }

        throw new UsernameNotFoundException("Usuário não encontrado com o email: " + email);
    }
}