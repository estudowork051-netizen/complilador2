#!/usr/bin/env python3
import os

BASE_DIR = "/guardians_of_the_last_base"

def write_svg(rel_path, content):
    full_path = os.path.join(BASE_DIR, rel_path)
    os.makedirs(os.path.dirname(full_path), exist_ok=True)
    with open(full_path, "w", encoding="utf-8") as f:
        f.write(content.strip())
    print(f"Generated: {rel_path}")

# 1. Gem Currency Icon
gem_svg = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64" width="64" height="64">
  <defs>
    <linearGradient id="gemGrad" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#00FFFF"/>
      <stop offset="50%" stop-color="#00A8FF"/>
      <stop offset="100%" stop-color="#0984E3"/>
    </linearGradient>
    <linearGradient id="gemShine" x1="0%" y1="0%" x2="0%" y2="100%">
      <stop offset="0%" stop-color="#FFFFFF" stop-opacity="0.8"/>
      <stop offset="100%" stop-color="#FFFFFF" stop-opacity="0.1"/>
    </linearGradient>
  </defs>
  <polygon points="32,4 56,20 44,58 20,58 8,20" fill="url(#gemGrad)" stroke="#74B9FF" stroke-width="2.5"/>
  <polygon points="32,4 44,20 20,20" fill="url(#gemShine)"/>
  <polygon points="20,20 8,20 20,58" fill="#0077B6" opacity="0.6"/>
  <polygon points="44,20 56,20 44,58" fill="#023E8A" opacity="0.7"/>
  <polygon points="20,20 44,20 32,58" fill="#0096C7" opacity="0.85"/>
  <circle cx="26" cy="16" r="3" fill="#FFFFFF" opacity="0.9"/>
</svg>"""
write_svg("assets/icons/gem.svg", gem_svg)

# 2. Rarity Badges / Frames
rarities = {
    "common": ("#7F8C8D", "#BDC3C7", "COMUM"),
    "rare": ("#0984E3", "#74B9FF", "RARO"),
    "epic": ("#6C5CE7", "#A29BFE", "ÉPICO"),
    "legendary": ("#D35400", "#F1C40F", "LENDÁRIO")
}

for r_name, (color1, color2, label) in rarities.items():
    badge_svg = f"""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 120 36" width="120" height="36">
  <defs>
    <linearGradient id="bgGrad_{r_name}" x1="0%" y1="0%" x2="100%" y2="0%">
      <stop offset="0%" stop-color="{color1}"/>
      <stop offset="100%" stop-color="{color2}"/>
    </linearGradient>
  </defs>
  <rect x="2" y="2" width="116" height="32" rx="6" fill="#111827" stroke="url(#bgGrad_{r_name})" stroke-width="2"/>
  <rect x="6" y="6" width="108" height="24" rx="4" fill="url(#bgGrad_{r_name})" opacity="0.25"/>
  <text x="60" y="23" font-family="sans-serif" font-size="13" font-weight="bold" fill="{color2}" text-anchor="middle" letter-spacing="1.5">{label}</text>
