package com.openworldsimulator.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Population {
    private List<Person> people = new ArrayList<>();

    public Population() {
    }

    public void add(Person p) {
        people.add(p);
    }

    public int size() {
        return people.size();
    }

    public List<Person> getPeople() {
        return people;
    }

    public List<Person> getAlivePeople() {
        return people.stream().filter(Person::isAlive).collect(Collectors.toList());
    }
}
