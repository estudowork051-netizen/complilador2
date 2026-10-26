# Heroes Tower Defense

> **Upgrade Gráfico Extremo — Etapa 1/3 (Cenários 3D e Câmera Isométrica) e Etapa 2/3 (Modelos 3D, Personagens Articulados e Torres Detalhadas)**

---

## 1. Nome Provisório do Jogo
**Heroes Tower Defense**

---

## 2. Upgrade Gráfico Extremo — Etapa 2/3: Modelos 3D, Personagens Articulados e Torres Detalhadas
Esta etapa transforma radicalmente os personagens, monstros, chefes e fortificações do Heroes Tower Defense em modelos 3D estilizados com materiais PBR vibrantes, proporções expressivas e esqueletos articulados inspirados no padrão de qualidade visual de jogos mobile comerciais como *Clash Royale*:

- **Os 6 Heróis de Fantasia com Modelos 3D e Animações Articuladas (`Character3DModelRegistry` & `scenes/heroes/`)**:
  - **Kael — Guerreiro de Fogo (`hero_kael.glb`)**: Armadura de placas de aço forjado com filigrana de ouro e tabardo vermelho, crista de elmo com labaredas incandescentes vivas (`emissiveFactor: [1.0, 0.45, 0.0]`), capa de herói carmesim e Espada Larga Flamejante (*Flaming Greatsword*) com aura de fogo. Animações articuladas completas: `IDLE`, `WINDUP`, `ATTACK`, `RECOVERY`, `SKILL`, `HIT` e `DEFEAT`.
  - **Lyra — Arqueira Arcana (`hero_lyra.glb`)**: Túnica élfica verde-esmeralda ajustada com detalhes de couro florestal, tiara estelar de prata com orelhas pontiagudas, aljava de carvalho com flechas empenadas nas costas e Arco Longo Composto Arcano recurvo com corda cintilante (`emissiveFactor: [0.2, 0.85, 1.0]`).
  - **Brakk — Guardião da Fortaleza (`hero_brakk.glb`)**: Placas monolíticas pesadas de granito e ferro negro, grande elmo com chifres chanfrados e fenda de visor dourado iluminado, espaldar de escudo maciço e Martelo de Guerra Colossal (*Colossal Warhammer*) com cabeça chanfrada de aço puro.
  - **Nyra — Assassina das Sombras (`hero_nyra.glb`)**: Traje de couro sombrio com seda violeta, capuz das sombras com fendas oculares roxas luminescentes (`emissiveFactor: [0.8, 0.1, 0.95]`), capa curta esfarrapada e Adagas Gêmeas Serrilhadas das Sombras com lâminas do vácuo.
  - **Tiko — Engenheiro Artífice (`hero_tiko.glb`)**: Caldeira metálica com avental de couro e rebites de bronze, boné com óculos de aviador de lentes ciano brilhantes, mochila mecânica a vapor com chaminé de escape de fumaça, Chave Inglesa Industrial Gigante e disparador de pulso.
  - **Vesper — Maga Astral (`hero_vesper.glb`)**: Manto celestial azul-meia-noite e índigo fluído com constelações estelares douradas, capuz místico com tiara estelar, saia cerimonial até o solo e Cetro da Lua Crescente Astral com orbe cósmico cintilante flutuante (`emissiveFactor: [0.5, 0.7, 1.0]`).

- **Os 4 Inimigos Comuns com Modelos 3D e Marcha Articulada (`scenes/enemies/`)**:
  - **Goblin Saqueador (`enemy_goblin.glb`)**: Corpo corcunda verde vivo, orelhas pontiagudas, tanga de couro e clava nodosa com cravos de ferro. Ciclo de passos rápidos e ataque com clava balançando.
  - **Esqueleto Amaldiçoado (`enemy_skeleton.glb`)**: Crânio de marfim com almas ciano nos olhos (`emissiveFactor: [0.05, 0.85, 1.0]`), vértebras e costelas expostas, espada de ferro antiga entalhada e broquel de madeira circular reforçado.
  - **Orc Brutamontes (`enemy_orc.glb`)**: Estrutura maciça musculosa verde-oliva, mandíbula proeminente com presas pontiagudas, ombreira pesada de ferro negro com chifre e machado de guerra de duas lâminas pesadíssimo.
  - **Mago Sombrio (`enemy_dark_mage.glb`)**: Túnica longa de veludo roxo escuro, capuz pontiagudo com olhar do vácuo penetrante, cajado de madeira retorcida coroado por orbe de crânio com chamas arcanas roxas flutuantes.

- **Os 3 Chefes Épicos com Escala Titânica e Mecânicas Multifase (`scenes/bosses/`)**:
  - **Golem de Obsidiana (`boss_obsidian_golem.glb`)**: Escala colossal de 2.2x. Basalto vulcânico escuro com fissuras profundas de magma ardente incandescente (`emissiveFactor: [1.0, 0.35, 0.0]`), núcleo de lava pulsante no peito e punhos maciços para golpes sísmicos. Suporta 3 fases (Marcha, Enfurecido com velocidade +25%, e Apocalipse de Magma com tremor violento de tela).
  - **Rainha das Sombras (`boss_shadow_queen.glb`)**: Escala imponente de 1.7x. Rainha gótica soberana das sombras, coroa de obsidiana pontiaguda, vestido estruturado com saia de sombras em camadas, asas etéreas flutuantes nas costas e rapieiras gêmeas de estocada espectrais com brilho violeta.
  - **Dragão Astral (`boss_astral_dragon.glb`)**: Escala titânica de 2.5x de envergadura. Dragão réptil cósmico quadrúpede com escamas azul-estelar profundas, cabeça com chifres celestiais dourados, envergadura de asas articuladas com membranas reluzentes e cauda longa com espinhos estrelares.

