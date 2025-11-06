package com.alexwan.csci310_team36_projectd.data;
public abstract class Reminder {
    protected boolean isActive;

    public Reminder() {
        this.isActive = false;
    }

    public boolean isActive() {
        return isActive;
    }

    public void activate() {
        isActive = true;
    }

    public void deactivate() {
        isActive = false;
    }

    public abstract void setReminder();
    public abstract void cancelReminder();
}
