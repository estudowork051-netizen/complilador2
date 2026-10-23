#!/usr/bin/env python3
import os

ASSETS_DIR = "/guardians_of_the_last_base/assets"

def write_svg(filepath, svg_content):
    with open(filepath, "w", encoding="utf-8") as f:
        f.write(svg_content.strip())
    print(f"Created SVG: {filepath}")

def generate_expansion_assets():
    # 1. Additional Character Sprites & Avatars
    # Thorne - Guardian Warrior (Heavy armor, war hammer, shield)
    write_svg(f"{ASSETS_DIR}/sprites/thorne_sprite.svg", """
<svg width="68" height="84" viewBox="0 0 68 84" xmlns="http://www.w3.org/2000/svg">
  <!-- Heavy Plate Body -->
  <rect x="20" y="32" width="28" height="46" rx="4" fill="#475569" stroke="#94a3b8" stroke-width="2.5"/>
  <!-- Shoulder Guards (Pauldrons) -->
  <polygon points="12,32 22,26 22,44 12,40" fill="#334155" stroke="#cbd5e1" stroke-width="2"/>
  <polygon points="56,32 46,26 46,44 56,40" fill="#334155" stroke="#cbd5e1" stroke-width="2"/>
  <!-- Helmet & Horns/Crest -->
  <polygon points="26,18 34,8 42,18 40,30 28,30" fill="#1e293b" stroke="#e2e8f0" stroke-width="2"/>
  <!-- Glowing Amber Visor Slot -->
  <rect x="29" y="20" width="10" height="3" fill="#f59e0b"/>
  <!-- War Hammer in Hand -->
  <line x1="52" y1="18" x2="52" y2="76" stroke="#78350f" stroke-width="3"/>
  <rect x="44" y="14" width="16" height="12" rx="2" fill="#64748b" stroke="#cbd5e1" stroke-width="2"/>
  <polygon points="52,10 56,14 48,14" fill="#f59e0b"/>
</svg>
""")

    write_svg(f"{ASSETS_DIR}/icons/thorne_avatar.svg", """
<svg width="128" height="128" viewBox="0 0 128 128" xmlns="http://www.w3.org/2000/svg">
  <circle cx="64" cy="64" r="60" fill="#1e293b" stroke="#f59e0b" stroke-width="4"/>
  <!-- Great Helm -->
  <path d="M40 36 L64 20 L88 36 L88 88 L64 96 L40 88 Z" fill="#334155" stroke="#94a3b8" stroke-width="3"/>
  <!-- Visor T-Slit -->
  <path d="M48 54 L80 54 M64 54 L64 80" stroke="#f59e0b" stroke-width="5" stroke-linecap="round"/>
</svg>
""")

    # Elysia - Elemental Mage (Crimson robe, flame orb, fire halo)
    write_svg(f"{ASSETS_DIR}/sprites/elysia_sprite.svg", """
<svg width="64" height="80" viewBox="0 0 64 80" xmlns="http://www.w3.org/2000/svg">
  <!-- Crimson Robe -->
  <path d="M20 76 L24 34 L40 34 L44 76 Z" fill="#991b1b" stroke="#ef4444" stroke-width="2"/>
  <!-- Mage Hood / Hair -->
  <ellipse cx="32" cy="22" rx="12" ry="14" fill="#b91c1c"/>
  <circle cx="32" cy="22" r="8" fill="#450a0a"/>
  <!-- Flame Eyes -->
  <circle cx="35" cy="21" r="2.5" fill="#fef08a"/>
  <!-- Floating Flame Orb -->
  <circle cx="48" cy="30" r="8" fill="#f97316"/>
  <circle cx="48" cy="30" r="4" fill="#fef08a"/>
</svg>
""")

    write_svg(f"{ASSETS_DIR}/icons/elysia_avatar.svg", """
<svg width="128" height="128" viewBox="0 0 128 128" xmlns="http://www.w3.org/2000/svg">
  <circle cx="64" cy="64" r="60" fill="#2d0a0a" stroke="#ef4444" stroke-width="4"/>
  <!-- Crimson Hood -->
  <path d="M32 96 C32 46 46 22 64 22 C82 22 96 46 96 96 Z" fill="#7f1d1d"/>
  <ellipse cx="64" cy="62" rx="22" ry="26" fill="#1c0505"/>
  <ellipse cx="54" cy="58" rx="5" ry="3" fill="#f97316"/>
  <ellipse cx="74" cy="58" rx="5" ry="3" fill="#f97316"/>
  <circle cx="64" cy="40" r="4" fill="#fbbf24"/>
</svg>
""")

    # Boran - Trap Engineer (Goggles, wrench, mechanical backpack)
    write_svg(f"{ASSETS_DIR}/sprites/boran_sprite.svg", """
<svg width="64" height="80" viewBox="0 0 64 80" xmlns="http://www.w3.org/2000/svg">
  <!-- Mechanical Leather Coat -->
  <path d="M22 76 L24 36 L40 36 L42 76 Z" fill="#78350f" stroke="#d97706" stroke-width="2"/>
  <!-- Head with Goggles -->
  <circle cx="32" cy="22" r="11" fill="#b45309"/>
  <circle cx="28" cy="21" r="5" fill="#1e293b" stroke="#e2e8f0" stroke-width="1.5"/>
  <circle cx="36" cy="21" r="5" fill="#1e293b" stroke="#e2e8f0" stroke-width="1.5"/>
  <circle cx="28" cy="21" r="2" fill="#38bdf8"/>
  <circle cx="36" cy="21" r="2" fill="#38bdf8"/>
  <!-- Wrench / Tool in Hand -->
  <line x1="46" y1="26" x2="48" y2="60" stroke="#94a3b8" stroke-width="3.5"/>
  <circle cx="46" cy="24" r="5" fill="none" stroke="#94a3b8" stroke-width="3"/>
</svg>
""")

    write_svg(f"{ASSETS_DIR}/icons/boran_avatar.svg", """
<svg width="128" height="128" viewBox="0 0 128 128" xmlns="http://www.w3.org/2000/svg">
  <circle cx="64" cy="64" r="60" fill="#2d1c07" stroke="#f59e0b" stroke-width="4"/>
  <circle cx="64" cy="60" r="32" fill="#78350f"/>
  <!-- Brass Goggles -->
  <circle cx="50" cy="56" r="14" fill="#0f172a" stroke="#d97706" stroke-width="4"/>
  <circle cx="78" cy="56" r="14" fill="#0f172a" stroke="#d97706" stroke-width="4"/>
  <line x1="60" y1="56" x2="68" y2="56" stroke="#d97706" stroke-width="4"/>
  <circle cx="50" cy="56" r="5" fill="#38bdf8"/>
  <circle cx="78" cy="56" r="5" fill="#38bdf8"/>
</svg>
""")

    # Vesper - Long-Range Assassin (Dark coat, sniper muzzle, glowing monocle)
    write_svg(f"{ASSETS_DIR}/sprites/vesper_sprite.svg", """
<svg width="64" height="80" viewBox="0 0 64 80" xmlns="http://www.w3.org/2000/svg">
  <!-- Dark Assassin Cloak -->
  <path d="M22 76 L26 34 L38 34 L42 76 Z" fill="#18181b" stroke="#71717a" stroke-width="2"/>
  <!-- Hood -->
  <ellipse cx="32" cy="22" rx="11" ry="13" fill="#27272a"/>
  <circle cx="32" cy="22" r="7" fill="#09090b"/>
  <!-- Sniper Monocle Target Scope -->
  <circle cx="35" cy="21" r="3" fill="#ef4444" stroke="#fecaca" stroke-width="1"/>
  <!-- Long Sniper Barrel -->
  <line x1="42" y1="36" x2="62" y2="24" stroke="#52525b" stroke-width="2.5"/>
</svg>
""")

    write_svg(f"{ASSETS_DIR}/icons/vesper_avatar.svg", """
<svg width="128" height="128" viewBox="0 0 128 128" xmlns="http://www.w3.org/2000/svg">
  <circle cx="64" cy="64" r="60" fill="#09090b" stroke="#a1a1aa" stroke-width="4"/>
  <path d="M34 98 C34 48 48 24 64 24 C80 24 94 48 94 98 Z" fill="#18181b"/>
  <ellipse cx="64" cy="62" rx="20" ry="24" fill="#050505"/>
  <!-- Crosshair Eye -->
  <circle cx="58" cy="58" r="8" fill="none" stroke="#ef4444" stroke-width="2"/>
  <line x1="58" y1="48" x2="58" y2="68" stroke="#ef4444" stroke-width="1.5"/>
  <line x1="48" y1="58" x2="68" y2="58" stroke="#ef4444" stroke-width="1.5"/>
</svg>
""")

    # 2. Advanced Enemies & Boss Sprites
    # Brute Blindado (Armored Brute - massive HP, heavy plate)
    write_svg(f"{ASSETS_DIR}/sprites/enemy_brute.svg", """
<svg width="76" height="76" viewBox="0 0 76 76" xmlns="http://www.w3.org/2000/svg">
  <!-- Massive Plate Armor Body -->
  <rect x="18" y="24" width="40" height="42" rx="8" fill="#1f2937" stroke="#9ca3af" stroke-width="3"/>
  <!-- Heavy Head / Horned Plate -->
  <polygon points="30,22 38,10 46,22 44,32 32,32" fill="#374151" stroke="#f3f4f6" stroke-width="2"/>
  <rect x="33" y="23" width="10" height="3" fill="#ef4444"/>
  <!-- Spiked Shoulder Guards -->
  <polygon points="10,26 18,20 18,36" fill="#4b5563" stroke="#e5e7eb" stroke-width="2"/>
  <polygon points="66,26 58,20 58,36" fill="#4b5563" stroke="#e5e7eb" stroke-width="2"/>
  <!-- Heavy Tower Shield -->
  <rect x="12" y="34" width="14" height="32" rx="3" fill="#111827" stroke="#dc2626" stroke-width="2"/>
</svg>
""")

    # Criatura Voadora (Gargoyle / Shadow Wing)
    write_svg(f"{ASSETS_DIR}/sprites/enemy_gargoyle.svg", """
<svg width="72" height="64" viewBox="0 0 72 64" xmlns="http://www.w3.org/2000/svg">
  <!-- Bat Wings -->
  <path d="M36 28 Q18 6 6 22 Q16 36 36 36" fill="#4c1d95" stroke="#a78bfa" stroke-width="2"/>
  <path d="M36 28 Q54 6 66 22 Q56 36 36 36" fill="#4c1d95" stroke="#a78bfa" stroke-width="2"/>
  <!-- Body -->
  <ellipse cx="36" cy="34" rx="10" ry="12" fill="#2e1065"/>
  <circle cx="36" cy="22" r="8" fill="#581c87" stroke="#c084fc" stroke-width="1.5"/>
  <!-- Glowing Violet Eyes -->
  <circle cx="33" cy="21" r="2" fill="#38bdf8"/>
  <circle cx="39" cy="21" r="2" fill="#38bdf8"/>
  <!-- Ground Shadow projection -->
  <ellipse cx="36" cy="58" rx="14" ry="4" fill="#000000" opacity="0.4"/>
</svg>
""")

    # Chefe - Ogro das Ruínas (Ruin Ogre Boss - gigantic, brutal club, runic markings)
    write_svg(f"{ASSETS_DIR}/sprites/enemy_boss_ogre.svg", """
<svg width="110" height="110" viewBox="0 0 110 110" xmlns="http://www.w3.org/2000/svg">
  <!-- Colossal Muscular Body -->
  <ellipse cx="55" cy="62" rx="36" ry="34" fill="#365314" stroke="#14532d" stroke-width="4"/>
  <!-- Runic Scars Glowing Orange/Red -->
  <path d="M36 50 L48 60 L40 74" fill="none" stroke="#f97316" stroke-width="3" stroke-dasharray="4,3"/>
  <path d="M72 52 L62 62 L68 76" fill="none" stroke="#f97316" stroke-width="3" stroke-dasharray="4,3"/>
  <!-- Boss Head with Tusks -->
  <circle cx="55" cy="32" r="20" fill="#4d7c0f" stroke="#1a2e05" stroke-width="3"/>
  <!-- Red Eyes of Enrage -->
  <circle cx="47" cy="28" r="4" fill="#ef4444"/>
  <circle cx="63" cy="28" r="4" fill="#ef4444"/>
  <!-- Tusks -->
  <polygon points="44,36 46,46 50,38" fill="#f8fafc"/>
  <polygon points="66,36 64,46 60,38" fill="#f8fafc"/>
  <!-- Giant War Club -->
  <line x1="84" y1="16" x2="94" y2="92" stroke="#78350f" stroke-width="8" stroke-linecap="round"/>
  <rect x="80" y="14" width="20" height="30" rx="4" fill="#475569" stroke="#94a3b8" stroke-width="3"/>
  <!-- Spikes on Club -->
  <polygon points="76,20 80,24 80,16" fill="#e2e8f0"/>
  <polygon points="104,26 100,22 100,30" fill="#e2e8f0"/>
</svg>
""")

    # 3. New Skill Icons
    # Thorne Skills
    write_svg(f"{ASSETS_DIR}/icons/skill_ground_slam.svg", """
<svg width="64" height="64" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
  <rect width="64" height="64" rx="14" fill="#451a03" stroke="#f59e0b" stroke-width="3"/>
  <!-- Cracking Earth -->
  <path d="M32 12 L32 38 M22 48 L32 38 L42 48 M12 56 L22 48 M52 56 L42 48" stroke="#fef08a" stroke-width="4" stroke-linecap="round"/>
  <polygon points="32,8 24,20 40,20" fill="#f59e0b"/>
</svg>
""")

    write_svg(f"{ASSETS_DIR}/icons/skill_bastion_shield.svg", """
<svg width="64" height="64" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
  <rect width="64" height="64" rx="14" fill="#0f172a" stroke="#38bdf8" stroke-width="3"/>
  <!-- Shield Emblem -->
  <path d="M32 14 L48 22 L44 44 L32 54 L20 44 L16 22 Z" fill="#1e293b" stroke="#38bdf8" stroke-width="3"/>
  <path d="M32 20 L40 25 L38 38 L32 44 L26 38 L24 25 Z" fill="#0284c7"/>
</svg>
""")

    # Elysia Skills
    write_svg(f"{ASSETS_DIR}/icons/skill_meteor.svg", """
<svg width="64" height="64" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
  <rect width="64" height="64" rx="14" fill="#450a0a" stroke="#ef4444" stroke-width="3"/>
  <!-- Falling Meteor -->
  <path d="M14 14 L36 36" stroke="#f97316" stroke-width="8" stroke-linecap="round"/>
  <circle cx="42" cy="42" r="14" fill="#ef4444" stroke="#fef08a" stroke-width="3"/>
  <circle cx="42" cy="42" r="6" fill="#fef08a"/>
</svg>
""")

    write_svg(f"{ASSETS_DIR}/icons/skill_flame_wall.svg", """
<svg width="64" height="64" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
  <rect width="64" height="64" rx="14" fill="#431407" stroke="#ea580c" stroke-width="3"/>
  <!-- Flame Wall -->
  <path d="M12 50 Q18 20 24 50 Q30 16 36 50 Q42 12 48 50 Q54 22 58 50" fill="#f97316" stroke="#fef08a" stroke-width="2"/>
</svg>
""")

    # Boran Skills
    write_svg(f"{ASSETS_DIR}/icons/skill_minefield.svg", """
<svg width="64" height="64" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
  <rect width="64" height="64" rx="14" fill="#1c1917" stroke="#eab308" stroke-width="3"/>
  <!-- Landmine -->
  <ellipse cx="32" cy="36" rx="20" ry="12" fill="#44403c" stroke="#ca8a04" stroke-width="3"/>
  <circle cx="32" cy="32" r="6" fill="#ef4444"/>
  <line x1="32" y1="20" x2="32" y2="26" stroke="#fef08a" stroke-width="3"/>
</svg>
""")

    write_svg(f"{ASSETS_DIR}/icons/skill_turret.svg", """
<svg width="64" height="64" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
  <rect width="64" height="64" rx="14" fill="#0c4a6e" stroke="#0284c7" stroke-width="3"/>
  <!-- Turret Base and Barrel -->
  <polygon points="18,52 46,52 38,38 26,38" fill="#334155" stroke="#94a3b8" stroke-width="2"/>
  <circle cx="32" cy="34" r="10" fill="#0284c7" stroke="#e0f2fe" stroke-width="2"/>
  <line x1="32" y1="34" x2="54" y2="22" stroke="#e2e8f0" stroke-width="5" stroke-linecap="round"/>
</svg>
""")

    # Vesper Skills
    write_svg(f"{ASSETS_DIR}/icons/skill_snipe.svg", """
<svg width="64" height="64" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
  <rect width="64" height="64" rx="14" fill="#09090b" stroke="#dc2626" stroke-width="3"/>
  <!-- Sniper Crosshair -->
  <circle cx="32" cy="32" r="20" fill="none" stroke="#ef4444" stroke-width="3"/>
  <line x1="32" y1="8" x2="32" y2="56" stroke="#ef4444" stroke-width="2"/>
  <line x1="8" y1="32" x2="56" y2="32" stroke="#ef4444" stroke-width="2"/>
  <circle cx="32" cy="32" r="4" fill="#dc2626"/>
</svg>
""")

    write_svg(f"{ASSETS_DIR}/icons/skill_smoke_veil.svg", """
<svg width="64" height="64" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
  <rect width="64" height="64" rx="14" fill="#18181b" stroke="#71717a" stroke-width="3"/>
  <!-- Smoke Cloud Vortex -->
  <circle cx="28" cy="34" r="14" fill="#3f3f46" opacity="0.8"/>
  <circle cx="40" cy="30" r="12" fill="#52525b" opacity="0.8"/>
  <circle cx="32" cy="24" r="10" fill="#71717a" opacity="0.8"/>
</svg>
""")

    # 4. Expansion Projectile & Effect Sprites
    write_svg(f"{ASSETS_DIR}/effects/fireball.svg", """
<svg width="36" height="36" viewBox="0 0 36 36" xmlns="http://www.w3.org/2000/svg">
  <circle cx="18" cy="18" r="14" fill="#ea580c"/>
  <circle cx="18" cy="18" r="9" fill="#f97316"/>
  <circle cx="18" cy="18" r="4" fill="#fef08a"/>
</svg>
""")

    write_svg(f"{ASSETS_DIR}/effects/flame_wall_fx.svg", """
<svg width="128" height="64" viewBox="0 0 128 64" xmlns="http://www.w3.org/2000/svg">
  <ellipse cx="64" cy="48" rx="58" ry="12" fill="#7c2d12" opacity="0.6"/>
  <path d="M12 48 Q24 10 36 48 Q48 16 60 48 Q72 8 84 48 Q96 14 108 48 Q118 20 124 48" fill="#f97316" stroke="#fef08a" stroke-width="3"/>
</svg>
""")

    write_svg(f"{ASSETS_DIR}/effects/turret_sprite.svg", """
<svg width="48" height="48" viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">
  <circle cx="24" cy="28" r="16" fill="#334155" stroke="#0284c7" stroke-width="3"/>
  <circle cx="24" cy="28" r="8" fill="#0284c7"/>
  <line x1="24" y1="28" x2="42" y2="16" stroke="#cbd5e1" stroke-width="4" stroke-linecap="round"/>
</svg>
""")

    write_svg(f"{ASSETS_DIR}/effects/sniper_bullet.svg", """
<svg width="40" height="8" viewBox="0 0 40 8" xmlns="http://www.w3.org/2000/svg">
  <line x1="2" y1="4" x2="38" y2="4" stroke="#ef4444" stroke-width="3" stroke-linecap="round"/>
  <line x1="20" y1="4" x2="38" y2="4" stroke="#ffffff" stroke-width="1.5" stroke-linecap="round"/>
</svg>
""")

    write_svg(f"{ASSETS_DIR}/effects/shockwave_ground.svg", """
<svg width="128" height="128" viewBox="0 0 128 128" xmlns="http://www.w3.org/2000/svg">
  <circle cx="64" cy="64" r="56" fill="none" stroke="#f59e0b" stroke-width="4" stroke-dasharray="10,6"/>
  <circle cx="64" cy="64" r="38" fill="none" stroke="#fef08a" stroke-width="3"/>
  <circle cx="64" cy="64" r="18" fill="#d97706" opacity="0.4"/>
</svg>
""")

if __name__ == "__main__":
    generate_expansion_assets()
    print("Expansion assets successfully created!")
