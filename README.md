# CadastroDeNinjas 🥷

API REST desenvolvida com **Java 21** e **Spring Boot 4.1.1** para gerenciamento de ninjas, missões e suas regras de associação.

## Tecnologias

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA
- Bean Validation
- Maven e Maven Wrapper
- Flyway
- H2 Database, utilizado em arquivo na execução local e em memória nos testes automatizados
- JUnit 5, Spring Boot Test e MockMvc
- JaCoCo, para cobertura de testes
- GitHub Actions, para integração contínua

## Funcionalidades

### Ninjas

- Cadastrar, listar, consultar por ID, atualizar e excluir ninjas.
- Validar os dados recebidos na API.
- Verificar conflitos de e-mail cadastrado.

### Missões

- Cadastrar, listar, consultar por ID, atualizar e excluir missões.
- Gerenciar rank e status das missões.
- Ativar missões inativas.

### Associação de ninjas a missões

- Atribuir uma missão a um ninja.
- Remover o vínculo entre ninja e missão.
- Transferir um ninja para outra missão.
- Inativar e concluir missões respeitando as regras de negócio.
- Reavaliar automaticamente o status das missões conforme a composição da equipe.


## Regras de negócio

### Qualificação por rank

Um ninja é considerado qualificado para uma missão quando seu rank é maior ou igual ao rank exigido pela missão.

A hierarquia dos ranks é: `D < C < B < A < S`.

### Status das missões

- Toda missão é criada com status `DISPONIVEL`.
- Uma missão passa automaticamente para `EM_ANDAMENTO` quando possui pelo menos um ninja qualificado.
- Uma missão `EM_ANDAMENTO` retorna para `DISPONIVEL` quando deixa de possuir ninjas qualificados.
- O status é reavaliado após atribuições, remoções, transferências e exclusões de ninjas, além de alterações nos ranks de ninjas ou missões.
- Apenas missões com status `DISPONIVEL` ou `EM_ANDAMENTO` podem receber ninjas.

### Associação e transferência

- Um ninja pode estar vinculado a, no máximo, uma missão por vez.
- Para mudar de missão, o ninja deve remover seu vínculo atual ou realizar uma transferência.
- Uma transferência deve ter como destino uma missão diferente da atual e apta a receber ninjas.
- Não é permitido transferir o último ninja qualificado de uma missão `EM_ANDAMENTO` quando isso deixaria a missão sem outro ninja qualificado.

### Inativação, ativação e conclusão

- **Inativar:** permitido para missões que não estejam `CONCLUIDA`. A operação remove os vínculos dos ninjas com a missão.
- **Ativar:** permitido apenas para missões `INATIVA`, alterando seu status para `DISPONIVEL`.
- **Concluir:** permitido apenas para missões `EM_ANDAMENTO`. A operação remove os vínculos dos ninjas com a missão.
- Uma missão só pode ser excluída quando não possui ninjas vinculados.

### Unicidade de e-mail

- O e-mail dos ninjas é normalizado antes de ser salvo, removendo espaços nas extremidades e convertendo letras para minúsculas.
- Não é permitido cadastrar ou atualizar um ninja utilizando um e-mail já associado a outro ninja.
- Conflitos de e-mail retornam `409 Conflict`.


## Executando o projeto

### Pré-requisitos

- JDK 21
- Git

Clone o repositório:

```bash
git clone https://github.com/1-lucas-sena/CadastroNinjas.git
cd CadastroNinjas
```

No Windows, com PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux ou macOS:

```bash
./mvnw spring-boot:run
```

### Banco de dados

A aplicação utiliza o **H2 Database** como banco de dados.

- **Execução local:** H2 em arquivo, com persistência dos dados em `./data/CadastroDeNinjasDb`.
- **Testes automatizados:** H2 em memória, utilizando o perfil `test`, sem alterar os dados da execução local.
- **Migrações:** gerenciadas pelo Flyway, responsável pelo versionamento da estrutura do banco de dados.

O console web do H2 está habilitado na configuração local da aplicação.

As configurações podem ser consultadas em:

- `src/main/resources/application.properties`
- `src/test/resources/application-test.properties`

## Principais rotas

