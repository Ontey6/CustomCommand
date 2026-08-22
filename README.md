# CustomCommand v2
v2 is based on brigadier.
That is the modern command system that Minecraft uses.
It's built so every command is a tree-like structure.
That means you could exactly replicate every Mojang provided command.

v2 is currently under development. I'm not on alpha/beta builds anymore, but there are still many features to add and features to change.
When finished, it will feature:
- ✅ Basic command features like description, permission and console only.
- ✅ brigadier commands with (many) preset
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
I will not try to keep a wide compatibility range, but currently:
- The base plugin should support 1.21+ fine.
- The extra plugin coming later will only support 1.21.8+ because of dialogs.

### Bedrock Support (GeyserMC)
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

### Fabric (Server)
Not yet, I'd love to tho.

### Fabric (Client)
Not yet.
I can imagine that I will make it as soon as the plugin has all features I want to add.

# License
ACTUAL LICENSE ON [GITHUB](https://github.com/Ontey6/CustomCommand/blob/master/LICENSE).

# [Wiki](https://github.com/Ontey6/CustomCommand/wiki/Getting-Started-v2)
The wiki is not fully finished yet

# AI Usage?
- ❌ Vibecoding (fully AI generated files or parts of the project)
- ✅ Debugging (When something doesn't work and I don't get why or am burnt out)
- ❌ Generating text (description, example commands, tutorials)
- ✅ Generating long Markdown tables (I hate them)
- ❌ Correcting my grammar (It ain't that bad, right?)
