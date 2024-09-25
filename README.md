<div>

<h1 align="center">🏷️ Display Name Tags</h1>

</div>

Replace your players' boring old name tags with customizable ones based on 
text displays!


<p align="center">
    <img width="650px" src=nametags.gif />
</p>


## Configuration

Currently, you can customize default name tags and create grouped name tags.

You can also choose if players get to see their own name tags (Disabled by default).

## API

Designed primarily for developers, the NameTags api gives you lightweight yet
powerful control over how the plugin operates.

You can override default behaviours using the `setDefaultProvider` method, and
the [NameTagEntityCreateEvent](./src/main/java/com/mattmx/nametags/event/NameTagEntityCreateEvent.java)
to hook into a tag's creation. You can add your own features using the 
[Trait](./src/main/java/com/mattmx/nametags/entity/trait/Trait.java) api.

```java

public void onEnable() {
    NameTags nameTags = NameTags.getInstance();
    
    // Override the default "base" settings of a tag.
    nameTags.getEntityManager()
        .setDefaultProvider((entity, meta) -> {
            meta.setText(Component.text(entity.getName()));
            /* ... */
        });
}

```

Here is an example where we can add an Item Display above the player's name tag
by using the `Trait` system.

```java

class MyCustomTrait extends Trait {
    // TODO create example by putting an ItemStack above a name tag.
    
    @Override
    public void onDisable() {
        // Clean up stuff
    }
}

class MyCustomListener implements Listener {
    
    @EventHandler
    public void onTagCreate(@NotNull NameTagEntityCreateEvent event) {
        if (!event.getBukkitEntity().getName().equals("MattMX")) return;
        
        event.getTraits().getOrAddTrait(MyCustomTrait.class, MyCustomTrait::new);
    }
    
}

```

## Roadmap

- `/feat/rel_placeholders`
    Currently the plugin does not support PlaceholderAPI's
    relational placeholders.


- `/feat/align`
    Ability to specify text align.
    
    e.g We want to align `z` on the right side, `y` on the left.

    ```
    |             z         | 
    |         xxxxx         |
    |         y             |
    ```

    We would want to do this by writing `<align:right>z` and `<align:left>`.


- `/feat/customization`
    Extension plugin to give players ability to customize their own
    name tags by using a command and customizable GUI interface.
