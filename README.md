# Meu Influencer PB

Marketplace para conectar empresas a criadores de conteúdo.

## Tecnologias

- Java 22 e Spring Boot (Spring MVC, Spring Data JPA, Spring Security)
- Thymeleaf e Bootstrap 5
- PostgreSQL

## Funcionalidades

### Autenticação e recuperação de senha

- Cadastro só é persistido após a confirmação do código de e-mail.
- A opção "Esqueci minha senha" no login e no cadastro solicita um código de seis dígitos para uma conta existente e verificada.
- O código de recuperação expira em 10 minutos, é armazenado como hash e permite no máximo cinco tentativas; há intervalo mínimo de 60 segundos entre envios.
- Depois da validação, um token temporário mantido na sessão autoriza a troca de senha por até cinco minutos. A nova senha usa o `PasswordEncoder` BCrypt existente e é gravada no perfil de empresa ou influenciador.
- A resposta ao pedido de código não revela se o endereço está cadastrado. A recuperação não autentica automaticamente o usuário.

### Influenciadores

- Perfil editável com apresentação, foto, categorias e redes sociais.
- Pacotes de serviços com descrição, preço e prazo configurados pelo criador.
- Portfólio de até 20 imagens JPG/PNG, com tamanho máximo de 5 MB por imagem.
- Pedidos recebidos com ações para aceitar, recusar e enviar a entrega.
- O perfil só aparece na vitrine após a verificação do e-mail, preenchimento do nome e da rede social, e criação de um pacote ativo.

### Empresas

- Perfil editável com nome, CNPJ, segmento, site, apresentação e logo.
- Vitrine com filtros por texto, categoria, seguidores e preço.
- Perfil público do criador com portfólio e pacotes disponíveis.
- Envio de briefing, acompanhamento de pedidos, aprovação de entrega e abertura de disputa.

### Pedidos

- Fluxo: aguardando resposta → aguardando pagamento → em produção → entregue → concluído.
- O pedido preserva cópias do nome, escopo, preço e prazo do pacote no momento da contratação.
- Cada parte só pode consultar ou alterar pedidos associados à própria conta; as ações também validam o status atual.
- O botão de pagamento é apenas uma simulação de desenvolvimento: não há cobrança, custódia ou integração com o Mercado Pago.

### Hall de parcerias

- A rota autenticada `/hall` adapta a aba inicial ao papel da conta e só permite acesso a empresas e influenciadores com e-mail verificado.
- A vitrine demonstra campanhas e perfis locais com filtros no cliente e endpoints GET; empresas podem publicar projetos, receber propostas e convidar perfis, enquanto influenciadores podem enviar propostas e favoritar itens.
- Os exemplos de projetos e influenciadores, favoritos, propostas e projetos publicados ficam em memória e são perdidos ao reiniciar a aplicação. O contador de custódia e a atividade ao vivo são demonstrações: nenhum pagamento ou notificação real é processado.
- Os endpoints de ação verificam a role, consultam a conta verificada no banco e, nas operações empresariais, validam se o projeto pertence ao e-mail autenticado. O Hall não cria um fluxo próprio de login.
- Os dashboards têm um atalho para `/hall`. A taxa exibida no resumo de proposta é configurada em `TAXA_PLATAFORMA` no JavaScript e não representa uma comissão cobrada.
- APIs de leitura: `GET /hall/api/projetos`, `GET /hall/api/influenciadores` e `GET /hall/api/propostas`. Ações: `POST /hall/api/company/projetos`, `POST /hall/api/influencer/propostas`, `POST /hall/api/company/propostas/{id}/status`, `POST /hall/api/company/projetos/{projectId}/convites/{influencerId}` e `POST /hall/api/favoritos/{id}`.

Entidades sugeridas para substituir os dados demonstrativos:

- `Projeto`: id, empresa, título, briefing, categoria, rede social, orçamento mínimo/máximo, prazo, faixa de seguidores, bairro, status e datas.
- `Proposta`: id, projeto, influenciador, valor, prazo, mensagem, link do portfólio, status e datas.
- `Favorito`: id, usuário proprietário (tipo e id, ou vínculo a uma entidade comum), tipo de alvo, id do projeto/influenciador e data de criação.

## Imagens

Fotos de perfil, logos e imagens de portfólio são salvas localmente em `uploads/`, diretório ignorado pelo Git. Para produção, use armazenamento persistente compartilhado antes de executar múltiplas instâncias da aplicação.

As novas tabelas são criadas/atualizadas pelo Hibernate de acordo com a configuração `ddl-auto` do projeto.
