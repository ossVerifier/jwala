<html>
  <head>
    <title>React</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/public-resources/css/react/tab.css">
    <script src="public-resources/js/react/react-0.8.0.js"></script>
    <script src="public-resources/js/react/JSXTransformer-0.8.0.js"></script>
    <script src="public-resources/js/react/jquery-1.10.0.min.js"></script>
  </head>
  <body>
    <script src="public-resources/js/react/tab.js" type="text/jsx"></script>
    <script type="text/jsx">
        /** @jsx React.DOM */
        React.renderComponent(
            <Tabs items={[{title: 'JVMs', content: 'JVMs'},
                          {title: 'Web Servers', content: 'Web Servers'},
                          {title: 'Configure', content:

                <Tabs items={[{title: 'JVM', content: 'JVM'},
                              {title: 'Web Server', content: 'Web Server'},
                              {title: 'Web Apps', content: 'Web Apps'},
                              {title: 'Resources', content: 'Resources'},
                              {title: 'Group', content: 'Group'},
                              {title: 'Deploy', content: 'Deploy'}]} />

                          }]} />,
            document.body
        );
    </script>
  </body>
</html>