#!/usr/bin/env python3
import os

ASSETS_DIR = "/guardians_of_the_last_base/assets"

def ensure_dirs():
    for sub in ["icons", "sprites", "effects", "audio"]:
        os.makedirs(f"{ASSETS_DIR}/{sub}", exist_ok=True)

def write_svg(filepath, svg_content):
    with open(filepath, "w", encoding="utf-8") as f:
        f.write(svg_content.strip())
    print(f"Created SVG: {filepath}")

def generate_assets():
    ensure_dirs()

    # 1. Main Game Icon
    write_svg(f"{ASSETS_DIR}/icons/icon.svg", """
<svg width="512" height="512" viewBox="0 0 512 512" xmlns="http://www.w3.org/2000/svg">
  <defs>
    <linearGradient id="bgGrad" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#141a29"/>
      <stop offset="50%" stop-color="#0f1420"/>
      <stop offset="100%" stop-color="#080a10"/>
    </linearGradient>
    <linearGradient id="goldGrad" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#f6d365"/>
      <stop offset="100%" stop-color="#fda085"/>
    </linearGradient>
    <linearGradient id="magicGrad" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#00f2fe"/>
      <stop offset="100%" stop-color="#4facfe"/>
    </linearGradient>
  </defs>
  <rect width="512" height="512" rx="100" fill="url(#bgGrad)" stroke="#f6d365" stroke-width="8"/>
  <circle cx="256" cy="256" r="190" fill="none" stroke="#4facfe" stroke-width="4" stroke-dasharray="12,8" opacity="0.6"/>
  <!-- Fortress Shape -->
  <path d="M156 380 L156 220 L196 220 L196 250 L236 250 L236 180 L276 180 L276 250 L316 250 L316 220 L356 220 L356 380 Z" fill="#2d3748" stroke="#f6d365" stroke-width="8"/>
  <!-- Magic Gem / Crystal -->
  <polygon points="256,100 290,160 256,220 222,160" fill="url(#magicGrad)" stroke="#ffffff" stroke-width="4"/>
  <!-- Gate -->
  <path d="M226 380 L226 300 C226 280 286 280 286 300 L286 380 Z" fill="#0f1420" stroke="#f6d365" stroke-width="4"/>
  <!-- Stars/Runes -->
  <circle cx="256" cy="160" r="8" fill="#ffffff"/>
  <circle cx="160" cy="140" r="4" fill="#00f2fe"/>
  <circle cx="352" cy="140" r="4" fill="#00f2fe"/>
</svg>
""")

    # 2. Coin Icon
    write_svg(f"{ASSETS_DIR}/icons/coin.svg", """
<svg width="64" height="64" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
  <defs>
    <radialGradient id="coinGrad" cx="35%" cy="35%" r="65%">
      <stop offset="0%" stop-color="#fff176"/>
      <stop offset="60%" stop-color="#fbc02d"/>
      <stop offset="100%" stop-color="#f57f17"/>
    </radialGradient>
  </defs>
  <circle cx="32" cy="32" r="30" fill="url(#coinGrad)" stroke="#ffb300" stroke-width="3"/>
  <circle cx="32" cy="32" r="23" fill="none" stroke="#fff59d" stroke-width="2" stroke-dasharray="4,2"/>
  <text x="32" y="42" font-size="28" font-family="Arial, sans-serif" font-weight="bold" fill="#795548" text-anchor="middle">G</text>
</svg>
""")

    # 3. Base / Castle Icon
    write_svg(f"{ASSETS_DIR}/icons/base.svg", """
<svg width="64" height="64" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
  <path d="M12 56 L12 28 L20 28 L20 36 L28 36 L28 20 L36 20 L36 36 L44 36 L44 28 L52 28 L52 56 Z" fill="#4a5568" stroke="#cbd5e0" stroke-width="3"/>
  <path d="M26 56 L26 44 C26 40 38 40 38 44 L38 56 Z" fill="#1a202c" stroke="#cbd5e0" stroke-width="2"/>
  <polygon points="32,8 38,18 26,18" fill="#3182ce"/>
</svg>
""")

    # 4. Kael Avatar
    write_svg(f"{ASSETS_DIR}/icons/kael_avatar.svg", """
<svg width="128" height="128" viewBox="0 0 128 128" xmlns="http://www.w3.org/2000/svg">
  <circle cx="64" cy="64" r="60" fill="#1a2035" stroke="#00d2ff" stroke-width="4"/>
  <!-- Arcane Hood -->
  <path d="M30 96 C30 50 44 20 64 20 C84 20 98 50 98 96 Z" fill="#2b1055"/>
  <!-- Inner Face Shadow -->
  <ellipse cx="64" cy="64" rx="24" ry="28" fill="#110726"/>
  <!-- Glowing Arcane Eyes -->
  <ellipse cx="54" cy="60" rx="6" ry="3" fill="#00f2fe"/>
  <ellipse cx="74" cy="60" rx="6" ry="3" fill="#00f2fe"/>
  <!-- Arcane Gem on Forehead -->
  <polygon points="64,36 70,44 64,52 58,44" fill="#00f2fe" stroke="#ffffff" stroke-width="1.5"/>
</svg>
""")

    # 5. Lyra Avatar
    write_svg(f"{ASSETS_DIR}/icons/lyra_avatar.svg", """
<svg width="128" height="128" viewBox="0 0 128 128" xmlns="http://www.w3.org/2000/svg">
  <circle cx="64" cy="64" r="60" fill="#182822" stroke="#48bb78" stroke-width="4"/>
  <!-- Shadow Hood -->
  <path d="M32 98 C32 46 48 24 64 24 C80 24 96 46 96 98 Z" fill="#1c3d2f"/>
  <!-- Face Mask / Shadow -->
  <ellipse cx="64" cy="62" rx="22" ry="24" fill="#0d1f17"/>
  <!-- Piercing Amber/Green Eyes -->
  <ellipse cx="54" cy="58" rx="5" ry="3" fill="#a7f3d0"/>
  <ellipse cx="74" cy="58" rx="5" ry="3" fill="#a7f3d0"/>
  <path d="M52 80 L64 74 L76 80" stroke="#38a169" stroke-width="2" fill="none"/>
</svg>
""")

    # 6. Skill: Arcane Blast
    write_svg(f"{ASSETS_DIR}/icons/skill_arcane_blast.svg", """
<svg width="64" height="64" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
  <rect width="64" height="64" rx="14" fill="#2d1354" stroke="#805ad5" stroke-width="3"/>
  <circle cx="32" cy="32" r="16" fill="#b794f4"/>
  <circle cx="32" cy="32" r="8" fill="#ffffff"/>
  <line x1="32" y1="6" x2="32" y2="16" stroke="#d6bcfa" stroke-width="3"/>
  <line x1="32" y1="48" x2="32" y2="58" stroke="#d6bcfa" stroke-width="3"/>
  <line x1="6" y1="32" x2="16" y2="32" stroke="#d6bcfa" stroke-width="3"/>
  <line x1="48" y1="32" x2="58" y2="32" stroke="#d6bcfa" stroke-width="3"/>
</svg>
""")

    # 7. Skill: Slow Field
    write_svg(f"{ASSETS_DIR}/icons/skill_slow_field.svg", """
<svg width="64" height="64" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
  <rect width="64" height="64" rx="14" fill="#0d2847" stroke="#3182ce" stroke-width="3"/>
  <circle cx="32" cy="32" r="20" fill="none" stroke="#63b3ed" stroke-width="3" stroke-dasharray="6,4"/>
  <circle cx="32" cy="32" r="12" fill="none" stroke="#90cdf4" stroke-width="2"/>
  <!-- Hourglass symbol in center -->
  <polygon points="24,20 40,20 24,44 40,44" fill="none" stroke="#e2e8f0" stroke-width="2.5"/>
</svg>
""")

    # 8. Skill: Arrow Rain
    write_svg(f"{ASSETS_DIR}/icons/skill_arrow_rain.svg", """
<svg width="64" height="64" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
  <rect width="64" height="64" rx="14" fill="#133626" stroke="#38a169" stroke-width="3"/>
  <!-- 3 falling arrows -->
  <g stroke="#68d391" stroke-width="3" stroke-linecap="round">
    <line x1="20" y1="14" x2="26" y2="46"/>
    <polyline points="21,41 26,46 29,40" fill="none"/>
    <line x1="32" y1="10" x2="35" y2="52"/>
    <polyline points="30,47 35,52 38,46" fill="none"/>
    <line x1="44" y1="16" x2="42" y2="44"/>
    <polyline points="38,39 42,44 46,39" fill="none"/>
  </g>
</svg>
""")

    # 9. Skill: Piercing Shot
    write_svg(f"{ASSETS_DIR}/icons/skill_piercing_shot.svg", """
<svg width="64" height="64" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
  <rect width="64" height="64" rx="14" fill="#2d3748" stroke="#e2e8f0" stroke-width="3"/>
  <line x1="12" y1="32" x2="52" y2="32" stroke="#48bb78" stroke-width="4"/>
  <polygon points="52,32 40,24 44,32 40,40" fill="#48bb78"/>
  <line x1="28" y1="18" x2="28" y2="46" stroke="#fc8181" stroke-width="3" stroke-dasharray="4,4"/>
  <line x1="38" y1="18" x2="38" y2="46" stroke="#fc8181" stroke-width="3" stroke-dasharray="4,4"/>
</svg>
""")

    # 10. Generic icons
    write_svg(f"{ASSETS_DIR}/icons/settings.svg", """
<svg width="64" height="64" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
  <circle cx="32" cy="32" r="12" fill="none" stroke="#e2e8f0" stroke-width="6"/>
  <path d="M32 8 L32 16 M32 48 L32 56 M8 32 L16 32 M48 32 L56 32 M15 15 L21 21 M43 43 L49 49 M15 49 L21 43 M43 21 L49 15" stroke="#e2e8f0" stroke-width="5" stroke-linecap="round"/>
</svg>
""")

    write_svg(f"{ASSETS_DIR}/icons/play.svg", """
<svg width="64" height="64" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
  <circle cx="32" cy="32" r="28" fill="#2b6cb0" stroke="#63b3ed" stroke-width="3"/>
  <polygon points="26,18 46,32 26,46" fill="#ffffff"/>
</svg>
""")

    write_svg(f"{ASSETS_DIR}/icons/upgrade.svg", """
<svg width="64" height="64" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
  <path d="M32 12 L48 28 L38 28 L38 52 L26 52 L26 28 L16 28 Z" fill="#ecc94b" stroke="#d69e2e" stroke-width="2"/>
</svg>
""")

    write_svg(f"{ASSETS_DIR}/icons/locked.svg", """
<svg width="64" height="64" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
  <rect x="18" y="28" width="28" height="26" rx="4" fill="#718096" stroke="#cbd5e0" stroke-width="2"/>
  <path d="M24 28 L24 20 C24 14 40 14 40 20 L40 28" fill="none" stroke="#cbd5e0" stroke-width="4"/>
  <circle cx="32" cy="39" r="3" fill="#2d3748"/>
</svg>
""")

    # Character Sprites (Visual in-game actors)
    write_svg(f"{ASSETS_DIR}/sprites/kael_sprite.svg", """
<svg width="64" height="80" viewBox="0 0 64 80" xmlns="http://www.w3.org/2000/svg">
  <!-- Arcane Robes Body -->
  <path d="M20 76 L22 36 L42 36 L44 76 Z" fill="#312e81" stroke="#6366f1" stroke-width="2"/>
  <!-- Hood/Head -->
  <ellipse cx="32" cy="24" rx="12" ry="14" fill="#4338ca"/>
  <circle cx="32" cy="24" r="8" fill="#1e1b4b"/>
  <!-- Glowing Eye -->
  <circle cx="35" cy="23" r="2.5" fill="#38bdf8"/>
  <!-- Arcane Staff -->
  <line x1="46" y1="12" x2="48" y2="78" stroke="#78350f" stroke-width="3"/>
  <circle cx="46" cy="12" r="7" fill="#06b6d4" stroke="#e0f2fe" stroke-width="2"/>
  <!-- Left Hand / Casting Hand -->
  <circle cx="20" cy="42" r="4" fill="#a5b4fc"/>
</svg>
""")

    write_svg(f"{ASSETS_DIR}/sprites/lyra_sprite.svg", """
<svg width="64" height="80" viewBox="0 0 64 80" xmlns="http://www.w3.org/2000/svg">
  <!-- Cloak / Body -->
  <path d="M22 76 L24 34 L40 34 L42 76 Z" fill="#14532d" stroke="#22c55e" stroke-width="2"/>
  <!-- Hood/Head -->
  <ellipse cx="32" cy="22" rx="11" ry="13" fill="#166534"/>
  <circle cx="32" cy="22" r="7" fill="#052e16"/>
  <!-- Eye -->
  <circle cx="35" cy="21" r="2" fill="#86efac"/>
  <!-- Bow -->
  <path d="M44 14 Q52 40 44 66" fill="none" stroke="#78350f" stroke-width="3"/>
  <line x1="44" y1="14" x2="44" y2="66" stroke="#cbd5e0" stroke-width="1"/>
</svg>
""")

    # Enemy Sprites
    write_svg(f"{ASSETS_DIR}/sprites/enemy_goblin.svg", """
<svg width="56" height="56" viewBox="0 0 56 56" xmlns="http://www.w3.org/2000/svg">
  <!-- Goblin Body -->
  <ellipse cx="28" cy="38" rx="14" ry="12" fill="#4d7c0f"/>
  <!-- Goblin Head -->
  <circle cx="28" cy="22" r="12" fill="#65a30d" stroke="#365314" stroke-width="2"/>
  <!-- Ears -->
  <polygon points="16,20 6,14 14,26" fill="#65a30d"/>
  <polygon points="40,20 50,14 42,26" fill="#65a30d"/>
  <!-- Eyes -->
  <circle cx="23" cy="20" r="3" fill="#facc15"/>
  <circle cx="33" cy="20" r="3" fill="#facc15"/>
  <!-- Dagger -->
  <line x1="40" y1="36" x2="50" y2="30" stroke="#94a3b8" stroke-width="3"/>
</svg>
""")

    write_svg(f"{ASSETS_DIR}/sprites/enemy_orc.svg", """
<svg width="68" height="68" viewBox="0 0 68 68" xmlns="http://www.w3.org/2000/svg">
  <!-- Armored Body -->
  <rect x="18" y="30" width="32" height="30" rx="6" fill="#475569" stroke="#94a3b8" stroke-width="2"/>
  <!-- Orc Head -->
  <circle cx="34" cy="22" r="14" fill="#3f6212" stroke="#1a2e05" stroke-width="2"/>
  <!-- Tusks -->
  <polygon points="26,26 28,32 30,28" fill="#f8fafc"/>
  <polygon points="38,28 40,32 42,26" fill="#f8fafc"/>
  <!-- Eyes -->
  <circle cx="28" cy="20" r="2.5" fill="#ef4444"/>
  <circle cx="40" cy="20" r="2.5" fill="#ef4444"/>
  <!-- Shield -->
  <rect x="8" y="32" width="12" height="24" rx="3" fill="#78350f" stroke="#b45309" stroke-width="2"/>
</svg>
""")

    write_svg(f"{ASSETS_DIR}/sprites/enemy_sorcerer.svg", """
<svg width="60" height="68" viewBox="0 0 60 68" xmlns="http://www.w3.org/2000/svg">
  <!-- Robe -->
  <path d="M16 64 L22 28 L38 28 L44 64 Z" fill="#4a044e" stroke="#c026d3" stroke-width="2"/>
  <!-- Dark Hood -->
  <ellipse cx="30" cy="20" rx="12" ry="14" fill="#701a75"/>
  <circle cx="30" cy="20" r="7" fill="#2e1065"/>
  <!-- Glowing Violet Eyes -->
  <circle cx="27" cy="19" r="2" fill="#f0abfc"/>
  <circle cx="33" cy="19" r="2" fill="#f0abfc"/>
  <!-- Dark Wand -->
  <line x1="44" y1="16" x2="48" y2="52" stroke="#3b0764" stroke-width="2.5"/>
  <circle cx="44" cy="16" r="5" fill="#d946ef"/>
</svg>
""")

    # Base Fortress Sprite
    write_svg(f"{ASSETS_DIR}/sprites/base_fortress.svg", """
<svg width="140" height="180" viewBox="0 0 140 180" xmlns="http://www.w3.org/2000/svg">
  <defs>
    <linearGradient id="wallGrad" x1="0%" y1="0%" x2="100%" y2="0%">
      <stop offset="0%" stop-color="#334155"/>
      <stop offset="50%" stop-color="#475569"/>
      <stop offset="100%" stop-color="#1e293b"/>
    </linearGradient>
  </defs>
  <!-- Main Keep -->
  <rect x="30" y="50" width="80" height="120" fill="url(#wallGrad)" stroke="#64748b" stroke-width="4"/>
  <!-- Battlements -->
  <rect x="25" y="35" width="20" height="25" fill="#334155" stroke="#94a3b8" stroke-width="2"/>
  <rect x="60" y="35" width="20" height="25" fill="#334155" stroke="#94a3b8" stroke-width="2"/>
  <rect x="95" y="35" width="20" height="25" fill="#334155" stroke="#94a3b8" stroke-width="2"/>
  <!-- Central Crystal Core -->
  <polygon points="70,10 82,32 70,50 58,32" fill="#38bdf8" stroke="#ffffff" stroke-width="3"/>
  <!-- Gate -->
  <path d="M50 170 L50 120 C50 105 90 105 90 120 L90 170 Z" fill="#0f172a" stroke="#f59e0b" stroke-width="3"/>
  <!-- Banners -->
  <polygon points="40,75 52,75 46,110" fill="#2563eb"/>
  <polygon points="88,75 100,75 94,110" fill="#2563eb"/>
</svg>
""")

    # Effects & Projectiles
    write_svg(f"{ASSETS_DIR}/effects/arcane_projectile.svg", """
<svg width="32" height="32" viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg">
  <circle cx="16" cy="16" r="10" fill="#38bdf8" opacity="0.85"/>
  <circle cx="16" cy="16" r="5" fill="#ffffff"/>
</svg>
""")

    write_svg(f"{ASSETS_DIR}/effects/shadow_arrow.svg", """
<svg width="36" height="12" viewBox="0 0 36 12" xmlns="http://www.w3.org/2000/svg">
  <line x1="2" y1="6" x2="32" y2="6" stroke="#4ade80" stroke-width="2.5"/>
  <polygon points="34,6 26,2 28,6 26,10" fill="#22c55e"/>
  <polygon points="2,2 6,6 2,10" fill="#15803d"/>
</svg>
""")

    write_svg(f"{ASSETS_DIR}/effects/arcane_blast_fx.svg", """
<svg width="96" height="96" viewBox="0 0 96 96" xmlns="http://www.w3.org/2000/svg">
  <circle cx="48" cy="48" r="42" fill="none" stroke="#a855f7" stroke-width="4" opacity="0.75"/>
  <circle cx="48" cy="48" r="28" fill="#c084fc" opacity="0.5"/>
  <circle cx="48" cy="48" r="14" fill="#ffffff" opacity="0.9"/>
</svg>
""")

    write_svg(f"{ASSETS_DIR}/effects/slow_field_fx.svg", """
<svg width="128" height="128" viewBox="0 0 128 128" xmlns="http://www.w3.org/2000/svg">
  <circle cx="64" cy="64" r="58" fill="#0284c7" fill-opacity="0.25" stroke="#38bdf8" stroke-width="3" stroke-dasharray="8,6"/>
  <circle cx="64" cy="64" r="40" fill="none" stroke="#7dd3fc" stroke-width="2" stroke-dasharray="4,4"/>
  <polygon points="64,28 72,50 94,64 72,78 64,100 56,78 34,64 56,50" fill="none" stroke="#bae6fd" stroke-width="1.5" opacity="0.6"/>
</svg>
""")

    write_svg(f"{ASSETS_DIR}/effects/piercing_beam.svg", """
<svg width="120" height="24" viewBox="0 0 120 24" xmlns="http://www.w3.org/2000/svg">
  <line x1="4" y1="12" x2="116" y2="12" stroke="#4ade80" stroke-width="6" stroke-linecap="round"/>
  <line x1="10" y1="12" x2="114" y2="12" stroke="#ffffff" stroke-width="2" stroke-linecap="round"/>
</svg>
""")

if __name__ == "__main__":
    generate_assets()
    print("All vector assets successfully generated!")
