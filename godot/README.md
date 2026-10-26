# Heroes Tower Defense — Módulo 3D Isométrico Godot 4 (Upgrade Gráfico Extremo — Etapas 1/3 e 2/3)

Este módulo implementa a reformulação gráfica extrema dos cenários 3D, câmera isométrica canônica, personagens articulados e torres detalhadas do **Heroes Tower Defense** utilizando a engine **Godot 4.3 (Mobile/Vulkan & GL Compatibility)**.

---

## 1. Visão Geral da Etapa 2/3 — Modelos 3D, Personagens Articulados e Torres Detalhadas

A Etapa 2/3 substitui geometrias provisórias por modelos 3D estilizados com materiais PBR vibrantes, silhuetas reconhecíveis e articulação hierárquica completa inspirada na qualidade visual de jogos mobile comerciais como *Clash Royale*:

### 1.1. Os 6 Heróis de Fantasia com Modelos 3D e Articulação
Cada herói possui modelo 3D binário glTF 2.0 (`.glb`) em `models/`, cena dedicada em `scenes/heroes/` e controlador articulado em `scripts/hero_base_3d.gd`:

1. **Kael — Guerreiro de Fogo (`hero_kael.glb`, `hero_kael.tscn`)**:
   - **Modelo 3D**: Peitoral de armadura em aço com filigrana dourada, capacete com fenda de visor e crista flamejante incandescente (`emissiveFactor`), ombreiras em placas duplas, grevas e sabatons blindados, capa de herói carmesim esvoaçante.
   - **Arma**: Espada Larga Flamejante (*Flaming Greatsword*) com guarda-mão dourado, gema de rubi e lâmina envolvida em aura de fogo viva.
   - **Animações Articuladas**: `IDLE` (respiração rítmica com brilho na lâmina), `WINDUP` (torção de tronco e elevação de espada para trás), `ATTACK` (golpe descendente veloz com avanço de passada), `RECOVERY` (retorno elástico à guarda), `SKILL` (salto de invocação flamejante), `HIT` (recuo de tronco e cabeça) e `DEFEAT` (queda de joelhos e colapso no solo).

2. **Lyra — Arqueira Arcana (`hero_lyra.glb`, `hero_lyra.tscn`)**:
   - **Modelo 3D**: Túnica élfica verde-esmeralda ajustada com detalhes de couro florestal, tiara de prata na cabeça, orelhas pontiagudas, aljava de madeira com flechas empenadas nas costas e botinas leves de caçadora.
   - **Arma**: Arco Longo Composto Arcano recurvo com corda brilhante e extremidades energizadas (`emissiveFactor`).
   - **Animações Articuladas**: `IDLE` (postura atenta com oscilação do arco), `WINDUP` (braço esticado apontando o alvo e puxada de corda), `ATTACK` (disparo tenso e vibração de recuo), `RECOVERY` (recarga veloz), `SKILL` (salto acrobático com saraivada astral), `HIT` e `DEFEAT`.

3. **Brakk — Guardião da Fortaleza (`hero_brakk.glb`, `hero_brakk.tscn`)**:
   - **Modelo 3D**: Armadura de placas monolíticas de granito e ferro negro, grande elmo com chifres e fenda de visor dourado iluminado, espaldar de escudo maciço no ombro esquerdo e pernas de pilar de pedra.
   - **Arma**: Martelo de Guerra Colossal (*Colossal Warhammer*) com cabeça retangular chanfrada de aço forjado e cabo de carvalho reforçado.
   - **Animações Articuladas**: `IDLE` (respiração pesada e firmeza de guarda), `WINDUP` (elevação do martelo acima da cabeça com inclinação de ombro), `ATTACK` (martelada sísmica com choque no solo), `RECOVERY`, `SKILL` (investida com aura de baluarte impenetrável), `HIT` e `DEFEAT`.

