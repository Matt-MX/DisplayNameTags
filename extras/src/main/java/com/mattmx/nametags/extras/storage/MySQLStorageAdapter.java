package com.mattmx.nametags.extras.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mattmx.nametags.extras.BukkitUtils;
import com.mattmx.nametags.extras.NameTagsExtras;
import com.mattmx.nametags.extras.schema.CustomNameTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

public class MySQLStorageAdapter implements NameTagStorageAdapter {
    private final @NotNull NameTagsExtras plugin;
    public static final String IDENTIFIER = "mysql";
    private static final String TABLE_NAME = "tags";
    private static final String NAME_TAG_COLUMN_NAME = "nameTag";
    private static final Gson gson = new GsonBuilder().create();

    public Connection sql;

    public MySQLStorageAdapter(@NotNull NameTagsExtras plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        final String connectionString = plugin.getConfig().getString("storage.connection-string");

        try {
            Objects.requireNonNull(connectionString, "Missing connection-string in config.yml!");

            sql = DriverManager.getConnection(connectionString);


            final PreparedStatement createTable = sql.prepareStatement("""
                CREATE TABLE IF NOT EXISTS tags(
                    id VARCHAR(255) NOT NULL,
                    nameTag TEXT DEFAULT NULL,
                    PRIMARY KEY(id)
                )
                """);

            createTable.execute();
        } catch (NullPointerException | SQLException ex) {
            plugin.getLogger().log(Level.WARNING, "Failed to connect to MySQL server: {}", ex.getMessage());
        }
    }

    @Override
    public void setNameTag(@NotNull String id, @Nullable CustomNameTag nameTag) {
        BukkitUtils.ensureNotOnPrimaryThread("setNameTag");

        final String json = nameTag == null
            ? null
            : gson.toJson(nameTag);

        try (
            final PreparedStatement insertOrUpdate = sql.prepareStatement("""
                INSERT INTO tags
                VALUES(?, ?)
                ON DUPLICATE KEY UPDATE
                nameTag = ?
                """)
        ) {
            insertOrUpdate.setString(1, id);
            insertOrUpdate.setString(2, json);
            insertOrUpdate.setString(3, json);

            insertOrUpdate.execute();
        } catch (SQLException ex) {
            this.plugin.getLogger().log(Level.WARNING, "Unable to save data for {}: {}", new Object[]{id, ex.getMessage()});
        }
    }

    @Override
    public @NotNull Optional<CustomNameTag> getNameTag(@NotNull String id) {
        BukkitUtils.ensureNotOnPrimaryThread("setNameTag");

        try (
            final PreparedStatement insertOrUpdate = sql.prepareStatement("SELECT nameTag FROM tags WHERE id = ?")
        ) {
            insertOrUpdate.setString(1, id);

            ResultSet set = insertOrUpdate.executeQuery();

            if (set.next()) {
                final CustomNameTag nameTag = gson.fromJson(set.getString(NAME_TAG_COLUMN_NAME), CustomNameTag.class);
                return Optional.ofNullable(nameTag);
            } else return Optional.empty();

        } catch (SQLException | JsonParseException ex) {
            this.plugin.getLogger().log(Level.WARNING, "Unable to retrieve data for {}: {}", new Object[]{id, ex.getMessage()});
        }
        return Optional.empty();
    }

    @Override
    public void stop() {
        try {
            this.sql.close();
            this.sql = null;
        } catch (SQLException ex) {
            this.plugin.getLogger().log(Level.WARNING, "Unable to close MySQL connection: {}", ex.getMessage());
        }
    }

    @Override
    public boolean isConnected() {
        try {
            return this.sql != null && !this.sql.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
