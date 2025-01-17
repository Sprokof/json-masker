package dev.blaauwendraad.masker.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import dev.blaauwendraad.masker.json.config.JsonMaskingConfig;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class JsonMaskerTestUtil {
    private JsonMaskerTestUtil() {
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<JsonMaskerTestInstance> getJsonMaskerTestInstancesFromFile(String fileName) throws IOException {
        List<JsonMaskerTestInstance> testInstances = new ArrayList<>();
        ArrayNode jsonArray = mapper.readValue(
                JsonMaskerTestUtil.class.getClassLoader().getResource(fileName),
                ArrayNode.class
        );
        for (JsonNode jsonNode : jsonArray) {
            JsonNode jsonMaskingConfig = jsonNode.findValue("maskingConfig");
            JsonMaskingConfig.Builder builder = JsonMaskingConfig.builder();
            if (jsonMaskingConfig != null) {
                applyConfig(jsonMaskingConfig, builder);
            }
            JsonMaskingConfig maskingConfig = builder.build();
            var input = jsonNode.get("input").toString();
            if (jsonNode.get("input").isTextual() && jsonNode.get("input").textValue().startsWith("file://")) {
                URL resourceUrl = JsonMaskerTestUtil.class.getClassLoader()
                        .getResource(jsonNode.get("input").textValue().replace("file://", ""));
                try {
                    input = Files.readString(Path.of(Objects.requireNonNull(resourceUrl).toURI()));
                } catch (URISyntaxException e) {
                    throw new IOException("Cannot read file " + resourceUrl, e);
                }
            }
            var expectedOutput = jsonNode.get("expectedOutput").toString();
            if (jsonNode.get("expectedOutput").isTextual() && jsonNode.get("expectedOutput")
                    .textValue()
                    .startsWith("file://")) {
                URL resourceUrl = JsonMaskerTestUtil.class.getClassLoader()
                        .getResource(jsonNode.get("expectedOutput").textValue().replace("file://", ""));
                try {
                    expectedOutput = Files.readString(Path.of(Objects.requireNonNull(resourceUrl).toURI()));
                } catch (URISyntaxException e) {
                    throw new IOException("Cannot read file " + resourceUrl, e);
                }
            }
            testInstances.add(new JsonMaskerTestInstance(
                    input,
                    expectedOutput,
                    new KeyContainsMasker(maskingConfig)
            ));
        }
        return testInstances;
    }

    private static void applyConfig(JsonNode jsonMaskingConfig, JsonMaskingConfig.Builder builder) {
        jsonMaskingConfig.fields().forEachRemaining(e -> {
            String key = e.getKey();
            JsonNode value = e.getValue();
            switch (key) {
                case "maskKeys" -> builder.maskKeys(asSet(value, JsonNode::asText));
                case "maskJsonPaths" -> builder.maskJsonPaths(asSet(value, JsonNode::asText));
                case "allowKeys" -> builder.allowKeys(asSet(value, JsonNode::asText));
                case "allowJsonPaths" -> builder.allowJsonPaths(asSet(value, JsonNode::asText));
                case "caseSensitiveTargetKeys" -> {
                    if (value.booleanValue()) {
                        builder.caseSensitiveTargetKeys();
                    }
                }
                case "maskStringsWith" -> builder.maskStringsWith(value.textValue());
                case "maskStringCharactersWith" -> builder.maskStringCharactersWith(value.textValue());
                case "disableNumberMasking" -> {
                    if (value.booleanValue()) {
                        builder.disableNumberMasking();
                    }
                }
                case "maskNumbersWith" -> {
                    if (value.isInt()) {
                        builder.maskNumbersWith(value.intValue());
                    } else {
                        builder.maskNumbersWith(value.textValue());
                    }
                }
                case "maskNumberDigitsWith" -> builder.maskNumberDigitsWith(value.intValue());
                case "disableBooleanMasking" -> {
                    if (value.booleanValue()) {
                        builder.disableBooleanMasking();
                    }
                }
                case "maskBooleansWith" -> {
                    if (value.isBoolean()) {
                        builder.maskBooleansWith(value.booleanValue());
                    }
                    builder.maskBooleansWith(value.textValue());
                }
                default -> throw new IllegalArgumentException("Unknown option " + key);
            }
        });
    }

    private static <T> Set<T> asSet(JsonNode value, Function<JsonNode, T> mapper) {
        return StreamSupport.stream(value.spliterator(), false).map(mapper).collect(Collectors.toSet());
    }
}
