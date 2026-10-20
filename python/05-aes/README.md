# Atividade 05 – AES: confusão, difusão e efeito avalanche

> Seguir o roteiro da prática e completar o código base em Python (`aluno.py`), medindo o efeito
> avalanche do AES-128 rodada a rodada e sem a etapa MixColumns. Entregar o `aluno.py` completo e um
> relatório curto (máx. 1 página).

Esta atividade é um script Python fornecido pelo professor, por isso fica fora da aplicação Spring.
O núcleo do AES (validado com o vetor do FIPS-197) **não foi alterado**: só os trechos `# TODO` foram completados.

## A ideia

- **Confusão:** a relação entre chave e ciphertext é não linear. No AES, é feita pela **S-box** (`SubBytes`).
- **Difusão:** cada bit de entrada afeta muitos bits de saída. No AES, é feita por `ShiftRows`, que espalha os
  bytes entre colunas, e `MixColumns`, que mistura os 4 bytes de cada coluna.
- **Efeito avalanche:** inverter 1 bit da entrada deve mudar cerca de 50% dos bits do ciphertext.

## O que foi feito

| Parte | O que mede | Resultado |
|-------|------------|-----------|
| 1 (já vinha pronta) | avalanche do AES completo | ~64/128 (50%) para 1 bit no texto e na chave |
| 2 (`TODO 2`) | avalanche com 1 a 10 rodadas | 3,1% → 12,5% → **50% na 3ª rodada** (satura) |
| 3 (`TODO 3`) | avalanche sem MixColumns (2, 4, 6, 10 rodadas) | presa em ~3% (≈ 4 bits, um único byte) |

A curva confirma a propagação: 1 byte → 1 coluna (4 bytes) → bloco inteiro (16 bytes). Sem MixColumns,
a diferença nunca sai do byte original. A análise completa está no relatório.

## Como rodar

Requer apenas Python 3.9+ (biblioteca padrão):

```bash
cd python/05-aes
python3 aluno.py
```

A execução leva alguns segundos e termina com `=== EXPERIMENTOS CONCLUÍDOS ===`.
Os números variam levemente a cada execução, porque as amostras são aleatórias.

## Arquivos

```
05-aes/
├── aluno.py            código base do professor com as Partes 2 e 3 completadas
└── relatorio_aes.pdf   relatório (parágrafo técnico + parágrafo crítico sobre margem de segurança)
```

## Uso de IA

Nesta atividade, a IA (Claude) foi usada para:

- explicar confusão, difusão, efeito avalanche e o papel de cada etapa do AES;
- prever à mão a curva de difusão antes dos experimentos;
- completar os dois trechos `# TODO`;
- pesquisar as referências sobre ataques a versões reduzidas do AES e redigir o relatório e este README.

A execução dos experimentos e a conferência dos resultados foram feitas por mim.
