"""Generate original, pixel-aligned menu widgets and a standalone 1.21.1 UI pack.

No game artwork is copied. Rectangles and orthogonal paths extend the mod's
black, silver and charcoal UI palette. Run from any directory with Python 3.
"""
from pathlib import Path
import json
import struct
import zlib
import zipfile

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/minecraft/textures/gui'
BLACK = (0, 0, 0, 255)


class Canvas:
    def __init__(self, width, height, background=(0, 0, 0, 0)):
        self.width, self.height = width, height
        self.pixels = [[background for _ in range(width)] for _ in range(height)]

    def rect(self, x, y, width, height, color):
        for py in range(max(0, y), min(self.height, y + height)):
            for px in range(max(0, x), min(self.width, x + width)):
                self.pixels[py][px] = color

    def path(self, points, color):
        for (ax, ay), (bx, by) in zip(points, points[1:]):
            assert ax == bx or ay == by, 'UI paths must follow the pixel grid'
            self.rect(min(ax, bx), min(ay, by), abs(bx - ax) + 1, abs(by - ay) + 1, color)

    def save(self, path):
        path.parent.mkdir(parents=True, exist_ok=True)
        def chunk(kind, data):
            return struct.pack('!I', len(data)) + kind + data + struct.pack('!I', zlib.crc32(kind + data))
        data = b''.join(b'\0' + bytes(channel for pixel in row for channel in pixel) for row in self.pixels)
        path.write_bytes(b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('!2I5B', self.width, self.height, 8, 6, 0, 0, 0))
                         + chunk(b'IDAT', zlib.compress(data, 9)) + chunk(b'IEND', b''))


def gray(value, alpha=255):
    return (value, value, value, alpha)


def framed(width, height, highlighted=False, disabled=False, ornament=True, surface=None):
    c = Canvas(width, height, BLACK)
    rail = gray(72 if disabled else 185 if highlighted else 128)
    c.rect(1, 1, width - 2, height - 2, rail)
    c.rect(2, 2, width - 4, height - 4, BLACK)
    c.rect(3, 3, width - 6, height - 6, gray(surface if surface is not None else 18 if disabled else 40 if highlighted else 26))
    if ornament and width >= 16 and height >= 16:
        # One closed rail: detailing individual corners with padded endpoints
        # would cut a pixel out of the adjoining straight borders.
        # Start transparent: only the rail's one-pixel halo should be black.
        # An opaque rectangular base would leave square corners outside the steps.
        c = Canvas(width, height)
        c.rect(3, 3, width - 6, height - 6,
               gray(surface if surface is not None else 18 if disabled else 40 if highlighted else 26))
        points = [(1, height - 2), (1, 5), (3, 5), (3, 3), (5, 3), (5, 1),
                  (width - 2, 1), (width - 2, height - 6),
                  (width - 4, height - 6), (width - 4, height - 4),
                  (width - 6, height - 4), (width - 6, height - 2), (1, height - 2)]
        for (ax, ay), (bx, by) in zip(points, points[1:]):
            c.rect(min(ax, bx) - 1, min(ay, by) - 1, abs(bx - ax) + 3, abs(by - ay) + 3, BLACK)
        c.path(points, rail)
    return c


def save_widget(name, canvas, border=None):
    file = ASSETS / 'sprites/widget' / (name + '.png')
    canvas.save(file)
    if border is not None:
        file.with_suffix('.png.mcmeta').write_text(json.dumps({'gui': {'scaling': {
            'type': 'nine_slice', 'width': canvas.width, 'height': canvas.height, 'border': border
        }}}, indent=2) + '\n', encoding='utf-8')


def world_join_arrow(highlighted=False, marked=False):
    """Transparent play glyph; marked variants leave room for vanilla warnings."""
    c = Canvas(32, 32)
    offset = 6 if marked else 0
    # A deliberate coarse pixel grid keeps every diagonal step and outline the
    # same weight. A solid glyph reads more cleanly at this size than a tiny frame.
    pixels = (
        '##',
        '#s#',
        '#ss#',
        '#sss#',
        '#ssss#',
        '#sssss#',
        '#ssss#',
        '#sss#',
        '#ss#',
        '#s#',
        '##',
    )
    for y, row in enumerate(pixels):
        for x, pixel in enumerate(row):
            c.rect(10 + offset + x * 2, 5 + y * 2, 2, 2,
                   BLACK if pixel == '#' else gray(240 if highlighted else 170))
    return c


def server_reorder_arrow(highlighted=False, down=False):
    c = Canvas(32, 32)
    rows = ('     #     ', '    #s#    ', '   #sss#   ', '  #sssss#  ',
            ' #sssssss# ', '#sssssssss#', '###########')
    for y, row in enumerate(reversed(rows) if down else rows):
        for x, pixel in enumerate(row):
            if pixel != ' ':
                c.rect(3 + x, (20 if down else 5) + y, 1, 1,
                       BLACK if pixel == '#' else gray(240 if highlighted else 170))
    return c


