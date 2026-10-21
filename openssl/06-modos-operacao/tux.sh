#!/usr/bin/env bash
# Atividade 06 – Modos de operação do AES-128 com OpenSSL (imagem do Tux)
# Regenera do zero: chave, IVs, texto cifrado e imagens cifradas em cada modo.
set -euo pipefail
cd "$(dirname "$0")"

SENHA="minha-senha-secreta"
ROTEIRO_URL="https://docs.google.com/document/d/16HVq6x4IU-noU6zhy5eYGJpmNoPFqEab9GMSrsqf7cM/export?format=txt"
TUX_URL="https://upload.wikimedia.org/wikipedia/commons/a/af/Tux.png"

[[ $(openssl version) == OpenSSL* ]] || { echo "Erro: requer OpenSSL 3 (encontrado: $(openssl version)). No macOS: brew install openssl@3 e coloque /opt/homebrew/opt/openssl@3/bin no PATH."; exit 1; }
command -v magick >/dev/null || { echo "Erro: ImageMagick não encontrado. No macOS: brew install imagemagick"; exit 1; }
openssl version

mkdir -p imagens

echo "== Passo 2: chave AES-128 derivada da senha com PBKDF2 =="
openssl enc -pbkdf2 -aes-128-ecb -k "$SENHA" -P | awk -F= '/^key/{print $2}' > key.priv
echo "key.priv: $(cat key.priv)"

echo "== Passo 3: texto do Hacker Manifesto em ECB =="
curl -fsSL "$ROTEIRO_URL" | tr -d '\r' | sed -n '/This is our world now/,/all alike/p' > cleartext.txt
[[ -s cleartext.txt ]] || { echo "Erro: não foi possível extrair o texto do roteiro."; exit 1; }
openssl enc -aes-128-ecb -in cleartext.txt -K "$(cat key.priv)" -out ciphertext.txt
xxd ciphertext.txt | head -5 || true
wc -c cleartext.txt ciphertext.txt
diff cleartext.txt <(openssl enc -d -aes-128-ecb -in ciphertext.txt -K "$(cat key.priv)")
echo "Decifragem: IDENTICO ao original"

echo "== Passo 4: preparar a imagem (PNG -> PPM, cabeçalho + corpo) =="
curl -fsSLO "$TUX_URL"
file Tux.png
magick Tux.png tux.ppm
head -n 3 tux.ppm > header.txt
tail -n +4 tux.ppm > body.bin
cat header.txt
wc -c tux.ppm header.txt body.bin
cp Tux.png imagens/tux-original.png

echo "== Passo 5: ECB =="
openssl enc -aes-128-ecb -in body.bin -K "$(cat key.priv)" -out body.ecb.enc
cat header.txt body.ecb.enc > tux-ecb-enc.ppm
magick tux-ecb-enc.ppm imagens/tux-ecb.png
echo "Blocos de ciphertext mais repetidos (contagem, bloco):"
xxd -p -c 16 body.ecb.enc | sort | uniq -c | sort -rn | head -5 || true

echo "== Passo 6: CBC =="
openssl rand -hex 16 > iv.txt
echo "iv.txt: $(cat iv.txt)"
openssl enc -aes-128-cbc -in body.bin -K "$(cat key.priv)" -iv "$(cat iv.txt)" -out body.cbc.enc
cat header.txt body.cbc.enc > tux-cbc-enc.ppm
magick tux-cbc-enc.ppm imagens/tux-cbc.png

echo "== Passo 7: CFB, OFB e CTR =="
for modo in cfb ofb ctr; do
  openssl rand -hex 16 > "iv-$modo.txt"
  openssl enc "-aes-128-$modo" -in body.bin -K "$(cat key.priv)" -iv "$(cat "iv-$modo.txt")" -out "body.$modo.enc"
  cat header.txt "body.$modo.enc" > "tux-$modo-enc.ppm"
  magick "tux-$modo-enc.ppm" "imagens/tux-$modo.png"
  echo "$modo: IV $(cat "iv-$modo.txt")"
done

echo "== Comparação =="
wc -c body.bin body.*.enc
for modo in ecb cbc cfb ofb ctr; do
  echo "$modo: $(xxd -p -c 16 "body.$modo.enc" | sort | uniq -d | wc -l | tr -d ' ') blocos distintos que se repetem"
done

ls -l imagens/