# Currency and nearby trading

Coins are a server-owned, per-world wallet keyed by player UUID. They have no item
form and survive death, respawning, dimension changes and reconnecting. Only the
owner receives their balance. A compact framed panel beside the equipped bags
shows a bronze coin and the balance; large amounts are abbreviated, with the
exact value on hover. Clicking the wallet opens the trading dock alongside the
normal inventory, bags and stats.

## Rewards

Combat coins use the existing combat-XP encounter eligibility and contribution
rules, including active party support. Natural eligible enemies pay once;
spawner/command/egg/trial-spawner mobs, passive creatures, automated kills and idle
party members do not earn coins. The unmodified base mob XP is used only as a
configurable starting scale for reward amounts. XP bonuses and party XP bonuses
do not multiply coins. One fixed total is allocated between contributing groups,
then split equally within each group, with integer remainders distributed between
members. Default reward: rounded base XP times 0.6, bounded to 1–10,000 coins.

Generated loot chests, barrels, shulker containers and chest minecarts may have
personal coin rewards. Each player claims a generated container once, on opening;
its items still use shared vanilla storage until the personal-loot rework. The
default coin roll is 10–30 per player. Claims and generated-container identity
persist; reopening, sorting, hoppers generating item loot first, unloading and
reloading cannot reset them. A double chest may contain two generated loot pools.
Player-placed ordinary containers never create rewards. Containers already looted
before this update have no reliable generation history and receive no retroactive
reward; unopened old loot-table containers can still be identified. Breaking a
container does not put a coin reward into its dropped item.

`config/cranks-dungeons/economy.json` controls rewards, trade distance (default
three blocks) and invitation timeout (default 30 seconds); restart to reload.
`declinedRequestSeconds` defaults to 30: declining an invitation prevents that
sender from requesting another trade with you during this interval.
Amounts are provisional until NPC prices and services are introduced. There are
no NPC shops, profession requirements, coin drop items or death penalties here.

## Player trading

1. Click the wallet and select a nearby player, in the same dimension and with
   clear line of sight. The recipient opens their inventory to accept or cancel.
2. Click an inventory stack to offer it; right-click offers one. Up to nine
   different stacks fit on each side. Click your offered stack to remove it, or
   right-click to remove one. Equipped gear and equipped bags must first be moved
   into normal storage. Offers never remove items early or protect bag contents
   from death.
3. Type a coin amount and press Set or Enter; Ready also applies the typed amount.
   Both offers show exact, unabridged coin amounts, even when very large. Hover
   the input to see your available balance. Both barter and coin-only payments work.
4. Both players mark Ready and Confirm. Edit, changed offers or changed source
   items invalidate approvals. Current counts, components, balances, range and
   receiving space are checked again on the server before completing anything.

Closing the trade, opening another container, dying, disconnecting, moving out of
reach or losing line of sight cancels it without moving the offered items. Death
then uses the existing inventory rules, including dropping bag-storage contents.
Coins and items are exchanged in one server-thread operation after simulating both
resulting inventories. Item components, counts, empty slots and the ten-slot
hotbar are preserved. No item is dropped to make space during trading.

The trade browser's **Incoming requests: On/Off** control is a personal preference
saved with the player, including through death and reconnects. Turning it off
declines a pending incoming invitation; outgoing requests and active trades are
unaffected. The server enforces this preference and the decline cooldown.

Quick coin rewards accumulate in one notification until 2.5 seconds pass without
another reward. The wallet still updates immediately for every reward.

Completed trades have a short entry in the server log and a detailed JSON record
under the world's `data/cranks-dungeons/trades/YYYY-MM-DD.jsonl` (UTC dates).
Records contain the transaction ID, time, player names and UUIDs, exact amounts,
before/after balances and exchanged item counts/components. Cancelled and rejected
trades are not recorded as completed. These records are for server administration;
they never determine balances or replay trades. A detailed-log write failure is
reported in the server log and does not undo an already committed exchange.

The economy ledger is `data/cranks_dungeons_economy.dat` in the world. Before an
exchange, a durable journal records both resulting inventories and balances.
Player inventories receive transaction markers and are saved before the journal
is cleared. A reconnect completes an interrupted inventory save; an acknowledged
transaction is not replayed over later inventory changes. Failed saves retain the
journal and disconnect the affected player for recovery rather than allowing
play against an unsaved exchange. Back up the whole world and its player data
together.

`/coins` shows your balance. Operators can use `/coins give <player> <amount>` and
`/coins set <player> <amount>` for testing and administration. Balances use whole
coins, capped at 9,000,000,000,000,000; clients cannot award themselves money.

The original bronze icon is generated by `python scripts/generate_wallet_icon.py`.
