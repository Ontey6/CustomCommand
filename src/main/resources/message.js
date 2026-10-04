(() => {
  const Component = Java.type("net.kyori.adventure.text.Component")
  const Color = Java.type("net.kyori.adventure.text.format.NamedTextColor")
  //const MiniMessage = Java.type("net.kyori.adventure.text.minimessage.MiniMessage")
  //const Placeholder = Java.type("net.kyori.adventure.text.minimessage.tag.resolver.Placeholder")

  var target = args.get("target").getFirst() // A player argument type always returns a list, so we need to get the first element
  var message = args.get("message")

  // This is the MiniMessage approach to creating components
  //var component = MiniMessage.miniMessage().deserialize(
  //  "<gray>[<reset><sender> <gray>-> <yellow><target><gray>]<reset> <message>",
  //  Placeholder.component("sender", sender.name()),
  //  Placeholder.component("target", target.displayName()),
  //  Placeholder.unparsed("message", message)
  //)

  // This is the raw Component creation approach
  var component = Component
    .text("[", Color.GRAY)
    .append(sender.name().color(Color.WHITE))
    .append(Component.text(" -> ", Color.GRAY))
    .append(target.displayName().color(Color.YELLOW))
    .append(Component.text("] ", Color.GRAY))
    .append(Component.text(message, Color.WHITE))

  if(sender !== target)
    sender.sendMessage(component)
  
  target.sendMessage(component)
})