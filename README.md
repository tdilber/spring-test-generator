# Spring Web Project Integration Test Generator

This project provides a way to generate integration tests for your Spring Web project. It offers two methods for
generating test data:

1. Recording request and response results using AOP and Interceptor
2. Filling random values

After generate tests then you should delete dependency, annotation and profile. You need only one time this project on
long term periods. Check all test files and delete wrong and hardcoded values. Because we can't detect which fields
required, static or dynamic, random. It means this generation project cannot provide %100 prod ready tests (I think it
is impossible). You should check all generated test files and delete wrong test parts.

## 1. Recording Request and Response Results

If your project completed and you want to generate integration tests for your project, you can use this method. This
method records the request and response results of your application. It uses AOP and Interceptor to record the request
and response results of your application. This is useful when you want to test how your application behaves with actual
data.

1. If more than one request is made, the test will be generated for only one request. This check is made by comparing
   the request and response values MD5 hash. If the hash is the same, the test will not be generated. But if request or
   response parameters are different, the test will be generated.
2. All files created on while application gracefully shutdown. It means if you want to see generated tests, you should
   gracefully shutdown your application. After that you can see generated tests on your project the outputPath
   directory.

Here is an example of how you can use this method:

```java
@IntegrationTestGenerator(mainClass = MainClass.class, generationType =  IntegrationGenerator.Type.LIVE_TEST_RECORDER, deleteGenerationDirectory = true, packageForTest = "com.beyt.generated", outputPath = "full-path-of-project/src/test/java/com/beyt/generated", ignoreMethodReturnGeneric = {ResponseCustomObject.class, ResponseEntity.class}, recordedRequestHeaders = {"auth-token", "Language", "Currency", "Channel", "AppVersion", "CountryCode"})
```

If your application uses custom objects in its response, you can use the `ignoreMethodReturnGeneric` parameter to ignore
them during the test generation process. This is useful when you want to test how your application behaves with actual
data.

```java
@Bean
public <T> MethodReturnIgnoreGenericConverter<ResponseCustomObject<T>, T> responseCustomObjectConverter() {
    return new MethodReturnIgnoreGenericConverter<>() {
        @Override
        public Class<?> getGenericType() {
            return ResponseCustomObject.class;
        }

        @Override
        public T convert(ResponseCustomObject<T> x) {
            return x.getData(); //  ResponseCustomObjects data getter method
        }
    };
}
```

### Not:

After generation, you must check all generated test files. Because some of the generated tests may be wrong. For
example, if your application returns a different response for each request, the generated test will be wrong. You can
delete the wrong test files, or you can fix them.

## 2. Filling Random Values

This method generates random values for your test data. It uses libraries such as JavaFaker and Lorem to generate random
strings, numbers, dates, etc. This is useful when you want to test how your application behaves with different kinds of
input data.

Here is an example of how you can use this method:

```java
@IntegrationTestGenerator(mainClass = MainClass.class, generationType = IntegrationGenerator.Type.TEST_WITH_RANDOM_DATA_GENERATOR, deleteGenerationDirectory = true, packageForTest = "com.beyt.generated", outputPath = "full-path-of-project/src/test/java/com/beyt/generated", ignoreMethodReturnGeneric = {ResponseCustomObject.class, ResponseEntity.class})
```

# Getting Started

To use the `IntegrationTestGenerator` in your Spring Web project, follow these steps:

#### Step 1: Add the IntegrationTestGenerator Annotation

In your test class, add the `IntegrationTestGenerator` annotation and configure it according to your needs. Here's an
example:

```java
@IntegrationTestGenerator(
        mainClass = MainClass.class,
        generationType = IntegrationGenerator.Type.LIVE_TEST_RECORDER,
    packageForTest = "com.beyt.generated",
    outputPath = "full-path-of-project/src/test/java/com/beyt/generated",
        recordedRequestHeaders = {"auth-token", "Language", "Currency", "Channel", "AppVersion", "CountryCode"},
        ...
)
@SpringBootApplication
public class MainClass {
    public static void main(String[] args) {
        SpringApplication.run(MainClass.class, args);
    }
}
```

#### Step 2: Add the Active Profile

Start your application with "integration-test-generator" Spring Profile. This will activate the integration test
generator profile for your tests.

In your VM options, add `-Dspring.profiles.active=integration-test-generator` to activate the profile.

#### Step 3: Add the Dependency

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.beyt.generator</groupId>
    <artifactId>spring-test-generator</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <exclusions>
        <exclusion>
            <groupId>org.yaml</groupId>
            <artifactId>snakeyaml</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

After these steps, you can now use the `IntegrationTestGenerator` annotation in your test classes to generate
integration tests for your Spring Web project.

### IntegrationTestGenerator Annotation Parameters

The `IntegrationTestGenerator` is an annotation used to configure the generation of integration tests in a Spring Web
project. Here's a breakdown of its fields:

- `generationType`: This is an enum of type `IntegrationGenerator.Type` that specifies the type of test generation to be
  used. It can be either `LIVE_TEST_RECORDER` for recording live tests or `TEST_WITH_RANDOM_DATA_GENERATOR` for
  generating tests with random data.
- `mainClass`: This is a class that represents the main class of the application. It's used as a reference point for
  generating tests.
- `packageForTest`: This is a string that specifies the package where the generated test classes will be placed.
- `outputPath`: This is a string that specifies the path where the generated test classes will be written to.
- `recordedRequestHeaders`: This is an array of strings that specifies the headers to be recorded during the test
  generation process.
- `deleteGenerationDirectory`: This is a boolean that specifies whether the generation directory should be deleted
  before generating tests. If set to true, the directory specified in `outputPath` will be deleted before tests are
  generated.
- `ignoreClasses`: This is an array of classes that specifies the classes to be ignored during the test generation
  process. By default, `BasicErrorController.class` is ignored.
- `ignoreMethodReturnGeneric`: This is an array of classes that specifies the generic return types to be ignored during
  the test generation process. By default, `ResponseEntity.class` is ignored.
- `ignoreMethodArgTypes`: This is an array of classes that specifies the argument types to be ignored during the test
  generation process. By default, `HttpServletRequest.class` and `HttpServletResponse.class` are ignored.

```java
public @interface IntegrationTestGenerator {
    String packageForTest();

    Class<?> mainClass();

    String outputPath();

    String[] recordedRequestHeaders() default {};

    IntegrationGenerator.Type generationType() default IntegrationGenerator.Type.LIVE_TEST_RECORDER;

    boolean deleteGenerationDirectory() default false;

    Class<?>[] ignoreClasses() default {BasicErrorController.class};

    Class<?>[] ignoreMethodReturnGeneric() default {ResponseEntity.class};

    Class<?>[] ignoreMethodArgTypes() default {HttpServletRequest.class, HttpServletResponse.class};
}
```

## Conclusion

The Integration Test Generator for Spring Web projects is a powerful tool that can help you improve the quality of your
tests and make your testing process more efficient. By using this tool, you can ensure that your application is robust
and can handle a wide range of input data.
