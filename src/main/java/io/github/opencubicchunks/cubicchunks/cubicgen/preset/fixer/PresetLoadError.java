package io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer;

public class PresetLoadError extends RuntimeException {

    public PresetLoadError() {
    }

    public PresetLoadError(String message) {
        super(message);
    }

    public PresetLoadError(String message, Throwable cause) {
        super(message, cause);
    }

    public PresetLoadError(Throwable cause) {
        super(cause);
    }

    public PresetLoadError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
