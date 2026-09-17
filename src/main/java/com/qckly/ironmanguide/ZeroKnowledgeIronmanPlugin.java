package com.qckly.ironmanguide;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

@PluginDescriptor(
    name = "Zero Knowledge Ironman Guide",
    description = "Step-by-step zero-knowledge progression guide for OSRS Ironman accounts",
    tags = {"ironman", "guide", "progression", "helper", "quest", "skilling"}
)
public class ZeroKnowledgeIronmanPlugin extends Plugin
{
    @Inject
    private ClientToolbar clientToolbar;

    private ZeroKnowledgeIronmanPanel panel;
    private NavigationButton navigationButton;

    @Provides
    ZeroKnowledgeIronmanConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(ZeroKnowledgeIronmanConfig.class);
    }

    @Override
    protected void startUp()
    {
        panel = new ZeroKnowledgeIronmanPanel();
        navigationButton = NavigationButton.builder()
            .tooltip("Zero Knowledge Ironman Guide")
            .icon(createIcon())
            .priority(6)
            .panel(panel)
            .build();

        clientToolbar.addNavigation(navigationButton);
    }

    @Override
    protected void shutDown()
    {
        if (navigationButton != null)
        {
            clientToolbar.removeNavigation(navigationButton);
        }

        navigationButton = null;
        panel = null;
    }

    private static BufferedImage createIcon()
    {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try
        {
            graphics.setColor(new Color(214, 170, 0));
            graphics.fillRoundRect(1, 1, 14, 14, 4, 4);
            graphics.setColor(new Color(40, 40, 40));
            graphics.fillRect(4, 4, 8, 2);
            graphics.fillRect(4, 7, 6, 2);
            graphics.fillRect(4, 10, 8, 2);
        }
        finally
        {
            graphics.dispose();
        }

        return image;
    }
}
