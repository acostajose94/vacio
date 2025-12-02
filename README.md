# Auto SMS Perú 🇵🇪

Aplicación Android para enviar SMS automáticos con frases personalizadas **cada 60 segundos**. Diseñada específicamente para números de teléfono peruanos.

## 📱 Características

- **Envío automático periódico** de SMS cada minuto
- **Botones de control**: Iniciar y detener envío automático
- **Contador en tiempo real**: Muestra segundos restantes hasta el próximo SMS
- **5 frases personalizadas** predefinidas
- Selección **aleatoria** de mensajes en cada envío
- **Estadísticas en vivo**: Contador de mensajes enviados
- Validación de números peruanos (9 dígitos, inicia con 9)
- Solicitud de permisos en tiempo de ejecución
- Confirmación de envío y entrega para cada SMS
- Interfaz moderna con Material Design

## 🎯 Frases Disponibles

La aplicación incluye 5 frases personalizadas:

1. "¡Hola! Espero que estés teniendo un día increíble. ¡Saludos desde Perú! 🇵🇪"
2. "¡Qué tal! Solo quería enviarte un saludo y desearte mucha suerte en todo. ¡Arriba Perú!"
3. "¡Hola amigo/a! Que tengas un excelente día lleno de bendiciones. ¡Un abrazo!"
4. "¡Saludos cordiales! Espero que todo te esté yendo muy bien. ¡Éxitos siempre!"
5. "¡Hola! Solo un mensaje para recordarte que eres increíble. ¡Que tengas un gran día!"

## 🔧 Requisitos

- Android Studio Arctic Fox o superior
- Android SDK 23 (Android 6.0) o superior
- JDK 8 o superior
- Dispositivo Android físico con SIM card (el emulador no puede enviar SMS reales)

## 📦 Instalación y Compilación

### 1. Clonar el repositorio

```bash
git clone <url-del-repositorio>
cd vacio
```

### 2. Abrir en Android Studio

1. Abre Android Studio
2. Selecciona "Open an Existing Project"
3. Navega hasta la carpeta del proyecto y ábrela
4. Espera a que Gradle sincronice el proyecto

### 3. Compilar el proyecto

Desde la terminal en la raíz del proyecto:

```bash
# En Linux/Mac
./gradlew build

# En Windows
gradlew.bat build
```

O desde Android Studio:
- Menú: Build → Build Bundle(s) / APK(s) → Build APK(s)

### 4. Instalar en dispositivo

**Opción A: Desde Android Studio**
1. Conecta tu dispositivo Android por USB
2. Habilita "Depuración USB" en las opciones de desarrollador
3. Click en el botón "Run" (▶️) en Android Studio