- **As 3 Torres Defensivas Detalhadas com Geometria e Recuo (`scenes/`)**:
  - **Torre de Arqueiros Detalhada (`tower_archer_detailed.glb`)**: Fundação de cantaria octogonal chanfrada de pedra, galeria intermediária de vigas de madeira cruzadas, plataforma superior com ameias e seteiras de flecha, cobertura cônica com estandarte dourado ao vento e montaria giratória de balista dupla de assédio com recuo mecânico.
  - **Torre dos Magos Detalhada (`tower_mage_detailed.glb`)**: Base de mármore esculpido com runas místicas, três colunas de ametista que sobem em espiral ao redor de um anel ritualístico de runas, coroada por um mefacristal focalizador luminescente azul/ciano que levita no centro pulsando continuamente.
  - **Torre de Artilharia / Canhoneiro Detalhada (`tower_cannon_detailed.glb`)**: Bastião blindado circular com placas de ferro rebitadas, parapeito defensivo de aço escuro, carruagem de arma giratória de 360°, canhão naval bombarda com anel de boca de bronze reforçado, pistões de amortecimento pneumático duplos de recuo e pilha de balas de ferro na plataforma.

- **Catálogo de 25 Modelos 3D GLTF 2.0 (`godot/models/`)**:
  - 9 modelos de cenário (terreno, ponte, árvore, rocha, pilar, cristais, tocha, castelo, portal).
  - 6 modelos de heróis (`hero_kael.glb`, `hero_lyra.glb`, `hero_brakk.glb`, `hero_nyra.glb`, `hero_tiko.glb`, `hero_vesper.glb`).
  - 4 modelos de monstros comuns (`enemy_goblin.glb`, `enemy_skeleton.glb`, `enemy_orc.glb`, `enemy_dark_mage.glb`).
  - 3 modelos de chefes épicos (`boss_obsidian_golem.glb`, `boss_shadow_queen.glb`, `boss_astral_dragon.glb`).
  - 3 modelos de torres detalhadas (`tower_archer_detailed.glb`, `tower_mage_detailed.glb`, `tower_cannon_detailed.glb`).

---

## 3. Upgrade Gráfico Extremo — Etapa 1/3: Cenários 3D, Câmera Isométrica e Integração Godot 4
Esta etapa transforma radicalmente os campos de batalha em ambientes 3D estilizados com acabamento inspirado em jogos mobile comerciais (referência artística *Clash Royale*):

- **Câmera Isométrica Canônica (`IsometricCamera` - `isometric_camera.gd` & `IsometricCoordinateConverter.kt`)**:
  - Ângulos matematicamente puros de projeção isométrica canônica: **Pitch = -35.264°** (`-atan(1/sqrt(2))`) e **Yaw = 45.0°** (`pi/4`).
  - Suporte a dois modos de projeção em tempo de execução:
    - **Ortográfica (2.5D Clássico)**: Sem fuga de perspectiva, proporção de diamante 2:1 exata e zero distorção angular.
    - **Perspectiva Isométrica (38° FOV)**: Profundidade e paralaxe volumétrica suave em orientação vertical portrait (9:16).
  - Tremor de tela tátil (`trigger_shake`) com amortecimento harmônico para impactos de canhão, explosões e golpes sísmicos.

- **Reconstrução Volumétrica do Terreno 3D**:
  - Bloco de terreno base com espessura de 1.4m (14.0 x 20.0m de área útil), bordas de desfiladeiro chanfradas e relevo escalonado.
  - Estrada sinuosa de paralelepípedos chanfrados individuais com meio-fio de pedra elevado.
  - Ponte de pedra medieval com arco de sustentação e pranchas de madeira reforçadas cruzando o canal central de perigo líquido.
  - Portal megalítico ancestral de pedra no ponto de invocação (noroeste) com lintel chanfrado, anel de vórtice dimensional e luz omnidirecional (`OmniLight3D`).
  - Fortaleza Castelo Real ao sul com bastiões circulares gêmeos, ameias de granito, portão de rastilho com cravos de ferro e bandeiras esvoaçantes.
  - 8 plintos de pedra para posicionamento tático de torres com prévia holográfica translúcida e anel indicador de alcance.

- **Catálogo dos 6 Biomas 3D Dedicados (`EnvironmentTheme` & `scenes/biomes/`)**:
  - **Fase 1: O Desfiladeiro dos Goblins (`highland_forest.tscn`)**: Grama esmeralda, estrada de terra quente, rio azul cristalino, coníferas em 3 níveis, tochas de pedra âmbar e partículas de pólen dourado flutuante.
  - **Fase 2: Floresta dos Sussurros (`whispering_woods.tscn`)**: Musgo verde-escuro denso, trilha úmida, córrego de névoa esmeralda, árvores com folhagem compacta, lanternas ciano e vaga-lumes reluzentes.
  - **Fase 3: Ruínas Ancestrais (`ancient_ruins.tscn`)**: Lajes de granito antigo desgastado, calçada de pedra cinzenta, poço abissal de energia do vácuo, pilares e arcos clássicos em ruínas, fogo-fátuo violeta e névoa espectral.
  - **Fase 4: Cavernas de Cristal (`crystal_caverns.tscn`)**: Basalto subterrâneo escuro, passarela de rocha esculpida, lago luminescente safira, aglomerados de cristais facetados azuis/ciano, lanternas arcanas e poeira de cristal cintilante.
  - **Fase 5: Fortaleza Vulcânica (`volcanic_fortress.tscn`)**: Basalto de obsidiana escurecido com fissuras de magma, estrada de lajes de ferro, rio torrencial de lava ardente incandescente, árvores carbonizadas, braseiros ardentes e fagulhas de brasas vulcânicas ascendentes.
  - **Fase 6: Pico do Dragão (`astral_peak.tscn`)**: Plataforma cósmica de poeira estelar azul-meia-noite, via celestial iluminada, abismo de nébula cósmica violeta, monólitos astrais flutuantes, balizas solares rosas e poeira estelar cintilante.

