angular.module('aemLogin', [])

.controller('aemLoginCtrl', function($scope) {

    $scope.userName = 'USERNAME';

    $scope.userNameOnFocus = function() {
        if ($scope.userName = 'USERNAME') {
            $scope.userName = '';
        }
    };

})

.directive('aemLogin', [function(){
    return {
        restrict: 'E',
        template: '<div ng-controller="aemLoginCtrl"><input type="text" ng-model="userName" ng-click="userNameOnFocus()" />&nbsp;&nbsp;<input type="password">&nbsp;&nbsp;<a href="#">Log In</a><div>'
    }
}]);