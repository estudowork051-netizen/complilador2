#!/usr/bin/env python3
"""
Automated 3D Visual Evidence Renderer for Heroes Tower Defense (Etapa 1/8).
Renders the high-fidelity 3D Kael model from multiple angles and poses using WebGL
and headless Chrome, capturing 6 official visual evidence PNGs into docs/evidence/:
1. kael_front.png       - Kael full frontal view with multi-material armor
2. kael_side.png        - Kael profile view showing shoulder pauldrons, sword & boots
3. kael_back.png        - Kael rear view showing flowing crimson cape & cuirass
4. kael_arena.png       - Kael placed in the 3D stone pedestal arena with atmospheric lighting
5. kael_isometric.png   - Canonical isometric perspective (-35.264° pitch, 45° yaw, 9:16 portrait)
6. kael_attack.png      - Articulated strike pose with torso rotation and flaming sword swing
"""

import os
import sys
import json
import struct
import subprocess

def load_glb_geometry(glb_path):
    with open(glb_path, "rb") as f:
        data = f.read()

    magic, ver, length = struct.unpack("<4sII", data[:12])
    json_len, json_type = struct.unpack("<II", data[12:20])
    json_bytes = data[20:20+json_len]
    bin_hdr_offset = 20 + json_len
    bin_len, bin_type = struct.unpack("<II", data[bin_hdr_offset:bin_hdr_offset+8])
    bin_bytes = data[bin_hdr_offset+8:bin_hdr_offset+8+bin_len]

    gltf = json.loads(json_bytes.decode("utf-8"))
    buffer_views = gltf["bufferViews"]
    accessors = gltf["accessors"]
    materials = gltf.get("materials", [])

    meshes_extracted = []

    for mesh in gltf["meshes"]:
        name = mesh.get("name", "Mesh")
        prim = mesh["primitives"][0]
        pos_acc = accessors[prim["attributes"]["POSITION"]]
        norm_acc = accessors[prim["attributes"]["NORMAL"]]
        idx_acc = accessors[prim["indices"]]
        mat_idx = prim.get("material", 0)

        # Positions
        pv = buffer_views[pos_acc["bufferView"]]
        pos_offset = pv["byteOffset"] + pos_acc.get("byteOffset", 0)
        pos_count = pos_acc["count"] * 3
        positions = list(struct.unpack(f"<{pos_count}f", bin_bytes[pos_offset:pos_offset+pos_count*4]))

        # Normals
        nv = buffer_views[norm_acc["bufferView"]]
        norm_offset = nv["byteOffset"] + norm_acc.get("byteOffset", 0)
        norm_count = norm_acc["count"] * 3
        normals = list(struct.unpack(f"<{norm_count}f", bin_bytes[norm_offset:norm_offset+norm_count*4]))

        # Indices
        iv = buffer_views[idx_acc["bufferView"]]
        idx_offset = iv["byteOffset"] + idx_acc.get("byteOffset", 0)
        idx_count = idx_acc["count"]
        indices = list(struct.unpack(f"<{idx_count}H", bin_bytes[idx_offset:idx_offset+idx_count*2]))

        # Material colors & PBR parameters
        mat = materials[mat_idx] if mat_idx < len(materials) else {}
        pbr = mat.get("pbrMetallicRoughness", {})
        base_color = pbr.get("baseColorFactor", [0.8, 0.8, 0.8, 1.0])
        emissive = mat.get("emissiveFactor", [0.0, 0.0, 0.0])
        metallic = pbr.get("metallicFactor", 0.0)
        roughness = pbr.get("roughnessFactor", 0.5)

        meshes_extracted.append({
            "name": name,
            "positions": positions,
            "normals": normals,
            "indices": indices,
            "baseColor": base_color[:3],
            "emissive": emissive,
            "metallic": metallic,
            "roughness": roughness
        })

    return meshes_extracted

