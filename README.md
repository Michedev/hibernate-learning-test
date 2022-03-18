# Hibernate Learning tests

Maven repository containing learning tests for Hibernate ORM library. 
The project tests rely upon an external PostgreSQL container to execute them.

## Requirements

- Docker
- Maven
- Java 8


## Test suite

The project skips the _test_ phase in favor of the _integration test_ phase because tests
we want to start PostgreSQL before testing (_pre-integration-test_) and stopping 
them afterwards (_post-integration-test_).

## How to run

To run all learning tests prompt in a shell

```bash
mvn clean verify
```

