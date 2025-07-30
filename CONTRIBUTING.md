# Contributing to Java-FSRS

## Reporting issues

If you encounter an issue with Java-FSRS and would like to report it, you'll first want to make sure you're using the latest version of Java-FSRS.

The latest version of java-fsrs can be found under [releases](https://github.com/open-spaced-repetition/java-fsrs/releases).

Once you've confirmed that you're using the latest version, please report your issue in the [issues tab](https://github.com/open-spaced-repetition/java-fsrs/issues).

## Contributing code

### Pass the tests

In order for you contribution to be accepted, your code must pass the unit tests.

Run the tests with:
```bash
./gradlew test jacocoTestReport
```

Additionally, you are strongly encouraged to contribute your own tests to [src/test/](src/test/) to help make Java-FSRS more reliable.