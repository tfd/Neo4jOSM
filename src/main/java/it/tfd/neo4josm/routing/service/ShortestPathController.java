package it.tfd.neo4josm.routing.service;

import java.util.concurrent.atomic.AtomicLong;

import it.tfd.neo4josm.routing.model.Route;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShortestPathController {

    @RequestMapping("/shortestpath")
    public Route shortestPath() {
        return new Route(1, "route");
    }

}
