# CustomCommand v2
<div style="text-align: center;">

**A custom command creation plugin using brigadier.**
<br><br>
<a href="https://github.com/Ontey6/CustomCommand/wiki"><img alt="ghpages" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/documentation/ghpages_vector.svg"></a>
<a href="https://modrinth.com/plugin/ccmd/gallery"><img alt="modrinth-gallery" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/documentation/modrinth-gallery_vector.svg"></a>
<a href="https://discord.gg/g8jdgT7Ve"><img alt="discord-singular" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/social/discord-singular_vector.svg"></a>
<a href="https://bstats.org/plugin/bukkit/ccmd/33295"><img alt="bstats" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/bstats_vector.svg"></a>
<br>
<img alt="paper" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact-minimal/supported/paper_vector.svg">
<img alt="purpur" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact-minimal/supported/purpur_vector.svg">
<img alt="spigot" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact-minimal/unsupported/spigot_vector.svg">
<img alt="bungeecord" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact-minimal/unsupported/bungeecord_vector.svg">

1.21-26.2 · Bedrock compatible · Datapack compatible · Javascript execution · **No** AI Code* · Automatic Updater
</div>

# Early stages of development
v2 is currently under development. I'm not on alpha/beta builds anymore, but there are still many features to add and features to change.
When finished, it will feature:
- ✅ basic command features like description, permission, disabling and console only.
- ✅ preset argument types
- and custom argument types (for re-using and de-duplication)
- ✅ custom suggestions, requirements and execution
- a plugin reload command
- ✅ more execution types like TEXT or even
- calling URLs easily
- warmups and cooldowns
- ✅ commands usable in datapacks (functions)
- an extra features addon plugin which will add many utils like clickable items to execute commands

# Reloadability (/reload support)
I don't plan to make this plugin reloadable using the reload command.
It uses paper's brigadier command registration, which is a lifecycle "event", so you will get an error message when trying to run the reload command.

# Compatibility
## Minecraft Versions
The latest versions will always be supported
I will not try to keep a wide compatibility range, but currently:
- The base plugin should support 1.21+ fine.
- The extra plugin coming later will only support 1.21.8+ because of dialogs.

## Bedrock Support (GeyserMC)
Commands created by CustomCommand work like any other command on bedrock.
Suggestions are not shown for bedrock players as the bedrock chat doesn't support them.
I will consider adding a work-around with suggestions in the actionbar or the chat (or both).

## Folia Support
I assume the plugin supports Folia.
It does not currently work as I have to explicitly enable it.
In the next few updates, I will add folia compatibility.

### Velocity Support
Not yet, but it is a priority.

## Fabric (Server)
Not yet, I'd love to tho.

## Fabric (Client)
Not yet.
I can imagine that I will make it as soon as the plugin has all features I want to add.

# *: AI Usage?
- ❌ Vibecoding/Agentic Engineering (fully AI generated files or parts of the project)
- ✅ Debugging
- ❌ Generating text (description, example commands, tutorials)
- ✅ Generating long Markdown tables
- ❌ Correcting my grammar

# Statistics
My test servers are not included.

![bStats Servers](https://img.shields.io/bstats/servers/33295?color=blue&style=flat-square)
![bStats Players](https://img.shields.io/bstats/players/33295?color=green&style=flat-square)