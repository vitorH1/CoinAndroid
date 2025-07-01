# Crypto Tracker 🪙

## 📄 Descrição

Crypto Tracker é um aplicativo Android moderno e elegante, desenvolvido em Kotlin, para o acompanhamento das principais criptomoedas do mercado. Com uma interface limpa e reativa, o app permite que os usuários visualizem cotações, analisem gráficos históricos de preços e pesquisem por seus ativos digitais favoritos.

Construído com uma arquitetura robusta e as tecnologias mais recentes do ecossistema Android, o Crypto Tracker oferece uma experiência de usuário fluida, rápida e com capacidade de funcionamento offline.

## ✨ Funcionalidades Implementadas

* **Design Moderno:**
    * Tema escuro (Dark Theme) profissional baseado no **Material Design 3**.
    * Interface limpa com uso de `MaterialCardView` para melhor organização visual.
    * **Splash Screen** personalizada na abertura do aplicativo.

* **Lista de Criptomoedas:**
    * Exibição das 50 principais moedas do mercado, consumindo a API da CryptoCompare.
    * Efeito de **"shimmer"** (brilho animado) durante o carregamento inicial dos dados.
    * Função de **"Puxar para atualizar"** (`SwipeRefreshLayout`) para buscar as cotações mais recentes.

* **Pesquisa em Tempo Real:**
    * Barra de pesquisa na tela principal para filtrar moedas por nome ou símbolo instantaneamente.

* **Tela de Detalhes Completa:**
    * Gráfico de preços interativo (`MPAndroidChart`) com seleção de períodos (7, 30 e 90 dias).
    * **MarkerView** que exibe o preço e a data exatos ao tocar em um ponto do gráfico.
    * Dashboard com estatísticas de mercado (Capitalização, Volume 24h, etc.).
    * Animação de transição fluida (**Shared Element Transition**) do ícone e nome da lista para a tela de detalhes.

* **Performance e Funcionamento Offline:**
    * Uso de um banco de dados local **Room** para cache de dados.
    * O app abre instantaneamente e exibe os dados mais recentes, mesmo sem conexão com a internet, seguindo o padrão de "Fonte Única da Verdade".

##  captures de tela

**(Instrução para você, Vitor: Tire screenshots do seu app e substitua os links abaixo. Você pode subir as imagens na aba "Issues" do seu repositório para gerar links, ou usar um site como o [Imgur](https://imgur.com/)).**

| Splash Screen | Tela Principal | Pesquisa |
| :---: | :---: | :---: |
| ![Splash Screen](https://i.imgur.com/image_link_here.png) | ![Tela Principal](https://i.imgur.com/image_link_here.png) | ![Pesquisa](https://i.imgur.com/image_link_here.png) |

| Tela de Detalhes | Gráfico Interativo |
| :---: | :---: |
| ![Tela de Detalhes](https://i.imgur.com/image_link_here.png) | ![Gráfico Interativo](https://i.imgur.com/image_link_here.png) |

## 🛠️ Decisões Técnicas e Arquitetura

Este projeto foi desenvolvido com foco em boas práticas e tecnologias modernas do ecossistema Android.

* **Linguagem:** **100% Kotlin**. Aproveitamento de recursos como Coroutines para operações assíncronas (chamadas de rede, acesso ao banco de dados), resultando em um código mais limpo, seguro e performático.

* **Arquitetura:** **MVVM (Model-View-ViewModel)** com os componentes do Android Jetpack.
    * **ViewModel:** Gerencia o estado da UI e sobrevive a mudanças de configuração.
    * **LiveData:** Utilizado para criar fluxos de dados observáveis entre o repositório, ViewModel e a UI.
    * **ViewBinding:** Para acesso seguro e eficiente às views do XML, eliminando a necessidade de `findViewById`.

* **Injeção de Dependência:** **Dagger Hilt** foi utilizado para gerenciar o ciclo de vida das dependências (como `Repository`, `API`, `Database`) e facilitar a injeção em componentes Android, o que simplifica o código e melhora a testabilidade.

* **Camada de Dados:**
    * **Retrofit:** Para o consumo da API pública da CryptoCompare.
    * **Room Database:** Utilizado como **Fonte Única da Verdade (Single Source of Truth)**. Os dados da rede são salvos no banco local, e a UI (via ViewModel) observa o banco. Isso garante funcionamento offline, inicialização instantânea da UI com dados cacheados e uma interface sempre reativa.

* **Interface do Usuário (UI):**
    * **Material Design 3:** Para um visual moderno, consistente e que segue as diretrizes do Google.
    * **MPAndroidChart:** Biblioteca robusta e altamente customizável escolhida para a exibição dos gráficos de preços.
    * **Shared Element Transition:** Implementada para criar uma experiência de navegação mais fluida e profissional entre a lista e a tela de detalhes.
