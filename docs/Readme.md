# Open World Simulator

Open World Simulator is an open source project aiming at creating a computer model of multiple aspects of the world's economy, demography, environmental, energy and social behaviours. 
This will be achieved by the application of agent based simulation, aiming at identifying and experimenting with emergent 
behaviours arising from agent decisions.

This long term project will be developed on an iterative, incremental way, with simplicity in mind and minimizing the amount of technical complexities.

## The First Prototype

This blog describes some of the results of the first prototype.

**Simulation model:** 

The first simulation model aims at describing, in the most simplistic way, a single population and economy 
by modeling the following aspects:

- Demography
- Household economy
- Government expending
- Business sector
- Bank sector
- External sector

Simulation runs will produce a series of reports and raw data in CSV format.  

**Demographics model**

This model is the most basic description of a human population. It covers the following aspects

- Segments of population by age
- Migration flows
- Fertility rates
- Evolution of population

For simplicity and accuracy with real world, it loads the initial population status from an external service instead of a synthetic generation of it.

**Household economy**

**Government expending**

**Business sector**

**Bank sector**

**External sector**

## First simulation experiment: Narnia

Narnia, an imaginary country, is the first case being analyzed with this prototype.

- [Narnia Simulation parameters](https://github.com/open-world-simulator/prototype/blob/master/simulation/src/main/resources/defaults/Narnia.defaults)

The first step is looking at the demographics outlook of Narnia.

- **[Narnia Demographics](Narnia/Narnia-demographics.md)**
