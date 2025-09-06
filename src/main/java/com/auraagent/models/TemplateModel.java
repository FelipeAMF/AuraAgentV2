package com.auraagent.models;

public class TemplateModel {
    private String name = "";
    private String spintaxContent = "";
    private int delayInSeconds;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSpintaxContent() { return spintaxContent; }
    public void setSpintaxContent(String spintaxContent) { this.spintaxContent = spintaxContent; }
    public int getDelayInSeconds() { return delayInSeconds; }
    public void setDelayInSeconds(int delayInSeconds) { this.delayInSeconds = delayInSeconds; }
}