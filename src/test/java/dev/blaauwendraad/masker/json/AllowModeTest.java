package dev.blaauwendraad.masker.json;

import dev.blaauwendraad.masker.json.config.JsonMaskingConfig;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

final class AllowModeTest {

    @ParameterizedTest
    @MethodSource("testAllowMode")
    void targetKeyAllowMode(JsonMaskerTestInstance testInstance) {
        assertThat(testInstance.jsonMasker().mask(testInstance.input())).isEqualTo(testInstance.expectedOutput());
    }

    private static Stream<JsonMaskerTestInstance> testAllowMode() throws IOException {
        return JsonMaskerTestUtil.getJsonMaskerTestInstancesFromFile("test-allow-mode.json").stream();
    }

    @ParameterizedTest
    @MethodSource("targetKeyAllowModeNotPretty")
    void targetKeyAllowModeNotPretty(JsonMaskerTestInstance testInstance) {
        Assertions.assertThat(testInstance.jsonMasker().mask(testInstance.input()))
                .isEqualTo(testInstance.expectedOutput());
    }

    private static Stream<JsonMaskerTestInstance> targetKeyAllowModeNotPretty() {
        JsonMasker jsonMasker = JsonMasker.getMasker(JsonMaskingConfig.builder()
                .allowKeys(Set.of("allowedKey"))
                .build());
        return Stream.of(
                new JsonMaskerTestInstance("""
                         [                                                                                      
                          {
                            "allowedKey": "yes",
                            "notAllowedKey": "hello"
                          }
                        ]
                        """, """
                         [                                                                                      
                          {
                            "allowedKey": "yes",
                            "notAllowedKey": "***"
                          }
                        ]
                        """, jsonMasker
                ),
                new JsonMaskerTestInstance("""
                            [
                              false,
                              "value2",
                              123,
                              false,
                              null,
                              {
                                "allowedKey": "yes",
                                "notAllowedKey": "hello"
                              },
                              "value3"
                            ]
                        """, """
                            [
                              "&&&",
                              "***",
                              "###",
                              "&&&",
                              null,
                              {
                                "allowedKey": "yes",
                                "notAllowedKey": "***"
                              },
                              "***"
                            ]
                        """, jsonMasker
                ),
                new JsonMaskerTestInstance("""
                            [
                              "value1",
                              "value2",
                              123,
                              false,
                              null,
                              {
                                "allowedKey": "yes",
                                "notAllowedKey": "hello"
                              },
                              "value3"
                            ]
                        """, """
                            [
                              "***",
                              "***",
                              "###",
                              "&&&",
                              null,
                              {
                                "allowedKey": "yes",
                                "notAllowedKey": "***"
                              },
                              "***"
                            ]
                        """, jsonMasker
                ),
                new JsonMaskerTestInstance("""
                        ["value",{}]
                        """, """
                        ["***",{}]
                        """, jsonMasker
                ),
                new JsonMaskerTestInstance("""
                        [ {     "_@":  [   [  "This is a random value",{    "allowedKey": "This is allowed" }  ]  ,    [   "This is a random value",  {
                                    
                                      "allowedKey":   "This is allowed" }    ] ,     {   "allowedKey":    "This is allowed" }]  },   [   {
                                     "allowedKey":  "This is allowed" }    ,  {
                                         "_+<#?[&":  [ "This is a random value",  {
                                    
                                     "allowedKey":  "This is allowed" }    ]   ,"<%*^&()?":   {
                                    
                                       "allowedKey":   "This is allowed" }    ,   "%-%[ ;=!}    )(":  {
                                    
                                     "allowedKey":    "This is allowed" }  , "?":    {
                                     "allowedKey": "This is allowed" }     }   ,     {   "allowedKey":    "This is allowed" }],  {
                                    
                                     "allowedKey":    "This is allowed" }  ,{
                                    
                                     "*()_$-?(": [   {
                                    
                                        "allowedKey":   "This is allowed" } ] ,  "%=>@^}   [  -&": {
                                     ";=":   {
                                       "allowedKey": "This is allowed" }    , "[ ^,  ^[  .*":  {
                                      "allowedKey":  "This is allowed" }    ,   "$]  {  (?%<":    {
                                       "allowedKey":    "This is allowed" } ,    "allowedKey":   "This is allowed" }  }    , {
                                      "}    =!.^$: ]":    {
                                    
                                         "allowedKey":  "This is allowed", "-:   ?|_!&;@": {
                                    
                                     "allowedKey":  "This is allowed" }     },  "allowedKey":  "This is allowed",    "+,   )>[ [ =;":  {
                                       "^@.$,  *": {   "allowedKey":  "This is allowed" }  ,     "_[  [  =": [  "This is a random value",     {
                                     "allowedKey": "This is allowed" } ],   "+>":  [   "This is a random value", {
                                        "allowedKey": "This is allowed" }    ]   }  ,   "} >+]    |_(^":   {
                                        "?}-}    -*+!":    {
                                      "allowedKey":    "This is allowed" } , "*.:   }<^{    *&%":   [   "This is a random value",   {
                                    
                                         "allowedKey":  "This is allowed" }    ] }     }  ] \s
                        """, """
                        [ {     "_@":  [   [  "***",{    "allowedKey": "This is allowed" }  ]  ,    [   "***",  {
                                                
                                      "allowedKey":   "This is allowed" }    ] ,     {   "allowedKey":    "This is allowed" }]  },   [   {
                                     "allowedKey":  "This is allowed" }    ,  {
                                         "_+<#?[&":  [ "***",  {
                                                
                                     "allowedKey":  "This is allowed" }    ]   ,"<%*^&()?":   {
                                                
                                       "allowedKey":   "This is allowed" }    ,   "%-%[ ;=!}    )(":  {
                                                
                                     "allowedKey":    "This is allowed" }  , "?":    {
                                     "allowedKey": "This is allowed" }     }   ,     {   "allowedKey":    "This is allowed" }],  {
                                                
                                     "allowedKey":    "This is allowed" }  ,{
                                                
                                     "*()_$-?(": [   {
                                                
                                        "allowedKey":   "This is allowed" } ] ,  "%=>@^}   [  -&": {
                                     ";=":   {
                                       "allowedKey": "This is allowed" }    , "[ ^,  ^[  .*":  {
                                      "allowedKey":  "This is allowed" }    ,   "$]  {  (?%<":    {
                                       "allowedKey":    "This is allowed" } ,    "allowedKey":   "This is allowed" }  }    , {
                                      "}    =!.^$: ]":    {
                                                
                                         "allowedKey":  "This is allowed", "-:   ?|_!&;@": {
                                                
                                     "allowedKey":  "This is allowed" }     },  "allowedKey":  "This is allowed",    "+,   )>[ [ =;":  {
                                       "^@.$,  *": {   "allowedKey":  "This is allowed" }  ,     "_[  [  =": [  "***",     {
                                     "allowedKey": "This is allowed" } ],   "+>":  [   "***", {
                                        "allowedKey": "This is allowed" }    ]   }  ,   "} >+]    |_(^":   {
                                        "?}-}    -*+!":    {
                                      "allowedKey":    "This is allowed" } , "*.:   }<^{    *&%":   [   "***",   {
                                                
                                         "allowedKey":  "This is allowed" }    ] }     }  ] \s
                        """, jsonMasker
                ),
                new JsonMaskerTestInstance("""
                        {
                                   "|":    true,   ")+*":   {
                                    "allowedKey": "Nested allowed value" }   ,"number"    :13452547 ,     ",    #":   false }
                        """,
                        """
                        {
                                   "|":    "&&&",   ")+*":   {
                                    "allowedKey": "Nested allowed value" }   ,"number"    :"###" ,     ",    #":   "&&&" }
                        """, jsonMasker
                )
        );
    }
}
