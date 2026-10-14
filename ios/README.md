# TasaCambio 2.0 iOS — Guía de Implementación y Compilación para iPhone / iPad

Desarrollada por **ArtesWebVE** (https://arteswebve.com)

Esta carpeta contiene el código fuente nativo completo en **Swift & SwiftUI** compatible con **iOS 16, 17 y 18+**, diseñado siguiendo las directrices de interfaz humana de Apple (Human Interface Guidelines / HIG).

---

## 📁 Estructura del Proyecto iOS

```text
ios/
├── TasaCambio/
│   ├── TasaCambioApp.swift          # Punto de entrada de la aplicación (@main)
│   ├── Models/
│   │   └── CurrencyModels.swift     # Modelos de datos (USD, EUR, USDT, VES, Historial, Alertas)
│   ├── Services/
│   │   └── DolarVzlaService.swift   # Cliente de red URLSession asíncrono (dolarvzla.com)
│   ├── ViewModels/
│   │   └── RatesViewModel.swift     # @MainActor ViewModel con Combine y SwiftUI
│   └── Views/
│       ├── ContentView.swift        # TabView nativo (Calculadora, Tendencias, Historial, Alertas)
│       ├── CalculatorView.swift     # Calculadora estilo Inset Grouped con UISegmentedControl
│       ├── TrendsView.swift         # Gráficos interactivos con Apple Swift Charts
│       ├── HistoryView.swift        # Historial de conversiones con swipe actions
│       └── AlertsView.swift         # Alertas con Toggle estilo Cupertino y notificaciones
└── README.md                        # Esta guía
```

---

## 🚀 Pasos para Abrir y Ejecutar en Xcode (Mac)

### 1. Requisitos
- Una computadora Mac con **macOS Ventura, Sonoma o Sequoia**.
- **Xcode 15 o superior** (disponible gratis en la Mac App Store).
- (Opcional) Cuenta de desarrollador de Apple para desplegar en tu iPhone físico o TestFlight.

### 2. Crear el Proyecto en Xcode
1. Abre **Xcode** y selecciona **"Create a new Xcode project"**.
2. Selecciona la pestaña **iOS** y elige la plantilla **App**. Haz clic en **Next**.
3. Configura las opciones del proyecto:
   - **Product Name:** `TasaCambio`
   - **Team:** Tu equipo o cuenta personal de Apple ID.
   - **Organization Identifier:** `com.arteswebve`
   - **Interface:** `SwiftUI`
   - **Language:** `Swift`
4. Guarda el proyecto en tu carpeta de preferencia.

### 3. Copiar los Archivos Swift
Arrastra y suelta los archivos de esta carpeta `/ios/TasaCambio/` directamente en el navegador de proyectos de Xcode:
- `TasaCambioApp.swift`
- Carpeta `Models/`
- Carpeta `Services/`
- Carpeta `ViewModels/`
- Carpeta `Views/`

Marca la casilla **"Copy items if needed"** y asegúrate de que el Target `TasaCambio` esté seleccionado.

### 4. Compilar y Ejecutar en el Simulador o iPhone Físico
1. En la barra superior de Xcode, selecciona el dispositivo de destino: **iPhone 16 Pro** (o tu iPhone conectado por cable/Wi-Fi).
2. Presiona **⌘ + R** (Command + R) o el botón **Play**.
3. ¡La aplicación se compilará y ejecutará de forma 100% nativa con animaciones de resorte fluidas, retroalimentación háptica y compatibilidad total con iOS!

---

## 🎨 Características Implementadas para iOS
- **Diseño Inset Grouped:** Estilo nativo de iOS (Dark Mode OLED `#000000` y `#1C1C1E`).
- **Segmented Control:** Selector deslizante tipo cápsula para divisas e intervalos.
- **Swift Charts:** Gráfica vectorial de cotizaciones con gradiente de área e interpolación cúbica.
- **ShareSheet nativo:** Comparte cálculos formateados a través del menú nativo de iOS (WhatsApp, Mensajes, AirDrop, etc.).
- **Persistencia local:** Historial y alertas guardadas automáticamente.
- **Fuentes oficiales:** Sincronización en segundo plano con dolarvzla.com y el Banco Central de Venezuela.