- **Ambientes Vivos e Animações Orgânicas (`LivingEnvironment` - `living_environment.gd`)**:
  - Balanço orgânico senoidal de árvores e folhagens com defasagem harmônica de vento (`wind_sway`).
  - Bruxuleio natural de chamas e pulsação de energia luminosa em tochas e braseiros (`OmniLight3D`).
  - Ondulação senoidal suave da superfície da água e líquidos de perigo.
  - Emissão de partículas ambientais em 3D escalonáveis por patamar de qualidade gráfica (LOW = desativado, MEDIUM = 16 partículas, HIGH = 32 partículas).

- **Ponte Matemática de Coordenadas 2D/3D (`IsometricCoordinateConverter`)**:
  - Conversão bidirecional entre coordenadas normalizadas (0.0..1.0, 0.0..1.0) do `StageOneMap` e coordenadas 3D de mundo (X, Y, Z).
  - Mapeamento exato dos 8 waypoints da rota e dos 8 pontos de torre táticos.

- **Gerador Automatizado de Modelos 3D GLTF 2.0 (`tools/generate_3d_assets.py`)**:
  - Gera 9 modelos binários `.glb` com normais de vértice e materiais PBR em `godot/models/` (`terrain_chunk.glb`, `bridge_stone.glb`, `tree_stylized.glb`, `rock_boulder.glb`, `ruin_pillar.glb`, `crystal_cluster.glb`, `torch_brazier.glb`, `fortress_castle.glb`, `summon_portal.glb`).

- **Validador Automatizado do Módulo Godot 4 (`tools/validate_godot_project.py`)**:
  - Suíte de verificação que valida 100% dos arquivos `.tscn`, scripts `.gd`, modelos `.glb` e fórmulas matemáticas do projeto via terminal.

---

## 3. Upgrade Visual (Parte 4/6) — Reformulação Extrema do Combate, Projéteis e Habilidades
Esta etapa eleva a apresentação das batalhas a um padrão visual de jogo mobile comercial de fantasia, substituindo formas geométricas e efeitos simples por animações articuladas, projéteis estilizados em 2.5D com elevação balística, impactos cinematográficos com detritos e vibração de tela, além de efeitos exclusivos para as habilidades especiais dos 6 heróis:

- **Animações Articuladas de Ataque dos 6 Heróis (`HeroCharacterRenderer`)**:
  - Máquina de estados rítmica (`HeroAnimState`): `IDLE` (respiração e prontidão), `WINDUP` (antecipação e puxada), `STRIKE` (golpe e projeção), `RECOVERY` (recuo e equilíbrio), `SKILL_CAST` (canalização rúnica) e virada dinâmica de orientação (`facingDirection`).
  - **Kael (Guerreiro do Fogo)**: Golpes de espada de movimento amplo com rastro flamejante incandescente em arco duplo, faíscas dinâmicas de brasa e recuo elástico de lâmina.
  - **Lyra (Arqueira Arcana)**: Puxada física da corda do arco místico com curvatura dos arcos, preparação da flecha de luz, disparo veloz com rastro de vento translúcido e estilhaços celestes.
  - **Brakk (Guardião das Montanhas)**: Elevação monumental da maça de guerra, impacto violento no solo com ondas de choque de pedra chanfrada e poeira sísmica ascendente.
  - **Nyra (Assassina das Sombras)**: Investidas velozes com lâminas gêmeas sombrias em corte em tesoura cruzada e fumaça violeta do vácuo.
  - **Tiko (Artífice / Engenheiro)**: Chave mecânica articulada com engrenagens visíveis, dispositivos de arremesso com faíscas elétricas e fumaça de pólvora.
  - **Vesper (Maga Astral)**: Conjuradora cósmica com orbe flutuante e cajado cósmico, anéis rúnicos brilhantes nos pés e nas mãos, e projéteis estelares com rastro de cometa.

- **Arsenal de Projéteis Estilizados 2.5D (`CombatVfxRenderer` & `CombatProjectile`)**:
  - **Flecha Balística (Torre de Arqueiros & Lyra)**: Haste de cedro escuro polido, ponta de aço triangular perfurante com brilho especular no gume e barbas talhadas, empenagem dupla de penas esmeralda e fita de vento translúcida.
  - **Míssil de Energia Arcana (Torre de Magos & Vesper)**: Núcleo giratório de cristal mana em diamante lapidado, anel orbital de runas douradas acelerando com a rotação, aura de plasma ciano/violeta e rastro de poeira estelar cintilante.
  - **Bala de Canhão Pesada (Torre Canhoneira & Tiko)**: Esfera 3D de ferro fundido com iluminação radial volumétrica, cinta central de reforço rebitada em ouro, rastro de pavio aceso com faíscas e nuvens de fumaça cinzenta na trajetória.
  - **Estilhaço de Gelo (Torre de Gelo)**: Espícula facetada de gelo glacial translúcido com névoa fria.
  - **Elevação Balística Parabólica 3D (`arcHeightOffset`)**: Curva senoidal tridimensional real que eleva flechas e balas de canhão no eixo Z durante a trajetória, conferindo sensação de altura e gravidade.

