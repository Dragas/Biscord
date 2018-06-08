# Biscord


[![pipeline status](https://gitlab.com/Dragas/Biscord/badges/master/pipeline.svg)](https://gitlab.com/Dragas/Biscord/commits/master)

Also known as Jeeves, The tavern keeper

An implementation of `Chatty-Discord` provided at [Chatty](https://github.com/Dragas/Chatty)

## Building

Prior doing anything, be sure you have `JAVA_HOME` environmental variable pointing to your JDK.

Build application using the either `:distZip` or `:installDist` Gradle tasks like so:
```
./gradlew :distZip
```
Then binaries appear in either `./build/distributions` or `./build/install` folder.

## Deployment

Application is run using `bin/biscord` command. It requires following environmental variables:
```
DISCORD_KEY - discord API key
RAW_LINK_REGEX - Regex pattern, which is used to test messages for HTTP links
DATABASE_URL - URI for database access. Should include username, password, host and database.
OWNER_ID - Owner's user snowflake id in discord platform
office_id - Channel's snowflake id, where reports about deleted messages are posted.
debug_id - Channel's snowflake id, where error reports are posted.
HSG - Server's snowflake id, where the messages are tested against `RAW_LINK_REGEX` environmental variable.
```
Application by default requires PostgreSQL 9.6. This can be changed at `./src/main/resources/hibernate.cfg.xml`