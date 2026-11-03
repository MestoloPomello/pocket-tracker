# PocketTracker

Prototipo offline di PDR sperimentale, senza GPS, fotocamera, mappe o backend.

## Build e test
Aprire in Android Studio con JDK 17 e Android SDK 35, quindi sincronizzare Gradle. Comandi: `gradle assembleDebug` e `gradle :core:testDebugUnitTest`. Il progetto usa Android Gradle Plugin 8.7.3.

## Installazione
Collegare Redmi Note 10 Pro via USB con Debug USB attivo e usare Run in Android Studio. In alternativa: `adb install -r app/build/outputs/apk/debug/app-debug.apk`.

## Uso
Concedere Attività fisica e notifiche quando richiesto. Impostare la lunghezza media del passo, toccare Avvia, interrompere al termine, visualizzare o esportare CSV attraverso il selettore Android. I dati sono nel file privato locale `files/sessions.csv`; Esporta CSV passa il testo CSV all'app scelta. Colonne: timestamp Unix ms, passi, heading radianti, sorgente orientamento, anomalia, x/y metri.

## Limiti sperimentali
La bussola game rotation vector non ha riferimento al nord magnetico: l'origine dell'heading è arbitraria per sessione e serve una calibrazione sul campo. Phone-up non è sempre direzione di marcia; qui si corregge l'assetto capovolto di 180°, ma non sono stimate le rotazioni del corpo. L'estrazione è rilevata con una semplice soglia accelerometrica, soggetta a falsi positivi/negativi. Senza step detector il prototipo non inventa passi: non registra displacement. La direzione è marcata affidabile solo con game rotation vector e fuori dalla finestra anomala. La UI mostra i punti aggregati, mentre il CSV conserva il log. Test unitari coprono geometria e comportamento PDR.
