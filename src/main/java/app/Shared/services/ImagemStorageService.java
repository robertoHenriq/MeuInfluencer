package app.Shared.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.Locale;
import javax.imageio.ImageIO;

@Service
public class ImagemStorageService {
    private static final long TAMANHO_MAXIMO = 5 * 1024 * 1024;
    private static final Map<String, String> EXTENSOES_POR_TIPO = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png"
    );

    private final Path diretorioBase = Paths.get("uploads").toAbsolutePath().normalize();

    public String salvar(MultipartFile arquivo, String tipoUsuario, Integer usuarioId) {
        if (arquivo == null || arquivo.isEmpty()) {
            return null;
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException("A imagem deve ter no máximo 5 MB.");
        }

        String extensao = arquivo.getContentType() == null
                ? null
                : EXTENSOES_POR_TIPO.get(arquivo.getContentType().toLowerCase(Locale.ROOT));
        if (extensao == null) {
            throw new IllegalArgumentException("Formato inválido. Use uma imagem JPG ou PNG.");
        }
        try {
            try (var conteudo = arquivo.getInputStream()) {
                BufferedImage imagem = ImageIO.read(conteudo);
                if (imagem == null || (long) imagem.getWidth() * imagem.getHeight() > 25_000_000L) {
                    throw new IllegalArgumentException("A imagem está inválida ou tem dimensões muito grandes.");
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Não foi possível ler a imagem enviada.", e);
        }

        Path pastaUsuario = diretorioBase.resolve(tipoUsuario).resolve(usuarioId.toString()).normalize();
        if (!pastaUsuario.startsWith(diretorioBase)) {
            throw new IllegalArgumentException("Destino de imagem inválido.");
        }

        String nomeArquivo = UUID.randomUUID() + extensao;
        try {
            Files.createDirectories(pastaUsuario);
            arquivo.transferTo(pastaUsuario.resolve(nomeArquivo));
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível salvar a imagem enviada.", e);
        }
        return "/uploads/" + tipoUsuario + "/" + usuarioId + "/" + nomeArquivo;
    }

    public void remover(String url) {
        if (url == null || !url.startsWith("/uploads/")) {
            return;
        }
        Path arquivo = diretorioBase.resolve(url.substring("/uploads/".length())).normalize();
        if (!arquivo.startsWith(diretorioBase)) {
            throw new IllegalArgumentException("Caminho de imagem inválido.");
        }
        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível remover a imagem.", e);
        }
    }
}
