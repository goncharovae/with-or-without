package com.abinitio.withorwithout.dto;

import com.abinitio.withorwithout.WithOrWithout;

import java.util.Set;

public class User<P extends WithOrWithout<Address>, R extends WithOrWithout<Set<Role>>> {

    public Long id;
    public String email;

    public P address;
    public R roles;
}