4. **Nyra — Assassina das Sombras (`hero_nyra.glb`, `hero_nyra.tscn`)**:
   - **Modelo 3D**: Traje furtivo de couro negro e seda violeta escura, capuz das sombras com fendas oculares roxas luminescentes, capas esfarrapadas nos ombros e botinas silenciosas de shinobi.
   - **Arma**: Adagas Gêmeas Serrilhadas das Sombras com lâminas afiadas de brilho violeta etéreo.
   - **Animações Articuladas**: `IDLE` (postura agachada ágil), `WINDUP` (cruzamento de braços em X com as lâminas), `ATTACK` (corte duplo cruzado veloz), `RECOVERY` (giro de lâmina e recuo ágil), `SKILL` (passo das sombras com névoa do vácuo), `HIT` e `DEFEAT`.

5. **Tiko — Engenheiro Artífice (`hero_tiko.glb`, `hero_tiko.tscn`)**:
   - **Modelo 3D**: Avental de couro resistente com rebites de bronze sobre caldeira metálica, boné de inventor com óculos de aviador de lentes ciano brilhantes, mochila geradora a vapor com chaminé de cobre e engrenagens de latão.
   - **Arma**: Chave Inglesa Industrial Gigante e lançador mecânico de pulso.
   - **Animações Articuladas**: `IDLE` (balanceio de ferramentas e escape de vapor na chaminé), `WINDUP` (preparação de disparo mecânico e ajuste de manômetro), `ATTACK` (arremesso de projétil engenhoso e golpe com a chave), `RECOVERY`, `SKILL` (superaquecimento da caldeira com labaredas ciano), `HIT` e `DEFEAT`.

6. **Vesper — Maga Astral (`hero_vesper.glb`, `hero_vesper.tscn`)**:
   - **Modelo 3D**: Manto celestial azul-meia-noite e índigo fluído com bordados de constelações estelares douradas, capuz místico com tiara estelar, saia de tecido cerimonial até o solo.
   - **Arma**: Cetro da Lua Crescente Astral dourado encimado por uma esfera cósmica cintilante flutuante (`emissiveFactor`).
   - **Animações Articuladas**: `IDLE` (levitação do cetro e pulso suave de poeira estelar), `WINDUP` (canalização mística com as duas mãos erguidas), `ATTACK` (liberação de projétil cósmico estelar), `RECOVERY`, `SKILL` (conjunção estelar com eclipse dimensional), `HIT` e `DEFEAT`.

---

### 1.2. Os 4 Inimigos Comuns Articulados
Controlados por `scripts/enemy_base_3d.gd` ao longo do caminho `Path3D`:

1. **Goblin Saqueador (`enemy_goblin.glb`, `enemy_goblin.tscn`)**:
   - Corpo corcunda verde vivo, orelhas pontiagudas alongadas, tanga e ombreira de couro rústico desgastado, clava de madeira nodosa com cravos pontiagudos. Ciclo de marcha rápida e passos curtos com clava balançando.
2. **Esqueleto Amaldiçoado (`enemy_skeleton.glb`, `enemy_skeleton.tscn`)**:
   - Crânio de marfim com órbitas profundas acesas por alma ciano espectral, tórax com costelas e vértebras expostas, espada de ferro antiga entalhada e broquel de madeira circular reforçado com pregos.
3. **Orc Brutamontes (`enemy_orc.glb`, `enemy_orc.tscn`)**:
   - Estrutura maciça musculosa verde-musgo, mandíbula proeminente com presas inferiores pontiagudas, ombreira pesada de ferro com chifre esquerdo e machado de guerra de duas lâminas pesadíssimo.
4. **Mago Sombrio (`enemy_dark_mage.glb`, `enemy_dark_mage.tscn`)**:
   - Túnica longa de veludo roxo escuro, capuz pontiagudo com olhar do vácuo penetrante, cajado de madeira retorcida coroado por orbe de crânio com chamas arcanas roxas flutuantes.

---

