angular.module('aemScript', [])

.directive('aemMaintabs', [function(){
    return {
        restrict: 'E',
        templateUrl: 'public-resources/template/main-tabs.html'
    };
}])

.controller('aemJvmtableCtrl', function($scope) {
    $scope.jvmHeaders = ['JVM', 'Host', 'HTTP/1.1 Port', 'Available Heap', 'Total Heap', 'HTTP Session Count',
                         'HTTP Request Count', 'Group'];
    $scope.jvmFields = ['name', 'host', 'httpPort', 'availableHeap', 'totalHeap', 'httpSessionCount', 'httpRequestCount', 'group'];
    $scope.jvmItems = [{name: 'CTO_HC_SRN012_1', host: 'SRN012', httpPort: '8080', availableHeap: '1.2 gb', totalHeap: '3 gb', httpSessionCount: '5', httpRequestCount: '2', group: 'Group 1'},
                       {name: 'CTO_HC_SRN012_2', host: 'SRN013', httpPort: '8080', availableHeap: '2 gb', totalHeap: '3 gb', httpSessionCount: '2', httpRequestCount: '10', group: 'Group 2'}];
})

.directive('aemJvmtable', [function(){
    return {
        restrict: 'E',
        scope: {
            jvmHeaders: '=headers',
            jvmFields: '=fields',
            jvmData: '=data'
        },
        templateUrl: 'public-resources/template/jvm-tbl.html',
    };
}]);