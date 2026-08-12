# Version 2.0
Version 2.0 is based on brigadier.
That is the modern command system that Minecraft uses.
It's built so every command is a tree-like structure.
That means you could exactly replicate every Mojang provided command.

Version 2.0 is currently under development. When finished, it will feature:
- ✅ Basic command features like description, permission, console only and disabling the command.
- ✅ **brigadier** commands with (many) preset
- and custom argument types
- ✅ Custom Suggestions
- ✅ Custom requirements
- ✅ Command execution
- ✅ and JavaScript execution
- A plugin reload command
- ✅ More, execution types like TEXT or even
- calling URLs
- Warmups and Cooldowns
- ✅ Commands usable in datapacks
- An extra features addon plugin which will add many utils like clickable items to execute commands
- ✅ Bedrock Compatibility

## Reloadability (/reload support)
I don't plan to make this plugin reloadable using the reload command.
It uses paper's brigadier command registration, which is a lifecycle "event", so you will get an error message when trying to run the reload command.

## Compatibility
The latest versions will always be supported
I will not try to keep a wide compatibility range, but currently usually:
- The base plugin should support 1.21+ fine.
- The extra plugin coming later will only support 1.21.8+ because of dialogs.
  1.20.5+ may work, but the plugin is not intended for that version at all.

## Bedrock Support (GeyserMC)
Commands created by CustomCommand work like any other command on bedrock.
Suggestions are not shown for bedrock players as the bedrock chat doesn't support them.
I will consider adding a work-around with suggestions in the actionbar or the chat (or both).

### Folia Support
I assume the plugin supports Folia.
It does not currently work as I have to explicitly enable folia.
In the next few updates, I will add folia compatibility.

### Velocity Support
Not yet, but it is a priority.

### Spigot, Sponge, BungeeCord and Waterfall Support
BungeeCord and Waterfall are deprecated and not used by anyone anymore, so no.
I don't know Sponge's API, so no.
Spigot is old and doesn't have many users anymore.

# License
ACTUAL LICENSE ON [GITHUB](https://github.com/Ontey6/CustomCommand/blob/master/LICENSE).

# [Wiki](https://github.com/Ontey6/CustomCommand/wiki/Getting-Started-v2)
The wiki is not fully finished yet

# Version 1.0 (v0.1-v0.5.1)
Version 1.0 works without bugs on the latest Version.
It doesn't have brigadier and javascript support.

You can add _commands_, _arguments_, _multiple regex signatures_, _tab completer_ and way more!

The plugin also fully supports _PlaceholderAPI_ and _own placeholders_.

Also, colors don't come short, as there is _legacy_, _mini message_ and and other color support:

`&#RRGGBB` `&/#RRGGBB` `&/e` `<cmd:/cmd>` `<url:modrinth.com>` `<copy:text>`

There is much **customization** for _placeholders_, _actionholders_ and even the _YAML paths_.

This is a great choice for **customization** and **simplicity**, but also **advanced** command making.

The wiki and tutorial is available on [GitHub](https://github.com/Ontey6/CustomCommand/wiki/Getting-Started).

Until **v2.0** comes out, there will still be many changes, but this plugin uses **no deprecated features**, so older versions will still work a very long time.