**Opción B: APK directo**
```bash
# Compilar APK de debug
./gradlew assembleDebug

# El APK se generará en:
# app/build/outputs/apk/debug/app-debug.apk

# Instalar con adb
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 🚀 Uso

1. **Abrir la aplicación** en tu dispositivo Android
2. La aplicación mostrará las 5 frases disponibles
3. **Ingresar el número de teléfono** destinatario (9 dígitos, debe empezar con 9)
4. **Hacer click en "▶️ Iniciar Envío Automático"**
5. Si es la primera vez, **conceder permisos** de envío de SMS
6. La aplicación comenzará a enviar SMS automáticamente:
   - **Primer SMS**: Se envía inmediatamente
   - **SMS siguientes**: Cada 60 segundos
   - **Contador**: Muestra los segundos restantes hasta el próximo envío
   - **Estadísticas**: Muestra la cantidad de mensajes enviados
7. **Para detener**: Click en "⏹️ Detener Envío" en cualquier momento
8. Cada SMS usa una **frase aleatoria diferente** de las 5 disponibles

### 📊 Pantalla de Envío Activo

Mientras el envío automático está activo, verás:
- ⏱️ **Temporizador**: Cuenta regresiva hasta el próximo SMS
- 📈 **Mensajes enviados**: Contador total de SMS enviados
- 📱 **Número destinatario**: Número al que se están enviando los SMS
- ✅ **Estado del último SMS**: Confirmación de envío exitoso o error
- 📝 **Mensaje enviado**: La frase que se envió en el último SMS

## ⚙️ Estructura del Proyecto

```
vacio/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/peru/autosms/
│   │       │   └── MainActivity.java          # Lógica principal
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   └── activity_main.xml      # Interfaz de usuario
│   │       │   └── values/
│   │       │       ├── strings.xml            # Textos de la app
│   │       │       ├── colors.xml             # Colores
│   │       │       └── themes.xml             # Temas
│   │       └── AndroidManifest.xml            # Configuración y permisos
│   └── build.gradle                           # Configuración de la app
├── build.gradle                               # Configuración del proyecto
├── settings.gradle                            # Configuración de módulos
└── README.md                                  # Este archivo
```

## 🔐 Permisos

La aplicación solicita los siguientes permisos:

- `SEND_SMS`: Para enviar mensajes de texto
- `READ_PHONE_STATE`: Para verificar el estado del teléfono

Estos permisos se solicitan en tiempo de ejecución cuando el usuario intenta enviar un SMS por primera vez.

## 📝 Personalización

### Modificar las frases

Edita el archivo `app/src/main/java/com/peru/autosms/MainActivity.java` y modifica el array `messages`:

```java
private final String[] messages = {
    "Tu frase personalizada 1",
    "Tu frase personalizada 2",
    "Tu frase personalizada 3",
    "Tu frase personalizada 4",
    "Tu frase personalizada 5"
};
```

### Cambiar el intervalo de envío

Para modificar el tiempo entre SMS (por defecto 60 segundos), edita la constante `SMS_INTERVAL` en `MainActivity.java`:

```java
private static final long SMS_INTERVAL = 60000; // 60 segundos en milisegundos
// Ejemplos:
// 30 segundos: 30000
// 2 minutos: 120000
// 5 minutos: 300000
```

### Cambiar la validación de número

Si necesitas usar la app en otro país, modifica el método `validatePhoneNumber()` en `MainActivity.java`:

```java
private boolean validatePhoneNumber(String phoneNumber) {
    // Ejemplo para otros países
    return phoneNumber.length() >= 8 && phoneNumber.matches("[0-9]+");
}
```

## ⚠️ Notas Importantes

1. **Solo funciona en dispositivos reales**: Los emuladores no pueden enviar SMS reales
2. **Requiere SIM card activa**: El dispositivo debe tener una SIM card con saldo/plan
3. **Costo de SMS**: Cada envío consume un SMS de tu plan telefónico. Con envío cada minuto, ¡son 60 SMS por hora!
4. **Números peruanos**: La validación actual solo acepta números de 9 dígitos que empiecen con 9
5. **Mantener la app abierta**: Para que el envío automático funcione, mantén la aplicación en primer plano
6. **Batería**: El envío continuo puede consumir batería significativamente

## 🐛 Solución de Problemas

### El SMS no se envía
- Verifica que concediste los permisos de SMS
- Asegúrate de tener señal y saldo/plan activo
- Comprueba que el número sea válido (9 dígitos, empieza con 9)
- Verifica que tienes suficiente saldo para SMS

### El envío automático se detiene
- Mantén la aplicación en primer plano (no la minimices)
- Desactiva optimizaciones de batería para esta app
- Verifica que no se agotó el saldo de SMS

### El temporizador no se actualiza
- La app debe estar visible en pantalla
- Si el teléfono entra en modo de ahorro de energía, puede pausarse

### Error de compilación
- Ejecuta: `./gradlew clean build`
- Verifica que tienes Android SDK 34 instalado
- Asegúrate de tener JDK 8 o superior

### La app no se instala
- Habilita "Orígenes desconocidos" en la configuración de Android
- Verifica que la versión de Android sea 6.0 o superior

## 📄 Licencia

Este proyecto es de código abierto y está disponible para uso educativo y personal.

## 👨‍💻 Autor

Desarrollado para facilitar el envío de mensajes amigables en Perú 🇵🇪

---

**¡Importante!**: Usa esta aplicación de manera responsable. No envíes mensajes spam ni no solicitados.
