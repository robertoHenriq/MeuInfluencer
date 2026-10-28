package app.security;

import app.Empresa.models.Empresa;
import app.Influencer.models.Influencer;
import app.Empresa.repository.EmpresaRepository;
import app.Influencer.repository.InfluencerRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final EmpresaRepository empresaRepository;
    private final InfluencerRepository influencerRepository;

    public CustomUserDetailsService(
            EmpresaRepository empresaRepository,
            InfluencerRepository influencerRepository) {
        this.empresaRepository = empresaRepository;
        this.influencerRepository = influencerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
        Optional<Empresa> empresaOptional = empresaRepository.findByEmailIgnoreCase(emailNormalizado);
        if (empresaOptional.isPresent()) {
            Empresa empresa = empresaOptional.get();
            return User.withUsername(empresa.getEmail())
                    .password(empresa.getSenha())
                    .roles("EMPRESA")
                    .disabled(!empresa.isEmailVerificado())
                    .build();
        }

        List<Influencer> influencers = influencerRepository.findByEmailIgnoreCase(emailNormalizado);
        if (!influencers.isEmpty()) {
            Influencer influencer = influencers.get(0);
            return User.withUsername(influencer.getEmail())
                    .password(influencer.getSenha())
                    .roles("INFLUENCER")
                    .disabled(!influencer.isEmailVerificado())
                    .build();
        }

        throw new UsernameNotFoundException("Usuário não encontrado com o email: " + email);
    }
}