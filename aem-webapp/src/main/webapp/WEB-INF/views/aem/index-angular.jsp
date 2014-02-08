<html ng-app="aemScript">
    <head>
        <title>Angular</title>
        <link rel="stylesheet" type="text/css" href="public-resources/css/angular/tab.css">
        <script src="public-resources/js/angular/angular-1.2.10.min.js"></script>
        <script src="public-resources/js/angular/angular-resource-1.2.7.min.js"></script>
        <script src="public-resources/js/angular/aem-login.js"></script>
        <script src="public-resources/js/angular/aem-script.js"></script>
        <div>
            <!-- directives should be wrapped inside a parent tag e.g. div.
            If div is removed the directives found after this one will not render. -->
            <aem-login />
        </div>
        <br/><br/>
    </head>
    <body>
        <aem-maintabs />
    </body>
</html>