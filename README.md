# Nejma

Application Android pour la pratique de l'islam : horaires de prière avec rappels, boussole qibla,
ablutions et prière pas à pas, sourates pour prier, calendrier hégirien, zakat, douas et tasbih,
Coran, mode Ramadan, suivi des prières, histoire des prophètes, guide funéraire, mémorisation du Coran,
et un widget pour l'écran d'accueil.

## Installer l'appli

Chaque envoi sur `main` compile l'APK et le publie dans les [Releases](../../releases/latest) :
ouvrez la dernière release depuis le téléphone et touchez `Nejma.apk`.
La version enfants s'installe de la même façon avec `Nejma-Junior.apk`.

## Organisation

- `web/app.html` : l'interface (HTML/JS), la même source que la maquette en ligne.
  Elle détecte l'appli via `window.Android` et y active GPS, boussole, rappels et sauvegarde.
- `tools/assemble.sh` copie l'interface dans `app/src/main/assets/www`.
- `app/src/main/java/fr/nejma` : la partie Android (affichage de l'interface, alarmes de prière,
  notifications, widget). Le calcul des horaires (`PrayerTimes.kt`) reprend celui de l'interface :
  méthode UOIF, Fajr et Isha à 12°.

## Nejma Junior

L'appli enfants (histoires des 25 prophètes, quiz, piliers, ablutions et prière, arabe pour débutants)
est un second module, `junior/` (identifiant `fr.nejma.junior`), installable à côté de Nejma.

- `web-junior/app.html` : l'interface, la même page que la maquette Junior (images intégrées).
  `tools/assemble.sh` la place dans `junior/src/main/assets/www`.
- La lecture à voix haute passe par la synthèse vocale du téléphone (`window.AndroidTTS`) ;
  le bouton retour du téléphone remonte d'un écran via `window.appBack()`.

## Sources

- Texte arabe (uthmani), traduction française de Muhammad Hamidullah et translittération :
  Tanzil et QuranEnc, via le paquet [quran-json](https://www.npmjs.com/package/quran-json) (CC BY 4.0).
  Une sourate par fichier dans `web/quran/`.
- Récits des prophètes : `web/prophetes.json`, rédigés d'après le Coran.