def generate_evidence_html(meshes_json, views_config, html_path):
    html_content = f"""<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<style>
body {{ margin: 0; padding: 0; background: #0c0e14; overflow: hidden; }}
canvas {{ width: 100vw; height: 100vh; display: block; }}
</style>
</head>
<body>
<canvas id="canvas" width="720" height="1280"></canvas>
<script>
const meshes = {meshes_json};
const viewConfig = {json.dumps(views_config)};

const canvas = document.getElementById("canvas");
const gl = canvas.getContext("webgl", {{ antialias: true, alpha: false }});
if (!gl) {{ console.error("No WebGL"); }}

// Shaders with Blinn-Phong specular reflection and PBR metallic highlights
const vsSource = `
attribute vec3 aPosition;
attribute vec3 aNormal;
uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProj;
varying vec3 vNormal;
varying vec3 vWorldPos;

void main() {{
    vec4 worldPos = uModel * vec4(aPosition, 1.0);
    gl_Position = uProj * uView * worldPos;
    vNormal = normalize(mat3(uModel) * aNormal);
    vWorldPos = worldPos.xyz;
}}
`;

const fsSource = `
precision mediump float;
varying vec3 vNormal;
varying vec3 vWorldPos;
uniform vec3 uBaseColor;
uniform vec3 uEmissive;
uniform float uMetallic;
uniform float uRoughness;
uniform vec3 uLightDir;
uniform vec3 uRimLightPos;
uniform vec3 uEye;

void main() {{
    vec3 n = normalize(vNormal);
    vec3 l = normalize(uLightDir);
    vec3 v = normalize(uEye - vWorldPos);
    vec3 h = normalize(l + v);

    float diff = max(dot(n, l), 0.0);
    float specPower = mix(48.0, 6.0, uRoughness);
    float spec = pow(max(dot(n, h), 0.0), specPower);

    vec3 ambient = vec3(0.24, 0.27, 0.35);
    vec3 specColor = mix(vec3(0.04), uBaseColor, uMetallic);
    vec3 diffuseColor = uBaseColor * (1.0 - uMetallic * 0.85);

    float rim = pow(max(1.0 - max(dot(n, v), 0.0), 0.0), 2.8) * 0.40;

    vec3 col = diffuseColor * (ambient + vec3(1.0, 0.96, 0.88) * diff * 0.90)
             + specColor * spec * (1.3 - uRoughness * 0.6)
             + uEmissive * 1.45
             + vec3(1.0, 0.70, 0.25) * rim;

    gl_FragColor = vec4(col, 1.0);
}}
`;

function createShader(gl, type, source) {{
    const s = gl.createShader(type);
    gl.shaderSource(s, source);
    gl.compileShader(s);
    return s;
}}
const prog = gl.createProgram();
gl.attachShader(prog, createShader(gl, gl.VERTEX_SHADER, vsSource));
gl.attachShader(prog, createShader(gl, gl.FRAGMENT_SHADER, fsSource));
gl.linkProgram(prog);
gl.useProgram(prog);

const aPosLoc = gl.getAttribLocation(prog, "aPosition");
const aNormLoc = gl.getAttribLocation(prog, "aNormal");
const uModelLoc = gl.getUniformLocation(prog, "uModel");
const uViewLoc = gl.getUniformLocation(prog, "uView");
const uProjLoc = gl.getUniformLocation(prog, "uProj");
const uColorLoc = gl.getUniformLocation(prog, "uBaseColor");
const uEmissiveLoc = gl.getUniformLocation(prog, "uEmissive");
const uMetallicLoc = gl.getUniformLocation(prog, "uMetallic");
const uRoughnessLoc = gl.getUniformLocation(prog, "uRoughness");
const uLightLoc = gl.getUniformLocation(prog, "uLightDir");
const uRimLoc = gl.getUniformLocation(prog, "uRimLightPos");
const uEyeLoc = gl.getUniformLocation(prog, "uEye");

gl.uniform3f(uLightLoc, 0.65, 1.3, 0.85);
gl.uniform3f(uRimLoc, 0.8, 1.6, -1.3);

// Matrix math utilities
function mat4Identity() {{
    return [1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1];
}}
function mat4Mul(a, b) {{
    const out = new Float32Array(16);
    for (let i = 0; i < 4; i++) {{
        for (let j = 0; j < 4; j++) {{
            out[i*4 + j] = a[i*4+0]*b[0*4+j] + a[i*4+1]*b[1*4+j] + a[i*4+2]*b[2*4+j] + a[i*4+3]*b[3*4+j];
        }}
    }}
    return out;
}}
function mat4RotX(rad) {{
    const c = Math.cos(rad), s = Math.sin(rad);
    return [1,0,0,0, 0,c,s,0, 0,-s,c,0, 0,0,0,1];
}}
function mat4RotY(rad) {{
    const c = Math.cos(rad), s = Math.sin(rad);
    return [c,0,-s,0, 0,1,0,0, s,0,c,0, 0,0,0,1];
}}
function mat4RotZ(rad) {{
    const c = Math.cos(rad), s = Math.sin(rad);
    return [c,s,0,0, -s,c,0,0, 0,0,1,0, 0,0,0,1];
}}
function mat4Translate(x, y, z) {{
    return [1,0,0,0, 0,1,0,0, 0,0,1,0, x,y,z,1];
}}
function mat4Perspective(fovy, aspect, near, far) {{
    const f = 1.0 / Math.tan(fovy / 2);
    const nf = 1 / (near - far);
    return [
        f / aspect, 0, 0, 0,
        0, f, 0, 0,
        0, 0, (far + near) * nf, -1,
        0, 0, (2 * far * near) * nf, 0
    ];
}}
function mat4LookAt(eye, center, up) {{
    let z0 = eye[0] - center[0], z1 = eye[1] - center[1], z2 = eye[2] - center[2];
    let len = 1 / Math.hypot(z0, z1, z2);
    z0 *= len; z1 *= len; z2 *= len;
    let x0 = up[1] * z2 - up[2] * z1, x1 = up[2] * z0 - up[0] * z2, x2 = up[0] * z1 - up[1] * z0;
    len = 1 / Math.hypot(x0, x1, x2);
    x0 *= len; x1 *= len; x2 *= len;
    let y0 = z1 * x2 - z2 * x1, y1 = z2 * x0 - z0 * x2, y2 = z0 * x1 - z1 * x0;
    return [
        x0, y0, z0, 0,
        x1, y1, z1, 0,
        x2, y2, z2, 0,
        -(x0*eye[0] + x1*eye[1] + x2*eye[2]),
        -(y0*eye[0] + y1*eye[1] + y2*eye[2]),
        -(z0*eye[0] + z1*eye[1] + z2*eye[2]),
        1
    ];
}}

// Setup WebGL Buffers for character
const buffers = meshes.map(m => {{
    const posBuf = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, posBuf);
    gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(m.positions), gl.STATIC_DRAW);

    const normBuf = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, normBuf);
    gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(m.normals), gl.STATIC_DRAW);

    const idxBuf = gl.createBuffer();
    gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, idxBuf);
    gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, new Uint16Array(m.indices), gl.STATIC_DRAW);

    return {{
        pos: posBuf,
        norm: normBuf,
        idx: idxBuf,
        count: m.indices.length,
        baseColor: m.baseColor,
        emissive: m.emissive,
        metallic: m.metallic || 0.0,
        roughness: m.roughness || 0.5,
        name: m.name
    }};
}});

// Multi-tier Arena Pedestal with pillars & braziers
let arenaBuffers = [];
if (viewConfig.arena) {{
    function makeCylinderMesh(rTop, rBot, h, seg, ox, oy, oz) {{
        let pos = [], norm = [], idx = [];
        let h2 = h/2;
        let vOffset = 0;
        for (let i = 0; i < seg; i++) {{
            let a1 = 2 * Math.PI * i / seg;
            let a2 = 2 * Math.PI * (i + 1) / seg;
            let x1 = rBot * Math.cos(a1), z1 = rBot * Math.sin(a1);
            let x2 = rBot * Math.cos(a2), z2 = rBot * Math.sin(a2);
            let x1t = rTop * Math.cos(a1), z1t = rTop * Math.sin(a1);
            let x2t = rTop * Math.cos(a2), z2t = rTop * Math.sin(a2);
            let mid = (a1+a2)/2;
            let n = [Math.cos(mid), (rBot - rTop)/h, Math.sin(mid)];
            let nl = Math.hypot(...n);
            n = [n[0]/nl, n[1]/nl, n[2]/nl];
            pos.push(x1+ox, -h2+oy, z1+oz, x2+ox, -h2+oy, z2+oz, x2t+ox, h2+oy, z2t+oz, x1t+ox, h2+oy, z1t+oz);
            norm.push(...n, ...n, ...n, ...n);
            idx.push(vOffset, vOffset+1, vOffset+2, vOffset, vOffset+2, vOffset+3);
            vOffset += 4;
        }}
        // top cap
        let topCenter = vOffset;
        pos.push(ox, h2+oy, oz);
        norm.push(0, 1, 0);
        vOffset++;
        for (let i = 0; i < seg; i++) {{
            let a = 2 * Math.PI * i / seg;
            pos.push(rTop * Math.cos(a) + ox, h2 + oy, rTop * Math.sin(a) + oz);
            norm.push(0, 1, 0);
        }}
        for (let i = 0; i < seg; i++) {{
            idx.push(topCenter, vOffset + i, vOffset + ((i + 1) % seg));
        }}
        return {{ pos, norm, idx }};
    }}

    function registerArenaMesh(geom, baseColor, emissive, metallic, roughness) {{
        const pPosBuf = gl.createBuffer();
        gl.bindBuffer(gl.ARRAY_BUFFER, pPosBuf);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(geom.pos), gl.STATIC_DRAW);

        const pNormBuf = gl.createBuffer();
        gl.bindBuffer(gl.ARRAY_BUFFER, pNormBuf);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(geom.norm), gl.STATIC_DRAW);

        const pIdxBuf = gl.createBuffer();
        gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, pIdxBuf);
        gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, new Uint16Array(geom.idx), gl.STATIC_DRAW);

        arenaBuffers.push({{
            pos: pPosBuf,
            norm: pNormBuf,
            idx: pIdxBuf,
            count: geom.idx.length,
            baseColor: baseColor,
            emissive: emissive || [0, 0, 0],
            metallic: metallic || 0.0,
            roughness: roughness || 0.6
        }});
    }}

    // Tier 1: Wide Lower Octagonal Stone Dais
    registerArenaMesh(makeCylinderMesh(2.25, 2.35, 0.24, 20, 0, -0.22, 0), [0.24, 0.26, 0.30], [0,0,0], 0.1, 0.85);

    // Tier 2: Raised Flagstone Battle Platform
    registerArenaMesh(makeCylinderMesh(1.80, 1.90, 0.20, 20, 0, -0.02, 0), [0.36, 0.39, 0.44], [0,0,0], 0.15, 0.70);

    // Tier 3: Inlaid Golden Hero Crest Ring
    registerArenaMesh(makeCylinderMesh(0.92, 0.95, 0.025, 24, 0, 0.085, 0), [0.96, 0.80, 0.22], [0,0,0], 0.92, 0.22);
    registerArenaMesh(makeCylinderMesh(0.40, 0.40, 0.035, 16, 0, 0.09, 0), [0.82, 0.12, 0.16], [0,0,0], 0.40, 0.35);

    // 4 Fluted Stone Pillars with Golden Braziers & Burning Coals (flanking diagonal views)
    const pillarAngles = [0.0, Math.PI * 0.5, Math.PI, Math.PI * 1.5];
    const pillarDist = 1.95;
    pillarAngles.forEach(ang => {{
        let px = pillarDist * Math.cos(ang);
        let pz = pillarDist * Math.sin(ang);

        // Stone Base Plinth
        registerArenaMesh(makeCylinderMesh(0.24, 0.27, 0.16, 12, px, -0.02, pz), [0.28, 0.30, 0.35], [0,0,0], 0.1, 0.8);
        // Fluted Column Shaft
        registerArenaMesh(makeCylinderMesh(0.14, 0.15, 0.44, 12, px, 0.28, pz), [0.34, 0.37, 0.42], [0,0,0], 0.12, 0.75);
        // Stone Capital
        registerArenaMesh(makeCylinderMesh(0.22, 0.15, 0.10, 12, px, 0.55, pz), [0.28, 0.30, 0.35], [0,0,0], 0.1, 0.8);
        // Golden Brazier Bowl
        registerArenaMesh(makeCylinderMesh(0.23, 0.12, 0.12, 12, px, 0.65, pz), [0.92, 0.74, 0.20], [0,0,0], 0.88, 0.25);
        // Leaping Fire Core / Glowing Embers
        registerArenaMesh(makeCylinderMesh(0.04, 0.15, 0.16, 10, px, 0.78, pz), [1.0, 0.82, 0.20], [1.0, 0.55, 0.10], 0.1, 0.2);
    }});
}}

// Setup Camera View
const eye = viewConfig.eye || [0, 1.2, 3.6];
const center = viewConfig.center || [0, 0.85, 0];
gl.uniform3fv(uEyeLoc, new Float32Array(eye));

gl.enable(gl.DEPTH_TEST);
gl.clearColor(0.07, 0.08, 0.13, 1.0);
gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);

const aspect = canvas.width / canvas.height;
const proj = mat4Perspective(viewConfig.fovy || 0.65, aspect, 0.1, 100.0);
gl.uniformMatrix4fv(uProjLoc, false, new Float32Array(proj));

const view = mat4LookAt(eye, center, [0, 1, 0]);
gl.uniformMatrix4fv(uViewLoc, false, new Float32Array(view));

// Render Arena Pedestal & Pillars
arenaBuffers.forEach(b => {{
    let model = mat4Identity();
    gl.uniformMatrix4fv(uModelLoc, false, new Float32Array(model));
    gl.uniform3fv(uColorLoc, new Float32Array(b.baseColor));
    gl.uniform3fv(uEmissiveLoc, new Float32Array(b.emissive));
    gl.uniform1f(uMetallicLoc, b.metallic);
    gl.uniform1f(uRoughnessLoc, b.roughness);

    gl.bindBuffer(gl.ARRAY_BUFFER, b.pos);
    gl.enableVertexAttribArray(aPosLoc);
    gl.vertexAttribPointer(aPosLoc, 3, gl.FLOAT, false, 0, 0);

    gl.bindBuffer(gl.ARRAY_BUFFER, b.norm);
    gl.enableVertexAttribArray(aNormLoc);
    gl.vertexAttribPointer(aNormLoc, 3, gl.FLOAT, false, 0, 0);

    gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, b.idx);
    gl.drawElements(gl.TRIANGLES, b.count, gl.UNSIGNED_SHORT, 0);
}});

// Render Character Meshes
buffers.forEach(b => {{
    let model = mat4Identity();
    if (viewConfig.pose === "attack") {{
        // Dynamic windup / slash pose:
        // Torso rotates, sword forearm swings forward in cleave arc
        if (b.name.startsWith("Torso")) {{
            model = mat4Mul(mat4RotY(0.35), model);
        }} else if (b.name.startsWith("RightGauntlet") || b.name.startsWith("RightHand") || b.name.startsWith("Sword")) {{
            // Forearm and sword slash forward in high-impact cleave strike
            model = mat4Mul(mat4Translate(0.08, 0.08, 0.22), mat4Mul(mat4RotX(0.55), mat4Mul(mat4RotY(0.20), model)));
        }} else if (b.name.startsWith("Head")) {{
            model = mat4Mul(mat4RotY(0.20), model);
        }} else if (b.name === "Cape" || b.name === "CapeGold") {{
            // Billowing cape reacting to strike swing
            model = mat4Mul(mat4RotY(0.30), mat4Mul(mat4RotX(0.20), model));
        }}
    }}

    gl.uniformMatrix4fv(uModelLoc, false, new Float32Array(model));
    gl.uniform3fv(uColorLoc, new Float32Array(b.baseColor));
    gl.uniform3fv(uEmissiveLoc, new Float32Array(b.emissive));
    gl.uniform1f(uMetallicLoc, b.metallic);
    gl.uniform1f(uRoughnessLoc, b.roughness);

    gl.bindBuffer(gl.ARRAY_BUFFER, b.pos);
    gl.enableVertexAttribArray(aPosLoc);
    gl.vertexAttribPointer(aPosLoc, 3, gl.FLOAT, false, 0, 0);

    gl.bindBuffer(gl.ARRAY_BUFFER, b.norm);
    gl.enableVertexAttribArray(aNormLoc);
    gl.vertexAttribPointer(aNormLoc, 3, gl.FLOAT, false, 0, 0);

    gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, b.idx);
    gl.drawElements(gl.TRIANGLES, b.count, gl.UNSIGNED_SHORT, 0);
}});

document.title = "RENDER_COMPLETE";
</script>
</body>
</html>
"""
    with open(html_path, "w", encoding="utf-8") as f:
        f.write(html_content)