- **Sistema de Impactos, Explosões e Feedback Físico**:
  - **Flash de Impacto**: Núcleo branco incandescente instantâneo no ponto de colisão.
  - **Ondas de Choque Estilizadas**: Anéis expansivos concêntricos em degradê de cor e amortecimento quadrático de opacidade.
  - **Faíscas Direcionais**: Feixes de partículas de luz ejetadas no ângulo de impacto.
  - **Fissuras Sísmicas**: Rachaduras radiais no solo para impactos pesados e explosões.
  - **Detritos Balísticos (`VisualPhysicsDebris`)**: Fragmentos de rocha, brasas e lascas de metal com física 3D real e quiques no solo.
  - **Vibração Tátil de Câmera (`CameraScreenShake`)**: Amortecimento harmônico proporcional ao peso do ataque (tiros de canhão, pancada de Brakk, explosões de Tiko e derrota de chefes).

- **Efeitos Visuais Exclusivos das 12 Habilidades Especiais**:
  - **Kael**: *Golpe Flamejante* (coluna vertical de labaredas e anel abrasivo) & *Onda de Fogo* (onda circular serrada de chamas com línguas de fogo).
  - **Lyra**: *Tiro de Precisão* (feixe celestino perfurante de luz solar em cruz) & *Chuva de Flechas* (chuva mística de projéteis caindo do céu com pólen verde-ouro).
  - **Brakk**: *Pancada Terrestre* (terremoto com 6 fissuras e poeira de pedra) & *Fortificação Protetora* (cúpula de proteção runada com 8 glifos ancestrais giratórios).
  - **Nyra**: *Ataque Sombrio* (corte em cruz com lâminas do vácuo) & *Dança das Lâminas* (ciclone de adagas sombrias rodopiantes com caudas violetas).
  - **Tiko**: *Mina de Fragmentação* (detonação de engrenagens com dentes e estilhaços) & *Engenho Explosivo* (explosão incendiária de alta potência com cogumelo de fogo e fumaça).
  - **Vesper**: *Cometa Astral* (meteoro estelar radiante com raios cósmicos) & *Singularidade Cósmica* (vórtice de galáxia espiralada atraindo matéria cósmica).

- **Torres com Animação Ativa de Disparo (`IsometricTowerRenderer`)**:
  - **Torre de Arqueiros**: Recuo elástico do arco com faísca luminosa de liberação da flecha no parapeito.
  - **Torre de Magos**: Cristal superior levitante pulsa em clarão ofuscante de energia arcana e aceleração das runas orbitais ao lançar magia.
  - **Torre Canhoneira**: Recuo axial do canhão de ferro fundido com labareda de disparo na boca do canhão e baforada de fumaça escura.

- **Evolução do Módulo Godot 4 (`godot/`)**:
  - Implementação das 3 torres ativas em cena (`tower_archer`, `tower_mage` com cristal e `tower_cannon` com canhão giratório).
  - Implementação dos projéteis dedicados: `projectile_arrow.tscn`, `projectile_magic.tscn` (míssil teleguiado) e `projectile_cannonball.tscn` (bala de canhão com dano AOE).
  - Sistema de partículas de impacto com luz omnidirecional (`combat_vfx_impact.tscn`).

---

## 3. Upgrade Visual (Parte 3/6) — Reformulação Gráfica dos Mapas, Cenários e Ambientes
Esta etapa promoveu uma transformação gráfica profunda nos campos de batalha e construções do jogo, inspirada na direção de arte estilizada 3D isométrica de referência (estilo *Clash Royale*), com zero alocação de objetos em tempo de execução e total fidelidade à arquitetura Android:

