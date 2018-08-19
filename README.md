Open World Simulator
====
Open World Simulator is an open source project aiming at creating a computer model of multiple aspects of the world's economy, demography, environmental, energy and social behaviours. 
This will be achieved by the application of agent based simulation, that help to identify and experiment with emergent 
behaviours coming from multiple agent decisions.

This long term project will be developed on an iterative, incremental way, with simplicity in mind and minimizing the amount of technical complexities.

This is the first prototype - **WORK IN PROGRESS**


Stage 1. First prototype - synthetic model
----
* Single region
  - One population

* Generic model
  - Linear evolution of parameters 

* Basic demographics.
  - Fertility rate
  - Population segments
  - Simplify model even more
  - Inflow and outflow of population
  
* Basic household economics
 
  - Income
    - Wages
    - Subsidies
    - Financial yield
    - Real Estate yields

  - Tax on income
    - Tax on corporate profits
    - Tax on goods and services
    - Tax on payroll
    - Tax on personal income
    - Tax on property
 
  - Consumption
    - Housing
    - Mandatory
    - Discretionary
  
  - Tax on consumption
   
  - Propensity to saving
  
  - Debt
  
  - Savings
    - Financial
    - Real state - other
  
* Aggregate economics with balance sheets
   - Private sector
   - Public sector
   - Banking sector
   - GDP components
   
Technical improvements:
---
* Load model class dynamically
* Multi-threading
* Performance tuning
* Export results in CSV
* Report experiment summary
* Configurable chart size
* Continuous integration

Future ideas:
---
* Align with OECD Indicators
    - https://data.oecd.org/searchresults/?r=+f/type/indicators
    - https://datacatalog.worldbank.org/
    - http://api.population.io/
    - http://databank.worldbank.org/data/home.aspx

