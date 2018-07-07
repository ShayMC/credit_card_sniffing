BDApp.service('ProfileService', function ($http) {


    this.search = function (filters) {
        return $http.get('/cards/get-cards')
            .then(function (data) {
                return data;
            })
            .catch(function () {
                console.log("Caught error in predicting");
            });
    };


});