</svg>"""
    write_svg(f"assets/icons/rarity_{r_name}.svg", badge_svg)

# 3. Enhanced Character Sprites (Visual Revamp)
# Kael - Arcane Guardian (Common Mage)
kael_sprite = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 120" width="100" height="120">
  <defs>
    <radialGradient id="arcaneAura" cx="50%" cy="50%" r="50%">
      <stop offset="0%" stop-color="#81ECEC" stop-opacity="0.6"/>
      <stop offset="100%" stop-color="#00CEC9" stop-opacity="0"/>
    </radialGradient>
    <linearGradient id="robeGrad" x1="0%" y1="0%" x2="0%" y2="100%">
      <stop offset="0%" stop-color="#2D3436"/>
      <stop offset="50%" stop-color="#0984E3"/>
      <stop offset="100%" stop-color="#1E3799"/>
    </linearGradient>
  </defs>
  <!-- Shadow -->
  <ellipse cx="50" cy="112" rx="28" ry="7" fill="#000000" opacity="0.45"/>
  <!-- Aura -->
  <circle cx="50" cy="65" r="45" fill="url(#arcaneAura)"/>
  <!-- Cloak / Robe -->
  <path d="M30 45 L20 105 L80 105 L70 45 Z" fill="url(#robeGrad)"/>
  <path d="M40 45 L47 105 L53 105 L60 45 Z" fill="#74B9FF" opacity="0.5"/>
  <!-- Torso & Breastplate -->
  <rect x="36" y="42" width="28" height="30" rx="4" fill="#636E72" stroke="#DFE6E9" stroke-width="1.5"/>
  <!-- Arcane Gem on Chest -->
  <polygon points="50,48 56,56 50,64 44,56" fill="#00CEC9" stroke="#E056FD" stroke-width="1"/>
  <!-- Head & Hood -->
  <circle cx="50" cy="28" r="16" fill="#DFE6E9"/>
  <path d="M34 28 C34 10 66 10 66 28 C66 38 60 42 50 44 C40 42 34 38 34 28 Z" fill="#2D3436"/>
  <!-- Glowing Eyes -->
  <circle cx="45" cy="28" r="2.5" fill="#81ECEC"/>
  <circle cx="55" cy="28" r="2.5" fill="#81ECEC"/>
  <!-- Arcane Staff -->
  <rect x="76" y="15" width="5" height="95" rx="2" fill="#B2BEC3"/>
  <circle cx="78.5" cy="15" r="9" fill="#00CEC9" stroke="#81ECEC" stroke-width="2"/>
  <circle cx="78.5" cy="15" r="4" fill="#FFFFFF"/>
</svg>"""
write_svg("assets/sprites/kael_sprite.svg", kael_sprite)

# Lyra - Shadow Archer (Rare Archer)
lyra_sprite = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 120" width="100" height="120">
  <defs>
    <radialGradient id="shadowAura" cx="50%" cy="50%" r="50%">
      <stop offset="0%" stop-color="#00B894" stop-opacity="0.5"/>
      <stop offset="100%" stop-color="#00CEC9" stop-opacity="0"/>
    </radialGradient>
  </defs>
  <ellipse cx="50" cy="112" rx="26" ry="6" fill="#000000" opacity="0.45"/>
  <circle cx="50" cy="65" r="42" fill="url(#shadowAura)"/>
  <!-- Cape -->
  <path d="M32 42 L16 102 L42 104 Z" fill="#2D3436" opacity="0.9"/>
  <path d="M68 42 L84 102 L58 104 Z" fill="#2D3436" opacity="0.9"/>
  <!-- Body / Armor -->
  <path d="M36 44 L32 88 L68 88 L64 44 Z" fill="#006266" stroke="#00B894" stroke-width="1.5"/>
  <rect x="42" y="88" width="6" height="24" fill="#2D3436"/>
  <rect x="52" y="88" width="6" height="24" fill="#2D3436"/>
  <!-- Head & Cowl -->
  <circle cx="50" cy="26" r="14" fill="#FFEAA7"/>
  <path d="M36 24 C36 8 64 8 64 24 C64 36 50 40 50 40 C50 40 36 36 36 24 Z" fill="#00B894"/>
  <circle cx="46" cy="24" r="2" fill="#55EFC4"/>
  <circle cx="54" cy="24" r="2" fill="#55EFC4"/>
  <!-- Composite Recurve Bow -->
  <path d="M78 18 Q96 60 78 102" fill="none" stroke="#D63031" stroke-width="4" stroke-linecap="round"/>
  <path d="M78 20 Q92 60 78 100" fill="none" stroke="#FDCB6E" stroke-width="2"/>
  <line x1="78" y1="20" x2="78" y2="100" stroke="#DFE6E9" stroke-width="1" stroke-dasharray="2,2"/>
