Testing utilities
---

This jar contains classes that can help writing test cases. It is meant to be included in the dependencies
of projects that need them with a Maven dependency scope of "test":

```
<dependency>
    <groupId>com.nuance</groupId>
    <artifactId>webapi-utils-testing</artifactId>
    <scope>test</scope>
</dependency>
```

Note that the utility classes in this library are in the main/ source tree, and **not** test/, even though
they are meant to be only used in the test cases by those using the library. The code under test/ is to
unit test the utility classes at this level.