def generate():
    for state in ('', '_highlighted', '_disabled'):
        save_widget('button' + state, framed(200, 20, state == '_highlighted', state == '_disabled'),
                    {'left': 7, 'right': 7, 'top': 7, 'bottom': 7})
    for state in ('', '_highlighted'):
        lit = bool(state)
        save_widget('slider' + state, framed(200, 20, lit, ornament=False, surface=12), 3)
        handle = framed(8, 20, lit, ornament=False)
        handle.rect(3, 7, 2, 6, gray(185 if lit else 128))
        save_widget('slider_handle' + state, handle, 3)
        save_widget('text_field' + state, framed(200, 20, lit, ornament=False, surface=20), 3)
    for state in ('', '_selected', '_highlighted', '_selected_highlighted'):
        selected, lit = 'selected' in state, 'highlighted' in state
        c = framed(130, 24, lit or selected, surface=38 if selected else 22)
        if selected:
            c.rect(7, 21, 116, 1, gray(185))
        save_widget('tab' + state, c, 7)
    for name in ('scroller', 'scroller_background'):
        c = framed(6, 32, ornament=False, surface=24)
        if name == 'scroller':
            c.rect(2, 3, 2, 26, gray(128))
            c.rect(2, 14, 2, 1, gray(185))
            c.rect(2, 17, 2, 1, gray(185))
        save_widget(name, c, 2)
    for state in ('', '_highlighted', '_selected', '_selected_highlighted'):
        c = framed(20, 20, 'highlighted' in state)
        if 'selected' in state:
            for x, y in [(6, 9), (7, 10), (8, 11), (9, 10), (10, 9), (11, 8), (12, 7), (13, 6)]:
                c.rect(x, y, 1, 2, gray(224))
        save_widget('checkbox' + state, c)
    for locked in (True, False):
        for state in ('', '_highlighted', '_disabled'):
            c = framed(20, 20, state == '_highlighted', state == '_disabled')
            color = gray(72 if state == '_disabled' else 200)
            c.path([(6, 10), (6, 15), (13, 15), (13, 10), (6, 10)], color)
            c.path([(8 if locked else 11, 9), (8 if locked else 11, 6), (11 if locked else 14, 6), (11 if locked else 14, 9)], color)
            c.rect(9, 12, 1, 2, color)
            save_widget(('locked' if locked else 'unlocked') + '_button' + state, c)
    for state in ('', '_highlighted'):
        lit = bool(state)
        c = framed(14, 14, lit, ornament=False)
        for i in range(6):
            c.rect(4 + i, 4 + i, 1, 1, gray(224 if lit else 170))
            c.rect(9 - i, 4 + i, 1, 1, gray(224 if lit else 170))
        save_widget('cross_button' + state, c)
    for marked in (False, True):
        for highlighted in (False, True):
            name = ('marked_join' if marked else 'join') + ('_highlighted' if highlighted else '')
            world_join_arrow(highlighted, marked).save(ASSETS / 'sprites/world_list' / (name + '.png'))
    for highlighted in (False, True):
        suffix = '_highlighted' if highlighted else ''
        world_join_arrow(highlighted, marked=True).save(ASSETS / 'sprites/server_list' / ('join' + suffix + '.png'))
        for down in (False, True):
            name = ('move_down' if down else 'move_up') + suffix
            server_reorder_arrow(highlighted, down).save(ASSETS / 'sprites/server_list' / (name + '.png'))
    # Vanilla uses this as a stretched selection frame. Keep its interior empty.
    c = Canvas(80, 80)
    for x, y, w, h in [(0, 0, 80, 3), (0, 77, 80, 3), (0, 3, 3, 74), (77, 3, 3, 74)]:
        c.rect(x, y, w, h, BLACK)
    c.path([(1, 78), (1, 1), (78, 1), (78, 78), (1, 78)], gray(185))
    save_widget('slot_frame', c, 3)
    for name in ('menu_background', 'menu_list_background', 'inworld_menu_background', 'inworld_menu_list_background'):
        alpha = 230 if name.startswith('inworld') else 255
        base = 15 if 'list' in name else 23
        c = Canvas(16, 16, gray(base, alpha))
        # Deliberately quiet texture: no noisy high-contrast dirt or stone pattern.
        for x, y in [(1, 3), (9, 0), (12, 11), (5, 14)]:
            c.rect(x, y, 2, 1, gray(base + 2, alpha))
        c.save(ASSETS / (name + '.png'))
    for name in ('header_separator', 'footer_separator', 'inworld_header_separator', 'inworld_footer_separator'):
        c = Canvas(32, 2, BLACK)
        c.rect(0, 0 if 'footer' in name else 1, 32, 1, gray(105))
        c.save(ASSETS / (name + '.png'))
    files = sorted(ASSETS.rglob('*'))
    output = ROOT / 'build/distributions/Cranks-Dungeons-Dark-UI-1.21.1.zip'
    output.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(output, 'w', zipfile.ZIP_DEFLATED) as pack:
        pack.writestr('pack.mcmeta', json.dumps({'pack': {'pack_format': 34, 'description': 'Cranks Dungeons · Dark UI'}}))
        for file in files:
            if file.is_file():
                pack.write(file, file.relative_to(ROOT / 'src/main/resources').as_posix())
    print(f'Generated menu theme and {output}')


if __name__ == '__main__':
    generate()
