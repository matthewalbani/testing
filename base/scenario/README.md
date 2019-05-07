# Scenarios
This readme describes a library for building reusable "scenarios" for testing. The code lives in two packages: `testing/base/scenario` has the base classes, and `settlements/ledger/testing/scenario` has Ledger-specific subclasses, with access to the `LedgerDataStore` abstractions.

## Scenario Builders
The core class is `ScenarioBuilder`, which is a fluent builder pattern for constructing dependency forests. Let's first take a look inside.

```
public class ScenarioBuilder {
  private final Map<Pair<String, TypeToken>, Object> cache;
  private final Map<Pair<String, TypeToken>, InstanceBuilder> instanceBuilders;
```
The first map is just to keep track of the objects that we've built so far. Every object built by the `ScenarioBuilder` goes in the cache immediately. Looking at the type of the map, we can tell that you get one instance per `(name, type)` pair.

The second map is a store of builders for those same instances. There's a one-to-one mapping between built, cached instances and `InstanceBuilder`s. Every `InstanceBuilder` is invoked exactly once.

The `ScenarioBuilder` has exactly what you would expect:

```
public final Scenario build() { ... }
```
The `Scenario` is just a collection of instances of things. It has a variety of accessor methods:

```
public <T> T get(Class<T> klass) { ... }
public <T> T get(String name, Class<T> klass) { ... }
...
```


So to use a `Scenario`, you first construct one with a `ScenarioBuilder`, then just grab what you need by type and name. Pretty standard.

## Instance Builders
Above we saw that the `ScenarioBuilder` contains a bunch of `InstanceBuilders`, indexed by name and type. At root, an `InstanceBuilder<T>` is just a thing that produces a value of type `T`:

```
private final Supplier<T> instanceSupplier;
public T produceInstance() { // basic version
  return instanceSupplier.get();
}
```
Makes sense. So we're constructing a bag of [thunks](http://en.wikipedia.org/wiki/Thunk) and then evaluating them at the end. Immediately, notice that this means that we don't have to build all of an instance inline. In particular, we have the two methods

```
public InstanceBuilder<T> with(Consumer<T> mutation) {
  this.mutation = Optional.of(mutation);
}
public InstanceBuilder<T> map(UnaryOperator<T> transformation) {
  this.transformations.add(transformation);
}
```
When we invoke the thunk, we apply all of the "add-ons", as follows:

```
public T produceInstance() { // still merely illustrative
  T beforeTransformation = instanceSupplier.get();
  T afterTransformation = transformations.stream()
      .reduce(beforeTransformation, (t, f) -> f.apply(t), (t1, t2) -> t2);
  mutation.ifPresent(m -> m.accept(instance));
  return instance;
}
```
So we apply all the transformations that have been added to this instance builder, in order, then hand it off to the mutation, if there is one, before finally returning the instance.

There's also the option to name your `InstanceBuilder`: `myInstanceBuilder.named("name");`. This will cause the instance produced to be given the same name and be accessible only by that `(name, type)` pair.

Let's have an example:

```
public InstanceBuilder<String> myName() {
  return new InstanceBuilder<>(() -> "Jack Horsey", String.class);
}
public InstanceBuilder<String> yelling() {
  return myName().map(String::toUpperCase).named("loud");
}
public InstanceBuilder<String> backwards() {
  return yelling().map(s -> new StringBuilder(s).reversed().build()).named("loud and backwards");
}
public InstanceBuilder<AtomicInteger> someInt() {
  return new InstanceBuilder<>(AtomicInteger::new, AtomicInteger.class)
      .with(i -> i.set(12));
}
```
In practice, you'll probably be using methods like `Builder#fromPrototype` to transform immutable instances.

## Sharing State
The instance builders shown above have no dependencies. Much more commonly, we'll construct instances that rely on other instances. So can we do this?

```
public InstanceBuilder<User> user() {
  return new InstanceBuilder<>(User::new, User.class);
}
public InstanceBuilder<Payment> payment() {
  return new InstanceBuilder<>(() -> {
    User user = user().produceInstance();
    return new Payment(user);
  }, Payment.class);
}
public InstanceBuilder<Shipment> shipment() {
  return new InstanceBuilder<>(() -> {
    User user = user().produceInstance();
    return new Shipment(user);
  }, Shipment.class);
}
```
Spot the problem? The `Payment` and the `Shipment` have been constructed with different `User` instances. This is the reason for the `cache` field that we saw earlier inside the `ScenarioBuilder`. Instead of invoking an `InstanceBuilder` for a dependency directly, suppliers ask for the dependency to be **satisfied** by the relevant `ScenarioBuilder`. The required instance is supplied (hopefully) out of either the cache or the set of known `InstanceBuilder`s. But... how does an `InstanceBuilder` know which `ScenarioBuilder` to go to for assistance?

## I've been lying all along
So, you can't actually construct a complete `InstanceBuilder` directly, _per se_. Instead, you must construct a "detached" `InstanceBuilder`, which, instead of taking a `Supplier<T>`, takes a `Function<ScenarioBuilder, T>`. That is, it takes a function from the `ScenarioBuilder`, _to which it will be attached_, to the desired instance. It looks like this:

```
public InstanceBuilder<User> user() {
  return new InstanceBuilder<>(sb -> new User(), User.class);
}
public InstanceBuilder<Payment> payment() {
  return new InstanceBuilder<>(sb -> {
    User user = sb.satisfy(User.class);
    return new Payment(user);
  }, Payment.class);
}
public InstanceBuilder<Shipment> shipment() {
  return new InstanceBuilder<>(sb -> {
    User user = sb.satisfy(User.class);
    return new Shipment(user);
  }, Shipment.class);
}
```
The `sb` parameter is the _base scenario builder_ for that `InstanceBuilder`. When you are done applying transformations, names, and mutations to the instance builder, you have to attach it to the base scenario builder, which will then know how to supply an instance of that name and type. To attach an `InstanceBuilder`, it's as simple as doing this:

