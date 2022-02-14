package com.beyt.generator.helper;

import com.beyt.generator.annotation.IntegrationTestGenerator;
import com.beyt.generator.domain.enumeration.ITemplateVariableEnum;
import com.beyt.generator.domain.enumeration.eIntegrationTestVariable;
import com.beyt.generator.exception.TestIntegrationFolderNotEmptyException;
import com.beyt.generator.exception.TestTemplateFileReadException;
import com.beyt.generator.exception.TestTemplateFileWriteException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Created by tdilber at 11/6/2020
 */
@Slf4j
public final class IntegrationTestTemplateHelper {
    private IntegrationTestTemplateHelper() {
    }

    public static void checkFolderIsEmptyOrDeleteAll(String filePath, boolean deleteAll) {
        File directory = new File(filePath);
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                throw new TestTemplateFileWriteException(e.getMessage(), e);
            }
            directory.mkdirs();
        }

        if (Objects.nonNull(directory.listFiles()) && directory.listFiles().length > 0) {
            if (deleteAll) {
                Arrays.stream(directory.listFiles()).forEach(File::delete);
            } else {
                throw new TestIntegrationFolderNotEmptyException("Selected test folder not empty (\"" + directory.getAbsolutePath() + "\")! Please neither \"Clear this Folder\" nor \"delete " + IntegrationTestGenerator.class.getSimpleName() + " annotation!\"");
            }
        }

    }

    public static void writeFile(String text, String fileName, String filePath) {
        try {
            File file = new File(filePath + "/" + fileName);
            FileWriter myWriter = new FileWriter(file);
            myWriter.write(text);
            myWriter.close();
        } catch (Exception e) {
            throw new TestTemplateFileWriteException("Filename: " + fileName + ". " + e.getMessage(), e);
        }
    }

    public static String generateTemplate(String templateName, Map<ITemplateVariableEnum, CharSequence> templateVariableMap) {
        String template = getTemplateFullString(templateName);

        for (Map.Entry<ITemplateVariableEnum, CharSequence> entry : templateVariableMap.entrySet()) {
            template = template.replace(entry.getKey().getTemplateVariable(), entry.getValue().toString());
        }

        return template;
    }

    private static String getTemplateFullString(String templateName) {
        String template;
        templateName = "/Users/talhadilber/work/personnel/spring-test-generator/src/main/resources" + templateName;
        try {
            template = new String(Files.readAllBytes(new File(templateName).toPath()), StandardCharsets.UTF_8);
        } catch (NullPointerException | IOException e) {
            throw new TestTemplateFileReadException(e.getMessage(), e);
        }
        return template;
    }

    public static void generateAutowireVariable(Map<Class<?>, String> autowireClassMap, Map<eIntegrationTestVariable, StringBuilder> variableMap) {
        variableMap.get(eIntegrationTestVariable.AUTOWIRES).append("    @Autowired\n" +
                "    private MockMvc restMockMvc;\n\n");
        autowireClassMap.values().forEach(a -> variableMap.get(eIntegrationTestVariable.AUTOWIRES).append(a).append("\n\n"));
    }
}
