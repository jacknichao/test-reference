# test-reference
this project is used to generate references fronts on testing instances.

this project can only be executed after "**multi-object**" project.

**multi-project** generates reference front of training instances, while **test-reference** generates references front of test instances under the help of **multi-object**;


##Steps to execute this project:

###first :
    run FilterParetoFront.java to filter out those fronts which can not effectively select any features.

###second:
    run CheckOnTest.java to check on test instances and evaluate the performance with paretofront feature.

###third:
    run GenerateTestParetoFront.java to generate references fronts of test instances with the help of the fronts of trainning instances.