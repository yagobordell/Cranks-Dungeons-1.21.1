# Recipe discovery notifications

The vanilla recipe-book toast is suppressed. A compact ornamented dark toast in
the same top-right notification area shows the discovered recipe, output icon
and station. Discoveries close together merge into one notification with a count.
At most three examples are shown, newest first. An incoming batch goes ahead of
older examples. Small batches show their page indicator; larger batches show
`+X more` for discoveries outside the three examples. Examples advance once and
hold on the last entry instead of cycling back through old recipes. The normal
notification display-time accessibility setting still applies. Advancement and
other toast types retain their normal behavior.

Notifications follow the owner's server-synchronized item discovery component
and use the same ingredient visibility checks as the station lists. One known
alternative satisfies an ingredient; every ingredient must be discovered.
Inventory, bags and hotbar acquisition count through the existing discovery
system. Custom assembly stations, hand/table crafting, cutting, cooking and
alchemy extraction are included. Salvage-only foundry entries are excluded.
Station recipes and notifications share display-name and icon helpers, including
banner patterns and cartography operations.

Potion formulas are announced only when the owner's alchemy knowledge records a
successful experiment. Obtaining potion ingredients and failed experiments do
not announce a learned potion. Notification code does not unlock recipes, change
inventory or grant any knowledge.

Both discovery components must receive their initial data before a baseline is
built. Login, respawn, dimension changes and recipe catalogue replacement establish
a silent baseline, preventing history replay. Later component revisions are
compared against seen recipe IDs. Repeated pickups do not repeat announcements.
Disconnect and session/catalogue changes invalidate queued old notifications.
Recipe checks run only when discovery/knowledge revisions or the recipe catalogue
change, rather than scanning every recipe every frame. The existing component
sync is private to its owner; no additional server-wide unlock messages are sent.
