Considerations:
Prerequisites: Java 11 
-Install it on a MacOS(El Capitan), follow these steps: $ brew update $ brew tap caskroom/cask $ brew install Caskroom/cask/java -Verify it by: $ java -version, it should point to java 8 or bigger.

-Install it on a Ubuntu, follow these steps: $ sudo add-apt-repository ppa:linuxuprising/java $ sudo apt-get update $ sudo apt install oracle-java11-installer -Verify it by: $ java -version, it should point to java 11.

Run unit tests by:

mvn test jacoco:report You can then access target/site/jacoco/index.html in order to check the code coverage. You can then access target/surefire-reports/index.html in order to see the tests results.
Observations:

I came up with a random set of validations, just to prove a point. In real life, validations are more complex, and they are the result of business requirements.

The OrderRepository I created can be in real life a DB client or REST Http client call, or something else. For testing purposes it is only mocked.

Extra care would be required for dealing with the repository exceptional cases, but I considered it to be out of scope for this exercise.

When generating the order summary I took the freedom to assume the input parameter would be the order type.

A Custom Collector can also be a solution, which would produce the same behavior that we have right now. 
The only problem with it is the fact that it is a bit unreadable and may not cover all the possible situations, so I decided to go for standard approach, which is using java 8 streams, grouping and reducing.

More refinements can be done as always, as there is no "Done".