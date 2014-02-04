angular.module('aemScript', [])
.directive('aemMaintabs', [function(){
    return {
        restrict: 'E',
        templateUrl: 'public-resources/template/main-tabs.html'
    };
}])
.directive('aemJvmtable', [function(){
    return {
        restrict: 'E',
        templateUrl: 'public-resources/template/jvm-tbl.html',
    };
}]);