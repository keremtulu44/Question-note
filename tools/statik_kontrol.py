"""Fast, dependency-free resource checks. Does not replace Kotlin compilation or Android tests."""
from pathlib import Path
import re
import sys
from xml.etree import ElementTree as ET

root = Path(__file__).resolve().parents[1]
res = root / "app/src/main/res"
strings = ET.parse(res / "values/strings.xml").getroot()
names = [node.attrib.get("name") for node in strings if node.attrib.get("name")]
errors = []
if len(names) != len(set(names)):
    errors.append("Duplicate string/plural name")
drawables = {p.stem for folder in res.glob("drawable*") for p in folder.iterdir() if p.is_file()}
for file in (root / "app/src/main/java").rglob("*.kt"):
    text = file.read_text()
    for name in re.findall(r"R\.(?:string|plurals)\.(\w+)", text):
        if name not in names:
            errors.append(f"{file.relative_to(root)}: missing string {name}")
    for name in re.findall(r"R\.drawable\.(\w+)", text):
        if name not in drawables:
            errors.append(f"{file.relative_to(root)}: missing drawable {name}")

ranks = {p.stem for p in (res / "drawable-mdpi").glob("rank_*.webp")}
for density in ("mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi"):
    actual = {p.stem for p in (res / f"drawable-{density}").glob("rank_*.webp")}
    if actual != ranks:
        errors.append(f"Incomplete rank artwork at {density}")

for error in errors:
    print(error)
print(f"{len(names)} resources, {len(ranks)} rank emblems × 5 densities; {len(errors)} error(s).")
sys.exit(bool(errors))
