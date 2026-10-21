# Atividade 06 – Modos de operação do AES com OpenSSL

> 1) Cifrar a imagem do Tux (mascote do Linux) com OpenSSL em diferentes modos de operação e entregar
> um PDF com prints do terminal e a explicação dos resultados.
> 2) Escrever um texto de 3 parágrafos explicando o ataque [Sweet32](https://sweet32.info/).

Esta atividade é feita no terminal, com OpenSSL, por isso fica fora da aplicação Spring.
O roteiro original é para Ubuntu; aqui ele foi adaptado para macOS.

## A ideia

O AES cifra blocos de 16 bytes. O **modo de operação** define como aplicar essa cifra a uma mensagem inteira,
e é ele que decide se padrões dos dados aparecem no resultado.

- **ECB:** cada bloco é cifrado sozinho (`cᵢ = E_k(mᵢ)`). Blocos iguais geram cifrados iguais, então áreas
  de cor uniforme continuam visíveis e o contorno do Tux aparece na imagem cifrada.
- **CBC:** cada bloco é misturado com o cifrado anterior antes de ser cifrado (`cᵢ = E_k(mᵢ ⊕ cᵢ₋₁)`, com IV).
- **CFB, OFB e CTR:** transformam o AES em cifra de fluxo (keystream ⊕ dados), sem padding.

A imagem é convertida para **PPM**, um formato sem compressão: o cabeçalho fica em claro e só os pixels
são cifrados, de modo que o visualizador consegue "desenhar" o ciphertext.

## O que foi feito

| Modo | Usa IV | Padding | Tamanho cifrado (bytes) | Blocos distintos repetidos | Silhueta visível |
|------|--------|---------|-------------------------|----------------------------|------------------|
| ECB  | não    | sim     | 249.632                 | 242 (o mais frequente: 7.246×) | **sim**      |
| CBC  | sim    | sim     | 249.632                 | 0                          | não              |
| CFB  | sim    | não     | 249.630                 | 0                          | não              |
| OFB  | sim    | não     | 249.630                 | 0                          | não              |
| CTR  | sim    | não     | 249.630                 | 0                          | não              |

O corpo da imagem tem 249.630 bytes (265 × 314 × 3). As contagens do ECB não mudam quando a chave muda:
o vazamento é do modo, não da chave. A análise completa está no relatório.

## Como rodar

Requisitos: **OpenSSL 3** (o `openssl` padrão do macOS é o LibreSSL), **ImageMagick 7** e `curl`.

```bash
brew install openssl@3 imagemagick     # macOS
cd openssl/06-modos-operacao
./tux.sh
```

O script gera do zero uma chave nova (PBKDF2), os IVs, o texto cifrado e as imagens cifradas em
`imagens/`. Chave, IVs e arquivos intermediários ficam fora do Git (`.gitignore`).
No macOS, ele usa automaticamente o OpenSSL do Homebrew.

## Arquivos

```
06-modos-operacao/
├── tux.sh                 todos os passos da prática, em ordem
├── imagens/               Tux original e cifrado em ECB, CBC, CFB, OFB e CTR
└── relatorio_modos.pdf    prints + explicação (Parte 1) e texto sobre o Sweet32 (Parte 2)
```

Os prints do terminal estão em [`docs/06-modos-operacao/`](../../docs/06-modos-operacao).

## Uso de IA

Nesta atividade, a IA (Claude) foi usada para:

- explicar os modos de operação, padding, IV e o birthday bound por trás do Sweet32;
- adaptar os comandos do roteiro (Ubuntu) para o macOS;
- organizar os comandos no `tux.sh`;
- montar o relatório da Parte 1 e redigir este README.

A execução, os prints e a conferência dos resultados foram feitos por mim. O texto sobre o Sweet32
(Parte 2) foi escrito por mim, com revisão técnica da IA.
