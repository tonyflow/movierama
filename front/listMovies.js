


angular.module('movieRama', [])
.controller('listMovies', function($scope, $http) {

	$scope.doFetchLatest=function(){

		$http.get('http://localhost:8080/movies/latest').
        then(function(response) {
            $scope.movies = response.data;
        });
    }
    

	$scope.doFetchMovie=function(){

		if ($scope.movie_title==null) {
			$scope.movie_title='';
		};
		$http.get('http://localhost:8080/movies/search?title='
			+$scope.movie_title).
        then(function(response) {
            $scope.movies = response.data;
        });
	}
    

});

