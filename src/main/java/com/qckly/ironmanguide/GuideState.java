package com.qckly.ironmanguide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GuideState
{
    private final List<GuideStep> steps;
    private final List<Runnable> listeners = new ArrayList<>();
    private int currentIndex;

    public GuideState(List<GuideStep> steps)
    {
        this.steps = steps;
        this.currentIndex = 0;
    }

    public GuideStep getCurrentStep()
    {
        if (steps.isEmpty())
        {
            return null;
        }

        return steps.get(currentIndex);
    }

    public GuideStep getStep(int index)
    {
        if (index < 0 || index >= steps.size())
        {
            return null;
        }
        return steps.get(index);
    }

    public List<GuideStep> getSteps()
    {
        return Collections.unmodifiableList(steps);
    }

    public int getCurrentIndex()
    {
        return currentIndex;
    }

    public int getStepCount()
    {
        return steps.size();
    }

    public boolean hasPrevious()
    {
        return currentIndex > 0;
    }

    public boolean hasNext()
    {
        return currentIndex < steps.size() - 1;
    }

    public void previous()
    {
        setCurrentIndex(currentIndex - 1);
    }

    public void next()
    {
        setCurrentIndex(currentIndex + 1);
    }

    public void setCurrentIndex(int index)
    {
        if (steps.isEmpty())
        {
            return;
        }

        int clamped = Math.max(0, Math.min(index, steps.size() - 1));
        if (clamped == currentIndex)
        {
            return;
        }

        currentIndex = clamped;
        notifyListeners();
    }

    public void addListener(Runnable listener)
    {
        listeners.add(listener);
    }

    private void notifyListeners()
    {
        for (Runnable listener : listeners)
        {
            listener.run();
        }
    }
}
