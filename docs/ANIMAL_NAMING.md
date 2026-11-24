# Animal naming

The naming panel uses the shared dark journal frame, ornaments, field frame and
buttons. It focuses the field immediately, supports Enter to confirm, retains
typed text across resizing and exposes the field directly for InvMove's typing
detection. Name length is capped at 64 characters on both sides of the network.

Successful first taming opens a free naming prompt for horse-style tames (horses,
donkeys, mules and llamas) and TameableEntity pets (wolves, cats and parrots).
Existing custom names are preserved. Camels, pigs and striders keep their current
name-tag-based claiming method, as requested; there is no new taming interaction.

Free naming applies only to that initial prompt. Skip, Escape or replacing the
screen cancels its token. Later naming requires a name tag. The animal persists
`CranksInitialNamingOffered`, so saving, reloading, releasing and retaming do not
renew the opportunity. Transfer and loading do not trigger new prompts.

Invitations wait for an existing menu to close. Up to 16 can be queued per player;
invitations expire after five minutes. A free invitation is bound server-side to
the player, the animal's UUID and dimension, and is consumed once. Submission
checks ownership, life, distance (16 blocks), expiry, an absent custom name and
the name's validity. The client cannot request a free rename by setting a flag.
No item is consumed for the free name, including when a name tag is held.

Paid renaming retains main/offhand handling, normal interaction reach and Creative
exemption. Both owned pets and mounts reject non-owner naming outside Creative.
Rejected or cancelled paid requests do not consume a tag.

Build and regression checks passed. An isolated copy of the user's complete
testing modpack exercised seven native taming species, free naming, paid renaming,
skip/replay rejection, other-player and wrong-animal token rejection, NBT marker
persistence, retaming, existing names, unchanged custom mount claiming and payload
roundtrips. Client checks covered automatic prompts, initial keyboard focus,
typing and Enter confirmation with empty hands, offhand tag consumption, waiting
for another menu and cancellation by closing the prompt.

Logs: `build/animal-naming-build.log` and `build/animal-naming-integration.log`.
In-game captures: `build/animal-naming-previews/`.