</svg>"""
write_svg("assets/sprites/lyra_sprite.svg", lyra_sprite)

# Thorne - Guardian Knight (Rare Protector)
thorne_sprite = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 110 120" width="110" height="120">
  <ellipse cx="55" cy="112" rx="34" ry="8" fill="#000000" opacity="0.5"/>
  <!-- Heavy Armor Body -->
  <rect x="36" y="44" width="38" height="46" rx="6" fill="#636E72" stroke="#B2BEC3" stroke-width="2"/>
  <rect x="42" y="50" width="26" height="32" rx="3" fill="#2D3436"/>
  <rect x="38" y="90" width="14" height="22" rx="2" fill="#636E72"/>
  <rect x="58" y="90" width="14" height="22" rx="2" fill="#636E72"/>
  <!-- Massive Pauldrons -->
  <circle cx="28" cy="46" r="14" fill="#B2BEC3" stroke="#DFE6E9" stroke-width="2"/>
  <circle cx="82" cy="46" r="14" fill="#B2BEC3" stroke="#DFE6E9" stroke-width="2"/>
  <!-- Great Helm -->
  <rect x="42" y="16" width="26" height="26" rx="6" fill="#B2BEC3" stroke="#DFE6E9" stroke-width="2"/>
  <line x1="46" y1="26" x2="64" y2="26" stroke="#2D3436" stroke-width="3"/>
  <polygon points="55,10 60,16 50,16" fill="#D63031"/>
  <!-- Tower Shield -->
  <polygon points="12,35 34,35 30,95 23,105 16,95" fill="#0984E3" stroke="#F1C40F" stroke-width="2.5"/>
  <polygon points="23,45 28,55 23,65 18,55" fill="#F1C40F"/>
  <!-- War Hammer -->
  <rect x="85" y="25" width="6" height="80" rx="2" fill="#7F8C8D"/>
  <rect x="76" y="20" width="24" height="16" rx="3" fill="#BDC3C7" stroke="#F1C40F" stroke-width="1.5"/>
</svg>"""
write_svg("assets/sprites/thorne_sprite.svg", thorne_sprite)

# Elysia - Pyromancer (Epic Mage)
elysia_sprite = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 120" width="100" height="120">
  <defs>
    <radialGradient id="fireAura" cx="50%" cy="50%" r="50%">
      <stop offset="0%" stop-color="#F1C40F" stop-opacity="0.7"/>
      <stop offset="60%" stop-color="#E17055" stop-opacity="0.4"/>
      <stop offset="100%" stop-color="#D63031" stop-opacity="0"/>
    </radialGradient>
  </defs>
  <ellipse cx="50" cy="112" rx="26" ry="6" fill="#000000" opacity="0.45"/>
  <circle cx="50" cy="65" r="45" fill="url(#fireAura)"/>
  <!-- Crimson Robes -->
  <path d="M34 44 L20 106 L80 106 L66 44 Z" fill="#D63031"/>
  <path d="M44 44 L48 106 L52 106 L56 44 Z" fill="#F1C40F"/>
  <!-- Torso & Gold Corset -->
  <rect x="38" y="42" width="24" height="26" rx="4" fill="#E17055" stroke="#F1C40F" stroke-width="1.5"/>
  <!-- Head & Hair -->
  <circle cx="50" cy="26" r="14" fill="#FDCB6E"/>
  <path d="M36 18 C36 8 64 8 64 18 C68 28 64 36 62 44 C56 36 50 36 44 36 C38 36 34 28 36 18 Z" fill="#D63031"/>
  <!-- Eyes -->
  <circle cx="46" cy="25" r="2.5" fill="#F1C40F"/>
  <circle cx="54" cy="25" r="2.5" fill="#F1C40F"/>
  <!-- Levitating Fire Orbs -->
  <circle cx="24" cy="40" r="7" fill="#F1C40F" stroke="#D63031" stroke-width="2"/>
  <circle cx="76" cy="40" r="7" fill="#F1C40F" stroke="#D63031" stroke-width="2"/>
  <circle cx="50" cy="8" r="5" fill="#FFFFFF" stroke="#F1C40F" stroke-width="1.5"/>
</svg>"""
write_svg("assets/sprites/elysia_sprite.svg", elysia_sprite)

# Boran - Clockwork Trapper (Epic Engineer)
boran_sprite = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 120" width="100" height="120">
  <ellipse cx="50" cy="112" rx="28" ry="7" fill="#000000" opacity="0.45"/>
  <!-- Leather Vest & Copper Gear -->
  <rect x="34" y="44" width="32" height="42" rx="5" fill="#B33939" stroke="#E67E22" stroke-width="2"/>
  <circle cx="50" cy="62" r="10" fill="#E67E22" stroke="#F39C12" stroke-width="1.5"/>
  <circle cx="50" cy="62" r="4" fill="#333333"/>
  <rect x="36" y="86" width="10" height="24" fill="#2C3E50"/>
  <rect x="54" y="86" width="10" height="24" fill="#2C3E50"/>
  <!-- Head & Steampunk Goggles -->
  <circle cx="50" cy="26" r="15" fill="#F7D794"/>
  <rect x="38" y="14" width="24" height="12" rx="3" fill="#795548"/>
  <circle cx="44" cy="24" r="6" fill="#1ABC9C" stroke="#D35400" stroke-width="2"/>
  <circle cx="56" cy="24" r="6" fill="#1ABC9C" stroke="#D35400" stroke-width="2"/>
  <!-- Mechanical Gauntlet / Wrench -->
  <rect x="72" y="38" width="12" height="30" rx="3" fill="#BDC3C7" stroke="#7F8C8D" stroke-width="1.5"/>
  <circle cx="78" cy="72" r="8" fill="#F39C12"/>
</svg>"""
write_svg("assets/sprites/boran_sprite.svg", boran_sprite)

