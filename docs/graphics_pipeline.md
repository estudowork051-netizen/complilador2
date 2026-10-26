# Pipeline Gráfico 3D — Heroes Tower Defense

## 1. Visão Geral e Arquitetura

Este documento estabelece o pipeline de produção gráfica 3D do **Heroes Tower Defense**, especificando o fluxo de criação, exportação glTF 2.0, importação na Godot Engine 4 e integração mobile Android, tendo o herói **Kael, Guerreiro de Fogo** como referência artística e técnica (Etapa 1/8).

```
[Design / Especificação 3D]
          │
          ▼
[Gerador de Malha / Blender / Python glTF 2.0]
          │ (glTF 2.0 binário .glb)
          ▼
[godot/models/*.glb]
          │
          ▼
[Cenas Godot 4: Node3D + MeshInstance3D + AnimationPlayer + Shaders/PBR]
          │
          ▼
[Benchmark 3D & Exportação Android / SurfaceView Embed]
```

---

## 2. Ferramentas e Especificações Técnicas

| Componente | Ferramenta / Versão | Função |
| :--- | :--- | :--- |
| **Pipeline 3D** | Python 3.12 (`struct`, glTF 2.0 spec) / Blender | Geração procedural e modelagem de malhas e rigs |
| **Engine 3D** | Godot Engine 4.3+ (GLES3 / Mobile Vulkan) | Montagem de cenas, iluminação, shaders e animação |
| **Renderer WebGL** | Headless Chrome 136 + Three.js r128 | Validação headless e renderização de evidências visuais |
| **Plataforma Mobile** | Android SDK 34 / MinSDK 24 / Kotlin 1.9 | Runtime do jogo mobile |
| **Build System** | Gradle 8.7 + OpenJDK 17 | Compilação automatizada via GitHub Actions |

---

## 3. Especificação do Modelo 3D de Referência (Kael)

O modelo de Kael foi concebido com geometria estilizada premium seguindo a referência artística de fantasia mobile (proporções cartoon heroicas: cabeça expressiva, torso imponente, armadura ornamentada):

- **Arquivo:** `godot/models/hero_kael.glb` (206.6 KB binário)
- **Sub-malhas (Nodes):** 38 nós estruturados hierarquicamente.
- **Topologia:** ~5.400 triângulos / 2.800 vértices, curvas suaves em elipsoide, mechas de cabelo swept curve, ombreiras em domo com arcos de filigrana dourada e capa com dobras de tecido 3D.
- **Hierarquia de Nós:**
  - `TorsoSteel`, `TorsoCrimson`, `TorsoGold`, `TorsoBelt` (peitoral cinzelado, placas de esmalte carmesim, fivela com cabeça de leão e tabardo ornamentado)
  - `HeadSkin`, `HeadHair`, `HeadEyeWhite`, `HeadEyesDark` (crânio anatômico estilizado, olhos expressivos, sobrancelhas e 7 mechas de cabelo chamejantes)
  - `LeftPauldronCrimson`, `LeftPauldronGold`, `LeftPauldronRuby`, `RightPauldronCrimson`, `RightPauldronGold`, `RightPauldronRuby` (ombreiras em domo com arcos esculpidos e rubis de leão)
  - `LeftArmSleeve`, `LeftGauntletSteel`, `LeftGauntletGold`, `LeftHand`
  - `RightArmSleeve`, `RightGauntletSteel`, `RightGauntletGold`, `RightHand`
  - `LeftLegSteel`, `LeftKneeGold`, `LeftKneeGem`, `LeftBoot`, `RightLegSteel`, `RightKneeGold`, `RightKneeGem`, `RightBoot`
  - `Cape`, `CapeGold` (capa com ondulações senoidais e fechos duplos)
  - `SwordGold`, `SwordPommelGem`, `SwordGrip`, `SwordCrossguardRuby`, `SwordBlade`, `SwordFlame` (Espada Larga com sulco duplo de sangue, guarda alada dourada e chamas dinâmicas que contornam a lâmina)

---

## 4. Materiais PBR e Paleta de Cores

O pipeline adota 10 materiais PBR padronizados com canais `baseColorFactor`, `metallicFactor`, `roughnessFactor` e `emissiveFactor`:

1. **Dark Steel (`M_DarkSteel`):** `RGB(0.22, 0.25, 0.32)`, Metallic 0.88, Roughness 0.28.
2. **Gold Filigree (`M_GoldFiligree`):** `RGB(0.98, 0.80, 0.22)`, Metallic 0.92, Roughness 0.22.
3. **Crimson Enamel (`M_CrimsonPlate`):** `RGB(0.82, 0.10, 0.14)`, Metallic 0.40, Roughness 0.32.
4. **Reinforced Leather (`M_HeroLeather`):** `RGB(0.35, 0.20, 0.10)`, Metallic 0.05, Roughness 0.82.
5. **Hero Skin (`M_HeroSkin`):** `RGB(0.95, 0.77, 0.65)`, Metallic 0.0, Roughness 0.58.
6. **Flame Hair (`M_FlameHair`):** `RGB(0.92, 0.35, 0.06)`, Metallic 0.15, Roughness 0.42.
7. **Flame Aura (`M_FlameAura`):** `RGB(1.00, 0.82, 0.20)`, Emissive `RGB(1.0, 0.62, 0.10)`, Roughness 0.15.
8. **Dark Eyes / Brows (`M_EyeDark`):** `RGB(0.08, 0.09, 0.12)`, Roughness 0.20.
9. **Eye White (`M_EyeWhite`):** `RGB(0.96, 0.96, 0.98)`, Roughness 0.30.
10. **Ruby Gem (`M_RubyGem`):** `RGB(0.95, 0.08, 0.18)`, Emissive `RGB(0.85, 0.05, 0.15)`, Metallic 0.60, Roughness 0.18.