```
scenarioBuilder.given(user())
```

In the above example, because instances are unique per `(name, type)` pair, there's only one `User` with no name. Therefore, if we attach all three `InstanceBuilder`s to the same `ScenarioBuilder`, the return value of `sb.satisfy(User.class)` will be this unique `User`, and it will be shared between the `Payment` and `Shipment`:

```
Scenario scenario = new ScenarioBuilder()
    .given(user())
    .given(payment())
    .given(shipment())
    .build();
Payment payment = scenario.get(Payment.class);
Shipment shipment = scenario.get(Shipment.class);
assertThat(payment.getUser()).isEqualTo(shipment.getUser());
```
Let's look at applying some of the operators we discussed above to this toy example:

```
Scenario scenario = new ScenarioBuilder()
    .given(user()
        .map(u -> new UserBuilder(u)
            .location("CA")
            .build())
        .with(db::save))
    .given(payment()
        .with(Payment::moveTheMoney))
    .given(shipment().named("ca_shipment"))
    .build();
```
Here we've given the user a location of "CA". The shipment, therefore, is going to California. Note that we didn't have to update anything about the `shipment()` method: it just asks the `ScenarioBuilder` to supply whatever unnamed `User` it has, which in this case is the user with a California address. The `User` instance will be saved in the database, `db::save`, when it is constructed.

## Scrap. Your. Boilerplate.
Ledger has a super relational data model. When you're setting up a test, what this means is that you spend a lot of time doing this:

```
BankAccount payFromHere = testFixtures.newBankAccount();
TransmissionConfig theConfig = testFixtures.newTransmissionConfig();
theConfig.setAccount(payFromHere);
theConfig.setName(TRANSMISSION_NAME);
Transmission transmission1 = testFixtures.newTransmission();
transmission1.setConfig(theConfig);
// ... and so on FOREVER ...
ACHFile fileIActuallyCareAbout = testFixtures.newACHFile();
fileIActuallyCareAbout.setArcaneDependency(foo);
```
We would like to avoid a similar pattern when constructing scenarios! The `ScenarioBuilder` class exposes a method, `#satisfyWith`, that helps with this. Rather than calling `sb.satisfy(MyDependency.class)` in your supplier, you can do this instead:

```
public DetachedInstanceBuilder<MyDependency> myDependency() { ... }

public DetachedInstanceBuilder<ActuallyRelevant> actuallyRelevant() {
  ...
  MyDependency dep = sb.satisfyWith(myDependency());
  // do stuff with the dependency
}

new ScenarioBuilder()
    .given(actuallyRelevant().named("foo"))
    .build();
```
So you don't have to explicitly register `InstanceBuilder`s for the entire dependency tree up to the thing you actually want to test. What's more, you can still get the dependencies that you skipped:

```
MyDependency depIWantToAssertAgainst = scenario.get(MyDependency.class);
```

Finally, you can still hook into these dependencies and mess around with them using the `InstanceBuilder` operators:

```
Scenario scenario = new ScenarioBuilder()
    .given(myDependency().with(dep -> dep.setProperty("prop")))
    .given(actuallyRelevant())
    .build();

assertThat(scenario.get(ActuallyRelevant.class).getDep().getProperty())
    .isEqualTo("prop"); // yay!
```
This works because when you invoke `ScenarioBuilder#satisfyWith`, it's really just saying, "if you can't find one of these, this is where you should get it." Where "one of these" is, again, simply indexed by name and type. If we register an `InstanceBuilder` for that dependency ourselves, and then mess around with it, it'll use the one we supplied.

## Useful for testing!
Here's a more complete example of how you might use this library to construct a test.

```
private ScenarioBuilder scenarioBuilder;

@Before
public void before() {
  scenarioBuilder = new ScenarioBuilder()
      .given(user())
      .given(shipment())
      .given(payment());
}

@Test
public void aPaymentWorks() {
  Scenario scenario = scenarioBuilder.build();
  Payment payment = scenario.get(Payment.class);
  payment.sendTheMoney();
  assertThat(payment.moneyWasSent()).isTrue();
}

@Test
public void noMoneyNoPayment() {
  Scenario scenario = scenarioBuilder
      .given(user().with(u -> u.setMoney(0)))
      .build();
  Payment payment = scenario.get(Payment.class);
  payment.sendTheMoney();
  assertThat(payment.moneyWasSent()).isFalse();
}

@Test
public void moMoneyMoShipments() {
  Scenario scenario = scenarioBuilder
      .given(user().with(User::oneMillionDollars))
      .given(payment().map(p ->
          new Payment().fromPrototype(p).ofOneMillionDollars()))
      .given(bonusShipment().named("bonus"))
      .build()
  assertThat(scenario.get("bonus", Shipment.class).success()).isTrue();
}

@Test
public void refundedPayment() {
  Scenario scenario = scenarioBuilder
      // in addition to the original payment.
      .given(payment().named("refund"))
      // define an instance builder for a return shipment inline
      // note that we're using the version of #map that puts the scenario builder in scope
      .given(shipment().map((sb, shipment) -> {
        Payment refund = sb.satisfy("refund", Payment.class);
        return shipment.return().reason(refund.getReason());
      }).named("return"))
      .build();
  assertThat(scenario.get(Payment.class).findShipment())
      .isEqualTo(scenario.get(Shipment.class));
  assertThat(scenario.get("refund", Payment.class).findShipment())
      .isEqualTo(scenario.get("return", Shipment.class));
}
```

## Thanks for reading!
Feature and documentation requests are more than welcome.