# Nejma

Application Android pour la pratique de l'islam : horaires de prière avec rappels, boussole qibla,
ablutions et prière pas à pas, sourates pour prier, calendrier hégirien, zakat, douas et tasbih,
Coran, mode Ramadan, suivi des prières, histoire des prophètes, guide funéraire, mémorisation du Coran,
et un widget pour l'écran d'accueil.

## Installer l'appli

Chaque envoi sur `main` compile l'APK et le publie dans les [Releases](../../releases/latest) :
ouvrez la dernière release depuis le téléphone et touchez `Nejma.apk`.

## Organisation

- `web/app.html` : l'interface (HTML/JS), la même source que la maquette en ligne.
  Elle détecte l'appli via `window.Android` et y active GPS, boussole, rappels et sauvegarde.
- `tools/assemble.sh` copie l'interface dans `app/src/main/assets/www`.
- `app/src/main/java/fr/nejma` : la partie Android (affichage de l'interface, alarmes de prière,
  notifications, widget). Le calcul des horaires (`PrayerTimes.kt`) reprend celui de l'interface :
  méthode UOIF, Fajr et Isha à 12°.