- **Arquitetura Isométrica de 9 Camadas de Profundidade (`StageOneCanvasView`)**:
  1. **Camada de Terreno Base**: Texturas multi-tonais com relevo de elevação 3D, bordas de desfiladeiro e grade isométrica estilizada.
  2. **Camada de Caminhos & Pontes**: Estrada sinuosa com paralelepípedos chanfrados individuais desenhados proceduralmente, meio-fio de pedra elevado com iluminação no topo e sombras inferiores, além de pranchas de madeira reforçadas em travessias sobre rios e fendas.
  3. **Camada de Líquidos e Riscos Ambientais**: Rios com ondulação suave de água cristalina, córregos de névoa esmeralda, poços de energia do vácuo ou rios incandescentes de magma fervilhante com brilho costeiro.
  4. **Camada de Adornos e Elementos Vivos**:
     - Árvores com folhagem em 3 níveis com balanço suave pelo vento (*sway oscillation* com física senoidal).
     - Boulders e rochedos chanfrados com facetas de luz e musgo.
     - Tochas de pedra e braseiros de ferro forjado com chamas dançantes multi-línguas e iluminação radial dinâmica (*Point Lights*).
  5. **Camada de Portal & Fortaleza 3D**:
     - **Portal de Invocação**: Pilares de pedra rúnica ancestral com lintel chanfrado, vortex dimensional giratório e runas brilhantes pulsantes.
     - **Castelo Fortaleza do Reino**: Bastiões de canto em granito cinzento, ameias de pedra chanfrada, portão de rastilho com cravos de ferro e bandeiras reais esvoaçantes nas torres.
  6. **Ordenação de Profundidade Y (*Y-Sorting*)**:
     - Torres defensivas, heróis em campo, inimigos e chefões épicos são organizados dinamicamente no eixo Y da tela, garantindo oclusão realista (quem está ao sul sobrepõe quem caminha ao norte).
  7. **Torres em Perspectiva 3D Isométrica Volumétrica (`IsometricTowerRenderer`)**:
     - **Torre de Arqueiros (Archer Tower)**: Base hexagonal de pedra com facetas de luz e sombra, 4 pilares de toras de madeira reforçados por aros de ferro, parapeito de tábuas com ameias, telhado cônico em telhas verde-floresta com cata-vento medieval e arco visível.
     - **Torre de Magos (Mage Tower)**: Espiral cilíndrica escalonada em pedra mística azul-índigo com runas douradas incrustadas, canais verticais de energia arcana ciano, balcão de ferro e cristal arcano colossal lapidado levitando e girando com anéis orbitais.
     - **Torre Canhoneira (Cannon Tower)**: Forte robusto em blocos de granito escuro com chapas de ferro rebitadas e rebites dourados, cúpula giratória e canhão de ferro fundido negro de grosso calibre com recuo elástico de disparo.
     - **Torre de Gelo (Frost Tower)**: Pináculo glacial facetado com névoa fria.
     - **Animação de Construção e Prévia Holográfica**: Efeito elástico de pop-up (*bounce scale* 0.5s) ao assentar a torre com nuvem de poeira e detritos, além de projeção holográfica translúcida no modo de seleção com indicador de alcance.
  8. **Física Visual Leve & Impactos na Câmera**:
     - **Física de Detritos (`VisualPhysicsDebris`)**: Estilhaços, lascas de rocha e faíscas com altitude tridimensional (`z`), gravidade real (`550 px/s²`), atrito e quiques balísticos no solo.
     - **Tremor de Câmera (`CameraScreenShake`)**: Feedback tátil visual com amortecimento exponencial em disparos de canhão, impactos sísmicos de chefes e habilidades heróicas.
  9. **Atmosfera, Partículas Ambientais e Vinheta**:
     - Partículas flutuantes adaptadas a cada ambiente (pólen dourado, vaga-lumes ciano, névoa espectral, pó de cristal, brasas vulcânicas, poeira cósmica estelar).
     - Vinheta de contraste para leitura perfeita do combate.

- **Catálogo dos 6 Temas Ambientais da Campanha (`EnvironmentTheme`)**:
  - **Fase 1**: *Highland Forest (O Desfiladeiro dos Goblins)* — Grama esmeralda, estrada de terra quente, rio azul e pólen dourado.
  - **Fase 2**: *Whispering Woods (Floresta dos Sussurros)* — Vegetação densa com névoa esmeralda, lanternas ciano e vaga-lumes.
  - **Fase 3**: *Ancient Ruins (Ruínas Ancestrais)* — Lajes de granito antigo, pedras cinzentas, fogo-fátuo violeta e névoa espectral.
  - **Fase 4**: *Crystal Caverns (Cavernas de Cristal)* — Solo escuro subterrâneo, formações rochosas azuladas, cristais arcanos e pó cintilante.
  - **Fase 5**: *Volcanic Fortress (Fortaleza Vulcânica)* — Basalto escurecido, fendas incandescentes de magma, braseiros ardentes e brasas em ascensão.
  - **Fase 6**: *Astral Peak (Pico do Dragão / Dimensão Astral)* — Céu azul-meia-noite cósmico com vias estelares, monolitos celestes e poeira estelar cintilante.

- **Configuração de Qualidade Gráfica (`GraphicQuality`)**:
  - `LOW` (Econômico / Bateria): Desativa partículas ambientais e detritos pesados.
  - `MEDIUM` (Equilibrado): Partículas moderadas, iluminação dinâmica e tremor de tela.
  - `HIGH` (Visual Máximo - Padrão): Todos os efeitos visuais, física de detritos, iluminação dinâmica e partículas ambientais a 60 FPS.

---

## 3. Upgrade Visual (Parte 2/6) — Inimigos e Chefes Articulados
- **4 Inimigos Comuns Procedurais**: Goblins saqueadores com orelhas articuladas, Esqueletos amaldiçoados com chamas espectrais nas órbitas, Orcs guerreiros brutamontes com machado duplo e Magos Sombrios com mantos esvoaçantes e orbes de ametista.
- **3 Chefes Épicos**: Golem de Obsidiana (fissuras de magma e núcleo vulnerável), Rainha das Sombras (manto de sombras e lâmina escura) e Dragão Astral (asas cósmicas, chifres astrais e Fase 2 em Fúria Solar).
- **Máquina de Estados de Animação (`EnemyAnimState`)**: IDLE, WALK, ATTACK, HIT, DEATH e PHASE_TRANSITION.

---

## 4. Upgrade Visual (Parte 1/6) — Heróis Articulados e Identidade Própria
- **6 Heróis Lendários**: Kael (Cavaleiro do Fogo), Lyra (Patrulheira Arcana), Brakk (Sentinela das Montanhas), Nyra (Lâmina do Crepúsculo), Tiko (Artífice do Reino) e Vesper (Oráculo das Estrelas).
- **Renderização Esquelética e Animações (`HeroAnimState`)**: IDLE, WINDUP, STRIKE, RECOVERY, SKILL_CAST, VICTORY e virada dinâmica de orientação.

---

## 5. Estrutura do Projeto

