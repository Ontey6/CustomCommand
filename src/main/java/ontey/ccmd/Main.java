package ontey.ccmd;

import ontey.api.plugin.OnteyPlugin;
import ontey.ccmd.command.registry.CustomCommandRegistry;

public final class Main extends OnteyPlugin {
    
    public static Main plugin;
    
    public Main() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        load();
    }
}
