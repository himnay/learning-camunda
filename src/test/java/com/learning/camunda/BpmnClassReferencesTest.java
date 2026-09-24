package com.learning.camunda;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Every {@code camunda:class} in a deployed model must name a class on the classpath.
 * The engine only notices at runtime (ENGINE-09008 when an instance reaches the task),
 * so a stale package name ships silently unless something checks it at build time.
 */
class BpmnClassReferencesTest {

    private static final Pattern CAMUNDA_CLASS = Pattern.compile("camunda:class=\"([^\"]+)\"");

    static Stream<Resource> models() throws IOException {
        return Arrays.stream(new PathMatchingResourcePatternResolver().getResources("file:src/main/resources/*.bpmn"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("models")
    void camundaClassReferencesResolve(Resource model) throws IOException {
        Matcher m = CAMUNDA_CLASS.matcher(model.getContentAsString(StandardCharsets.UTF_8));
        while (m.find()) {
            String className = m.group(1);
            assertThatCode(() -> Class.forName(className))
                    .as("%s references %s", model.getFilename(), className)
                    .doesNotThrowAnyException();
        }
    }
}
