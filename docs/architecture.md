# Guardians of the Last Base — Documentação Arquitetural (Parte 1/2)

## 1. Visão Geral da Tecnologia
- **Engine**: Godot 4.3 (Mobile/Desktop compatível).
- **Linguagem**: GDScript 2.0 estritamente tipado.
- **Resolução de Referência**: 1280 × 720 (Landscape / Horizontal).
- **Emulação de Toque**: Ativada (`pointing/emulate_touch_from_mouse=true`).
- **Renderizador**: Mobile (compatível com aparelhos modestos).
- **Identificador Android**: `com.seuprojeto.guardianslastbase`.

## 2. Padrão Arquitetural
O projeto utiliza o padrão de **Gerenciadores Singletom (Autoloads)** desacoplados de cenas individuais:
- `GameStateManager`: Máquina de estados finitos (Menu, Preparação, Combate, Pausa, Vitória, Derrota) e orquestrador de transição de telas e escala temporal do motor.
- `SaveManager`: Persistência local em JSON (`user://savegame.json`), suporte a merge de dados padrão e reinicialização segura.
- `ProgressionManager`: Núcleo de regras econômicas, estruturas da base central, desbloqueio de heróis e regras de slots.
- `AudioManager`: Síntese procedural de ondas sonoras (PCM 16-bit Mono a 44.1kHz), eliminando dependências externas e permitindo funcionamento offline imediato.

## 3. Fluxo de Combate
1. A rota dos monstros é delimitada por um `Path2D` e percorrida por nós derivados de `PathFollow2D` (`EnemyBase`).
2. Os defensores realizam varredura espacial por alcance e priorizam inimigos mais adiantados no caminho (`progress_ratio`).
3. Ataques básicos geram projéteis físicos ou arcanos direcionados.
4. Quatro habilidades especiais foram implementadas e conectadas:
   - **Kael - Especial 1 (Explosão Arcana)**: Detonação em área com dano mágico massivo.
   - **Kael - Especial 2 (Campo de Lentidão)**: Vórtice temporal persistente por 6 segundos com restauração garantida de velocidade na saída ou término.
   - **Lyra - Especial 1 (Chuva de Flechas)**: Saraivada aérea que atinge inimigos no raio selecionado.
   - **Lyra - Especial 2 (Disparo Perfurante)**: Feixe retilíneo que atravessa adversários com registro unitário de impacto por alvo.
5. Ao alcançar o final do caminho, o monstro aplica dano à Fortaleza Central e se autodestrói com segurança.
6. A batalha encerra com `Derrota` se a vida da base for a zero, ou `Vitória` ao derrotar todos os inimigos das 3 ondas.