| Método | Rota | Descrição |
| --- | --- | --- |
| `GET` | `/ninjas` | Listar ninjas |
| `GET` | `/ninjas/{id}` | Consultar ninja por ID |
| `POST` | `/ninjas` | Cadastrar ninja |
| `PUT` | `/ninjas/{id}` | Atualizar ninja |
| `DELETE` | `/ninjas/{id}` | Excluir ninja |
| `GET` | `/missoes` | Listar missões |
| `GET` | `/missoes/{id}` | Consultar missão por ID |
| `POST` | `/missoes` | Cadastrar missão |
| `PUT` | `/missoes/{id}` | Atualizar missão |
| `DELETE` | `/missoes/{id}` | Excluir missão |
| `PATCH` | `/missoes/{id}/ativar` | Ativar missão |
| `PATCH` | `/ninjas/{ninjaId}/missoes/{missaoId}` | Atribuir missão a um ninja |
| `DELETE` | `/ninjas/{ninjaId}/missao` | Remover missão de um ninja |
| `PATCH` | `/ninjas/{ninjaId}/transferencias/{novaMissaoId}` | Transferir ninja para outra missão |
| `PATCH` | `/missoes/{id}/inativar` | Inativar missão |
| `PATCH` | `/missoes/{id}/concluir` | Concluir missão |

## Códigos de resposta HTTP

A API utiliza os seguintes códigos de resposta:

| Código | Significado | Quando ocorre |
| --- | --- | --- |
| `200 OK` | Requisição bem-sucedida | Consultas, atualizações e operações com retorno |
| `201 Created` | Recurso criado | Cadastro de ninjas e missões |
| `204 No Content` | Operação concluída sem corpo | Exclusão de ninjas e missões |
| `400 Bad Request` | Requisição inválida | Falhas de validação, JSON inválido ou parâmetros incorretos |
| `404 Not Found` | Recurso não encontrado | Ninja ou missão inexistente |
| `409 Conflict` | Conflito | Violação das regras de negócio ou integridade dos dados |

Os erros da API são tratados de forma centralizada pelo `GlobalExceptionHandler`, responsável por padronizar as respostas para falhas de validação, recursos não encontrados, conflitos de regras de negócio, violações de integridade dos dados e problemas na leitura do JSON.

As respostas tratadas pelo `GlobalExceptionHandler` incluem informações como código HTTP, mensagem descritiva e data e hora da ocorrência. Os erros de validação também apresentam os campos inválidos e suas respectivas mensagens. Algumas respostas HTTP geradas diretamente pelo framework podem ter formato diferente.


## Exemplos de utilização da API

Os exemplos abaixo consideram a aplicação executando localmente em `http://localhost:8080`. Os IDs apresentados são ilustrativos.

### Cadastrar um ninja

**Requisição**

```http
POST /ninjas
Content-Type: application/json
```

```json
{
  "nome": "Naruto Uzumaki",
  "email": "naruto@konoha.com",
  "idade": 17,
  "rank": "A"
}
```

**Resposta de sucesso, HTTP 201 Created**

```json
{
  "id": 1,
  "nome": "Naruto Uzumaki",
  "email": "naruto@konoha.com",
  "idade": 17,
  "rank": "A",
  "missaoId": null
}
```

O campo `missaoId` representa a missão associada ao ninja. Quando não existe associação, o valor é `null`.

O e-mail é normalizado antes de ser armazenado, utilizando letras minúsculas e removendo espaços nas extremidades.

### Cadastrar uma missão

**Requisição**

```http
POST /missoes
Content-Type: application/json
```

```json
{
  "nome": "Proteger a Vila da Folha",
  "rank": "B"
}
```

**Resposta de sucesso, HTTP 201 Created**

```json
{
  "id": 1,
  "nome": "Proteger a Vila da Folha",
  "rank": "B",
  "status": "DISPONIVEL"
}
```

Toda missão é criada com status `DISPONIVEL`.

### Atribuir uma missão a um ninja

**Requisição**

```http
PATCH /ninjas/1/missoes/1
```

**Resposta de sucesso, HTTP 200 OK**

```json
{
  "id": 1,
  "nome": "Naruto Uzumaki",
  "email": "naruto@konoha.com",
  "idade": 17,
  "rank": "A",
  "missaoId": 1
}
```

Como o ninja possui rank `A` e a missão exige rank `B`, ele é considerado qualificado. A missão passa automaticamente para `EM_ANDAMENTO`.


### Exemplo de erro de validação

Ao tentar cadastrar um ninja com nome vazio, idade negativa e e-mail inválido, a API responde com `400 Bad Request`.

**Requisição**

```http
POST /ninjas
Content-Type: application/json
```

```json
{
  "nome": "ab",
  "email": "email-invalido",
  "idade": -5,
  "rank": "A"
}
```

**Resposta de erro, HTTP 400 Bad Request**

```json
{
  "timestamp": "2026-10-29T20:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Erro de validação",
  "errors": {
    "nome": "Nome deve ter entre 3 e 100 caracteres",
    "email": "Email deve ser válido",
    "idade": "Idade deve ser maior que zero"
  }
}
```

