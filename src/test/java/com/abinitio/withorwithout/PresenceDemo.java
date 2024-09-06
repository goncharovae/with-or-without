package com.abinitio.withorwithout;

import com.abinitio.withorwithout.dto.Address;
import com.abinitio.withorwithout.dto.Role;
import com.abinitio.withorwithout.dto.User;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PresenceDemo {

    public void withAddresses() {
        var users = getUsers(Presence.with(), Presence.without());

        for (User<With<Address>, Without<Set<Role>>> user : users) {
            Address address = user.address.get();
            // do something with address

            // compiler won't allow to do anything with roles
        }
    }

    public void withAddressesDynamic() {
        var dynamicUsers = getUsers(Presence.withOrWithout(true), Presence.withOrWithout(false));
        for (User<WithOrWithout<Address>, WithOrWithout<Set<Role>>> user : dynamicUsers) {
            Address address = user.address instanceof With<Address> withAddress ? withAddress.get() : null;
            Set<Role> roles = user.roles instanceof With<Set<Role>> withRoles ? withRoles.get() : null;
        }
    }

    private <A extends WithOrWithout<Address>, R extends WithOrWithout<Set<Role>>> List<User<A, R>> getUsers(
            Presence<Address, A> withAddress, Presence<Set<Role>, R> withRoles
    ) {
        List<Map<String, Object>> userRows = List.of(); // get list of users

        List<User<A, R>> users = userRows.stream().map(row -> {
            // populate only required fields here
            User<A, R> user = new User<>();
            user.id = (Long) row.get("id");
            user.email = (String) row.get("email");
            return user;
        }).toList();

        // runtime handling of 'presence' aspect to provide compile-time 'presence' guarantee
        if (withAddress.isPresent()) {
            Map<Long, Address> addressesMap = Map.of(); // fetch addresses for all users
            for (User<A, R> user : users) {
                // ifPresent method allows us to wrap address into WithOrWithout<?> without casting
                user.address = withAddress.ifPresent(() -> addressesMap.get(user.id));
            }
        }

        // same for roles
        if (withRoles.isPresent()) {
            Map<Long, Set<Role>> rolesMap = Map.of(); // fetch roles for all users
            for (User<A, R> user : users) {
                user.roles = withRoles.ifPresent(() -> rolesMap.get(user.id));
            }
        }
        return users;
    }
}
