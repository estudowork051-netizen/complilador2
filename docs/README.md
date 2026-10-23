# Guardians of the Last Base — Tower Defense RPG Android

## 1. Visão Geral do Projeto
**Guardians of the Last Base** é um jogo original em 2D de Tower Defense com elementos marcantes de RPG, progressão permanente, heróis guardiões com habilidades ativas e evolução contínua de uma fortaleza central.

O projeto foi inteiramente construído com **Godot 4.3 estável**, utilizando **GDScript 2.0** com tipagem estrita e arquitetura modular orientada a eventos e dados.

---

## 2. Tecnologias Utilizadas
- **Engine**: Godot Engine 4.3 (Mobile/Desktop).
- **Linguagem**: GDScript 2.0 tipado.
- **Resolução Nativa**: 1280 × 720 (Landscape / Sensor Landscape).
- **Escalonamento**: `canvas_items` com aspecto `keep_height`.
- **Renderizador**: Mobile (compatível com aparelhos modestos e GPU móvel).
- **Arte Gráfica**: Formato SVG vetorial nativo renderizado via ThorVG (alta definição em qualquer DPI mobile).
- **Áudio**: Sintetizador procedural PCM em tempo real (ondas sintetizadas de 16-bits a 44.1 kHz, operando 100% offline).
- **Salvamento**: Arquivo JSON versionado persistido em `user://savegame.json`.

---

## 3. Estrutura de Diretórios
```text
guardians_of_the_last_base/
├── project.godot            # Configuração principal da Engine Godot
├── export_presets.cfg       # Definições de exportação para APK Android
├── assets/
│   ├── icons/               # Ícones de interface, heróis e habilidades
│   ├── sprites/             # Sprites dos heróis, inimigos e fortaleza
│   └── effects/             # Projéteis, feitiços, explosões e áreas
├── scenes/
│   ├── menus/               # Telas de menu principal e seleção
│   └── combat/              # Mapa de combate e arena de batalha
├── scripts/
│   ├── managers/            # Autoloads (GameState, Save, Progression, Audio)
│   ├── characters/          # Scripts dos 6 heróis defensores
│   ├── enemies/             # Scripts dos 5 arquétipos de inimigos e Chefe
│   ├── combat/              # Projéteis, feitiços em área e gerenciador
│   ├── progression/         # Modelos de dados de personagens e fases
│   └── ui/                  # Controladores de telas de UI
├── resources/
│   ├── characters/          # Recursos .tres dos 6 personagens
│   └── stages/              # Recursos .tres das fases 1 a 10
├── tests/
│   └── test_runner.py       # Suíte de testes automatizados (61 testes)
└── docs/
    ├── README.md            # Visão geral e guia de execução
    ├── BUILD_ANDROID.md     # Manual completo de compilação do APK
    ├── GAMEPLAY.md          # Manual de jogo, heróis, habilidades e fases
    └── test_report.md       # Relatório de validação e testes
```

---

## 4. Como Abrir o Projeto no Godot Editor
1. Baixe e instale o **Godot 4.3** (versão Standard) do site oficial [godotengine.org](https://godotengine.org/).
2. Abra o Gerenciador de Projetos do Godot.
3. Clique em **Importar** (*Import*).
4. Navegue até a pasta `guardians_of_the_last_base` e selecione o arquivo `project.godot`.
5. Clique em **Importar e Editar** (*Import & Edit*).

---

## 5. Como Executar e Testar
- **No Editor**: Pressione `F5` (ou o botão Play no canto superior direito) para iniciar a cena principal (`res://scenes/menus/main_menu.tscn`).
- **Navegação por Toque**: Com a emulação de toque ativa (`pointing/emulate_touch_from_mouse=true`), cliques do mouse simulam toques em telas capacitivas móveis.
- **Execução dos Testes**: No terminal, execute:
  ```bash
  python3 tests/test_runner.py
  ```
