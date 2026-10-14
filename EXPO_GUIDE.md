# Guía de Compilación en Expo (EAS Build para iOS y Android)

Desarrollada por **ArtesWebVE** (https://arteswebve.com)

Tu proyecto ahora está **100% configurado para compilarse en Expo** sin ningún problema.

---

## 📱 Datos para la pantalla de tu captura (Expo Dashboard)

En el formulario que tienes abierto en la web de Expo (*Organization and app setup*):

1. **Select an organization:** `arteswebve's team` (ya lo tienes seleccionado).
2. **App name:** `TasaCambio 2.0 iOS` (o `TasaCambio VE`).
3. **App slug:** `tasacambio-ve` (debe coincidir exactamente con el `slug` en `app.json`).
4. Haz clic en el botón negro **Continue**.

---

## 🛠️ Archivos de Expo que se han integrado en la raíz del proyecto

- **`package.json`**: Contiene la configuración de React Native y dependencias oficiales de Expo (`expo`, `@expo/vector-icons`, `@react-native-async-storage/async-storage`, etc.).
- **`app.json`**: Configuración de Expo con nombre, slug (`tasacambio-ve`), `bundleIdentifier` para iOS (`com.arteswebve.tasacambio`), y tema oscuro.
- **`eas.json`**: Configuración de **EAS Build** para compilar en la nube para iOS (simulador o producción IPA) y Android (APK/AAB).
- **`babel.config.js`** y **`tsconfig.json`**: Configuración del compilador para TypeScript y Babel.
- **`App.tsx`**: Código completo de la aplicación en React Native con interfaz Cupertino iOS (Calculadora, Tendencias, Historial con persistencia, Alertas en vivo y datos oficiales de `dolarvzla.com`).

---

## 🚀 Pasos para Compilar

### Opción 1: Probar en tu celular con Expo Go (Inmediato)
1. Instala las dependencias en tu computadora:
   ```bash
   npm install
   ```
2. Inicia Expo:
   ```bash
   npx expo start
   ```
3. Abre la app **Expo Go** en tu iPhone o Android y escanea el código QR en pantalla.

### Opción 2: Compilar en la nube con EAS Build (Sin necesidad de Mac para iOS)
1. Instala la herramienta de línea de comandos de EAS si no la tienes:
   ```bash
   npm install -g eas-cli
   ```
2. Inicia sesión en tu cuenta de Expo:
   ```bash
   eas login
   ```
3. Vincula el proyecto al slug que creaste:
   ```bash
   eas project:init
   ```
4. Para compilar el instalador de **iOS**:
   ```bash
   eas build --platform ios --profile preview
   ```
   *(EAS compilará el archivo `.ipa` o versión de simulador directamente en los servidores de Apple/Expo en la nube).*

5. Para compilar para **Android** (APK descargable):
   ```bash
   eas build --platform android --profile preview
   ```
