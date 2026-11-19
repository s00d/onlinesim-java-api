package ru.onlinesim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Compiles the code examples of README.md and docs/content/*.md against the SDK, so the docs cannot drift from the
 * API. A ```java block counts as an example when it uses {@code client} or has imports; the other blocks are
 * signatures. A block with a class declaration compiles as a file; any other one becomes a method body with
 * {@code client} and {@code tzid} declared unless the block declares them. Examples compile for Java 11, except JUnit
 * tests (testing.md), which compile for 17 like the JUnit 6 they import.
 */
class DocsExamplesTest {
    private static final Pattern BLOCK = Pattern.compile("```java[^\\n]*\\n(.*?)```", Pattern.DOTALL);
    private static final Pattern PUBLIC_CLASS = Pattern.compile("public\\s+(?:final\\s+)?class\\s+(\\w+)");

    @Test
    void docsExamplesCompileAgainstTheSdk(@TempDir Path tmp) throws IOException {
        Map<String, String> examples = examples();
        assertTrue(examples.size() > 20, "found only " + examples.size() + " examples");

        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        assertNotNull(javac, "tests must run on a JDK");
        List<String> errors = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, String> example : examples.entrySet()) {
            Path dir = Files.createDirectories(tmp.resolve("ex" + i++));
            Matcher name = PUBLIC_CLASS.matcher(example.getValue());
            Path file = dir.resolve((name.find() ? name.group(1) : "Example") + ".java");
            Files.writeString(file, example.getValue());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int rc = javac.run(
                    null,
                    out,
                    out,
                    "--release",
                    example.getValue().contains("org.junit") ? "17" : "11",
                    "-proc:none",
                    "-classpath",
                    System.getProperty("java.class.path"),
                    "-d",
                    dir.resolve("out").toString(),
                    file.toString());
            if (rc != 0)
                errors.add(example.getKey() + "\n" + out.toString(StandardCharsets.UTF_8) + "\n" + example.getValue());
        }
        assertEquals(List.of(), errors);
    }

    private static Map<String, String> examples() throws IOException {
        List<Path> sources = new ArrayList<>();
        sources.add(Paths.get("README.md"));
        try (Stream<Path> docs = Files.list(Paths.get("docs/content"))) {
            sources.addAll(
                    docs.filter(p -> p.toString().endsWith(".md")).sorted().collect(Collectors.toList()));
        }
        Map<String, String> out = new LinkedHashMap<>();
        for (Path source : sources) {
            Matcher m = BLOCK.matcher(Files.readString(source));
            int n = 0;
            while (m.find()) {
                String code = m.group(1);
                if (!Pattern.compile("\\bclient\\b|^import ", Pattern.MULTILINE)
                        .matcher(code)
                        .find()) continue;
                out.put(
                        source + " #" + n++,
                        Pattern.compile("\\bclass\\s+\\w+").matcher(code).find() ? code : wrap(code));
            }
        }
        return out;
    }

    private static String wrap(String code) {
        StringBuilder imports = new StringBuilder("import ru.onlinesim.*;\nimport java.util.*;\nimport java.time.*;\n");
        StringBuilder body = new StringBuilder();
        for (String line : code.split("\n"))
            (line.startsWith("import ") ? imports : body).append(line).append('\n');
        List<String> params = new ArrayList<>();
        if (!Pattern.compile("\\b(OnlineSim|var)\\s+client\\s*=").matcher(code).find()) params.add("OnlineSim client");
        if (!Pattern.compile("\\b(long|var)\\s+tzid\\s*=").matcher(code).find()) params.add("long tzid");
        return imports + "class Example {\n  static void run(" + String.join(", ", params) + ") throws Exception {\n"
                + body + "  }\n}\n";
    }
}
