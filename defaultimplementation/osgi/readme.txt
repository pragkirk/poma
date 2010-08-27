Create a CustomerManager class that is the published API and also a true singleton. This is what will get registered as the service. Provide the default implementation and when the client gets access to the CustomerManager, set the discount calculator that should be used.

Check iPhone notes.

------------------------------------------------------------

The problem with the existing design is that it places implementations close to the interfaces. The result is a smell. The customerservice project can be compiled independently, but at runtime it is dependent on an implementation of DefaultCalculator, which means it requires either the customerextension or customercalculator project be built and deployed so that a DiscounterService is created.

But if we move the DefaultCalculator to the customerservice project, it cannot be instantiated as a Service and used within the customerservice project. Spring DM doesn't support importing a service from the bundle that exports it.

In the end, we separated the DiscountCalculator and DefaultCalculator into the separate customercalculator project. To remove the compile dependency on customerservice, we created an OrdersProxy interface and the implementation lives in the customerservice project. This puts customercalculator on it's own, whereas without it, the Orders[] array was passed into the DiscountCalculator interface. 

To support this new structure, we also moved the DiscountCalculator to the customercalculator project.

Before, customerservice was Level 1, customercalculator and customerextension was level 2, and customerclient was level 3.

Now, customercalculator is level 1, customerservice and customerextension are level 2, and customerclient is level 3.

I want to move the customercalculator back into the customerservices project.