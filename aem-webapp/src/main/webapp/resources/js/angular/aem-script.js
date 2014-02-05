angular.module('aemScript', [])
.directive('aemMaintabs', [function(){
    return {
        restrict: 'E',
        templateUrl: 'public-resources/template/main-tabs.html'
    };
}])

.controller('aemJvmtableCtrl', function($scope) {
    $scope.jvmHeaders = ['JVM', 'Host', 'HTTP Port', 'Available Heap', 'Total Heap'];
    $scope.jvmFields = ['name', 'host', 'httpPort', 'availableHeap', 'totalHeap'];
    $scope.jvmItems = {item:{name: 'CR01_TRE_1', host: 'HOST_XX_1', httpPort: '8080', availableHeap: '400kb', totalHeap: '1024kb'},
                       item:{name: 'CR01_TRE_2', host: 'HOST_XX_2', httpPort: '8080', availableHeap: '400kb', totalHeap: '1024kb'}};

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