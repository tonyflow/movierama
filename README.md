#### MovieRama

The back end implementation of MovieRama is a Spring Boot application which uses Maven for managing its build lifecycle. An `mvn clean install`  
will provide you with a `.war` package which you can deploy on a local Tomcat or you can run the application from your IDE of your choice. 
Personally, I tested MovieRama through Eclipse's embedded Tomcat.

As far as the front end is concerned, you may find the required files under `/front` folder in the root of the repository.
Copy the files to your local Apache directory and you are ready to go.

The following is a class diagram of the application depicting mainly the services and resource important DTOs MovieRama handles.


