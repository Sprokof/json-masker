package dev.blaauwendraad.masker.json;

import com.fasterxml.jackson.databind.JsonNode;
import dev.blaauwendraad.masker.json.config.JsonMaskingConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import randomgen.json.RandomJsonGenerator;
import randomgen.json.RandomJsonGeneratorConfig;

import java.util.Set;

final class FuzzingTest {

    @ParameterizedTest
    @ValueSource(ints = {100})
        // number of tests
    void fuzzTestNoFailuresKeyContainsAlgorithm(int amountOfTests) {
        for (int i = 0; i < amountOfTests; i++) {
            Set<String> targetKeys = Set.of("targetKey1", "targetKey2");
            KeyContainsMasker keyContainsMasker = new KeyContainsMasker(JsonMaskingConfig.getDefault(targetKeys));
            RandomJsonGenerator randomJsonGenerator =
                    new RandomJsonGenerator(RandomJsonGeneratorConfig.builder().createConfig());
            JsonNode randomJsonNode = randomJsonGenerator.createRandomJsonNode();
            Assertions.assertDoesNotThrow(() -> keyContainsMasker.mask(randomJsonNode.toPrettyString()),
                                          randomJsonNode.toPrettyString());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {100})
        // number of tests
    void fuzzTestNoFailuresSingleTargetLoopAlgorithm(int amountOfTests) {
        for (int i = 0; i < amountOfTests; i++) {
            Set<String> targetKeys = Set.of("targetKey1", "targetKey2");
            JsonMasker singleTargetMasker = new SingleTargetMasker(JsonMaskingConfig.getDefault(targetKeys));
            RandomJsonGenerator randomJsonGenerator =
                    new RandomJsonGenerator(RandomJsonGeneratorConfig.builder().createConfig());
            JsonNode randomJsonNode = randomJsonGenerator.createRandomJsonNode();
            Assertions.assertDoesNotThrow(() -> singleTargetMasker.mask(randomJsonNode.toPrettyString()),
                                          randomJsonNode.toPrettyString());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {100})
        // number of tests
    void fuzzTestTwoTargets(int amountOfTests) {
        for (int i = 0; i < amountOfTests; i++) {
            Set<String> targetKeys = Set.of("targetKey1", "targetKey2");
            JsonMasker keyContainsMasker = new KeyContainsMasker(JsonMaskingConfig.getDefault(targetKeys));
            JsonMasker singleTargetMasker = new SingleTargetMasker(JsonMaskingConfig.getDefault(targetKeys));
            RandomJsonGenerator randomJsonGenerator =
                    new RandomJsonGenerator(RandomJsonGeneratorConfig.builder().createConfig());
            JsonNode randomJsonNode = randomJsonGenerator.createRandomJsonNode();
            String randomJsonNodeString = randomJsonNode.toPrettyString();
            String keyContainsOutput = keyContainsMasker.mask(randomJsonNodeString);
            String singleTargetOutput = singleTargetMasker.mask(randomJsonNodeString);
            String jacksonMaskingOutput = ParseAndMaskUtil.mask(randomJsonNode, targetKeys, JsonMaskingConfig.TargetKeyMode.MASK).toPrettyString();
            Assertions.assertEquals(keyContainsOutput, singleTargetOutput, "Failed for input: " + randomJsonNodeString);
            Assertions.assertEquals(jacksonMaskingOutput,
                                    keyContainsOutput,
                                    "Failed for input: " + randomJsonNodeString);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {100})
        // number of tests
    void fuzzTestAllowKeys(int amountOfTests) {
        for (int i = 0; i < amountOfTests; i++) {
            Set<String> targetKeys = Set.of("targetKey1", "targetKey2");
            JsonMasker keyContainsMasker = new KeyContainsMasker(JsonMaskingConfig.custom(targetKeys, JsonMaskingConfig.TargetKeyMode.ALLOW).build());
            RandomJsonGenerator randomJsonGenerator =
                    new RandomJsonGenerator(RandomJsonGeneratorConfig.builder().createConfig());
            JsonNode randomJsonNode = randomJsonGenerator.createRandomJsonNode();
            String randomJsonNodeString = randomJsonNode.toPrettyString();
            String keyContainsOutput = keyContainsMasker.mask(randomJsonNodeString);
            String jacksonMaskingOutput = ParseAndMaskUtil.mask(randomJsonNode, targetKeys, JsonMaskingConfig.TargetKeyMode.ALLOW).toPrettyString();
            Assertions.assertEquals(jacksonMaskingOutput,
                                    keyContainsOutput,
                                    "Failed for input: " + randomJsonNodeString);
        }
    }
}