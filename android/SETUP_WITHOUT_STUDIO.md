# Lancer l'app Android sans Android Studio

## Option 1: Utiliser les outils en ligne de commande Android

### 1. Installer les Android SDK Command Line Tools

```bash
# Télécharger les command line tools pour macOS
cd ~/Downloads
curl -O https://dl.google.com/android/repository/commandlinetools-mac-9477386_latest.zip

# Créer le répertoire SDK
mkdir -p ~/Library/Android/sdk/cmdline-tools
cd ~/Library/Android/sdk/cmdline-tools

# Extraire les outils
unzip ~/Downloads/commandlinetools-mac-9477386_latest.zip
mv cmdline-tools latest

# Ajouter au PATH
echo 'export ANDROID_HOME=$HOME/Library/Android/sdk' >> ~/.zshrc
echo 'export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin' >> ~/.zshrc
echo 'export PATH=$PATH:$ANDROID_HOME/platform-tools' >> ~/.zshrc
echo 'export PATH=$PATH:$ANDROID_HOME/emulator' >> ~/.zshrc
source ~/.zshrc
```

### 2. Installer les composants nécessaires

```bash
# Accepter les licences
sdkmanager --licenses

# Installer les outils nécessaires
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0" "emulator" "system-images;android-34;google_apis;arm64-v8a"
```

### 3. Créer un émulateur

```bash
# Créer un AVD (Android Virtual Device)
avdmanager create avd -n SmartDeliveryEmulator -k "system-images;android-34;google_apis;arm64-v8a" -d "pixel_5"
```

### 4. Démarrer l'émulateur

```bash
# Lancer l'émulateur
emulator -avd SmartDeliveryEmulator &
```

### 5. Compiler et installer l'app

```bash
cd /Users/houssem_zorgui/Desktop/Demo/SmartDeliveryApp/android

# Compiler l'APK
./gradlew assembleDebug

# Installer sur l'émulateur
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Option 2: Utiliser un appareil physique (PLUS SIMPLE)

### 1. Activer le mode développeur sur votre téléphone Android

1. Allez dans **Paramètres** > **À propos du téléphone**
2. Tapez 7 fois sur **Numéro de build**
3. Retournez dans **Paramètres** > **Options pour les développeurs**
4. Activez **Débogage USB**

### 2. Connecter votre téléphone

```bash
# Connectez votre téléphone via USB
# Vérifiez que l'appareil est détecté
adb devices
```

### 3. Compiler et installer

```bash
cd /Users/houssem_zorgui/Desktop/Demo/SmartDeliveryApp/android

# Si gradlew n'existe pas, créez-le d'abord
# (voir section suivante)

# Compiler l'APK
./gradlew assembleDebug

# Installer sur votre téléphone
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Option 3: Installer Android Studio (RECOMMANDÉ)

C'est la solution la plus simple et la plus fiable :

```bash
# Télécharger Android Studio
# Visitez: https://developer.android.com/studio
# Ou utilisez Homebrew:
brew install --cask android-studio
```

---

## Problème: Gradle Wrapper manquant

Si `./gradlew` n'existe pas, vous devez le créer :

```bash
cd /Users/houssem_zorgui/Desktop/Demo/SmartDeliveryApp/android

# Installer Gradle via Homebrew
brew install gradle

# Générer le wrapper
gradle wrapper --gradle-version 8.2
```

---

## Démarrer le backend avant de tester

```bash
cd /Users/houssem_zorgui/Desktop/Demo/SmartDeliveryApp/backend
npm start
```

L'app se connectera automatiquement à `http://10.0.2.2:5001` (émulateur) ou `http://localhost:5001` (appareil physique).