```
heroes-tower-defense/
├── .github/
│   └── workflows/
│       └── build-apk.yml               # Pipeline de CI/CD para compilação e artefato do APK
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties   # Configuração do Gradle 8.7 com distributionSha256Sum
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml     # Registro das 7 Activities em orientação portrait
│   │   │   ├── java/com/heroes/towerdefense/
│   │   │   │   ├── audio/
│   │   │   │   │   └── SoundManager.kt             # Motor de áudio procedural e sintetizador PCM 16-bit
│   │   │   │   ├── core/
│   │   │   │   │   └── GameConstants.kt            # Parâmetros, limites, constantes e versionamento
│   │   │   │   ├── data/
│   │   │   │   │   ├── GamePreferences.kt          # Preferências, moedas, heróis, fases e GraphicQuality
│   │   │   │   │   ├── InMemoryStorage.kt          # Armazenamento em memória para testes unitários
│   │   │   │   │   ├── KeyValueStorage.kt          # Interface de abstração chave-valor
│   │   │   │   │   └── SharedPrefsStorage.kt       # Implementação SharedPreferences Android
│   │   │   │   ├── game/
│   │   │   │   │   ├── boss/
│   │   │   │   │   │   ├── ActiveBoss.kt           # Instância ativa de chefe em combate
│   │   │   │   │   │   ├── BossAttack.kt           # Modelo de ataques telegrafados
│   │   │   │   │   │   ├── BossConfig.kt           # Configurações do Golem, Rainha e Dragão
│   │   │   │   │   │   └── BossType.kt             # Enum dos chefes épicos
│   │   │   │   │   ├── economy/
│   │   │   │   │   │   └── EconomyManager.kt       # Recompensas, deduplicação e bônus de vitória
│   │   │   │   │   ├── engine/
│   │   │   │   │   │   ├── GameEngine.kt           # Motor de simulação pura de partida e combate
│   │   │   │   │   │   └── GameEngineListener.kt   # Callbacks de eventos de combate e áudio
│   │   │   │   │   ├── map/
│   │   │   │   │   │   ├── EnvironmentTheme.kt     # 6 Temas visuais da campanha (Upgrade Visual 3)
│   │   │   │   │   │   ├── GraphicQuality.kt       # Tiers de qualidade gráfica LOW/MEDIUM/HIGH (Upgrade Visual 3)
│   │   │   │   │   │   ├── StageOneMap.kt          # Coordenadas, waypoints e pontos de torre
│   │   │   │   │   │   └── Waypoint.kt             # Modelo de waypoints do caminho
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── ActiveEnemy.kt          # Inimigos ativos com máquina de estados de animação
│   │   │   │   │   │   ├── AmbientParticle.kt      # Partículas ambientais e atmosféricas (Upgrade Visual 3)
│   │   │   │   │   │   ├── CombatParticle.kt       # Partículas dinâmicas de combate
│   │   │   │   │   │   ├── CombatProjectile.kt     # Projéteis de combate
│   │   │   │   │   │   ├── DeployedHero.kt         # Herói posicionado em batalha com estados de animação
│   │   │   │   │   │   ├── EnemyAnimState.kt       # Estados de animação dos monstros
│   │   │   │   │   │   ├── EnemyVisualType.kt      # Arquétipos visuais de monstros e chefes
│   │   │   │   │   │   ├── GameStatus.kt           # Enum de estados da partida
│   │   │   │   │   │   ├── HeroAnimState.kt        # Máquina de estados de animação dos heróis
│   │   │   │   │   │   ├── HeroSkillState.kt       # Estado e cooldown de habilidades
│   │   │   │   │   │   ├── PlacedTower.kt          # Torre com timers de spawn scale e recoil (Upgrade Visual 3)
│   │   │   │   │   │   ├── TowerSpot.kt            # Local para construção
│   │   │   │   │   │   ├── VisualEffect.kt         # Explosões e anéis visuais
│   │   │   │   │   │   ├── VisualPhysicsDebris.kt  # Detritos físicos balísticos com quique (Upgrade Visual 3)
│   │   │   │   │   │   └── WaveConfig.kt           # Configurações de ondas
│   │   │   │   │   ├── progression/
│   │   │   │   │   │   ├── BaseEvolutionManager.kt # Evolução de níveis da fortaleza
│   │   │   │   │   │   └── HeroUnlockManager.kt    # Desbloqueio e seleção de heróis
│   │   │   │   │   ├── shop/
│   │   │   │   │   │   └── ShopManager.kt          # Loja de melhorias permanentes
│   │   │   │   │   ├── stage/
│   │   │   │   │   │   └── StageConfig.kt          # Configuração centralizada das 6 fases
│   │   │   │   │   └── view/
│   │   │   │   │       ├── CameraScreenShake.kt     # Controlador de tremor de câmera harmônico (Upgrade Visual 3)
│   │   │   │   │       ├── EnemyAvatarView.kt       # Custom View de busto animado do chefe
│   │   │   │   │       ├── EnemyCharacterRenderer.kt# Renderizador procedural articulado 2D Canvas de monstros
│   │   │   │   │       ├── EnvironmentMapRenderer.kt# Renderizador isométrico de 9 camadas do terreno (Upgrade Visual 3)
│   │   │   │   │       ├── HeroCharacterRenderer.kt # Renderizador esquelético/articulado 2D Canvas de heróis
│   │   │   │   │       ├── IsometricTowerRenderer.kt# Renderizador 3D isométrico de torres (Upgrade Visual 3)
│   │   │   │   │       └── StageOneCanvasView.kt    # Canvas 2D completo com Y-sorting e física visual
│   │   │   │   ├── model/
│   │   │   │   │   ├── enemy/Enemy.kt              # Modelo de inimigos
│   │   │   │   │   ├── hero/
│   │   │   │   │   │   ├── Hero.kt                 # Catálogo dos 6 heróis planejados
│   │   │   │   │   │   ├── HeroRarity.kt           # 5 patamares de raridade
│   │   │   │   │   │   └── HeroSkill.kt            # Modelo de habilidades ativas
│   │   │   │   │   ├── stage/StageInfo.kt          # Metadados das fases
│   │   │   │   │   └── tower/Tower.kt              # Modelos de torres e atributos
│   │   │   │   └── ui/
│   │   │   │       ├── heroes/
│   │   │   │       │   ├── HeroAvatarView.kt           # View customizada animada de retrato dos heróis
│   │   │   │       │   └── HeroesActivity.kt           # Coleção dos heróis com fichas
│   │   │   │       ├── main/MainActivity.kt                # Menu principal com progresso
│   │   │   │       ├── preparation/StagePreparationActivity.kt # Preparação da fase com herói ativo
│   │   │   │       ├── settings/SettingsActivity.kt        # Configurações de som e gameplay
│   │   │   │       ├── shop/ShopActivity.kt                # Loja com debouncing
│   │   │   │       ├── stage/StageOneActivity.kt           # Arena jogável com áudio e partículas
│   │   │   │       └── stages/StageSelectActivity.kt       # Seleção dinâmica de fases
│   │   │   └── res/
│   │   │       ├── layout/                             # 7 layouts XML completos
│   │   │       ├── values/                             # Strings, cores e temas de fantasia
│   │   │       └── drawable/                           # Badges, botões estilizados e ícones
│   │   └── test/
│   │       └── java/com/heroes/towerdefense/
│   │           ├── BossAndProgressionTest.kt                 # 16 testes de chefes e progressão
│   │           ├── CombatAndVisualEffectsUpgradePart4Test.kt # 12 testes de combate, projéteis e VFX (Upgrade Visual 4)
│   │           ├── EconomyAndProgressionTest.kt              # 16 testes de economia e evolução
│   │           ├── EnemyAndBossVisualUpgradePart2Test.kt     # 16 testes de animações e chefes (Upgrade Visual 2)
│   │           ├── EnvironmentAndMapVisualUpgradePart3Test.kt# 12 testes de ambientes, física e torres 3D (Upgrade Visual 3)
│   │           ├── ExtremeVisualUpgradeStage1Test.kt         # 10 testes de cenários 3D, câmera e bridge (Etapa 1/3)
│   │           ├── GameConstantsTest.kt                      # 4 testes de constantes
│   │           ├── GameEngineTest.kt                         # 22 testes de motor de combate
│   │           ├── GameModelsTest.kt                         # 6 testes de modelos de domínio
│   │           ├── HeroSystemTest.kt                         # 14 testes de heróis e habilidades
│   │           ├── HeroVisualUpgradePart1Test.kt             # 11 testes de animações e heróis (Upgrade Visual 1)
│   │           ├── NavigationContractTest.kt                 # 4 testes de Activities e contratos
│   │           └── Part8PolishAndAudioTest.kt                # 12 testes de áudio, partículas e polimento
│   │   │   │   ├── CombatVfxRenderer.kt     # Renderizador estilizado 2.5D de projéteis e VFX (Upgrade Visual 4)
│   │   │   │   ├── IsometricCoordinateConverter.kt # Conversor matemático 2D/3D e waypoints canônicos (Etapa 1/3)
│   │   │   │   ├── ProjectileVisualType.kt  # Categorização e física de projéteis (Upgrade Visual 4)
│   │   │   │   └── VisualEffectType.kt      # Categorização e reatividade de efeitos (Upgrade Visual 4)
│   │   └── ...
```

