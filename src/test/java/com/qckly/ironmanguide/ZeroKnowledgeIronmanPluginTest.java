package com.qckly.ironmanguide;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ZeroKnowledgeIronmanPluginTest
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(ZeroKnowledgeIronmanPlugin.class);
        RuneLite.main(args);
    }
}
