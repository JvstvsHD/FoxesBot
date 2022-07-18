## 1.4.5

- Migrate /suppress command to new added member settings (/settings member, similar to /settings channel)
- abstract EntitySettings (currently channel and member settings)
- add /settings extra and eval commands to run some extra functions/external code more convenient and without updating
  the bot

## 1.4.4

- Re-implement presence check (former offline checker): user gets kicked if they join a channel with presence status set
  to invisible (offline)
- Add feature to flush new config content to the file on startup automatically via comparison of config versions

## 1.4.3

- Small change of the /channel settings message

## 1.4.2

- Channel features/settings:
  - /channel settings will send a message where you can activate/deactivate some channel-specific things
  - current existing features are: OneMessage (you can only send one message in this channel) and ChannelBarriers (not
    really working since it's not needed; access to these channels/categories will be restricted)

## 1.4.1

- Further migration ([see 1.4.0](#1.4.0))
- add license (header)
- add renovate for updating dependencies

## 1.4.0

- Migration of database access to [Exposed](https://github.com/JetBrains/Exposed)

Changes below version 1.4 are shortened.

## 1.3.0

- Discord Webhook Logger

## 1.2.1/1.2.2

- re-send deleted messages in countdown-event channels to avoid confusion
- change some messages

## 1.2.0

- Added the [countdown event](https://github.com/JvstvsHD/FoxesBot/blob/master/docs/countdown-event.md)

## 1.1.x

- 2021 christmas event

## 1.0.x

- initial bot release
- migration from Java with JDA to Kotlin with Kord