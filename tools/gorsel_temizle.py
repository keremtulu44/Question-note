"""Trim the generated artwork: drop the white background, crop to content and export Android densities."""
import sys
from pathlib import Path
from PIL import Image, ImageChops

# Icon display sizes in dp; Android density buckets.
DENSITIES = {
    "mdpi": 1.0,
    "hdpi": 1.5,
    "xhdpi": 2.0,
    "xxhdpi": 3.0,
    "xxxhdpi": 4.0,
}


def temizle(kaynak: Path, hedef_ad: str, dp: int, res_kok: Path, esik: int = 244, kenar: int = 2) -> None:
    img = Image.open(kaynak).convert("RGBA")
    px = img.load()
    w, h = img.size
    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]
            if r >= esik and g >= esik and b >= esik:
                px[x, y] = (r, g, b, 0)
    # Crop to the visible content so every icon fills its box the same way.
    bbox = img.getbbox()
    if bbox:
        img = img.crop(bbox)
    # Square canvas with a small breathing margin.
    yan = max(img.size)
    kare = Image.new("RGBA", (yan, yan), (0, 0, 0, 0))
    kare.paste(img, ((yan - img.size[0]) // 2, (yan - img.size[1]) // 2), img)
    for klasor, olcek in DENSITIES.items():
        boyut = int(round(dp * olcek))
        hedef = res_kok / f"drawable-{klasor}"
        hedef.mkdir(parents=True, exist_ok=True)
        yeniden = kare.resize((boyut, boyut), Image.LANCZOS)
        yeniden.save(hedef / f"{hedef_ad}.png", optimize=True)
    print(f"{hedef_ad}: {kare.size} -> {dp}dp x5")


if __name__ == "__main__":
    kaynak = Path(sys.argv[1])
    ad = sys.argv[2]
    dp = int(sys.argv[3])
    kok = Path(sys.argv[4]) if len(sys.argv) > 4 else Path("app/src/main/res")
    temizle(kaynak, ad, dp, kok)
