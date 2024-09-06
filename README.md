# with-or-without

A few classes to explore/demonstrate the approach for design of DTO classes that have optional fields in them.

## Issue with `Optional` / `null`

Sometimes we need to design an API to fetch objects (say, from a relational database) which have one or
multiple additional fields that we might not always want to fetch along with the base entity.

Consider this DTO class:

```java

public class User {

    public Long id;
    public String email;

    public Address address;
    public Set<Roles> roles;

    // getters and setters
}
```

In a very typical scenario `address` and `roles` values are stored in a separate tables,
so we might not want to fetch them every single time we fetch a list of users.

One straightforward approach to this is to allow client code to specify whether you need those extra fields
with parameters:
```java

public List<User> getUsers(boolean withAddress, boolean withRoles) {
    ///
}
```

The problem with this approach is that once we receive this `List<User>` in the client code,
we no longer have any indication whether this list of objects contain
`address` or `roles` info.

This isn't so bad when the client code handles that list of objects right away:

```java

    List<User> users = userService.getUsers(true, false); // with addresses, but without roles
    for (User user : users) {
        sendBill(user.getAddress()); // at this point we know addresses are fetched
    }
```

but it gets a lot harder to track once we start passing around this list of users between components:

```java
    List<User> users = userService.getUsers(true, false);
    billService.processBills(users);
    // the code of processBills implementation needs to implicitly rely on addresses being present in `users`
```

We could've declared address and roles fields as `Optional<User>` and `Optional<Set<Roles>>`,
but in that case we have to rely on runtime checks of `Optional` being present.

Another approach is to define separate DTO classes for combinations of optional fields,
like `UserWithAddress`, `UserWithRoles`, `UserWithAddressAndRoles`, but as you can see even for 2 optional fields it gets messy.
Besides, we can't properly define a single `getUsers` function
that would have a proper return type based on combination of its parameters.

## Solution

Let's define `WithOrWithout<T>` type, a compile time analogue of `Optional<T>`:

```java
public sealed interface WithOrWithout<T> permits With, Without {
}
```
and its implementations:
```java
public final class With<T> implements WithOrWithout<T> {

    private final T value;

    // private constructor and static `of()` method

    public T get() {
        return value;
    }
}


public final class Without<T> implements WithOrWithout<T> {

    private Without() {
    }
}
```

Now, we can define our User DTO like this:

```java
public class User<A extends WithOrWithout<Address>, R extends WithOrWithout<Set<Role>>> {

    public Long id;
    public String email;

    public A address;
    public R roles;
}
```

and then define a function that fetches users with addresses specifically - without parameters for optional fields for now.
The usage of this function might look like this:

```java
    List<User<With<Address>, Without<Set<Role>>>> users = getUsersWithAddresses();
    for (User<With<Address>, Without<Set<Role>>> user : users) {
        // presence of `get()` method on `user.getAddress()` guarantees addresses on users are loaded
        Address address = user.getAddress().get();
        // the compiler won't let us access roles
        Without<Set<Role>> noRoles = user.getRoles(); // Without<Set<Roles>> doesn't have `get()` method
    }
```

## Generic parameterized function

Let's go back to the case where we want one parameterized function for both dependencies.

We can define another type called `Presence`
that functions as a boolean flag indicating whether to load an optional field.

Now let's define a signature of `getUsers` function as follows:

```java
    private <P extends WithOrWithout<Address>, R extends WithOrWithout<Set<Role>>> List<User<P, R>> getUsers(
            Presence<Address, P> withAddress, Presence<Set<Role>, R> withRoles
    ) {
        // you can see a draft of what an implementation of such function can be in `PresenceDemo.java`
    }
```
And finally, the example of a usage of such function:

```java
    List<User<With<Address>, Without<Set<Role>>>> users = getUsers(Presence.with(), Presence.without());
```

The generic parameters of `users` type are inferred from `getUsers` signature and `with()`/`without()` types
which means that you might as well declare it as `var users` and still have the benefits of compile time checks
for `With`/`Without` types.

As you can see, a full declaration of the such type might be really long, but it helps a bit
that we can always omit `Without` parameter (or when we don't care about the presence):
```java
    var users = getUsers(Presence.with(), Presence.without());

    for (User<With<Address>, ?> user : users) {
        ///
    }
```