---

## 7. Suíte de Testes Automatizados (164 Testes no Total)

| Suíte de Testes | Quantidade | Foco da Validação | Status |
| :--- | :---: | :--- | :--- |
| **`ExtremeVisualUpgradeStage2Test`** | **8** | Arquétipos visuais 3D dos 6 heróis (Kael, Lyra, Brakk, Nyra, Tiko, Vesper), nós articulados e armas, máquina de estados de animação (IDLE, WINDUP, STRIKE, RECOVERY, SKILL_CAST, VICTORY), modelos 3D dos 4 inimigos comuns e ciclos de animação (WALK, ATTACK, HIT, DEATH), escala superior dos 3 chefes épicos (Golem, Rainha, Dragão) e mecânica multifase, geometria detalhada e recuo das 3 torres (Arqueiro, Mago, Canhoneiro), unicidade de modelos .glb | **100% APROVADO** |
| **`ExtremeVisualUpgradeStage1Test`** | **11** | Ângulos de câmera isométrica canônica (Pitch: -35.264°, Yaw: 45.0°), fidelidade de conversão bidirecional 2D normalizado <-> 3D mundo, mapeamento exato dos 8 waypoints da rota e dos 8 spots táticos de torre, atmosfera 3D e iluminação solar nos 6 biomas de `EnvironmentTheme`, projeção isométrica 2:1, proporções verticais do terreno para mobile (14x20m) e integridade de StageOneMap | **100% APROVADO** |
| **`CombatAndVisualEffectsUpgradePart4Test`** | **12** | Ciclo de animações de ataque dos 6 heróis (WINDUP, STRIKE, RECOVERY, IDLE), máquina de estados das 12 habilidades heróicas (SKILL_CAST -> IDLE), 9 tipos de ProjectileVisualType, trajetória balística com elevação parabólica 3D (arcHeightOffset), classificação e traços de VisualEffectType, feedback tátil com CameraScreenShake e VisualPhysicsDebris conforme GraphicQuality, animação de recuo e disparo de torres (recoilAnimTimer), disparo automático e combate com dano no GameEngine | **100% APROVADO** |
| **`EnvironmentAndMapVisualUpgradePart3Test`** | **12** | 6 EnvironmentThemes da campanha, mapeamento e fallback, atributos específicos, tiers de GraphicQuality (LOW/MEDIUM/HIGH) e persistência em GamePreferences, PlacedTower spawn scale e recoil kickback, física de CameraScreenShake com decay harmônico, física balística 3D de VisualPhysicsDebris com quique no solo, simulação de AmbientParticle com pulsação e wrapping, Y-sorting para profundidade isométrica, integridade de StageOneMap e GameEngine | **100% APROVADO** |
| **`EnemyAndBossVisualUpgradePart2Test`** | **16** | Ciclo de estados de animação dos monstros (`EnemyAnimState`), tipos visuais (`EnemyVisualType`), máquina de estados em `ActiveEnemy`, chefes épicos telegrafados e transição de fase do Dragão Astral | **100% APROVADO** |
| **`HeroVisualUpgradePart1Test`** | **11** | Ciclo de estados de animação dos heróis (`HeroAnimState`), facing direction, ativação e expiração de habilidades, pose de vitória e integridade dos 6 heróis | **100% APROVADO** |
| **`Part8PolishAndAudioTest`** | **12** | Inicialização do SoundManager, limites de volume, mute, sintetizador de tons PCM, ondas senoidais, ruído de explosão e física de partículas | **100% APROVADO** |
| **`BossAndProgressionTest`** | **16** | Spawning nas fases 2/3/6, isolamento na intro, dano e armadura, clamping de vida, ciclo telegrafado, emissão única de vitória e persistência | **100% APROVADO** |
| **`EconomyAndProgressionTest`** | **16** | Recompensas de vitória, deduplicação de partidas, derrota sem moedas, evolução da base, custos, desbloqueio de heróis e loja | **100% APROVADO** |
| **`HeroSystemTest`** | **14** | 5 raridades, 6 heróis, 12 habilidades, nível/escalonamento, posicionamento, cooldowns e combate | **100% APROVADO** |
| **`GameEngineTest`** | **22** | Ciclo de vida da partida, ondas, waypoints, dano à base, derrota, vitória, construção de torres, alcance e dano em área | **100% APROVADO** |
| **`GameModelsTest`** | **6** | Modelos de dados de heróis, torres, inimigos e fases da campanha | **100% APROVADO** |
| **`NavigationContractTest`** | **4** | Registro das Activities no manifesto, integridade do roster e contratos | **100% APROVADO** |
| **`GameConstantsTest`** | **4** | Limites numéricos de volume, constantes de torres, grade e versão | **100% APROVADO** |
| **Total de Testes Unitários** | **164** | Cobertura integral das 8 etapas, Upgrades Visuais Partes 1-4 e Upgrade Gráfico Extremo Etapas 1/3 e 2/3 | **100% APROVADO** |

