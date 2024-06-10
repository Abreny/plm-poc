# IMPROVEMENTS
### 1. Data integrity is essential (this includes entity and their related entities).

Database have many techniques to ensure data integrity. They includes constraints (foreign key, trigger, stored procedure, transaction). As a developer, I follow two practice to ensure data integrity:
- do not trust on user input data (always use validation on user input layer)
- use effectivelly spring transaction with @Transaction on bean or on a bean method.

For example, without transaction for PartService, there is high probability that our data is not consistent because after updating the Part, there is is no guarantee that document related to this part will be updated as well. The same for multiple user who reserve the same part or document.

Solution: use spring declarative transaction management.

### 2. Despite this, bugs related to data quality is very common (i.e. often bugs are not reproducible outside of the PROD)

Exactly. But testing is the key to reduce this. Every case of our code should be tested. More we have high code, less our production has not reproductible BUG.

There are some discipline that modern software should have:
- testing first (TDD): this help us to write a testable code and good architecture.
- refactor each time you add or remove some code
- separate untestable code into testable
- unit > functional > acceptance > ....


### 3. Performances must be optimals but this no real time software (i.e. code simplicity > micro optimisation)

Code simplicity itself is an art. It's not a work one time but every time and for everyone. The design principle (SOLID, KISS, YAGNI, Design Pattern) and component principles help developer team to write code simplicity and flexible.


### 4. Some new entities will be added soon, different than Part and Document but with the same behaviors but NOT necessarily all of them (i.e. réservation/life cycle/versionning)

If our sytem has a clean architecture, adding new entity or service is easy. We should create a class instead of modify the exist one (Close for modification and open for extension). I add more builder in order to make this task easy. You can use **ServiceCommandBuilder** directly to add/customize entity or service. The key is "leaving options open".


### 5. We have customers all arround the world, and so are their users too
Maintenability, extensibility and discipline is the key. 


### 6. Integration (i.e. solution customisation to fit customer specific needs) maybe done will by others companies
That the reason of Component design principles. 



# MY MODIFICATION
- add @Transaction for PartService and DocumentService
- remove code duplication about create next iteration and next version by applying factory pattern
- replace conditional with polymorphism. This let PartService and DocumentService to be customizable without changing its code. I've applyed command pattern and builder pattern to make this possible. So know, developer can change the behavior of DocumentService and PartService easily.
- encapsulate conditional guard for service in order to reuse them. Adding rule is now easy and we can use it in any builder.
- error management is done by throwing the rule violation exception and client application (like controller) can handle it in order to show or translate the error
- how to add new entity? Create your entity class and and use ServiceCommandBuilder to build the service for it. The same princimple for new service.