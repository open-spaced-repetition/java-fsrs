## Local development

### Install library locally

```bash
mvn clean install
```

### Test the package

```bash
mvn test
```

### Format package code

```bash
mvn spotless:apply
```

### Add as dependency

```xml
  <dependency>
    <groupId>io.github.open-spaced-repetition</groupId>
    <artifactId>fsrs</artifactId>
    <version>0.1.0</version>
  </dependency>
```