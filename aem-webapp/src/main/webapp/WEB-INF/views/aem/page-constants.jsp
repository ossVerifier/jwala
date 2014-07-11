<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

var tocVars = {};
<c:forEach var="javaScriptVariable" items="${javaScriptVariables}">
    tocVars["${javaScriptVariable.variableName}"] = ${javaScriptVariable.variableValue};
</c:forEach>