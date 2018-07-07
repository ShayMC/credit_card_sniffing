var BDApp = angular.module('BDApp', ['ngRoute']);

BDApp.config(function ($routeProvider, $locationProvider) {
    $locationProvider.html5Mode({ enabled: true, requireBase: false }).hashPrefix('!');

    $routeProvider

        .when('/', {
            templateUrl: '../pages/home.html',
            controller: 'homeController'
        })

    .otherwise({ redirectTo: '/'});


});





BDApp.factory('AuthInterceptor', function ($window, $q) {
    return {
        request: function(config) {
            config.headers = config.headers || {};
            if ($window.localStorage.getItem('token')) {
                config.headers['x-access-token'] = $window.localStorage.getItem('token');
            }
            return config || $q.when(config);
        },
        response: function(response) {
            if (response.status === 401) {
                //  Redirect loggedUser to login page / signup Page.
            }
            return response || $q.when(response);
        }
    };


});


BDApp.config(function ($httpProvider) {
    $httpProvider.interceptors.push('AuthInterceptor');
});



BDApp.config(['$httpProvider', function ($httpProvider) {
    //Reset headers to avoid OPTIONS request (aka preflight)
    $httpProvider.defaults.headers.common = {};
    $httpProvider.defaults.headers.post = {};
    $httpProvider.defaults.headers.put = {};
    $httpProvider.defaults.headers.patch = {};
}]);