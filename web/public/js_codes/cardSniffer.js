var cardSniffer = angular.module('cardSniffer', ['ngRoute']);

cardSniffer.config(function ($routeProvider, $locationProvider) {
    $locationProvider.html5Mode({ enabled: true, requireBase: false }).hashPrefix('!');

    $routeProvider

        .when('/', {
            templateUrl: '../pages/home.html',
            controller: 'homeController'
        })

    .otherwise({ redirectTo: '/'});


});




cardSniffer.config(['$httpProvider', function ($httpProvider) {
    //Reset headers to avoid OPTIONS request (aka preflight)
    $httpProvider.defaults.headers.common = {};
    $httpProvider.defaults.headers.post = {};
    $httpProvider.defaults.headers.put = {};
    $httpProvider.defaults.headers.patch = {};
}]);