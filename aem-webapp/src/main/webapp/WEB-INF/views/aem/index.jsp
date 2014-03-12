<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="x-ua-compatible" content="IE=Edge"/>
<title>Apache Enterprise Manager</title>
<link rel="stylesheet" type="text/css" href="public-resources/css/aem.css">
<link rel="stylesheet" type="text/css" href="public-resources/css/react/components/default/area.css">
<link rel="stylesheet" type="text/css" href="public-resources/css/jvm-config-area.css">
<link rel="stylesheet" type="text/css" href="public-resources/css/react/components/default/tabs.css">
<link rel="stylesheet" type="text/css" href="public-resources/css/react/components/default/data-grid.css">
<link rel="stylesheet" type="text/css" href="public-resources/css/react/components/default/modal-form-dialog.css">
<link rel="stylesheet" type="text/css" href="public-resources/css/themes/redmond/jquery-ui-1.10.4.custom.min.css">
<script src="public-resources/js/react/es5-shim-2.3.0.min.js"></script>
<script src="public-resources/js/react/es5-sham.min.js"></script>
<script src="public-resources/js/react/react-0.8.0.min.js"></script>
<script src="public-resources/js/react/jquery-1.10.0.min.js"></script>
<script src="public-resources/js/react/jquery-ui-1.10.4.custom.min.js"></script>
<script src="public-resources/js/json2.min.js"></script>
<script src="public-resources/js/react/components/area.js"></script>
<script src="public-resources/js/react/components/tabs.js"></script>
<script src="public-resources/js/react/components/modal-form-add-dialog.js"></script>
<script src="public-resources/js/react/components/modal-form-edit-dialog.js"></script>
<script src="public-resources/js/react/components/modal-button.js"></script>
<script src="public-resources/js/react/components/data-grid.js"></script>
<script src="public-resources/js/react/components/delete-item-widget.js"></script>
</head>
<body>

    <script type="text/javascript">
        React.renderComponent(Area({theme:"default",
            template: "public-resources/templates/main-area.html"}),
            document.body);
    </script>

    <!--
    <script>
      $(function() {
        $( "#dialog" ).dialog();
      });
    </script>

    <div id="dialog" title="Basic dialog">
      <p>This is the default dialog which is useful for displaying information. The dialog window can be moved, resized and closed with the 'x' icon.</p>
    </div>
    -->

</body>
</html>
