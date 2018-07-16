BDApp.controller('homeController', ['$scope', '$http', 'ProfileService', '$window', function ($scope, $http, ProfileService, $window) {

    $scope.result = [];
    $scope.accessGranted = false;

    $scope.search = function(pass) {
        $scope.loading = true;
        $scope.accessGranted = false;
        $scope.wrongPass = false;

        console.log("O");
        ProfileService.search(pass)
            .then(function (data) {
                console.log("Got data: " + JSON.stringify(data));
                $scope.loading = false;
                if (data) {
                    $scope.result = data.data.ans;
                    console.log($scope.result);
                    $scope.accessGranted = true;
                    $scope.wrongPass = false;
                } else {
                    $scope.wrongPass = true;
                }
            }, function(err) {
                $scope.loading = false;

            });

        console.log("K");

    };

    $scope.pastebin = function() {

        ProfileService.pasteBin()
            .then(function (data) {
                console.log("Pastebin: " + JSON.stringify(data))

                if (data.data.url) {
                        $window.open(data.data.url, '_blank');
                }
            }, function (err) {
                console.log("Pastebin err: " + JSON.stringify(err))
            });
    }


}]);