A resposta identifica os campos inválidos por meio do objeto `errors`, apresentando as respectivas mensagens de validação.

### Exemplo de conflito de e-mail

Quando uma tentativa de cadastro utiliza um e-mail já associado a outro ninja, a API retorna `409 Conflict`.

**Requisição**

```http
POST /ninjas
Content-Type: application/json
```

```json
{
  "nome": "Outro Ninja",
  "email": "naruto@konoha.com",
  "idade": 20,
  "rank": "C"
}
```

**Resposta de erro, HTTP 409 Conflict**

```json
{
  "timestamp": "2026-10-29T20:31:00",
  "status": 409,
  "error": "Conflict",
  "message": "Email já cadastrado"
}
```

A API impede o cadastro de ninjas com e-mails duplicados, considerando a normalização para letras minúsculas e a remoção de espaços nas extremidades.

> **Observação:** os valores de `timestamp` e os IDs apresentados são ilustrativos. As mensagens de erro seguem as definições do `GlobalExceptionHandler`, das validações dos DTOs e do `NinjaService`.

## Testes automatizados

O projeto conta com **166 testes automatizados**, abrangendo serviços, controllers, regras de negócio, validação de dados, persistência e tratamento de exceções.

Os testes utilizam **JUnit 5, Spring Boot Test, MockMvc e H2 em memória**, permitindo validar o funcionamento da aplicação em um ambiente isolado.

Para executar os testes e gerar o relatório de cobertura no Windows:

```powershell
.\mvnw.cmd clean verify
```

No Linux ou macOS:

```bash
./mvnw clean verify
```

O relatório HTML do JaCoCo é gerado em:

```text
target/site/jacoco/index.html
```

### Resultados de cobertura

O último relatório local analisado apresentou:

| Indicador | Cobertura |
| --- | ---: |
| Instruções | 99,6% |
| Ramificações | 100% |
| Linhas | 99,4% |
| Métodos | 98,7% |

Esses resultados representam a versão analisada e podem variar conforme novas funcionalidades forem implementadas.

A cobertura de 100% das ramificações indica que todos os caminhos condicionais contabilizados pelo JaCoCo foram executados pelos testes, mas não significa ausência completa de defeitos.

### Cobertura mínima exigida

O projeto utiliza o `jacoco:check` para exigir uma cobertura mínima de **90% das ramificações**.

Essa verificação é executada durante a fase `verify` do Maven. Caso a cobertura fique abaixo do limite estabelecido, a compilação é reprovada com `BUILD FAILURE`.

Como o GitHub Actions executa `mvn -B clean verify`, essa regra também é aplicada automaticamente no CI, impedindo o merge quando o check obrigatório não for aprovado.


## Integração contínua

O workflow em `.github/workflows/tests.yml` utiliza o GitHub Actions para:

1. Preparar um ambiente com Java 21.
2. Executar `mvn -B clean verify`.
3. Gerar o relatório de cobertura JaCoCo.
4. Publicar o relatório como artifact da execução.

### Execução automática

O workflow executa automaticamente em:

- **Pushes em qualquer branch:** valida as alterações enviadas ao repositório.
- **Pull Requests direcionadas à `main`:** executa os testes antes da integração do código.
- **Merge na `main`:** executa novamente os testes após a incorporação das alterações.

### Proteção da branch principal

A branch `main` possui uma regra de proteção que exige a aprovação do check `Testes Java 21` antes do merge.

Essa configuração permite validar continuamente as alterações e reduz o risco de incorporar código que não passou pelos testes automatizados.

## Fluxo de contribuição

O desenvolvimento de novas funcionalidades e correções deve seguir o fluxo:

1. Criar uma branch específica a partir da `main`.
2. Implementar a funcionalidade ou correção.
3. Adicionar ou atualizar os testes automatizados correspondentes.
4. Executar os testes localmente com `mvn clean verify`.
5. Realizar commit e push da branch.
6. Abrir uma Pull Request direcionada à `main`.
7. Verificar a aprovação dos checks obrigatórios do GitHub Actions.
8. Realizar o merge após a validação.


## Estrutura do projeto

```text
CadastroDeNinjas/
├── .github/
│   └── workflows/
│       └── tests.yml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── lucassena/CadastroDeNinjas/
│   │   │       ├── exceptions/
│   │   │       ├── missaoStatus/
│   │   │       ├── missoes/
│   │   │       ├── ninjaMissao/
│   │   │       ├── ninjas/
│   │   │       └── util/
│   │   └── resources/
│   └── test/
│       ├── java/
│       └── resources/
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```
