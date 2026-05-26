package com.aura.player.service;

public class ToolResult {
    private final boolean success;
    private final String stdout;
    private final String stderr;

    public ToolResult(boolean success, String stdout, String stderr) {
        this.success = success;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    public boolean isSuccess() { return success; }
    public String getStdout() { return stdout; }
    public String getStderr() { return stderr; }

    public String toDisplay() {
        if (success) {
            return stdout;
        } else {
            return stdout + (stderr.isEmpty() ? "" : "\n[ERROR] " + stderr);
        }
    }
}