# Vesper - Twilight Assassin (Legendary Assassin)
vesper_sprite = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 120" width="100" height="120">
  <defs>
    <radialGradient id="voidAura" cx="50%" cy="50%" r="50%">
      <stop offset="0%" stop-color="#8E44AD" stop-opacity="0.8"/>
      <stop offset="70%" stop-color="#2C3E50" stop-opacity="0.4"/>
      <stop offset="100%" stop-color="#000000" stop-opacity="0"/>
    </radialGradient>
  </defs>
  <ellipse cx="50" cy="112" rx="26" ry="6" fill="#000000" opacity="0.6"/>
  <circle cx="50" cy="65" r="46" fill="url(#voidAura)"/>
  <!-- Shadow Cloak / Shroud -->
  <path d="M30 40 L12 108 L50 100 L88 108 L70 40 Z" fill="#1E272E" stroke="#8E44AD" stroke-width="1.5"/>
  <!-- Slim Armored Torso -->
  <rect x="38" y="42" width="24" height="38" rx="4" fill="#2C3E50" stroke="#9B59B6" stroke-width="1.5"/>
  <rect x="42" y="80" width="6" height="30" fill="#1E272E"/>
  <rect x="52" y="80" width="6" height="30" fill="#1E272E"/>
  <!-- Cowl & Glowing Violet Mask -->
  <circle cx="50" cy="24" r="14" fill="#1E272E"/>
  <path d="M36 24 C36 8 64 8 64 24 C64 36 50 40 50 40 C50 40 36 36 36 24 Z" fill="#0A0A0A"/>
  <line x1="42" y1="23" x2="48" y2="25" stroke="#9B59B6" stroke-width="2.5" stroke-linecap="round"/>
  <line x1="58" y1="23" x2="52" y2="25" stroke="#9B59B6" stroke-width="2.5" stroke-linecap="round"/>
  <!-- Dual Twilight Daggers -->
  <polygon points="20,40 24,75 22,80 18,75" fill="#8E44AD" stroke="#E056FD" stroke-width="1.5"/>
  <polygon points="80,40 84,75 82,80 78,75" fill="#8E44AD" stroke="#E056FD" stroke-width="1.5"/>
