package app.Adm.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarEmailVerificacao(String paraEmail, String codigoOuToken) {
        try {
            SimpleMailMessage mensagem = new SimpleMailMessage();
            
            mensagem.setTo(paraEmail);
            mensagem.setSubject("Código de Verificação - MeuInfluencer");
            mensagem.setText("Olá!\n\n" +
                             "Obrigado por se cadastrar no MeuInfluencer.\n" +
                             "O seu código de verificação é: " + codigoOuToken + "\n\n" +
                             "Digite este código na tela de verificação para ativar sua conta.");

            mailSender.send(mensagem);
            System.out.println("E-mail enviado com sucesso para: " + paraEmail);
            
        } catch (Exception e) {
            System.err.println("Erro ao enviar o e-mail: " + e.getMessage());
            throw new RuntimeException("Não foi possível enviar o e-mail de verificação.");
        }
    }
}