BDApp.service('ProfileService', function ($http) {

    this.search = function (passwd) {
        return $http.get('/cards/get-cards?password=' + passwd)
            .then(function (data) {
                console.log(JSON.stringify(data));
                return data;
            })
            .catch(function () {
                console.log("Caught error in getting cards");
                return null;
            });
    };
    // this.search();

    this.pasteBin = function() {

        return $http.get('/cards/pastebin')
            .then(function (data) {
                console.log(JSON.stringify(data));
                return data;
            })
            .catch(function () {
                console.log("Caught error in getting cards");
                return null;
            });
    }


});