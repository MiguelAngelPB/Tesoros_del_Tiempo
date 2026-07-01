# Tesoros del Tiempo

<p align="center">
  <img src="Logo.png" alt="Logo de Tesoros del Tiempo" width="180">
</p>


[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-8.0%2B%20(API%2026)-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Licencia](https://img.shields.io/badge/Licencia-Apache%202.0-blue.svg)](LICENSE)

Aplicación Android de **terapia de reminiscencia** orientada a personas con deterioro cognitivo y a sus cuidadores. Permite reunir fotografías, vídeos, audios y textos en una galería sencilla, con almacenamiento local cifrado y uso sin conexión a internet.

**Trabajo de Fin de Grado** — Grado en Ingeniería Informática · Universidad de Burgos  
Autor: Miguel Ángel Parra Bolívar · Tutor: Pedro Renedo Fernández

---

## Descarga

Instala la aplicación descargando el APK:

**[Descargar Tesoros del Tiempo (APK)](https://universidaddeburgos-my.sharepoint.com/:u:/g/personal/mpb1019_alu_ubu_es/IQAs8E8EM6umRYh6AFCRhSIpAT_cTVKX5fi0Pf5Qa1odVxw)**

> En el dispositivo Android, activa *Instalar aplicaciones desconocidas* para el navegador o gestor de archivos que uses antes de abrir el APK.

---

## Funcionalidades principales

- **Galería de recuerdos** en cuadrícula: fotos, vídeos, audios y notas de texto.
- **Creación de contenidos** con cámara, micrófono o archivos ya guardados en el móvil.
- **Panel «¿Te acuerdas…?»** con información contextual y descripción multimedia opcional.
- **Búsqueda por etiquetas** desde la barra superior de la aplicación.
- **Modo cuidador** con registro e inicio de sesión local (credenciales cifradas).
- **Papelera** con restauración o borrado definitivo (solo con sesión de cuidador).
- **Exportación e importación** de la galería en un archivo `.zip` portable entre dispositivos.
- **Bloqueo al abrir** mediante biometría o PIN/patrón del sistema.
- **Tutorial integrado** y videoguías externas para el cuidador.
- **Interfaz en español e inglés.**

**Idiomas soportados:** español, inglés.

---

## Videotutoriales

Guías en vídeo organizadas por apartados (registro, galería, papelera, exportación, etc.):

**[Carpeta de tutoriales (SharePoint UBU)](https://universidaddeburgos-my.sharepoint.com/:f:/r/personal/mpb1019_alu_ubu_es/Documents/Tesoros%20del%20Tiempo/Tutoriales?csf=1&web=1&e=ucOmcd)**

---

## Requisitos del dispositivo

- Android **8.0 (Oreo)** o superior (API 26+).
- Cámara y micrófono operativos (para capturar recuerdos multimedia).
- Método de bloqueo de pantalla configurado (recomendado para el acceso inicial a la app).

---

## Tecnologías

| Área | Stack |
|------|--------|
| Lenguaje | Kotlin |
| UI | XML (Views) + Material Components |
| Arquitectura | MVVM, Repository |
| Base de datos | Room (SQLite) |
| Seguridad | AndroidX Security Crypto, EncryptedFile, EncryptedSharedPreferences, Android Keystore |
| Biometría | AndroidX Biometric |
| Concurrencia | Kotlin Coroutines, Flow |
| Pruebas | JUnit, MockK, Katalon Studio (E2E) |
| Calidad | Android Lint, SonarQube |

---

## Compilar desde el código fuente

### Requisitos de desarrollo

- JDK **17**
- **Android Studio** (última versión estable recomendada)
- Android SDK con **API 26** como mínimo y **API 36** como objetivo de compilación

### Pasos

```bash
git clone https://github.com/MiguelAngelPB/Tesoros_del_Tiempo.git
cd Tesoros_del_Tiempo
```

1. Abre la carpeta del proyecto en Android Studio.
2. Espera a que termine **Gradle Sync**.
3. Ejecuta la app en un emulador o dispositivo físico (**Run ▶**).

Para generar un APK de release: **Build → Generate Signed Bundle / APK**.

---

## Documentación del TFG

La memoria y la documentación técnica (requisitos, diseño, manual de programador y manual de usuario) forman parte de la entrega académica del proyecto.

---

## Privacidad

Tesoros del Tiempo funciona **100 % en local**: no envía datos a servidores externos ni incluye publicidad. Los archivos multimedia se guardan cifrados en el almacenamiento privado de la aplicación.

---

## Licencia

Copyright © 2025–2026 Miguel Ángel Parra Bolívar

El código fuente de este proyecto se distribuye bajo la **[Apache License 2.0](LICENSE)**.

La documentación académica, diagramas y recursos gráficos del TFG se publican bajo **Creative Commons Reconocimiento 4.0 (CC BY 4.0)**.

---

## Contacto

- Repositorio: [github.com/MiguelAngelPB/Tesoros_del_Tiempo](https://github.com/MiguelAngelPB/Tesoros_del_Tiempo)
- Universidad de Burgos — Escuela Politécnica Superior

*Tesoros del Tiempo* — recuerdos que permanecen cerca.
