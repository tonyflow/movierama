


angular.module('movieRama', [])
.controller('listMovies', function($scope, $http) {
    

	$scope.doFetch=function(){

		if ($scope.movie_title==null) {
			$scope.movie_title='';
		};
		$http.get('http://localhost:8080/movies/list?title='
			+$scope.movie_title).
        then(function(response) {
            $scope.movies = response.data;
        });
	}
    

});

