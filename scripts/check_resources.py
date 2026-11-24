"""Validate local asset links and runtime entry points without launching Minecraft.

Run from any directory with Python 3. Vanilla and other mods' resource namespaces
are deliberately left to Minecraft; generated tree sprites are checked explicitly.
"""
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
RESOURCES = ROOT / "src/main/resources"
JAVA = ROOT / "src/main/java"
errors = []
checks = 0


def require(path, source):
    global checks
    checks += 1
    if not path.is_file():
        errors.append(f"{source}: missing {path.relative_to(ROOT)}")


def asset(value, directory, extension, source):
    if isinstance(value, str) and value.startswith("cranks-dungeons:"):
        require(RESOURCES / "assets/cranks-dungeons" / directory / (value.split(":", 1)[1] + extension), source)


def java_class(name, source):
    # Inner mixins share their enclosing class's source file.
    require(JAVA / (name.split("$", 1)[0].replace(".", "/") + ".java"), source)


documents = {}
for folder in [RESOURCES, ROOT / "src/main/generated"]:
    for path in sorted(folder.rglob("*.json")):
        try:
            documents[path] = json.loads(path.read_text(encoding="utf-8-sig"))
        except (ValueError, UnicodeError) as error:
            errors.append(f"{path.relative_to(ROOT)}: {error}")

for path, data in documents.items():
    source = path.relative_to(ROOT)
    parts = path.relative_to(RESOURCES).parts if path.is_relative_to(RESOURCES) else ()
    if len(parts) > 3 and parts[0] == "assets":
        if parts[2] == "models":
            asset(data.get("parent"), "models", ".json", source)
            for texture in data.get("textures", {}).values():
                asset(texture, "textures", ".png", source)
            for override in data.get("overrides", []):
                asset(override.get("model"), "models", ".json", source)
        elif parts[2] == "particles":
            for texture in data.get("textures", []):
                asset(texture, "textures/particle", ".png", source)
        elif parts[2] == "blockstates":
            def models(node):
                if isinstance(node, dict):
                    asset(node.get("model"), "models", ".json", source)
                    for child in node.values():
                        models(child)
                elif isinstance(node, list):
                    for child in node:
                        models(child)
            models(data)
    if path.name == "sounds.json":
        for event in data.values():
            for sound in event.get("sounds", []):
                if isinstance(sound, dict) and sound.get("type") == "event":
                    continue
                asset(sound.get("name") if isinstance(sound, dict) else sound, "sounds", ".ogg", source)
    if path.name.endswith("mixins.json"):
        package = data.get("package", "")
        for name in data.get("mixins", []) + data.get("client", []) + data.get("server", []):
            java_class(package + "." + name, source)
        if data.get("plugin"):
            java_class(data["plugin"], source)

fabric = documents[RESOURCES / "fabric.mod.json"]
require(RESOURCES / fabric["icon"], "fabric.mod.json")
require(RESOURCES / fabric["accessWidener"], "fabric.mod.json")
for entrypoints in fabric["entrypoints"].values():
    for entrypoint in entrypoints:
        name = entrypoint if isinstance(entrypoint, str) else entrypoint["value"]
        java_class(name.split("::", 1)[0], "fabric.mod.json")

nodes = RESOURCES / "assets/cranks-dungeons/textures/gui/sprites/passive/nodes"
for shape in ["square", "circle", "diamond", "hexagon"]:
    for state in ["locked", "unlocked", "available", "hovered", "drafted", "selection"]:
        for size in ["", "_compact", "_medium", "_small"]:
            require(nodes / f"{shape}_{state}{size}.png", "dynamic tree node renderer")
for state in ["locked", "unlocked", "available", "hovered", "drafted"]:
    require(nodes / f"connection_{state}.png", "dynamic tree connection renderer")

if errors:
    raise SystemExit("\n".join(errors))
print(f"Resource checks passed: {len(documents)} JSON documents, {checks} asset and class references.")
