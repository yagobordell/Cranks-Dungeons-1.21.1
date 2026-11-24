"""Original bronze mint icon on the same pixel grid as the inventory UI."""
from pathlib import Path
import runpy

ROOT = Path(__file__).resolve().parents[1]
Canvas = runpy.run_path(str(ROOT / 'scripts/generate_menu_theme.py'))['Canvas']
coin = Canvas(16, 16)
shape = {(x, y) for y in range(2, 14) for x in range(2, 14)
         if (x - 7.5) ** 2 + (y - 7.5) ** 2 <= 36}
for x, y in shape:
    edge = any((x + dx, y + dy) not in shape for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
    if edge:
        rgb = (184, 119, 74) if x + y < 13 else (77, 48, 32) if x + y > 18 else (121, 74, 44)
    else:
        rgb = (187, 123, 78) if x + y < 14 else (156, 94, 55) if x + y < 20 else (130, 74, 44)
    coin.rect(x, y, 1, 1, (*rgb, 255))
# An inset C mint mark, with a small lower-right bevel rather than a floating symbol.
mark = [(7, 5), (8, 5), (6, 6), (6, 7), (6, 8), (7, 9), (8, 9)]
for x, y in mark:
    coin.rect(x + 1, y + 1, 1, 1, (194, 129, 80, 255))
for x, y in mark:
    coin.rect(x, y, 1, 1, (91, 54, 34, 255))
coin.save(ROOT / 'src/main/resources/assets/cranks-dungeons/textures/gui/wallet_coin.png')
print('Generated bronze wallet coin')
