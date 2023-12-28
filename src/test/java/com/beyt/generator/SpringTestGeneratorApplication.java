package com.beyt.generator;

import com.beyt.generator.annotation.IntegrationTestGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
@IntegrationTestGenerator(mainClass = SpringTestGeneratorApplication.class, deleteGenerationDirectory = true, packageForTest = "com.beyt.generator.generated", outputPath = "/Users/talhadilber/work/personnel/spring-test-generator/src/test/java/com/beyt/generator/generated")
public class SpringTestGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringTestGeneratorApplication.class, args);
    }

    // TODO get dynamic mapper
    // TODO ignore generic tree fisrt class like ResponseEntity

}
