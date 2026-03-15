package com.example.taskcenter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mattermost")
public class MattermostProperties {

    private String baseUrl = "http://localhost:8065";
    private String botToken = "";
    private String fallbackBotToken = "";
    private String alertChannelId = "4odsfctn8trymycthdk9qafqjr";
    private String alertMentions = "@bot-leader @admin";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBotToken() {
        return botToken;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    public String getFallbackBotToken() {
        return fallbackBotToken;
    }

    public void setFallbackBotToken(String fallbackBotToken) {
        this.fallbackBotToken = fallbackBotToken;
    }

    public String getAlertChannelId() {
        return alertChannelId;
    }

    public void setAlertChannelId(String alertChannelId) {
        this.alertChannelId = alertChannelId;
    }

    public String getAlertMentions() {
        return alertMentions;
    }

    public void setAlertMentions(String alertMentions) {
        this.alertMentions = alertMentions;
    }
}
