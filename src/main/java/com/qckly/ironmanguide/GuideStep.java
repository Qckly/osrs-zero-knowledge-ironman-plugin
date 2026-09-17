package com.qckly.ironmanguide;

public final class GuideStep
{
    private final String id;
    private final String chapter;
    private final String title;
    private final String instruction;
    private final String why;

    public GuideStep(String id, String chapter, String title, String instruction, String why)
    {
        this.id = id;
        this.chapter = chapter;
        this.title = title;
        this.instruction = instruction;
        this.why = why;
    }

    public String getId()
    {
        return id;
    }

    public String getChapter()
    {
        return chapter;
    }

    public String getTitle()
    {
        return title;
    }

    public String getInstruction()
    {
        return instruction;
    }

    public String getWhy()
    {
        return why;
    }
}
