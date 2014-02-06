angular.module('aemScript', [])

.directive('aemMaintabs', [function(){
    return {
        restrict: 'E',
        templateUrl: 'public-resources/template/main-tabs.html'
    };
}])

.controller('aemJvmtableCtrl', function($scope, $http, $location) {
    $scope.jvmHeaders = ['JVM', 'Host', 'HTTP/1.1 Port', 'Available Heap', 'Total Heap', 'HTTP Session Count',
                         'HTTP Request Count', 'Group'];
    $scope.jvmFields = ['name', 'host', 'httpPort', 'availableHeap', 'totalHeap', 'httpSessionCount',
                        'httpRequestCount', 'group'];

    $scope.initJvmItems = function() {

        $http({method: 'GET', url: 'jvminfo'}).
              success(function(data, status, headers, config) {
                $scope.jvmItems = data;
              }).
              error(function(data, status, headers, config) {
                // called asynchronously if an error occurs
                // or server returns response with an error status.
              });

    }

})

.directive('aemJvmtable', [function(){
    return {
        restrict: 'E',
        scope: {
            jvmHeaders: '=headers',
            jvmFields: '=fields',
            jvmItems: '=items'
        },
        templateUrl: 'public-resources/template/jvm-tbl.html',
    };
}]);