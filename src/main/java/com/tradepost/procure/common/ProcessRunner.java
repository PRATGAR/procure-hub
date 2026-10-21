package com.tradepost.procure.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class ProcessRunner {

    private static final int MAX_OUTPUT = 8192;

    public CommandResult run(List<String> command, Duration timeout) {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        try {
            Process process = builder.start();
            boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                return new CommandResult(-1, "timed out");
            }
            byte[] captured = process.getInputStream().readNBytes(MAX_OUTPUT);
            return new CommandResult(process.exitValue(), new String(captured, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ToolUnavailableException("the external tool could not be started");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ToolUnavailableException("the external tool was interrupted");
        }
    }
}
