BDApp.controller('homeController', ['$scope', '$http', 'ProfileService', function ($scope, $http, ProfileService) {

    $scope.result = [];
    $scope.loading = true;

    $scope.search = function() {

        console.log("O");
        ProfileService.search()
            .then(function (data) {
                console.log("controller" + JSON.stringify(data.data.ans));
                $scope.result = data.data.ans;
                console.log($scope.result);
                $scope.loading = false;
            }, function(err) {
                $scope.loading = false;

            })

        console.log("K");

    };





}]);