### 1.3. Os 3 Chefes Épicos com Escala Superior e Mecânicas Multifase
Controlados por `scripts/boss_base_3d.gd` com escala de 1.7x a 2.5x e três fases de combate:

1. **Golem de Obsidiana (`boss_obsidian_golem.glb`, `boss_obsidian_golem.tscn`)**:
   - **Escala**: Colossal (2.2x).
   - **Modelo 3D**: Placas de rocha de basalto angulares escuras com fissuras de magma ardente incandescente, núcleo de lava viva no peito pulsando calor, punhos de pedra maciços para esmagamento e cabeça de boulder vulcânica.
   - **Habilidade Especial**: Fissura Sísmica de Magma com tremor violento de tela.
   - **Fases**: Fase 1 (Marcha firme), Fase 2 (Enfurecido: velocidade +25%), Fase 3 (Apocalipse vulcânico).

2. **Rainha das Sombras (`boss_shadow_queen.glb`, `boss_shadow_queen.tscn`)**:
   - **Escala**: Imponente (1.7x).
   - **Modelo 3D**: Rainha gótica soberana das sombras, coroa de obsidiana pontiaguda, vestido estruturado com saia de sombras em camadas, asas etéreas flutuantes nas costas e rapieiras gêmeas de estocada espectrais com brilho violeta.
   - **Habilidade Especial**: Chuva de Lâminas das Sombras (*Shadow Blade Flurry*).
   - **Fases**: Fase 1 (Levitação régia), Fase 2 (Fúria do Vácuo), Fase 3 (Nuvem Dimensional de Sombras).

3. **Dragão Astral (`boss_astral_dragon.glb`, `boss_astral_dragon.tscn`)**:
   - **Escala**: Titânica (2.5x de envergadura).
   - **Modelo 3D**: Dragão réptil cósmico quadrúpede com escamas azul-estelar profundas, pescoço arqueado, cabeça com chifres celestiais dourados, envergadura de asas articuladas com membranas reluzentes e cauda longa com espinhos estrelares.
   - **Habilidade Especial**: Baforada de Nova Estelar (*Starlight Nova Breath*).
   - **Fases**: Fase 1 (Voo rasteiro e marcha cósmica), Fase 2 (Pulso Estelar enfurecido), Fase 3 (Chuva Cósmica Final).

---

### 1.4. As 3 Torres Defensivas Detalhadas com Geometria e Recuo
1. **Torre de Arqueiros Detalhada (`tower_archer_detailed.glb`, `tower_archer_detailed.tscn`)**:
   - Fundação de cantaria octogonal chanfrada de pedra, galeria intermediária de vigas de madeira cruzadas, plataforma superior com ameias e seteiras de flecha, cobertura cônica com estandarte dourado ao vento e montaria giratória de balista dupla de assédio com recuo mecânico.
2. **Torre dos Magos Detalhada (`tower_mage_detailed.glb`, `tower_mage_detailed.tscn`)**:
   - Base de mármore esculpido com runas místicas, três colunas de ametista que sobem em espiral ao redor de um anel ritualístico de runas, coroada por um mefacristal focalizador luminescente azul/ciano que levita no centro pulsando continuamente.
3. **Torre de Artilharia / Canhoneiro Detalhada (`tower_cannon_detailed.glb`, `tower_cannon_detailed.tscn`)**:
   - Bastião blindado circular com placas de ferro rebitadas, parapeito defensivo de aço escuro, carruagem de arma giratória de 360°, canhão naval bombarda com anel de boca de bronze reforçado, pistões de amortecimento pneumático duplos de recuo e pilha de balas de ferro na plataforma.

---

## 2. Visão Geral da Etapa 1/3 (Preservada e Integrada)

1. **Câmera Isométrica Canônica (`isometric_camera.gd`)**:
   - Ângulos exatos: **Pitch = -35.264°** (`-atan(1/sqrt(2))`) e **Yaw = 45.0°**.
   - Suporte duplo a modos Ortográfico (2.5D Clássico) e Perspectiva Isométrica (38° FOV).
   - Tremor tátil de câmera com amortecimento harmônico (`trigger_shake`).

