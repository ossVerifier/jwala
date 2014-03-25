<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="x-ua-compatible" content="IE=Edge"/>
<title>Apache Enterprise Manager</title>
<link rel="stylesheet" type="text/css" href="public-resources/css/aem.css">
<link rel="stylesheet" type="text/css" href="public-resources/css/react/generic-components/default/area.css">
<link rel="stylesheet" type="text/css" href="public-resources/css/config-area.css">
<link rel="stylesheet" type="text/css" href="public-resources/css/react/generic-components/default/tabs.css">
<link rel="stylesheet" type="text/css" href="public-resources/css/react/generic-components/default/data-grid.css">
<link rel="stylesheet" type="text/css" href="public-resources/css/react/generic-components/default/toc-datatable.css">
<link rel="stylesheet" type="text/css" href="public-resources/css/themes/redmond/jquery-ui-1.10.4.custom.min.css">
<link rel="stylesheet" type="text/css" href="public-resources/css/jquery.dataTables.css">

<!-- JQuery, React, ES5, JSON2 -->
<script src="public-resources/ext/js/es5-shim/es5-shim-2.3.0.min.js"></script>
<script src="public-resources/ext/js/es5-shim/es5-sham.min.js"></script>
<script src="public-resources/ext/js/react/react-0.8.0.min.js"></script>
<script src="public-resources/ext/js/jquery/jquery-1.10.0.min.js"></script>
<script src="public-resources/ext/js/jquery/jquery-ui-1.10.4.custom.min.js"></script>
<script src="public-resources/ext/js/jquery/jquery.validate.min.js"></script>
<script src="public-resources/ext/js/jquery/jquery.dataTables.js"></script>
<script src="public-resources/ext/js/json2.min.js"></script>

<!--  Services -->
<script src="public-resources/ext/js/bluebird/bluebird-1.0.4.js"></script>
<script src="public-resources/js/toc/v1/service/serviceFoundation.js"></script>
<script src="public-resources/js/toc/v1/service/groupService.js"></script>
<script src="public-resources/js/toc/v1/service/jvmService.js"></script>
<script src="public-resources/js/toc/v1/service/serviceFactory.js"></script>

<!-- React UI -->
<script src="public-resources/js/react/generic-components/area.js"></script>
<script src="public-resources/js/react/generic-components/tabs.js"></script>
<script src="public-resources/js/react/generic-components/modal-form-add-dialog.js"></script>
<script src="public-resources/js/react/generic-components/modal-form-edit-dialog.js"></script>
<script src="public-resources/js/react/modal-button.js"></script>
<script src="public-resources/js/react/generic-components/data-grid.js"></script>
<script src="public-resources/js/react/generic-components/datatable.js"></script>
<script src="public-resources/js/react/delete-item-widget.js"></script>
<script src="public-resources/js/error-alert.js"></script>
<script src="public-resources/js/react/generic-components/generic-button.js"></script>
<script src="gen-public-resources/js/react/group-config.js"></script>
</head>
<body>

    <script type="text/javascript">
        React.renderComponent(Area({theme:"default",
            template: "public-resources/templates/main-area.html"}),
            document.body);
    </script>

</body>
</html>
