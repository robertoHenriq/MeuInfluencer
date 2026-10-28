package app.Adm.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarEmailVerificacao(String paraEmail, String codigoOuToken) {
        SimpleMailMessage mensagem = new SimpleMailMessage();

        mensagem.setTo(paraEmail);
        mensagem.setSubject("Código de Verificação - MeuInfluencer");
        mensagem.setText("Olá!\n\n" +
                         "Obrigado por se cadastrar no MeuInfluencer.\n" +
                         "O seu código de verificação é: " + codigoOuToken + "\n\n" +
                         "Digite este código na tela de verificação para ativar sua conta.");

        mailSender.send(mensagem);
    }
}