2. **Reconstrução do Terreno 3D**:
   - Terreno base volumétrico de 14.0 x 20.0 x 1.4m com bordas chanfradas.
   - Estrada de paralelepípedos em curva S e ponte de pedra medieval.
   - Portal megalítico ancestral ao noroeste e Fortaleza Castelo ao sul.
   - 8 plintos de pedra para posicionamento tático de torres.

3. **Catálogo dos 6 Biomas 3D Completos (`scenes/biomes/`)**:
   - `highland_forest.tscn` (Desfiladeiro dos Goblins)
   - `whispering_woods.tscn` (Floresta dos Sussurros)
   - `ancient_ruins.tscn` (Ruínas Ancestrais)
   - `crystal_caverns.tscn` (Cavernas de Cristal)
   - `volcanic_fortress.tscn` (Fortaleza Vulcânica)
   - `astral_peak.tscn` (Pico do Dragão)

4. **Ambientes Vivos e Animações Orgânicas (`living_environment.gd`)**:
   - Balanço harmônico de vento em árvores (`wind_sway`).
   - Bruxuleio natural de tochas e braseiros (`OmniLight3D`).
   - Ondulação de rios e superfícies líquidas.
   - Emissão de partículas 3D escalonáveis por qualidade gráfica.

5. **Ponte de Coordenadas 2D/3D (`coordinate_converter.gd`)**:
   - Conversão bidirecional matemática entre coordenadas normalizadas (0.0..1.0) do `StageOneMap` e coordenadas 3D de mundo.

---

## 3. Estrutura Completa de Arquivos

