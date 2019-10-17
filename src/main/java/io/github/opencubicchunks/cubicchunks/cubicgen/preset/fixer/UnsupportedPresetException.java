package io.github.opencubicchunks.cubicchunks.cubicgen.preset.fixer;

public class UnsupportedPresetException extends PresetLoadError {

    public UnsupportedPresetException() {
    }

    public UnsupportedPresetException(String message) {
        super(message);
    }

    public UnsupportedPresetException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedPresetException(Throwable cause) {
        super(cause);
    }

    public UnsupportedPresetException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
