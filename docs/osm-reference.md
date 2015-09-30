# OSM files come in various types:

- XML
> the standard XML format
- pbf
> compressed OSM format

## Entities

An OSM file has the following entities:

* Node
> a single point with latitude and longitude
* Way
> a line, can be closed. Represents road, buildings, land-use, etc...
* Relations
> Relation between nodes and ways.

## Road features:

A way is a road when it has a highway key. This key can have a lot of values, but the only ones we need are:

* Roads
    * __motorway__
    > A restricted access major divided highway, normally with 2 or more running lanes plus emergency hard shoulder. Equivalent to the Freeway, Autobahn, etc..
    * __trunk__
    > The most important roads in a country's system that aren't motorways. (Need not necessarily be a divided highway.)
    * __primary__
    > The next most important roads in a country's system. (Often link larger towns.)
    * __secondary__
    > The next most important roads in a country's system. (Often link towns.)
    * __tertiary__
    > The next most important roads in a country's system. (Often link smaller towns and villages)
    * __unclassified__
    > The least most important through roads in a country's system – i.e. minor roads of a lower classification than tertiary, but which serve a purpose other than access to properties. Often link villages and hamletes. 
    * __residential__
    > Roads which serve as an access to housing, without function of connecting settlements. Often lined with housing.
    * __service__
    > For access roads to, or within an industrial estate, camp site, business park, car park etc.
* Link roads
    * __motorway_link__
    > The link roads (sliproads/ramps) leading to/from a motorway from/to a motorway or lower class highway. Normally with the same motorway restrictions.
    * __trunk_link__
    > The link roads (sliproads/ramps) leading to/from a trunk road from/to a trunk road or lower class highway.
    * __primary_link__
    > The link roads (sliproads/ramps) leading to/from a primary road from/to a primary road or lower class highway.
    * __secondary_link__
    > The link roads (sliproads/ramps) leading to/from a secondary road from/to a secondary road or lower class highway.
    * __tertiary_link__
    > The link roads (sliproads/ramps) leading to/from a tertiary road from/to a tertiary road or lower class highway.
* Special road types
    * __living_street__
    > Residential streets where pedestrians have legal priority over cars, speeds are kept very low and where children are allowed to play on the street.
    * __pedestrian__
    > For roads used mainly/exclusively for pedestrians.
    * __track__
    > Roads for mostly agricultural or forestry uses.
    * __bus_guideway__
    > A busway where the vehicle guided by the way (though not a railway) and is not suitable for other traffic.
    * __road__
    > A road where the mapper is unable to ascertain the classification from the information available.
* Paths
    * __footway__
    > For designated footpaths; i.e., mainly/exclusively for pedestrians. This includes walking tracks and gravel paths.
    * __bridleway__
    > For horses.
    * __steps__
    > For flights os steps on footways.
    * __cycleway__
    > For designated cycleways.
    * __path__
    > A non-specific path.
* Other highway features
    * __crossing__
    > Pedestrians can cross a street here; e.g., zebra crossing
    * __mini_roundabout__
    > Similar to roundabouts, but at the center there is either a painted circle or a fully traversable island.
    * __motorway_junction__
    > Indicates a junction (UK) or exit (US).
    * __turning_circle__
    > A turning circle is a rounded, widened area usually, but not necessarily, at the end of a road to facilitate easier turning of a vehicle. Also known as a cul de sac.
    
## Road access

### Default values

Road access is regulated first by default permissions designated by the highway type, in Italy they are:

