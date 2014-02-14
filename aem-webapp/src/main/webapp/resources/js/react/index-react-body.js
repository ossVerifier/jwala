 /** @jsx React.DOM */
React.renderComponent(
    <Tabs items={[{title: 'JVMs', content:

        <Grid />

                  },
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