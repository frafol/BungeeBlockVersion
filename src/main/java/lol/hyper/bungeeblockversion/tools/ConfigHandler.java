/*
 * This file is part of BungeeBlockVersion.
 *
 * BungeeBlockVersion is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BungeeBlockVersion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BungeeBlockVersion.  If not, see <https://www.gnu.org/licenses/>.
 */

package lol.hyper.bungeeblockversion.tools;

import lol.hyper.bungeeblockversion.BungeeBlockVersion;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.protocol.ProtocolConstants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigHandler {

    public Configuration configuration;
    public List<Integer> blockedVersions;
    private final BungeeBlockVersion bungeeBlockVersion;

    public ConfigHandler(BungeeBlockVersion bungeeBlockVersion) {
        this.bungeeBlockVersion = bungeeBlockVersion;
    }

    public void loadConfig() {
        File configFile = new File("plugins" + File.separator + "BungeeBlockVersion", "config.yml");
        if (!configFile.exists()) {
            InputStream is = bungeeBlockVersion.getResourceAsStream("config.yml");
            try {
                File path = new File("plugins" + File.separator + "BungeeBlockVersion");
                if (path.mkdir()) {
                    Files.copy(is, configFile.toPath());
                    bungeeBlockVersion.logger.info("Copying default config...");
                } else {
                    bungeeBlockVersion.logger.warning("Unable to create config folder!");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
            blockedVersions = configuration.getIntList("versions");
            int CONFIG_VERSION = 5;
            if (configuration.getInt("config-version") != CONFIG_VERSION) {
                bungeeBlockVersion.logger.warning("Your config is outdated. We will attempt to load your current config. However, things might not work!");
                bungeeBlockVersion.logger.warning("To fix this, delete your current config and let the server remake it.");
            }
            if (blockedVersions.isEmpty()) {
                bungeeBlockVersion.logger.warning("There are no versions listed in the config! There will be no attempts to block connections.");
                return;
            } else {
                bungeeBlockVersion.logger.info("Loaded " + blockedVersions.size() + " versions!");
            }
            bungeeBlockVersion.logger.info("Loaded versions: " + blockedVersions.stream().map(String::valueOf).collect(Collectors.joining(", ")));
            // use an iterator here so we can remove stuff
            Iterator<Integer> iter = blockedVersions.iterator();
            while (iter.hasNext()) {
                int version = iter.next();
                if (!ProtocolConstants.SUPPORTED_VERSION_IDS.contains(version)) {
                    bungeeBlockVersion.logger.warning("Version " + version + " is NOT a valid version number! Ignoring this version.");
                    iter.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            bungeeBlockVersion.logger.severe("Unable to load configuration file!");
        }
    }
}
