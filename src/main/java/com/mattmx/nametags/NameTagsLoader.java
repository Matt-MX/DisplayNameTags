package com.mattmx.nametags;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

@SuppressWarnings("UnstableApiUsage")
public class NameTagsLoader implements PluginLoader {

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        classpathBuilder.getContext().getLogger().info("Injecting dependencies");

        // File to override version
        final File override = classpathBuilder.getContext()
            .getDataDirectory()
            .resolve(".override")
            .toFile();

        String entityLibVersion = "+1f4aeef-SNAPSHOT";
        if (override.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(override))) {
                entityLibVersion = reader.readLine();
            } catch (Exception error) {
                error.printStackTrace();
            }
        }

        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(
            new RemoteRepository.Builder(
                "evoke-games",
                "default",
                "https://maven.evokegames.gg/snapshots"
            ).build()
        );
        resolver.addDependency(
            new Dependency(
                new DefaultArtifact("me.tofaa.entitylib:spigot:" + entityLibVersion),
                null
            ).setOptional(false)
        );

        classpathBuilder.addLibrary(resolver);
    }

}
