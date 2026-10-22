"""Remove a neutral white background, crop and export compact transparent Android WebP ranks.

Usage: python3 tools/gorsel_temizle.py INPUT NAME 96 [app/src/main/res]
Generated originals are working assets, not committed to the repository.
"""
import sys
from pathlib import Path
from PIL import Image

DENSITIES = {"mdpi": 1.0, "hdpi": 1.5, "xhdpi": 2.0, "xxhdpi": 3.0, "xxxhdpi": 4.0}


def temizle(kaynak: Path, hedef_ad: str, dp: int, res_kok: Path) -> None:
    img = Image.open(kaynak).convert("RGBA")
    pixels = []
    for r, g, b, a in img.getdata():
        # Only neutral whites are removed; colored enamel and gold highlights remain intact.
        low, high = min(r, g, b), max(r, g, b)
        if low >= 240 and high - low <= 12:
            a = round(a * max(0, min(1, (251 - low) / 11)))
        pixels.append((r, g, b, a))
    img.putdata(pixels)
    bbox = img.getbbox()
    if bbox:
        img = img.crop(bbox)
    side = max(img.size)
    margin = round(side * 0.04)
    square = Image.new("RGBA", (side + margin * 2, side + margin * 2))
    square.alpha_composite(img, (margin + (side - img.width) // 2, margin + (side - img.height) // 2))
    for density, scale in DENSITIES.items():
        size = round(dp * scale)
        output = res_kok / f"drawable-{density}" / f"{hedef_ad}.webp"
        output.parent.mkdir(parents=True, exist_ok=True)
        square.resize((size, size), Image.Resampling.LANCZOS).save(output, "WEBP", quality=88, method=6)
    print(f"{hedef_ad}: {dp}dp, 5 WebP densities")


if __name__ == "__main__":
    temizle(Path(sys.argv[1]), sys.argv[2], int(sys.argv[3]),
            Path(sys.argv[4]) if len(sys.argv) > 4 else Path("app/src/main/res"))
