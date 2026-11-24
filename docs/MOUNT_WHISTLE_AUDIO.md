# Mount whistle audio sources

The three whistle recordings were requested as ARK: Survival Evolved sounds.
They were obtained on 2026-11-20 from the public audio previews on the
101soundboards board **[ASE] ARK: Survival Evolved Game Sounds**, uploaded by
**frenzy**. These are third-party recordings, not original mod audio.

| Mod command | Source clip | Source page |
| --- | --- | --- |
| Come here | PW gen human whistle | https://www.101soundboards.com/sounds/40780586-pw-gen-human-whistle |
| Follow | Whistle2 | https://www.101soundboards.com/sounds/40780722-whistle2 |
| Stay | Whistle1 | https://www.101soundboards.com/sounds/40780720-whistle1 |

The command assignments above are this mod's mapping; the source does not
document which ARK command each recording accompanies. Source attribution does
not place these recordings under the project's code license or establish a
redistribution license.

Processing: remove the source previews' unrelated lead-in audio individually
(Come: first 1.235 s; Follow: first 0.438 s; Stay: first 2.095 s), apply a 6 ms
fade-in and 12 ms fade-out, normalize to -20 LUFS with a -3 dBTP ceiling, and
encode mono 44.1 kHz Ogg Vorbis. The whistle rhythms and pitch are retained.
The sound events use the Players category with 48-block positional attenuation.
Only a successfully accepted command broadcasts audio, from the caller's eye
position; the caller and nearby players receive the same server sound event.
