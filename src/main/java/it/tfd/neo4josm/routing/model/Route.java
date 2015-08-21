package it.tfd.neo4josm.routing.model;

/**
 * Created by ronald on 15/08/15.
 */
public class Route {

    private int id;

    private String name;

    public Route(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

}
