# Play Tagless Case Study

Copyright Dave Gurnell, 2019.

Licensed Apache 2.0.

# Overview

The goal of this case study is
to rewrite `PasswordStore` in finally tagless style.

There are two way to do this: using Guice or using manual DI.

## Integrating Finally Tagless Modules with Play using Guice

Guice doesn't understand higher hinded type parameters,
so it doesn't support injection of tagless style components:

```scala
trait ComponentApi[F[_]] {
  def doStuff: F[Unit]
}

class GenericComponent[F[_]: Monad] extends ComponentApi[F] {
  def doStuff: F[Unit] = ().pure[F]
}

// This doesn't work!
class ProductionController @Inject() (component: ProductionComponent[Future])
```

To work around this, we create a simplified subtype of each component we want to inject:

```scala
// This class removes the higher kinded type type parameter from GenericComponent:
class ProductionComponent extends GenericComponent[Future]

// We can inject ProductionComponent just fine:
class ProductionController @Inject() (component: ProductionComponent)
```

Now, nodify the `PasswordStore` example in this repo in this style:

1. Convert `PasswordStore` to finally tagless style
   like we did in the [previous case study](https://github.com/davegurnell/tagless-case-study)

2. Integrate `PasswordStore` with Guice
   by creating a concrete subtype that doesn't have any type parameters
   and injecting that into your controller

## Integrating Finally Tagless Modules with Play using Manual DI Play

**NOTE: This exercise will undo some of the changes you made in the last exercise.
Commit your code or switch branches before carrying on!**

Here's an alternate strategy to the one described above.
Instead of using Guice, we can do manual dependency injection using contstructors:

1. Delete the concrete subtype you introduced in step 2 above!

2. Convert the app to compile-time dependency injection
   using the [instructions in the Play docs](https://www.playframework.com/documentation/2.7.x/ScalaCompileTimeDependencyInjection)

    a. Remove the reference to `guice` from `build.sbt`
    b. Remove the reference to `PasswordServiceModule` from `application.conf`
    c. Uncomment the reference to `startup.AppLoader` in `application.conf`
    d. Implement `AppLoader` (draw the rest of the owl)

3. Implement an `AuthService` as an intermediary between `AuthAction` and `PasswordStore`.

    a. Include methods to set and delete a password.
    b. Include endpoints in `AppController` to call these methods.
    c. Implement *synchronous* tests of the methods
       using `Id` or `Either` as a monad instead of `Future`.

## Integrating Finally Tagless Modules with Play using Macwire

The [Macwire](https://github.com/softwaremill/macwire) library from Software Mill provides
a nice syntactic sugar over constructor-based DI.
It's a small change in this exercise that will make a big difference on a larger codebase.

Macwire is built specifically for Scala so it *does* understand higher-kinded types.
Reimplement your manual DI code using Macwire!

1. Add Macwire to build.sbt
2. Use the `wire` macro to replace the constructor calls in `AppLoader`
