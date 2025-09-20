# KarmaSMP

KarmaSMP adds an immersive morality system to your Minecraft server. Your actions change your alignment — Good, Neutral, or Evil — and the world reacts accordingly. You’ll see your karma as a personal BossBar and a small prefix above your name, gain lightweight effects, and even influence mob behavior.

## Highlights
- Alignment system: Good, Neutral, Evil
- Private BossBar showing karma value and alignment
- Above-head name tag prefix (no scoreboard objectives are created)
- Subtle green/red particles by alignment (optional)
- Periodic effects (Good: Regeneration & Speed, Evil: Strength & Speed)
- Hostile mobs (configurable list) ignore Evil players
- Fully configurable thresholds, values, visuals, and effects
- Optional alignment prefix in vanilla chat

## How karma works
Karma is an integer stored per player (UUID). It starts at 0 and changes based on your actions.
- Killing players: karma decreases (default -20)
- Killing villagers: karma decreases (default -15)
- Killing “evil” mobs: karma increases (default +10)

Your alignment depends on thresholds:
- Good: karma ≥ +50
- Evil: karma ≤ -50
- Neutral: otherwise

All numbers and the “evil mob” list are configurable.

## Visuals and effects
- BossBar: Title, color, and progress map to your alignment and karma. Updated on change and periodically.
- Name tag: A short prefix like [Good] / [Neutral] / [Evil] appears above your head via Scoreboard Teams (no objectives used). Optional color.
- Particles: Small colored dust near you (green=Good, red=Evil). Toggleable.
- Potion effects: Applied gently every few seconds to avoid stacking.
- Evil max health: Evil players’ max health can be reduced (default 7 hearts). Configurable.

## Requirements
- Server: Spigot 1.21.8
- Java: 22

## Install
1) Place the JAR into your server’s `plugins/` folder.
2) Start the server to generate `plugins/KarmaSMP/config.yml` and `karma.yml`.
3) Adjust settings in `config.yml` as desired.
4) Use `/karma reload` to apply changes without restarting.

## Commands
Everyone
- `/karma` — Show your current karma and alignment

Admin (permission `karma.admin`)
- `/karma get <player>` — View another player’s karma
- `/karma give <player> <amount>` — Add karma
- `/karma take <player> <amount>` — Remove karma
- `/karma set <player> <amount>` — Set karma exactly
- `/karma reload` — Reload configuration and refresh visuals/effects

Tab completion is provided for subcommands and player names.

## Permissions
- `karma.use` — Default: true (everyone may use `/karma`)
- `karma.view.others` — Default: op
- `karma.admin` — Default: op

## Configuration quick reference
Config file: `plugins/KarmaSMP/config.yml` (created on first run)

- `thresholds.good` — Default: 50
- `thresholds.evil` — Default: -50
- `values.kill_player` — Default: -20
- `values.kill_villager` — Default: -15
- `values.kill_evil_mob` — Default: 10
- `values.max_abs_karma` — Default: 9999 (hard clamp)
- `values.evil_max_health` — Default: 14.0 (7 hearts)
- `interval_seconds` — Default: 5 (how often effects and UI update)
- `display.bossbar_enabled` — Default: true
- `display.name_tag_enabled` — Default: true
- `display.name_color_enabled` — Default: true
- `display.particles_enabled` — Default: true
- `display.chat_enabled` — Default: false (prepend alignment prefix in vanilla chat)
- `display.bossbar_style` — e.g., `SEGMENTED_12`
- `display.bossbar_titles.good|neutral|evil` — BossBar titles; `%value%` = karma
- `display.name_tag.good_prefix|neutral_prefix|evil_prefix` — Prefixes above name (supports `&` color codes)
- `evil_mobs` — List of entity types treated as “evil” for +karma
- `colors.good|neutral|evil` — Default color codes per alignment

After changing, run `/karma reload`.

## Chat plugins
- Name tag prefixes are set via Scoreboard Teams and show above heads. Many chat plugins can include team prefixes in chat lines; consult your chat plugin’s format options.
- If your chat plugin doesn’t include team prefixes, enable `display.chat_enabled: true` to prepend the alignment prefix in vanilla chat formatting.

## Troubleshooting
- No BossBar: Ensure `display.bossbar_enabled: true` and your client HUD is visible.
- Prefix not colored: Ensure `display.name_tag_enabled: true` and `display.name_color_enabled: true`. Other plugins that modify scoreboards may interfere.
- Evil mobs still attack Evil players: Make sure those mobs are listed under `evil_mobs`. If the issue persists for a specific mob, report its exact type.
- Health not reducing for Evil: Check `values.evil_max_health`, then `/karma reload`. Some plugins might modify health attributes; try relogging.

## Privacy & performance
- Karma is stored in `plugins/KarmaSMP/karma.yml` by UUID. Writes are debounced to avoid lag.
- No scoreboard objectives are created or modified; only teams for name tag prefix/color are used.

## License
Provided as-is. Always back up your server before adding new plugins. See [the license](LICENSE.md) for more details.

---

Developers: See [DEVELOPERS.md](DEVELOPERS.md) for build instructions, architecture, and contribution guidelines.
