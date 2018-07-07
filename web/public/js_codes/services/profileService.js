BDApp.service('ProfileService', function ($http) {

    this.search = function () {
        return $http.get('/cards/get-cards')
            .then(function (data) {
                console.log(JSON.stringify(data));
                return data;
            })
            .catch(function () {
                console.log("Caught error in predicting");
            });
    };
    this.search();


});