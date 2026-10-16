#!/bin/sh
# Construit l'interface de l'appli (app/src/main/assets/www) à partir de web/,
# la même source que la maquette en ligne.
cd "$(dirname "$0")/.." || exit 1
out=app/src/main/assets/www
mkdir -p "$out"
{
  printf '<!doctype html>\n<html lang="fr">\n<head>\n<meta charset="utf-8">\n'
  printf '<meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">\n'
  printf '<style>body{margin:0}</style>\n</head>\n<body>\n'
  cat web/app.html
  printf '\n</body>\n</html>\n'
} > "$out/index.html"
cp web/logo.png "$out/logo.png"
echo "Interface assemblée dans $out"
