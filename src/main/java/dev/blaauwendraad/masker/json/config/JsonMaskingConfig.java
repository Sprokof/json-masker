package dev.blaauwendraad.masker.json.config;

import dev.blaauwendraad.masker.json.path.JsonPath;
import dev.blaauwendraad.masker.json.path.JsonPathParser;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Contains the JSON masker configurations.
 */
public class JsonMaskingConfig {
    /**
     * Specifies the set of JSON keys for which the string/number values should be targeted (either masked or allowed,
     * depending on the configured {@link JsonMaskingConfig#targetKeyMode}.
     */
    private final Set<String> targetKeys;
    /**
     * The target key mode specifies how to the JSON properties corresponding to the target keys are processed.
     */
    private final TargetKeyMode targetKeyMode;
    /**
     * Specifies the set of JSON paths for which the string/number values should be masked.
     */
    private final Set<JsonPath> targetJsonPaths;
    /**
     * Specifies the algorithm type that will be used for masking.
     */
    private final JsonMaskerAlgorithmType algorithmType;

    /**
     * @see JsonMaskingConfig.Builder#maskNumberValuesWith
     */
    private final int maskNumericValuesWith;
    /**
     * @see JsonMaskingConfig.Builder#obfuscationLength(int)
     */
    private final int obfuscationLength;
    /**
     * @see JsonMaskingConfig.Builder#caseSensitiveTargetKeys
     */
    private final boolean caseSensitiveTargetKeys;

    JsonMaskingConfig(Builder builder) {
        Set<String> targets = builder.targets;
        algorithmType = JsonMaskerAlgorithmType.KEYS_CONTAIN;
        obfuscationLength = builder.obfuscationLength;
        targetKeyMode = builder.targetKeyMode;
        if (targetKeyMode == TargetKeyMode.MASK && targets.isEmpty()) {
            throw new IllegalArgumentException("Target keys set in mask mode must contain at least a single target key");
        }
        caseSensitiveTargetKeys = builder.caseSensitiveTargetKeys;
        maskNumericValuesWith = builder.maskNumberValuesWith;
        if (builder.maskNumberValuesWith == 0) {
            if (builder.obfuscationLength < 0 || builder.obfuscationLength > 1) {
                throw new IllegalArgumentException(
                        "Mask number values with can only be 0 if obfuscation length is 0 or 1 to preserve valid JSON");
            }
        } else {
            if (builder.maskNumberValuesWith != -1 && (builder.maskNumberValuesWith < 1
                    || builder.maskNumberValuesWith > 9)) {
                throw new IllegalArgumentException(
                        "Mask number values with must be a digit between 1 and 9 when length obfuscation is disabled or obfuscation length is larger than than 0");
            }
        }
        if (builder.obfuscationLength == 0 && !(builder.maskNumberValuesWith == 0
                || builder.maskNumberValuesWith == -1)) {
            throw new IllegalArgumentException(
                    "If obfuscation length is set to 0, numeric values are replaced with a single 0, so mask number values with must be 0 or number masking must be disabled");
        }

        if (builder.resolveJsonPaths) {
            JsonPathParser jsonPathParser = new JsonPathParser();
            targetJsonPaths = targets.stream()
                    .filter(t -> t.startsWith("$"))
                    .map(jsonPathParser::parse)
                    .collect(Collectors.toSet());
            targetKeys = targets.stream()
                    .filter(t -> !t.startsWith("$"))
                    .collect(Collectors.toSet());
        } else {
            targetJsonPaths = Collections.emptySet();
            targetKeys = targets;
        }
    }

    public static JsonMaskingConfig getDefault(Set<String> targets) {
        return custom(targets, TargetKeyMode.MASK).build();
    }

    /**
     * Creates a new {@link JsonMaskingConfig} builder instance.
     *
     * @param targets       target keys of JSONPaths
     * @param targetKeyMode how to interpret the targets set
     * @return the {@link JsonMaskingConfig} builder instance
     */
    public static JsonMaskingConfig.Builder custom(Set<String> targets, TargetKeyMode targetKeyMode) {
        return new JsonMaskingConfig.Builder(targets, targetKeyMode);
    }

    public JsonMaskerAlgorithmType getAlgorithmType() {
        return algorithmType;
    }

    /**
     * Which number to mask numeric JSON values with (e.g. with value 8, the JSON property 1234 will be masked as
     * 8888).
     *
     * @return the number mask
     */
    public int getMaskNumericValuesWith() {
        return maskNumericValuesWith;
    }

    /**
     * Tests if numeric JSON values are masked
     *
     * @return true if number masking is enabled and false otherwise.
     */
    public boolean isNumberMaskingEnabled() {
        return maskNumericValuesWith != -1;
    }

    public TargetKeyMode getTargetKeyMode() {
        return targetKeyMode;
    }

    public Set<String> getTargetKeys() {
        return targetKeys;
    }

    public Set<JsonPath> getTargetJsonPaths() {
        return targetJsonPaths;
    }

    /**
     * Get the obfuscation length configuration value.
     *
     * @return the length of the mask to use for all values to obfuscate the original value length, or -1 if length
     * obfuscation is disabled.
     */
    public int getObfuscationLength() {
        return obfuscationLength;
    }

