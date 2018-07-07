BDApp.service('ProfileService', function ($http) {

    this.getByAge = function (age) {
        return $http.get('/queries/get-by-age?age=' + age)
            .then(function (data) {
                return data;
            }, function (err) {
                console.log("Error getting by age");
            });

    };

    this.getBySex = function (sex) {
        return $http.get('/queries/get-by-sex?sex=' + sex)
            .then(function (data) {
                return data
            })
            .catch(function () {
                console.log("Caught error in get-by-sex");
            });
    };

    this.getByAgeRange = function (min, max) {
        return $http.get('/queries/get-by-range?min=' + min + "&max=" + max)
            .then(function (data) {
                return data;
            })
            .catch(function () {
                console.log("Caught error in get-by-sex");
            });
    };


    this.loadCSV = function (filename) {
        return $http.get('/upload/load-to-mongo?name=' + filename)
            .then(function (data) {
                return data;
            })
            .catch(function () {
                console.log("Caught error in load-to-mongo");
            });
    };


    this.learnDataset = function (filename) {
        return $http.post('/learn/process?name=' + filename)
            .then(function (data) {
                return data;
            })
            .catch(function () {
                console.log("Caught error in load-to-mongo");
            });
    };


    this.predict = function (choices) {
        console.log("in service! choices = " + JSON.stringify(choices));
        return $http.post('/learn/predict?sex=' + choices.sex + '&intent=' + choices.intent + '&education=' + choices.education + '&place=' + choices.place)
            .then(function (data) {
                return data;
            })
            .catch(function () {
                console.log("Caught error in predicting");
            });
    };



    this.search = function (filters) {
        return $http.get('/queries/search?sex=' + filters.sex + '&intent=' + filters.intent + '&education=' + filters.education + '&place=' + filters.place + '&race=' + filters.race)
            .then(function (data) {
                return data;
            })
            .catch(function () {
                console.log("Caught error in predicting");
            });
    };


});