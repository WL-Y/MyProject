package com.aura.player.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

@Service
public class ToolExecutor {

    public ToolResult executeBash(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
            pb.redirectErrorStream(false);
            pb.environment().put("PATH", System.getenv("PATH") + ":/usr/local/bin:/usr/bin:/bin");

            Process process = pb.start();

            StringBuilder stdout = new StringBuilder();
            StringBuilder stderr = new StringBuilder();

            Thread stdoutThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (stdout.length() > 0) stdout.append("\n");
                        stdout.append(line);
                        if (stdout.length() > 10000) break;
                    }
                } catch (Exception ignored) {}
            });

            Thread stderrThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (stderr.length() > 0) stderr.append("\n");
                        stderr.append(line);
                        if (stderr.length() > 5000) break;
                    }
                } catch (Exception ignored) {}
            });

            stdoutThread.start();
            stderrThread.start();

            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return new ToolResult(false, "", "Command timed out after 30 seconds");
            }

            stdoutThread.join(2000);
            stderrThread.join(2000);

            int exitCode = process.exitValue();
            String out = stdout.toString().trim();
            String err = stderr.toString().trim();

            if (exitCode == 0) {
                return new ToolResult(true, out.isEmpty() ? "(no output)" : out, "");
            } else {
                return new ToolResult(false, out, err.isEmpty() ? "Exit code: " + exitCode : err);
            }

        } catch (Exception e) {
            return new ToolResult(false, "", "Execution error: " + e.getMessage());
        }
    }
}
