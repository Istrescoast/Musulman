#!/bin/sh
# Télécharge les polices (Reem Kufi, IBM Plex Sans, Amiri) pour qu'elles marchent hors ligne dans l'appli.
# Sans elles, l'appli utilise la police du téléphone.
cd "$(dirname "$0")/../app/src/main/assets/www/fonts" || exit 0
base=https://cdn.jsdelivr.net/npm/@fontsource
get() { curl -sfLo "$2" "$base/$1" || echo "$2 indisponible"; }
for w in 500 700; do
  get "reem-kufi@5/files/reem-kufi-latin-$w-normal.woff2" "reem-kufi-latin-$w.woff2"
  get "reem-kufi@5/files/reem-kufi-arabic-$w-normal.woff2" "reem-kufi-arabic-$w.woff2"
done
for w in 400 500 600; do
  get "ibm-plex-sans@5/files/ibm-plex-sans-latin-$w-normal.woff2" "ibm-plex-sans-latin-$w.woff2"
done
for w in 400 700; do
  get "amiri@5/files/amiri-arabic-$w-normal.woff2" "amiri-arabic-$w.woff2"
  get "amiri@5/files/amiri-latin-$w-normal.woff2" "amiri-latin-$w.woff2"
done
ls -la
