package ontey.ccmd;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import lombok.NonNull;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

public class CustomCommandLoader implements PluginLoader {
	
	@Override
	public void classloader(@NonNull PluginClasspathBuilder classpathBuilder) {
		MavenLibraryResolver resolver = new MavenLibraryResolver();
		resolver.addDependency(new Dependency(new DefaultArtifact("org.graalvm.js:js-language:25.0.3"), null));
		resolver.addRepository(new RemoteRepository.Builder("paper", "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR).build());
		
		classpathBuilder.addLibrary(resolver);
	}
}