| highway=*                                | access      | motorcar    | motorcycle  | goods=hgv   | bus         | taxi        | agricultural | moped       | horse       | bicycle     | foot        |
| ---------------------------------------- |:-----------:|:-----------:|:-----------:|:-----------:|:-----------:|:-----------:|:------------:|:-----------:|:-----------:|:-----------:|:-----------:|
| motorway motorway_link motorway_junction | designated  | designated  | designated  | designated  | designated  | designated  | no           | no          | no          | no          | no          |
| trunk trunk_link                         | yes         | yes         | yes         | yes         | yes         | yes         | no           | no          | no          | no          | no          |
| primary primary_link                     | yes         | yes         | yes         | yes         | yes         | yes         | yes          | yes         | yes         | yes         | yes         |
| secondary secondary_link                 | yes         | yes         | yes         | yes         | yes         | yes         | yes          | yes         | yes         | yes         | yes         |
| tertiary tertiary_link                   | yes         | yes         | yes         | yes         | yes         | yes         | yes          | yes         | yes         | yes         | yes         |
| unclassified                             | yes         | yes         | yes         | yes         | yes         | yes         | yes          | yes         | yes         | yes         | yes         |
| residential                              | yes         | yes         | yes         | yes         | yes         | yes         | yes          | yes         | yes         | yes         | yes         |
| service                                  | yes         | destination | destination | destination | destination | destination | yes          | yes         | yes         | yes         | yes         |
| living_street                            | yes         | destination | destination | destination | destination | destination | yes          | yes         | yes         | yes         | yes         |
| pedestrian                               | no          | no          | no          | no          | no          | no          | no           | no          | no          | no          | yes         |
| track                                    | no          | no          | no          | no          | no          | no          | yes          | yes         | yes         | yes         | yes         |
| bus_guideway                             | no          | no          | no          | no          | designated  | no          | no           | no          | no          | no          | no          |
| road                                     | yes         | yes         | yes         | yes         | yes         | yes         | yes          | yes         | yes         | yes         | yes         |
| footway                                  | no          | no          | no          | no          | no          | no          | no           | no          | no          | no          | designated  |
| bridleway                                | no          | no          | no          | no          | no          | no          | no           | no          | designated  | no          | no          |
| steps                                    | no          | no          | no          | no          | no          | no          | no           | no          | no          | no          | designated  |
| cycleway                                 | no          | no          | no          | no          | no          | no          | no           | yes         | no          | designated  | no          |
| path                                     | no          | no          | no          | no          | no          | no          | no           | yes         | yes         | yes         | yes         |
| crossing                                 | yes         | yes         | yes         | yes         | yes         | yes         | yes          | yes         | yes         | yes         | designated  |
| mini-roundabout                          | yes         | yes         | yes         | yes         | yes         | yes         | yes          | yes         | yes         | yes         | no          |
| turning_circle                           | yes         | yes         | yes         | yes         | yes         | yes         | yes          | yes         | yes         | yes         | no          |


### Modifiers

The following tags can modify the default access:

* __access__
> 
* __oneway__
> Indicates that the road is oneway (yes), both directions (no), or opposite direction (-1).
* __oneway{:transport_type}__
> Indicates that the road is oneway or not for a specific transportation vehicle (p.e. oneway:bicycle=no).
* __bicycle:backward=yes__, __cycleway=opposite_track__, __cycleway=opposite_lane__, __oneway:bicycle=no__, or __bicycle:oneway=no__
> In conjunction with **oneway=yes** indicates that bicycles are allowed only in the opposite direction.
* __junction=roundabout__
> On a highway indicates a roundabout. Implies **oneway=yes**.
* __motorroad=yes__
> The motorroad tag is used to describe highways that have motorway-like access restrictions but that are not a motorway.
* __vehicle:backward=no__
> Equal to **oneway=yes**
* __vehicle:forward=no__
> Equal to **oneway=-1**

## Road speed

### Default values

For Italy the default speed for each road type is:
 
| highway=*                      | urban | interurban |
| ------------------------------ |:-----:|:----------:|
| motorway                       |       | 130        |
| trunk (with **motorroad=yes**) | 70    | 110        |
| trunk (with **motorroad=no**)  | 50    | 90         |
| primary                        | 50    | 90         |
| secondary                      | 50    | 90         |
| tertiary                       | 50    | 90         |
| unclassified                   | 50    | 70         |
| residential                    | 50    |            |
| road                           | 50    | 70         |

The keys **maxspeed:type** and **source:maxspeed** can be used to indicate default speeds in that country. The values are:

| Value            | Speed |
| ---------------- |:-----:|
| AT:walk          | 7     |
| AT:living_street | 30    |
| AT:urban         | 50    |
| AT:rural         | 100   |
| AT:trunk         | 100   |
| AT:motorway      | 130   |
| CH:walk          | 7     |
| CH:living_street | 30    |
| CH:urban         | 50    |
| CH:rural         | 80    |
| CH:trunk         | 100   |
| CH:motorway      | 120   |
| FR:walk          | 7     |
| FR:living_street | 30    |
| FR:urban         | 50    |
| FR:rural         | 90    |
| FR:trunk         | 110   |
| FR:motorway      | 130   |
| IT:walk          | 7     |
| IT:living_street | 30    |
| IT:urban         | 50    |
| IT:rural         | 90    |
| IT:trunk         | 110   |
| IT:motorway      | 130   |

### Modifiers

The following tags can modify the default speed:

* __surface__
> Indicates the surface type. Values other than **paved**, **asphalt**, and **concrete** reduce the speed to 30 km/h.
* __maxspeed__
> Gives the legal speed limit.
* __maxspeed=default__
> Use default value for country and highway type.
* __maxspeed=city_limits__
> Use default urban value for country and highway type.
* __maxspeed:advisory__
> Gives the (legally) recommended speed.
* __maxspeed:pratical__
> Gives a realistic average speed.