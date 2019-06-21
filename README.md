# Play Tagless Case Study

Copyright Dave Gurnell, 2019.

Licensed Apache 2.0.

# Overview

The goal of this case study is 
to rewrite `PasswordStore` in finally tagless style. 
The recommended steps are as follows:

1. Convert the app to compile-time dependency injection
   using the [instructions in the Play docs](https://www.playframework.com/documentation/2.7.x/ScalaCompileTimeDependencyInjection)

    a. Remove the reference to `guice` from `build.sbt`
    b. Remove the reference to `PasswordServiceModule` from `application.conf`
    c. Uncomment the reference to `startup.AppLoader` in `application.conf`
    d. Implement `AppLoader`
  
2. Convert `PasswordStore` to finally tagless style
   like we did in the [previous case study](https://github.com/davegurnell/tagless-case-study)

3. Implement an `AuthService` as an intermediary between `AuthAction` and `PasswordStore`.
   
    a. Include methods to set and delete a password.
    b. Include endpoints in `AppController` to call these methods.
    c. Implement *synchronous* tests of the methods using `Id` or `Either` as a monad instead of `Future`.