```text
godot/
├── project.godot                     # Configuração do projeto Godot 4.3 (Mobile Vulkan)
├── README.md                         # Documentação técnica das Etapas 1/3 e 2/3
├── models/                           # 25 Modelos 3D estilizados em formato GLTF 2.0 (.glb)
│   ├── terrain_chunk.glb
│   ├── bridge_stone.glb
│   ├── tree_stylized.glb
│   ├── rock_boulder.glb
│   ├── ruin_pillar.glb
│   ├── crystal_cluster.glb
│   ├── torch_brazier.glb
│   ├── fortress_castle.glb
│   ├── summon_portal.glb
│   ├── hero_kael.glb                 # Guerreiro de Fogo (Armadura, Espada Larga e Crista)
│   ├── hero_lyra.glb                 # Arqueira Arcana (Arco Composto, Aljava e Tiara)
│   ├── hero_brakk.glb                # Guardião (Placas de Granito e Martelo Colossal)
│   ├── hero_nyra.glb                 # Assassina das Sombras (Capuz, Adagas e Capa)
│   ├── hero_tiko.glb                 # Engenheiro (Caldeira a Vapor, Óculos e Chave)
│   ├── hero_vesper.glb               # Maga Astral (Túnica Cósmica, Cetro e Orbe)
│   ├── enemy_goblin.glb              # Goblin Saqueador (Clava com cravos e orelhas)
│   ├── enemy_skeleton.glb            # Esqueleto Amaldiçoado (Crânio, Espada e Broquel)
│   ├── enemy_orc.glb                 # Orc Brutamontes (Mandíbula com presas e Machado)
│   ├── enemy_dark_mage.glb           # Mago Sombrio (Capuz e Cajado com crânio do vácuo)
│   ├── boss_obsidian_golem.glb       # Chefe: Golem de Obsidiana (Magma vivo e punhos)
│   ├── boss_shadow_queen.glb         # Chefe: Rainha das Sombras (Coroa, asas e rapieiras)
│   ├── boss_astral_dragon.glb        # Chefe: Dragão Astral (Asas cósmicas, chifres e cauda)
│   ├── tower_archer_detailed.glb     # Torre de Arqueiros Detalhada (Ameias, balista e bandeira)
│   ├── tower_mage_detailed.glb       # Torre dos Magos Detalhada (Colunas espirais e megacristal)
│   └── tower_cannon_detailed.glb     # Torre Canhoneira Detalhada (Bastião de ferro e bombarda)
├── scenes/
│   ├── biomes/                       # 6 Cenas de Biomas 3D Dedicadas
│   │   ├── highland_forest.tscn
│   │   ├── whispering_woods.tscn
│   │   ├── ancient_ruins.tscn
│   │   ├── crystal_caverns.tscn
│   │   ├── volcanic_fortress.tscn
│   │   └── astral_peak.tscn
│   ├── heroes/                       # 6 Cenas de Heróis Articulados
│   │   ├── hero_kael.tscn
│   │   ├── hero_lyra.tscn
│   │   ├── hero_brakk.tscn
│   │   ├── hero_nyra.tscn
│   │   ├── hero_tiko.tscn
│   │   └── hero_vesper.tscn
│   ├── enemies/                      # 4 Cenas de Inimigos Comuns
│   │   ├── enemy_goblin.tscn
│   │   ├── enemy_skeleton.tscn
│   │   ├── enemy_orc.tscn
│   │   └── enemy_dark_mage.tscn
│   ├── bosses/                       # 3 Cenas de Chefes Épicos
│   │   ├── boss_obsidian_golem.tscn
│   │   ├── boss_shadow_queen.tscn
│   │   └── boss_astral_dragon.tscn
│   ├── tower_archer_detailed.tscn    # Cena da Torre de Arqueiros Detalhada
│   ├── tower_mage_detailed.tscn      # Cena da Torre dos Magos Detalhada
│   ├── tower_cannon_detailed.tscn    # Cena da Torre Canhoneira Detalhada
│   ├── battlefield_prototype.tscn    # Campo de batalha 3D protótipo
│   ├── projectile_arrow.tscn         # Flecha balística 3D
│   ├── projectile_magic.tscn         # Esfera mágica arcana 3D
│   └── projectile_cannonball.tscn    # Bala de canhão esférica de ferro 3D
├── scripts/
│   ├── hero_base_3d.gd               # Controlador base para os 6 heróis articulados
│   ├── enemy_base_3d.gd              # Controlador base para os 4 inimigos comuns
│   ├── boss_base_3d.gd               # Controlador base para os 3 chefes épicos multifase
│   ├── battlefield_prototype.gd      # Orquestrador do campo de batalha e spawn
│   ├── isometric_camera.gd           # Câmera isométrica canônica (-35.264°, 45°)
│   ├── coordinate_converter.gd       # Conversor matemático bidirecional 2D/3D
│   ├── environment_theme.gd          # Configurações de iluminação e atmosfera dos 6 biomas
│   ├── living_environment.gd         # Controlador de vento, tochas e partículas
│   ├── tower_archer.gd               # Lógica de mira, disparo e recuo da Torre de Arqueiros
│   ├── tower_mage.gd                 # Lógica de carga arcana e pulso da Torre dos Magos
│   ├── tower_cannon.gd               # Lógica de mira e recuo da Torre Canhoneira
│   ├── enemy_goblin.gd               # Controlador específico do Goblin
│   ├── projectile_arrow.gd           # Cinemática de voo balístico da flecha
│   ├── projectile_magic.gd           # Rastreamento teleguiado da magia
│   └── projectile_cannonball.gd      # Trajetória parabólica e impacto do canhão
└── tools/
    ├── generate_3d_assets.py         # Gerador Python automatizado dos 25 arquivos .glb
    └── validate_godot_project.py     # Validador automatizado de 100% dos assets e cenas
```

---

## 4. Como Executar a Validação Automatizada

Para validar 100% da integridade de todos os 25 modelos 3D `.glb`, 15 GDScripts, 23 cenas `.tscn` e fórmulas matemáticas:

```bash
python3 /workspace/godot/tools/validate_godot_project.py
```