---

## 8. Como Compilar e Validar

### Compilação Local com Gradle
```bash
# Verificar ambiente
./gradlew --version

# Compilar código Kotlin
./gradlew :app:compileDebugKotlin

# Compilar e executar todos os 164 testes unitários
./gradlew :app:testDebugUnitTest

# Gerar o APK de Debug
./gradlew :app:assembleDebug

# Validação limpa completa
./gradlew clean testDebugUnitTest assembleDebug
```

### Onde Encontrar o APK Gerado
Após a execução do comando `assembleDebug`, o APK de debug assinado com a chave padrão de desenvolvimento do Android estará localizado em:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Execução no GitHub Actions
O workflow `.github/workflows/build-apk.yml` está configurado para:
1. Disparar em `push`, `pull_request` para branches `main` e `master`, e manualmente via `workflow_dispatch`.
2. Configurar o JDK 17 (Eclipse Temurin).
3. Configurar o Android SDK (plataforma android-34, build-tools 34.0.0, platform-tools).
4. Configurar o Gradle 8.7 e validar o Gradle Wrapper oficial.
5. Executar a suíte completa de testes unitários (`./gradlew testDebugUnitTest`).
6. Compilar o APK (`./gradlew assembleDebug`).
7. Validar a existência e tamanho não vazio do APK gerado.
8. Publicar o APK como artefato para download com o identificador `heroes-tower-defense-debug-apk`.

---

## 8. Integração Experimental com Godot 4 (Protótipo Isométrico 3D)

Para viabilizar uma evolução gráfica para cenários 3D/2.5D, iluminação dinâmica, sombras em tempo real e física de partículas, foi estruturado um módulo experimental em `godot/`:

- **Decisão Arquitetural (Opção B):** Projeto Godot 4.3 independente para benchmarking visual e testes de cena, sem impactar o pipeline de compilação do APK Android no GitHub Actions.
- **Cenário Demonstrado (`godot/scenes/battlefield_prototype.tscn`):**
  - Câmera em projeção isométrica 3D com rotação de 45° e inclinação de ~35.26°.
  - Terreno com profundidade real, estrada sinuosa de paralelepípedos e canal de água translúcida.
  - Iluminação direcional com sombras suaves (`DirectionalLight3D`) e tochas animadas com luz radial (`OmniLight3D`).
  - Torre de Arqueiros 3D (`ArcherTower`) com mira inteligente, detecção de alcance e recuo ao disparar.
  - Goblin Saqueador (`EnemyGoblin`) articulado com ciclo de caminhada ao longo de `Path3D`, animação de passos, oscilação de clava e reação a dano.
  - Projétil balístico em arco parabólico com compensação de gravidade e rastro de partículas (`ProjectileArrow`).
  - Colisão física via `Area3D`, estilhaços de impacto (`CPUParticles3D`) e tremor de câmera na batalha.
- Para detalhes e instruções de execução, consulte `godot/README.md`.

