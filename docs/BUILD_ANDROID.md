# Guia Completo de Compilação e Exportação para APK Android

Este documento fornece as instruções detalhadas para gerar o pacote APK de **Guardians of the Last Base** utilizando o Godot 4.3.

---

## 1. Pré-Requisitos do Sistema

1. **Godot Engine 4.3 estável**:
   - Versão padrão (Standard) para seu sistema operacional (Linux, Windows ou macOS).
2. **Java Development Kit (JDK)**:
   - JDK 17 (versão recomendada pelo Godot 4 para compatibilidade com o Gradle moderno).
   - Instalação no Linux: `sudo apt install openjdk-17-jdk`.
3. **Android SDK & Build Tools**:
   - Android SDK Command-line Tools.
   - Android SDK Platform-Tools (`adb`).
   - Android SDK Build-Tools (versão 34.0.0 recomendada).
   - Android Platforms: API Level 34 (`android-34`).

---

## 2. Configuração do Ambiente no Godot

1. Abra o Godot 4.3.
2. Acesse o menu superior: **Editor** -> **Configurações do Editor** (*Editor Settings*).
3. Na barra lateral esquerda, navegue até **Export** -> **Android**.
4. Configure os caminhos exatos das ferramentas instaladas:
   - **Java SDK Path**: Caminho da pasta raiz do JDK 17 (ex: `/usr/lib/jvm/java-17-openjdk-amd64` no Linux ou `C:/Program Files/Eclipse Adoptium/jdk-17...` no Windows).
   - **Android SDK Path**: Caminho da pasta raiz do Android SDK (ex: `~/Android/Sdk` ou `C:/Users/SEU_USUARIO/AppData/Local/Android/Sdk`).
   - **Debug Keystore**: Caminho para o arquivo `debug.keystore`.

---

## 3. Geração da Chave de Depuração (*Debug Keystore*)

Caso você ainda não possua o arquivo `debug.keystore`, gere-o pelo terminal utilizando a ferramenta `keytool` do JDK:

```bash
keytool -keyalg RSA -genkeypair -alias androiddebugkey -keypass android -keystore debug.keystore -storepass android -dname "CN=Android Debug,O=Android,C=US" -validity 9999 -deststoretype pkcs12
```

Insira o caminho absoluto desse arquivo no campo **Debug Keystore** das Configurações do Editor.

---

## 4. Instalação dos Templates de Exportação (*Export Templates*)

1. No Godot, acesse **Editor** -> **Gerenciar Modelos de Exportação** (*Manage Export Templates*).
2. Clique em **Download e Instalar** (*Download and Install*) correspondente à versão 4.3.
3. Aguarde o download oficial ser finalizado e verifique se o modelo Android está listado como instalado.

---

## 5. Como Gerar o APK de Depuração (*Debug APK*)

O arquivo `export_presets.cfg` já se encontra pronto e configurado na raiz do projeto com o identificador `com.seuprojeto.guardianslastbase` e orientação `sensor_landscape`.

### Via Interface do Editor:
1. Abra o projeto no Godot 4.3.
2. Acesse **Projeto** -> **Exportar** (*Project -> Export*).
3. Selecione o preset **Android** já cadastrado.
4. Certifique-se de que a opção **Export With Debug** esteja marcada.
5. Clique em **Exportar Projeto** (*Export Project*).
6. Escolha o destino do arquivo (ex: `builds/guardians_debug.apk`) e confirme.

### Via Linha de Comando (CLI):
```bash
godot --headless --export-debug "Android" builds/guardians_last_base_debug.apk
```

---

## 6. Como Gerar o APK Assinado para Lançamento (*Release APK*)

Para gerar uma versão final para distribuição ou publicação:

1. Gere uma chave privada com `keytool`:
   ```bash
   keytool -v -genkey -v -keystore guardians_release.keystore -alias guardians_key -keyalg RSA -keysize 2048 -validity 10000
   ```
2. No Godot, na janela de **Exportação Android**, preencha os campos em **Release**:
   - **Keystore**: Caminho de `guardians_release.keystore`.
   - **User**: `guardians_key`.
   - **Password**: Senha definida.
3. Desmarque a opção *Export With Debug* e clique em **Exportar Projeto**.

---

## 7. Como Instalar e Testar o APK em um Dispositivo Android

1. Ative a **Depuração USB** (*USB Debugging*) nas opções do desenvolvedor no seu celular ou tablet Android.
2. Conecte o aparelho ao computador via cabo USB.
3. Verifique se o dispositivo é reconhecido pelo ADB:
   ```bash
   adb devices
   ```
4. Instale o APK gerado:
   ```bash
   adb install -r builds/guardians_last_base_debug.apk
   ```
5. Para acompanhar os logs de execução e eventuais alertas em tempo real:
   ```bash
   adb logcat -s godot:I *:S
   ```

---

## 8. Resolução de Problemas Comuns

- **Erro "Unable to find Android SDK"**:
  - Verifique se o caminho informado em *Editor Settings* aponta para a pasta raiz do SDK (a pasta que contém `platform-tools` e `build-tools`).
- **Erro "Java JDK not configured"**:
  - Confirme que a versão instalada é o JDK 17 (o Godot 4 pode apresentar incompatibilidade com versões antigas como JDK 8 ou 11).
- **Tela preta ao iniciar no aparelho**:
  - Verifique se o renderizador está configurado como `mobile` ou `gl_compatibility` no `project.godot`. O preset do projeto já está pré-otimizado para `mobile`.

---

## 9. Compilação Automática no GitHub (GitHub Actions)

Você pode compilar o APK diretamente na nuvem do GitHub gratuitamente, sem precisar instalar Godot, JDK ou Android SDK no seu computador!

O projeto já inclui o arquivo de workflow pronto em `.github/workflows/build_android.yml`.

### Como Funciona:
1. Suba o projeto para um repositório seu no GitHub (`git push`).
2. Acesse a aba **Actions** no seu repositório no GitHub.
3. O workflow **Build Android APK (Godot 4.3)** iniciará automaticamente a cada `push` (ou você pode clicar em **Run workflow** para disparar manualmente).
4. O GitHub utilizará máquinas com Ubuntu, configurará o Java 17, o Android SDK 34, baixará o Godot 4.3 headless e exportará o APK.
5. Ao concluir (cerca de 3 a 5 minutos), vá na execução do workflow e clique no artefato **Guardians-Last-Base-Android-APK** para baixar o arquivo `.apk` diretamente para o seu celular ou computador!
