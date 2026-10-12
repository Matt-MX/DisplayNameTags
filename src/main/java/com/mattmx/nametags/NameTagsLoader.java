package com.mattmx.nametags;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

@SuppressWarnings("UnstableApiUsage")
public class NameTagsLoader implements PluginLoader {

    public static final String ENTITYLIB_VERSION = "3.3.7-SNAPSHOT";
    public static final String ENTITYLIB_REPOSITORY = "https://maven.pvphub.me/tofaa";

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        classpathBuilder.getContext().getLogger().info("Injecting dependencies");

        // File to override version
        final File override = classpathBuilder.getContext()
            .getDataDirectory()
            .resolve(".override")
            .toFile();

        String entityLibVersion = ENTITYLIB_VERSION;
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
                "pvphub-tofaa",
                "default",
                ENTITYLIB_REPOSITORY
            )
                .setPolicy(new RepositoryPolicy(
                    true,
                    RepositoryPolicy.UPDATE_POLICY_NEVER,
                    RepositoryPolicy.CHECKSUM_POLICY_WARN
                ))
                .build()
        );
        resolver.addDependency(
            new Dependency(
                new DefaultArtifact("io.github.tofaa2:spigot:" + entityLibVersion),
                null
            ).setOptional(false)
        );

        classpathBuilder.addLibrary(resolver);
    }

}
