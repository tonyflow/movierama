#### MovieRama

The backend implementation of MovieRama is a Spring Boot application which uses Maven for managing its build lifecycle. `mvn clean install`  
will provide you with a `war` package which you can deploy on a local Tomcat or you can run the application from your IDE of your choice. 
Personally, I tested MovieRama through Eclipse's embedded Tomcat.

So far the merging algorithm of `MovieRamaServiceImpl` handles only two movie API resources. This can be easily extended by using the `MovieResourceFactory` which can make resource specification and handling configurable (application.yml configuration), making it possible for our application to consume a multitude of API resources.

The merge algorithm which hitherto selects the longest of descriptions and sums the number of reviews returned by our API resources can run recursively to accommodate this future extension. The algorithm populates the required fields in the following order and manner : 

**title**: `RottenTomatoesService` is initially assigned the task of populating that field. If there was no result then the task will be delegated to the `MovieDbService`

**description**: The longest of the two retrieved descriptions will be displayed in our frontend.

**numberOfReviews**: The accumulative number of reviews retrieved from both APIs  

**productionYear**: `RottenTomatoesService` is initially assigned the task of populating that field. If there was no result then the task will be delegated to the `MovieDbService`

**actors**: Both APIs retrieve cast lists. Due to rate limitations Movie Db will produce 429 HTTP errors which will result in map entries containing `null` actors' lists. Nevertheless, this will be appropriately handled by the respective `ResourceService` which will continue with the production of the remaining map entries and further on by the `MovieRamaServiceImpl` merge algorithm.

The application caches its queries to an EhCache implementation for a week (*timeToIdleSeconds="604800" timeToLiveSeconds="604800"*). API's _now playing_ results are cached as individual elements while a _now playing_ element carrying their cached keys is also cached in order to accumulate them afterwards. You can check ehCache configuration under `ehcache.xml`.There is a `EhCacheController` where you can inspect the elements of the cache being updated every time a query is submitted to the application. There is also a `ehcache/clear` `POST` request which can be performed in order for the cache to evict all its elements. This utility was developed for testing purposes mainly.

Application's log level has been set to INFO. Nevertheless, it is configurable through the `application.yml` property `logging.level.root`.

As far as the frontend is concerned, you may find the required files under `/front` folder in the root of the repository.
Copy the files to your local Apache directory and you are ready to go. Using the frontend , the user must specify the exact name of the movie one wants to search or press the _Latest_ button and retrieve all latest movies in theaters. There is no approximate string matching (fuzzy search) results for MovieRama.


The following is a class diagram of the application depicting mainly the services' architecture.

![alt text](https://bytebucket.org/niko_strongioglou/movierama/raw/aa7e3756adbc1da145c348e7f87522098b59ee7f/movierama-class-diagram.png?token=35d06b1717e7470ef7457f8ac36b838bebe7c0c2)


