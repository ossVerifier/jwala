<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>Apache Enterprise Manager</title>
<link rel="stylesheet" type="text/css" href="public-resources/css/aem.css">
<link rel="stylesheet" type="text/css" href="public-resources/css/react/components/default/area.css">
<link rel="stylesheet" type="text/css" href="public-resources/css/jvm-config-area.css">
<link rel="stylesheet" type="text/css" href="public-resources/css/react/components/default/tabs.css">
<link rel="stylesheet" type="text/css" href="public-resources/css/react/components/default/data-grid.css">
<link rel="stylesheet" type="text/css" href="public-resources/css/react/components/default/modal-form-dialog.css">
<script src="public-resources/js/react/es5-shim-2.3.0.min.js"></script>
<script src="public-resources/js/react/es5-sham.min.js"></script>
<script src="public-resources/js/react/react-0.8.0.min.js"></script>
<script src="public-resources/js/react/jquery-1.10.0.min.js"></script>
<script src="public-resources/js/json2.min.js"></script>
<script src="public-resources/js/react/components/area.js"></script>
<script src="public-resources/js/react/components/tabs.js"></script>
<script src="public-resources/js/react/components/modal-form-dialog.js"></script>
<script src="public-resources/js/react/components/modal-button.js"></script>
<script src="public-resources/js/react/components/data-grid.js"></script>
</head>
<body>

    <script type="text/javascript">
        React.renderComponent(Area({theme:"default",
            template: "public-resources/templates/main-area.html"}),
            document.body);
    </script>

</body>
</html>
