import com.github.shynixn.petblocks.bukkit.addon.buffeffects.api.entity.BuffEffect;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.github.shynixn.petblocks.bukkit.addon.buffeffects.api.controller.BuffEffectController;
import com.github.shynixn.petblocks.bukkit.addon.buffeffects.logic.controller.PotionEffectController;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PotionEffectParsingIT {

    @Test
    public void onParseFromFile() throws IOException, InvalidConfigurationException {
        File file = new File("src/main/resources/config.yml");

        Plugin plugin = mock(Plugin.class);

        FileConfiguration configuration = new YamlConfiguration();
        configuration.load(file);

        when(plugin.getConfig()).thenReturn(configuration);

        BuffEffectController effectController = new PotionEffectController(plugin);

        effectController.reload();
        Assertions.assertEquals(2, effectController.size());
        Assertions.assertNotNull(effectController.getPotionEffectsFromEngine(1));
        Assertions.assertEquals(Optional.empty(), effectController.getPotionEffectsFromEngine(3));
        final BuffEffect[] effects = effectController.getPotionEffectsFromEngine(1).get();
        Assertions.assertEquals(2, effects.length);

        BuffEffect effect = effects[0];
        Assertions.assertEquals(0.0,effect.getAmplifier());
        Assertions.assertEquals(999999,effect.getDuration());
    }
}