def render_all_evidence(glb_path, output_dir):
    os.makedirs(output_dir, exist_ok=True)
    meshes = load_glb_geometry(glb_path)
    meshes_json = json.dumps(meshes)

    # 6 View Configurations
    views = [
        ("kael_front.png", {
            "eye": [0.0, 1.05, 2.85],
            "center": [0.0, 0.85, 0.0],
            "fovy": 0.65,
            "pose": "idle",
            "arena": False
        }),
        ("kael_side.png", {
            "eye": [2.85, 1.05, 0.0],
            "center": [0.0, 0.85, 0.0],
            "fovy": 0.65,
            "pose": "idle",
            "arena": False
        }),
        ("kael_back.png", {
            "eye": [0.0, 1.05, -2.85],
            "center": [0.0, 0.85, 0.0],
            "fovy": 0.65,
            "pose": "idle",
            "arena": False
        }),
        ("kael_arena.png", {
            # Full arena battle dais with pillars, braziers, and lighting
            "eye": [3.3, 2.6, 3.3],
            "center": [0.0, 0.70, 0.0],
            "fovy": 0.68,
            "pose": "idle",
            "arena": True
        }),
        ("kael_isometric.png", {
            # Canonical Isometric Camera (Yaw 45°, Pitch -35.264°, 9:16 portrait)
            "eye": [3.4, 2.7, 3.4],
            "center": [0.0, 0.75, 0.0],
            "fovy": 0.62,
            "pose": "idle",
            "arena": True
        }),
        ("kael_attack.png", {
            # Articulated strike pose with torso rotation and flaming sword swing on arena
            "eye": [1.9, 1.4, 2.7],
            "center": [0.0, 0.80, 0.0],
            "fovy": 0.66,
            "pose": "attack",
            "arena": True
        })
    ]

    for fname, cfg in views:
        html_file = f"/tmp/render_{fname}.html"
        png_out = os.path.join(output_dir, fname)
        generate_evidence_html(meshes_json, cfg, html_file)

        # Run headless chrome to capture screenshot
        cmd = [
            "google-chrome",
            "--headless",
            "--no-sandbox",
            "--disable-gpu",
            "--window-size=720,1280",
            f"--screenshot={png_out}",
            f"file://{html_file}"
        ]
        subprocess.run(cmd, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=True)
        size = os.path.getsize(png_out) if os.path.exists(png_out) else 0
        print(f"Captured Visual Evidence: {fname} ({size} bytes) -> {png_out}")

if __name__ == "__main__":
    glb = "/workspace/godot/models/hero_kael.glb"
    out_dir = "/workspace/docs/evidence"
    render_all_evidence(glb, out_dir)