---

## 5. Configuração Canônica de Câmera e Iluminação (Mobile Vertical)

### Câmera Isométrica
Para telas verticais de smartphone (9:16, 9:19.5, 9:20):
- **Projeção:** `PROJECTION_ORTHOGONAL` (ou perspectiva de baixo FOV: 28°~35°).
- **Ângulos Canônicos:**
  - Pitch (Rotação X): `-35.264°` (arco tangente de `1 / sqrt(2)`)
  - Yaw (Rotação Y): `45.0°`
  - Roll (Rotação Z): `0.0°`
- **Posicionamento:** `Vector3(8.0, 9.5, 8.0)` apontando para o centro da arena `(0, 1.0, 0)`.

### Iluminação 3 Pontos (Cinemática Mobile)
- **Key Light (DirectionalLight3D):**
  - Cor: `RGB(1.0, 0.95, 0.88)`, Intensidade: 2.4 lux, Sombras habilitadas (`shadow_enabled = true`).
  - Ângulo: 45° azimuth, 55° elevação.
- **Fill Light (DirectionalLight3D / OmniLight3D):**
  - Cor azulada de contraste celeste: `RGB(0.40, 0.55, 0.80)`, Intensidade: 0.8 lux, sem sombra.
- **Rim Light (OmniLight3D / SpotLight3D de Fogo):**
  - Cor de chamas carmesim: `RGB(1.0, 0.35, 0.05)`, Intensidade dinâmica pulsante (1.5 ~ 2.8 lux).
  - Posicionada atrás do personagem `(0, 1.8, -1.8)` para realçar a silhueta da armadura e capa.

---

## 6. Sistema de Animação Articulada

As animações são controladas via `godot/scripts/hero_base_3d.gd` e `godot/scripts/kael_showcase_stage1.gd`:
- **Estados Suportados:**
  - `IDLE`: Respiração rítmica do torso (`Y += sin(t * 3.0) * 0.03`), oscilação pendular dos braços (`rot_z`), ondulação da capa (`rot_x += sin(t * 2.5) * 0.12`).
  - `WINDUP`: Rotação do torso em -35°, recuo da espada para trás da cabeça, flexão das pernas.
  - `ATTACK`: Golpe frontal rápido em arco cortante de 110°, projeção do braço direito e inclinação da lâmina.
  - `RECOVERY`: Retorno suave à postura de guarda.
  - `HIT` e `DEFEAT`: Estados de reação para o ciclo completo de combate.

---

## 7. Integração Android: Decisão Arquitetural

Conforme auditado na Fase 9:
- **Abordagem Avaliada (Opção A - Godot Embed direto na Activity Android):**
  - Exigiria compilar ou empacotar a biblioteca nativa `godot-lib.aar` (60MB+ de binários C++ libgodot_android.so para arm64-v8a e armeabi-v7a).
  - No ambiente de CI do projeto, adicionar dependências locais sem repositório Maven quebrado desestabilizaria a pipeline de compilação oficial `build-apk.yml`.
- **Abordagem Adotada (Opção B - Módulo 3D Desacoplado & Showcase Técnico):**
  - O projeto Android Kotlin mantém 100% de integridade com seus 164 testes unitários e compilação limpa via GitHub Actions.
  - O módulo Godot 4 vive em `godot/`, contendo a cena completa de demonstração técnica (`godot/scenes/kael_showcase_stage1.tscn`), scripts e assets validados pelo validador automatizado `godot/tools/validate_godot_project.py`.
  - Esta arquitetura preserva a estabilidade do produto e permite a migração gradual sem comprometer os entregáveis de CI.

---

## 8. Convenções de Nomenclatura e Próximos Passos (Etapas 2 a 8)

- **Modelos de Heróis:** `godot/models/hero_<nome>.glb` (ex: `hero_kael.glb`, `hero_lyra.glb`, `hero_vorg.glb`).
- **Inimigos:** `godot/models/enemy_<tipo>.glb` (ex: `enemy_goblin.glb`, `enemy_orc.glb`).
- **Torres:** `godot/models/tower_<classe>_lv<nível>.glb` (ex: `tower_archer_lv1.glb`).
- **Cenas de Showcase / Arena:** `godot/scenes/<nome>_showcase.tscn`.
- **Próxima Etapa (Etapa 2/8):** Reconstrução dos Cenários Premium 3D com malhas geométricas de pedra, água com shaders, vegetação e suporte aos 6 biomas do jogo.
