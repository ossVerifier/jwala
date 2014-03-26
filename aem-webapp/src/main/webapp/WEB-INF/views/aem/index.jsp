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

<jsp:include page="/scripts?devMode=${devMode}"/>

</head>
<body>

    <script type="text/javascript">
        React.renderComponent(Area({theme:"default",
            template: "public-resources/templates/main-area.html"}),
            document.body);
    </script>

</body>
</html>