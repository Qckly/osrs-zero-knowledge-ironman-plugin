package com.qckly.ironmanguide;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

@PluginDescriptor(
    name = "Zero Knowledge Ironman Guide",
    description = "Step-by-step zero-knowledge progression guide for OSRS Ironman accounts",
    tags = {"ironman", "guide", "progression", "helper", "quest", "skilling"}
)
public class ZeroKnowledgeIronmanPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ConfigManager configManager;

    @Inject
    private ModelOutlineRenderer modelOutlineRenderer;

    @Inject
    private ZeroKnowledgeIronmanConfig config;

    private GuideState guideState;
    private GuideProgressStore progressStore;
    private TutorialStateTracker tutorialStateTracker;
    private ZeroKnowledgeIronmanPanel panel;
    private ZeroKnowledgeIronmanOverlay objectiveOverlay;
    private WorldGuidanceOverlay worldGuidanceOverlay;
    private NavigationButton navigationButton;

    @Provides
    ZeroKnowledgeIronmanConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(ZeroKnowledgeIronmanConfig.class);
    }

    @Override
    protected void startUp()
    {
        guideState = new GuideState(GuideRepository.getSteps());
        progressStore = new GuideProgressStore(configManager);

        // Restore the last stable step first. If live game-state detection is
        // confident, it may correct this below; otherwise this is our fallback.
        String savedStepId = progressStore.loadLastStepId();
        if (savedStepId != null)
        {
            guideState.setCurrentStepId(savedStepId);
        }

        tutorialStateTracker = new TutorialStateTracker(client);
        tutorialStateTracker.refresh();

        syncGuideStateFromGame();

        guideState.addListener(this::persistCurrentStep);
        persistCurrentStep();

        panel = new ZeroKnowledgeIronmanPanel(guideState, tutorialStateTracker);

        objectiveOverlay = new ZeroKnowledgeIronmanOverlay(
            guideState,
            config,
            tutorialStateTracker
        );

        worldGuidanceOverlay = new WorldGuidanceOverlay(
            client,
            guideState,
            config,
            modelOutlineRenderer,
            tutorialStateTracker
        );

        navigationButton = NavigationButton.builder()
            .tooltip("Zero Knowledge Ironman Guide")
            .icon(createIcon())
            .priority(6)
            .panel(panel)
            .build();

        clientToolbar.addNavigation(navigationButton);
        overlayManager.add(objectiveOverlay);
        overlayManager.add(worldGuidanceOverlay);
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (tutorialStateTracker != null)
        {
            tutorialStateTracker.refresh();
            syncGuideStateFromGame();
        }
    }

    private void syncGuideStateFromGame()
    {
        if (guideState == null || tutorialStateTracker == null)
        {
            return;
        }

        String detectedStepId = TutorialStepResolver.resolve(tutorialStateTracker);
        if (detectedStepId != null)
        {
            guideState.setCurrentStepId(detectedStepId);
        }
    }

    private void persistCurrentStep()
    {
        if (guideState == null || progressStore == null)
        {
            return;
        }

        GuideStep step = guideState.getCurrentStep();
        if (step != null)
        {
            progressStore.saveLastStepId(step.getId());
        }
    }

    @Override
    protected void shutDown()
    {
        if (worldGuidanceOverlay != null)
        {
            overlayManager.remove(worldGuidanceOverlay);
        }

        if (objectiveOverlay != null)
        {
            overlayManager.remove(objectiveOverlay);
        }

        if (navigationButton != null)
        {
            clientToolbar.removeNavigation(navigationButton);
        }

        navigationButton = null;
        worldGuidanceOverlay = null;
        objectiveOverlay = null;
        panel = null;
        tutorialStateTracker = null;
        progressStore = null;
        guideState = null;
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
