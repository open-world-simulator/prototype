Prototype V1
====
- Evolution of parameters
    - Test Suite pending
- Model business sector
- External Sector
- Warn about not provided variables
- Subsidies and other social expending
- Output output files to temp dir and rename dir when completed

**Refactoring**
- Transactions
- Replace PersonMonthlyData with MonthlyResults

**Basic simulation**
- Emigration flows
- YoY % growth stats
- Companies agents
- Job market, unemployment

**Better stats**
- Business stats
- Balance sheet stats

Prototype V2
===
- Better modeling than bell curves
    - Curve skew? 
    - Monte Carlo modeling?
- Inflation
- Productivity factors
- Real estate
- Personal investment

Prototype V3
===

Future
===
- Load model class dynamically
- Performance adjustments
- Align with indicators databases / sources. Examples:
    - OECD indicators
    - https://esa.un.org/unpd/wpp/Download/Standard/Population/
    - https://data.worldbank.org/indicator
    - https://data.oecd.org/searchresults/?r=+f/type/indicators
    - https://datacatalog.worldbank.org/
    - http://api.population.io/
    - http://databank.worldbank.org/data/home.aspx
    - https://wits.worldbank.org/Default.aspx?lang=en
- Indicators aggregation service



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
   