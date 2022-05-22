# FoxesBot

This bot is developed for the [Chilling Foxes Discord](https://discord.gg/K5rhddJtyW).
If you have discovered a bug, please [create an issue here](https://github.com/JvstvsHD/FoxesBot/issues).

Suggestions of improvements are always welcome!

## Exposed database

This branch exists as a rework of all database operations by using [Exposed](https://github.com/JetBrains/Exposed).
It should also serve as the removal of [init.sql](src/main/resources/init.sql)

## Changelog

- 18.05.2022: created this branch
- 19.05.2022: Added Exposed dependencies and updated other dependencies (including MariaDB)
- 21.05.2022: Implement Exposed DataBase connection establishment, deprecate Database (project class) and migrate the
  modules core & status to Exposed
- 22.05.2022: Migrate the event module and some other stuff excluding the offline checker module (which needs a rewrite)
  to Exposed and changed some other things