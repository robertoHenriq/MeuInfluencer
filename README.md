# Meu Influencer PB

Plataforma de conexão entre pequenas empresas e criadores de conteúdo da Paraíba.

## 📸 Prévia do Sistema

![Tela do Sistema](./captura-de-tela.png)

Sistema web desenvolvido para o gerenciamento de influenciadores digitais, empresas e suas respectivas redes sociais e nichos de mercado.

🛠️ Tecnologias Utilizadas
Backend: Java 17 com Spring Boot (Spring Data JPA / Hibernate)

Frontend: Thymeleaf integrado com Tailwind CSS para um design moderno e responsivo

Banco de Dados: PostgreSQL

Controle de Versão: Git / GitHub

🏛️ Arquitetura e Entidades Principais
O projeto segue o padrão MVC (Model-View-Controller), com as páginas organizadas em subpastas (/adm/ e /influencer/) para melhor manutenção:

Influencer: Armazena os dados principais do influenciador.

Empresa: Gerencia os dados das marcas parceiras.

RedeSocial: Vincula as diferentes plataformas de atuação do influenciador.

Categoria (Nicho): Segmenta os influenciadores por área de atuação (ex: Tecnologia, Moda, Fitness).

⚙️ Principais Funcionalidades Implementadas
Relacionamento Many-to-Many: Associação flexível entre influenciadores e múltiplas categorias através da tabela de junção influencer_categoria.

Painel Administrativo (/adm): Módulo restrito onde administradores cadastram e gerenciam as categorias oficiais do sistema. Essa restrição evita a duplicação de dados e mantém a consistência do banco de dados.

Dashboard do Influenciador (/influencer): Área voltada para listagem, navegação e gerenciamento dos perfis cadastrados.
