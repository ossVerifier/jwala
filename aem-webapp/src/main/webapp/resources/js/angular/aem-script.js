angular.module('aemScript', ['ngResource', 'aemLogin'])

.directive('aemMaintabs', [function(){
    return {
        restrict: 'E',
        templateUrl: 'public-resources/template/main-tabs.html'
    };
}])

.controller('aemJvmtableCtrl', function($scope, $timeout, $resource, $http, $location) {
    $scope.jvmHeaders = ['JVM', 'Host', 'HTTP/1.1 Port', 'Available Heap', 'Total Heap', 'HTTP Session Count',
                         'HTTP Request Count', 'Group'];
    $scope.jvmFields = ['name', 'host', 'httpPort', 'availableHeap', 'totalHeap', 'httpSessionCount',
                        'httpRequestCount', 'group'];

    $scope.initJvmItems = function() {};

    var dataService = $resource('jvminfo');

    var rebuildJvmItemsTbl = function() {

        // Invoking a $resource object method immediately returns an empty reference
        // therefore a callback function is required to manipulate the data
        // returned by the query.
        var data = dataService.query(function(){
                        var i = 0;

                        // This assumes for now that the list of jvmItems hasn't changed.
                        // TODO: Handle use case where the list of jvmItems changes e.g. row is added or deleted.
                        for (var i = 0; i < data.length; i++) {
                            for (key in data[i]) {
                                $scope.jvmItems[i][key] = data[i][key];
                            }
                        }
                   });

    };

    // There are 2 options to auto refresh data. The first one is through polling
    // and the other one is through sockets.
    // Let's make it simple for now and use polling.
    var poll = function() {
        $timeout(function() {
            rebuildJvmItemsTbl();
            poll();
        }, 5000);
    };

    // Initial data query
    $scope.jvmItems = dataService.query();
    poll();

})

.directive('aemJvmtable', [function(){
    return {
        restrict: 'E',
        scope: {
            jvmHeaders: '=headers',
            jvmFields: '=fields',
            jvmItems: '=items'
        },
        templateUrl: 'public-resources/template/jvm-tbl.html'
    };
}]);