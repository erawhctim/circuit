CircuitContent
==============

The simplest entry point of a circuit is the composable `CircuitContent` function. This function accepts a `Screen` and automatically finds and pairs corresponding `Presenter` and `Ui` instances to render in it.

```kotlin
CircuitCompositionLocals(circuitConfig) {
  CircuitContent(HomeScreen)
}
```

This can be used for simple screens or as nested components of larger, more complex screens.