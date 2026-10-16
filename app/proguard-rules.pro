# Les méthodes appelées depuis le JavaScript de l'interface ne doivent pas être renommées.
-keepclassmembers class fr.nejma.Bridge {
    @android.webkit.JavascriptInterface <methods>;
}