    /**
     * Tests if length obfuscation is enabled.
     *
     * @return true if length obfuscation is enabled and false otherwise
     */
    public boolean isLengthObfuscationEnabled() {
        return obfuscationLength != -1;
    }

    /**
     * Tests if target keys should be considered case-sensitive.
     *
     * @return true if target keys are considered case-sensitive and false otherwise.
     */
    public boolean caseSensitiveTargetKeys() {
        return caseSensitiveTargetKeys;
    }

    /**
     * Builder to create {@link JsonMaskingConfig} instances using the builder pattern.
     */
    public static class Builder {
        private final Set<String> targets;
        private final TargetKeyMode targetKeyMode;
        private int maskNumberValuesWith;
        private boolean resolveJsonPaths;
        
        private int obfuscationLength;
        private boolean caseSensitiveTargetKeys;

        public Builder(Set<String> targets, TargetKeyMode targetKeyMode) {
            // targets can be either target keys or target JSON paths
            this.targets = targets;
            this.targetKeyMode = targetKeyMode;
            // by default, mask number values with is -1 which means number value masking is disabled
            this.maskNumberValuesWith = -1;
            // by default, JSON paths are resolved, every target starting with "$." is interpreted as a JSONPath
            this.resolveJsonPaths = true;
            // by default, length obfuscation is disabled
            this.obfuscationLength = -1;
            // by default, target keys are considered case-insensitive
            this.caseSensitiveTargetKeys = false;
        }

        /**
         * Specifies the number with which numeric values should be replaced. -1 denotes number masking is disabled.
         * <p>
         * In case maskNumericValuesWith is set to 0, obfuscationLength must be set to either 0 or 1 in which both case
         * numbers are replaced with a single 0. This is in order to preserve valid JSON.
         * <p>
         * Default value: -1 (numeric values are not masked)
         *
         * @param maskNumericValuesWith the number to mask numeric JSON properties with
         * @return the builder instance
         */
        public Builder maskNumericValuesWith(int maskNumericValuesWith) {
            this.maskNumberValuesWith = maskNumericValuesWith;
            return this;
        }

        /**
         * @param obfuscationLength specifies the fixed length of the mask when target value lengths is obfuscated. E.g.
         *                          masking any string value with obfuscation length 2 results in "**".
         *                          <p>
         *                          In case obfuscationLength is set to 0 and number masking is enabled, it must be 0
         *                          (i.e. each numeric value is replaced with a single 0 because JSON does not support
         *                          empty values).
         *                          <p>
         *                          Default value: -1 (length obfuscation disabled).
         * @return the builder instance
         */
        public Builder obfuscationLength(int obfuscationLength) {
            this.obfuscationLength = obfuscationLength;
            return this;
        }

        /**
         * Configures whether the target keys are considered case-sensitive (e.g. cvv != CVV)
         * <p>
         * Default value: false (target keys are considered case-insensitive)
         *
         * @return the builder instance
         */
        public Builder caseSensitiveTargetKeys() {
            this.caseSensitiveTargetKeys = true;
            return this;
        }

        /**
         * Disables that target keys starting with a '$' are interpreted as JSON paths
         * <p>
         * Default value: true (JSON path resolving is enabled)
         *
         * @return the builder instance
         */
        public Builder disableJsonPathResolving() {
            this.resolveJsonPaths = false;
            return this;
        }

        /**
         * Creates a new {@link JsonMaskingConfig} instance.
         *
         * @return the new instance
         */
        public JsonMaskingConfig build() {
            return new JsonMaskingConfig(this);
        }
    }

    /**
     * Defines how target keys should be interpreted.
     */
    public enum TargetKeyMode {
        /**
         * In this mode, target keys are interpreted as the only JSON keys for which the corresponding property is
         * allowed (should not be masked).
         */
        ALLOW,
        /**
         * In the mode, target keys are interpreted as the only JSON keys for which the corresponding property should be
         * masked.
         */
        MASK
    }

    /**
     * Checks if the target key mode is set to "ALLOW". If the mode is set to "ALLOW", it means that the target keys are
     * interpreted as the only JSON keys for which the corresponding property is allowed (should not be masked).
     *
     * @return true if the target key mode is set to "ALLOW", false otherwise
     */
    public boolean isInAllowMode() {
        return targetKeyMode == TargetKeyMode.ALLOW;
    }

    /**
     * Checks if the target key mode is set to "MASK". If the mode is set to "MASK", it means that the properties
     * corresponding to the target keys should be masked.
     *
     * @return true if the current target key mode is in "MASK" mode, false otherwise
     */
    public boolean isInMaskMode() {
        return targetKeyMode == TargetKeyMode.MASK;
    }

    @Override
    public String toString() {
        return "JsonMaskingConfig{" +
                "targetKeys=" + targetKeys +
                ", targetKeyMode=" + targetKeyMode +
                ", algorithmType=" + algorithmType +
                ", maskNumericValuesWith=" + maskNumericValuesWith +
                ", obfuscationLength=" + obfuscationLength +
                ", caseSensitiveTargetKeys=" + caseSensitiveTargetKeys +
                '}';
    }
}
