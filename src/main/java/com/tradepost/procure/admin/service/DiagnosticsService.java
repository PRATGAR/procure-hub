package com.tradepost.procure.admin.service;

import com.tradepost.procure.common.ShellCommands;
import com.tradepost.procure.common.ToolUnavailableException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
public class DiagnosticsService {

    public Map<String, Object> ping(String host) {
        List<String> command = ShellCommands.ping(host);
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        try {
            Process process = builder.start();
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return Map.of("host", host, "reachable", false, "output", "timed out");
            }
            String output = new String(process.getInputStream().readNBytes(2048), StandardCharsets.UTF_8);
            return Map.of("host", host, "reachable", process.exitValue() == 0, "output", output);
        } catch (IOException e) {
            throw new ToolUnavailableException("ping is not available");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ToolUnavailableException("ping was interrupted");
        }
    }
}
