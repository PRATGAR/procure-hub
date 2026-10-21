package com.tradepost.procure.document.service;

import com.tradepost.procure.common.CommandResult;
import com.tradepost.procure.common.ProcessRunner;
import com.tradepost.procure.document.ConvertProfile;
import com.tradepost.procure.document.Document;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component("txt")
public class TextConverter implements DocumentConverter {

    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    private final ProcessRunner runner;

    public TextConverter(ProcessRunner runner) {
        this.runner = runner;
    }

    @Override
    public CommandResult convert(Document document, String prefix) {
        return runner.run(ConvertProfile.TEXT.argv(document.id()), TIMEOUT);
    }
}
