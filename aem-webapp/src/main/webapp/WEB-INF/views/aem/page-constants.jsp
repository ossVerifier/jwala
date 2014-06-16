<%@ page import = "java.util.Properties" %>
<%@ page import = "com.siemens.med.hs.soarian.config.PropertiesStore" %>

<%
    final String DEFAULT_LOAD_BALANCER_STATUS_MOUNT = "/jk/status";
    String loadBalancerStatusMount;
    try {
        Properties props = PropertiesStore.getProperties("TocFiles");
        loadBalancerStatusMount = props.getProperty("mod_jk.load-balancer.status.mount");
        if (loadBalancerStatusMount == null) {
            loadBalancerStatusMount = DEFAULT_LOAD_BALANCER_STATUS_MOUNT;
        }
    } catch(Exception e) {
        loadBalancerStatusMount = DEFAULT_LOAD_BALANCER_STATUS_MOUNT;
    }
%>

<script type="text/javascript">
    var contextPath = "${pageContext.request.contextPath}";
    var loadBalancerStatusMount = "<%=loadBalancerStatusMount%>";
</script>