Open World Simulator
====
Open World Simulator is an open source project aiming at creating a computer model of multiple aspects of the world's economy, demography, environmental, energy and social behaviours. 
This will be achieved by the application of agent based simulation, that help to identify and experiment with emergent 
behaviours coming from multiple agent decisions.

This long term project will be developed on an iterative, incremental way, with simplicity in mind and minimizing the amount of technical complexities.

This is the first prototype - **WORK IN PROGRESS**


Stage 1. First prototype - simple model
----
* Single economy region
  - Household Sector  : Population represented by agents
  - Business Sector   : Aggregated view, not agent based
  - Government Sector : Aggregated view, not agent based
  - Foreign Sector    : Aggregated view
  - Banking Sector    : None

* Evolution of model parameters
  - Linear evolution - yearly increase (%)
  - Stochastic modeling by simple normal curves
   
* Basic demographics.
  - Fertility rate
  - Population segments per age  
  - Inflow and outflow of population
  
* Basic household economics
  - GDP calculation
    - Demand
        - Consumption
        - Company investment
        - Public expenses
        - Exports
        - Imports
 
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
   
Technical improvements TODO:
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
* Integrate with existing data sources
    - https://esa.un.org/unpd/wpp/Download/Standard/Population/
    - https://data.worldbank.org/indicator
    - https://data.oecd.org/searchresults/?r=+f/type/indicators
    - https://datacatalog.worldbank.org/
    - http://api.population.io/
    - http://databank.worldbank.org/data/home.aspx
    - https://wits.worldbank.org/Default.aspx?lang=en

