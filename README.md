**Vending Machine Unit Testing**
===
# **Problem Statement**
Write a program to design Vending Machine using your 'favourite language' with all possible tests
Accepts coins of 1,5,10,25 Cents i.e. penny, nickel, dime, and quarter.
Allow user to select products Coke(25), Pepsi(35), Soda(45)
Allow user to take refund by cancelling the request.
Return selected product and remaining change if any.
Allow reset operation for vending machine supplier.

IN the document it was mentioned to copy the vending machine code from google and write unite test cases for the same.

# **Technologies used**

- Java 11
- JUnit 5
- Mockito 2

# **Build Requirements**
Before being able to build he code you need:
- Maven 3.3+
- Java 11

# **How to run the automation suite locally?**
````
git clone https://github.com/nehaoza/vending-machine.git

cd vending-machine
mvn clean test
````

# **Continuous Integration with GitHub Actions**

## **When it triggers?**
1. WHen commited to main branch
Sample commit link - https://github.com/nehaoza/vending-machine/commit/f07bfec2f2613eb85d37a8c5fe2f844a6622fc58
2. When Merge request is raised against main branch
Sampel PR link - https://github.com/nehaoza/vending-machine/pull/1

## **What it does?**
1. Compiles and builds the code
2. Runs automation suitein GitHub Actions container
3. Does Sonar scan and pushes hte result to sonar dashboard
4. Find the code coverage of newly added code
5. Extract the sonar summery and attaches it to Merge Request in case of Merge Request
6. Attaches the summury of cucumber scenarios as a part of automation suite


# **Achieves**

1. Wrote Unit test cases for vending machine
2. Achived 97% code coverage - https://sonarcloud.io/component_measures?id=nehaoza_vending-machine&metric=coverage&view=list 
2. Added CI pipeline with GitHUb Actions
3. Project is built with zero bugs and Vulnerabilities in Sonar. 
Sonar Dashboard : https://sonarcloud.io/component_measures?id=nehaoza_vending-machine
