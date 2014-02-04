angular.module('aemScript', [])
.directive('aemMaintabs', [function(){
    return {
        restrict: 'E',
        templateUrl: 'public-resources/template/main-tabs.html'
    };
}])

.controller('aemJvmtableCtrl', function($scope) {
    $scope.jvmItems = [{name: 'CR01_TRE_1', host: 'HOST_XX_1', httpPort: '8080', availableHeap: '400kb', totalHeap: '1024kb'},
                       {name: 'CR01_TRE_2', host: 'HOST_XX_2', httpPort: '8080', availableHeap: '400kb', totalHeap: '1024kb'}];
})

.directive('aemJvmtable', [function(){
    return {
        restrict: 'E',
        scope: {
            jvmInfos: '=info'
        },
        templateUrl: 'public-resources/template/jvm-tbl.html',
    };
}]);