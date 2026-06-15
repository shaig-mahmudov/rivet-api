package com.engine.taskmanagement.ai;

import org.springframework.stereotype.Component;

@Component
public class DisabledAiProvider implements AiProvider {

    @Override
    public AiResponse generate(AiRequest request) {
        throw new AiProviderException("AI provider is not configured");
    }
}