</svg>"""
write_svg("assets/sprites/vesper_sprite.svg", vesper_sprite)

# 4. Boss Ogre - Ancestral Devastator
boss_ogre_sprite = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 160 160" width="160" height="160">
  <defs>
    <radialGradient id="bossAura" cx="50%" cy="50%" r="50%">
      <stop offset="0%" stop-color="#FF3838" stop-opacity="0.6"/>
      <stop offset="100%" stop-color="#711919" stop-opacity="0"/>
    </radialGradient>
  </defs>
  <ellipse cx="80" cy="150" rx="55" ry="10" fill="#000000" opacity="0.6"/>
  <circle cx="80" cy="80" r="70" fill="url(#bossAura)"/>
  <!-- Massive Torso -->
  <rect x="42" y="55" width="76" height="70" rx="15" fill="#4B6584" stroke="#2C3E50" stroke-width="3"/>
  <!-- Glowing War Runes on Chest -->
  <path d="M60 70 L80 90 L100 70 M80 90 L80 115" fill="none" stroke="#FF3838" stroke-width="4" stroke-linecap="round"/>
  <!-- Massive Legs -->
  <rect x="46" y="125" width="28" height="25" rx="6" fill="#2C3E50"/>
  <rect x="86" y="125" width="28" height="25" rx="6" fill="#2C3E50"/>
  <!-- Huge Spiked Pauldrons -->
  <circle cx="34" cy="60" r="22" fill="#778CA3" stroke="#2C3E50" stroke-width="2.5"/>
  <polygon points="26,42 34,22 42,42" fill="#A5B1C2"/>
  <circle cx="126" cy="60" r="22" fill="#778CA3" stroke="#2C3E50" stroke-width="2.5"/>
  <polygon points="118,42 126,22 134,42" fill="#A5B1C2"/>
  <!-- Menacing Head & Horns -->
  <circle cx="80" cy="38" r="24" fill="#4B6584" stroke="#2C3E50" stroke-width="3"/>
  <path d="M62 30 Q50 10 40 18" fill="none" stroke="#D1D8E0" stroke-width="6" stroke-linecap="round"/>
  <path d="M98 30 Q110 10 120 18" fill="none" stroke="#D1D8E0" stroke-width="6" stroke-linecap="round"/>
  <!-- Glowing Red Eyes & Tusks -->
  <circle cx="70" cy="36" r="4.5" fill="#FF3838"/>
  <circle cx="90" cy="36" r="4.5" fill="#FF3838"/>
  <polygon points="66,54 70,44 74,54" fill="#FFFFFF"/>
  <polygon points="86,54 90,44 94,54" fill="#FFFFFF"/>
  <!-- Devastator Spiked Club -->
  <rect x="135" y="40" width="12" height="100" rx="3" fill="#4B4B4B"/>
  <rect x="125" y="20" width="32" height="35" rx="5" fill="#2D3436" stroke="#FF3838" stroke-width="2"/>
  <polygon points="120,30 125,32 125,28" fill="#D1D8E0"/>
  <polygon points="162,30 157,32 157,28" fill="#D1D8E0"/>
  <polygon points="141,12 143,20 139,20" fill="#D1D8E0"/>
</svg>"""
write_svg("assets/sprites/enemy_boss_ogre.svg", boss_ogre_sprite)

# 5. Five Thematic Vertical Scenario Backgrounds (720 x 850)
scenarios = [
    ("scenario_forest.svg", "#0F2419", "#1B4332", "#2D6A4F", "#52B788", "#74C69D"), # Enchanted Forest
    ("scenario_ruins.svg", "#1A1715", "#2C2621", "#4A3F35", "#8C7863", "#D4A373"),  # Ancient Ruins
    ("scenario_volcano.svg", "#1A0A0A", "#330D0D", "#661414", "#D63031", "#FF7675"),# Volcanic Valley
    ("scenario_fortress.svg", "#0C1017", "#171F2C", "#273347", "#485E80", "#768DAE"),# Dark Fortress
    ("scenario_temple.svg", "#0F0B1E", "#1E163B", "#362669", "#6C5CE7", "#A29BFE")   # Arcane Temple
]

for filename, c_bg, c_ground, c_path, c_accent, c_light in scenarios:
    scenario_svg = f"""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 720 850" width="720" height="850">
  <defs>
    <linearGradient id="skyGrad_{filename[:5]}" x1="0%" y1="0%" x2="0%" y2="100%">
      <stop offset="0%" stop-color="{c_bg}"/>
      <stop offset="100%" stop-color="{c_ground}"/>
    </linearGradient>
    <radialGradient id="glow_{filename[:5]}" cx="50%" cy="10%" r="60%">
      <stop offset="0%" stop-color="{c_light}" stop-opacity="0.3"/>
      <stop offset="100%" stop-color="{c_bg}" stop-opacity="0"/>
    </radialGradient>
  </defs>
  <rect x="0" y="0" width="720" height="850" fill="url(#skyGrad_{filename[:5]})"/>
  <rect x="0" y="0" width="720" height="850" fill="url(#glow_{filename[:5]})"/>
  <!-- Decorative Environment Features -->
  <circle cx="80" cy="180" r="45" fill="{c_accent}" opacity="0.25"/>
  <circle cx="640" cy="240" r="55" fill="{c_accent}" opacity="0.2"/>
  <circle cx="100" cy="520" r="60" fill="{c_accent}" opacity="0.25"/>
  <circle cx="630" cy="620" r="50" fill="{c_accent}" opacity="0.2"/>
  <!-- Ambient Runes / Particles -->
  <polygon points="120,300 130,315 110,315" fill="{c_light}" opacity="0.4"/>
  <polygon points="600,420 610,435 590,435" fill="{c_light}" opacity="0.4"/>
  <polygon points="160,700 170,715 150,715" fill="{c_light}" opacity="0.4"/>
</svg>"""
    write_svg(f"assets/sprites/{filename}", scenario_svg)

print("All upgrade visual assets generated successfully!")
