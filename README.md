#### MovieRama

The back end implementation of MovieRama is a Spring Boot application which uses Maven for managing its build lifecycle. `mvn clean install`  
will provide you with a `.war` package which you can deploy on a local Tomcat or you can run the application from your IDE of your choice. 
Personally, I tested MovieRama through Eclipse's embedded Tomcat.

So far the merging algorithm of `MovieRamaServiceImpl` handles only two movie API resources. This can be easily extended by creating a `MovieResourceServiceFactory` which can make resource specification and handling configurable, making it possible for our application to consume a multitude of API resources.

The merge algorithm which hitherto selects the longest of descriptions and sums the number of reviews returned by our API resources can run recursively to accommodate this future extension.

The application caches its queries to an EhCache implementation for a week. You can check ehCache configuration under `ehcache.xml`.There is a `EhCacheController` where you can inspect the elements of the cache being updated everytime a query is submitted to the application. There is also a `ehcache/clear` `POST` request which can be performed in order for the cache to evict all its elements. This utility was developed for testing purposes mainly.

As far as the front end is concerned, you may find the required files under `/front` folder in the root of the repository.
Copy the files to your local Apache directory and you are ready to go. Using the front end , the user must specify the exact name of the movie one wants to search or specify no title at all. There is no approximate string matching (fuzzy search) results for MovieRama.


The following is a class diagram of the application depicting mainly the services and resource important DTOs MovieRama handles.

![alt text](https://bytebucket.org/niko_strongioglou/movierama/raw/b29edf9eeb6c561bdd776d2d906a5ea317008bcb/movierama-class-diagram.png?token=f43932d1b87f6931c9e5276c94b7c3e90375aeec)


