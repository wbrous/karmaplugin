# KarmaSMP — Developer Guide

This document explains the code structure, build instructions, configuration keys, and contribution guidelines for KarmaSMP.

## Build
- Toolchain: Java 22, Maven
- API: Spigot/Bukkit 1.21.8 (spigot-api, scope=provided)

Commands:
- `mvn -q -DskipTests package`
- Output JAR in `target/`

## Project structure
```
src/main/java/org/gir0fa/karmaPlugin/
  KarmaPlugin.java                # Main plugin class (enable, reload, wiring)
  model/Alignment.java            # GOOD / NEUTRAL / EVIL
  util/ConfigKeys.java            # Config path constants & helpers
  storage/
    KarmaStorage.java             # Storage interface
    YamlKarmaStorage.java         # YAML persistence (debounced writes)
  service/
    KarmaService.java             # Core domain logic: thresholds, clamping, evil mob list, listeners
  display/
    BossBarManager.java           # Per-player boss bars; title/color/progress mapping
    NameTagManager.java           # Scoreboard teams for prefixes/colors only (no objectives)
    ParticlesManager.java         # Subtle alignment particle emission
  listeners/
    KarmaListener.java            # Kills, joins/quits, respawn, targeting rules
    ChatListener.java             # Optional vanilla chat prefix injection
  scheduler/
    KarmaEffectsTask.java         # Periodic effects + UI refresh
    IronGolemHuntTask.java        # Iron golem hunting behavior for evil players
  commands/
    KarmaCommand.java             # /karma get/give/take/set/reload + tab completion
src/main/resources/
  plugin.yml
  config.yml
```

## Key behaviors
- Karma clamped to ± `values.max_abs_karma` (default 9999)
- Alignment thresholds (`thresholds.good`, `thresholds.evil`)
- Evil mob list from `evil_mobs` used for +karma and targeting ignore
- BossBar progress: centered at 0.5; mapped to thresholds independently
- Name tags via Scoreboard Teams; no scoreboard objectives created
- Particles optional and subtle; avoid spam
- EVIL max health configurable via `values.evil_max_health`
- Iron golems actively hunt Evil players: runs every 2 seconds, scans 16-block radius
- Debounced YAML writes; avoid blocking main thread

## Configuration keys (summary)
- thresholds.good (int), thresholds.evil (int)
- values.kill_player, values.kill_villager, values.kill_evil_mob (ints)
- values.max_abs_karma (int)
- values.evil_max_health (double)
- interval_seconds (int)
- display.bossbar_enabled, name_tag_enabled, name_color_enabled, particles_enabled, chat_enabled (booleans)
- display.bossbar_style (string; BarStyle name)
- display.bossbar_titles.good|neutral|evil (strings; `%value%` supported)
- display.name_tag.good_prefix|neutral_prefix|evil_prefix (strings; `&` color codes)
- evil_mobs (string list of EntityType)
- colors.good|neutral|evil (strings; `&` codes or color names)

## Extending
- Add new karma sources: create a listener for the relevant event and call `karmaService.addKarma(uuid, delta)`.
- New visuals: add a manager under `display/` and wire in `KarmaPlugin`.
- Storage backends: implement `KarmaStorage`, register in `KarmaPlugin`.
- New mob behaviors: create a scheduler task similar to `IronGolemHuntTask` for custom mob AI.

## Coding conventions
- Java 22; clear variable names; short, readable methods
- Prefer early returns; handle edge cases first
- Keep managers isolated (UI logic) and services pure (domain logic)
- Avoid blocking the main thread; use scheduler for debouncing or async when safe

## Testing locally
- Build JAR and drop into a Spigot 1.21.8 dev server
- Verify: kills adjust karma; BossBar/title/progress; name tag prefixes; effects/particles; `/karma` commands; `/karma reload`
- Test iron golem hunting: become Evil alignment and spawn iron golems nearby; they should actively target you

## Contributions
- Open issues/PRs with clear descriptions and reproduction steps
- Match existing style; keep changes focused; include config docs as needed

## License
- See repository [license](LICENSE) file
