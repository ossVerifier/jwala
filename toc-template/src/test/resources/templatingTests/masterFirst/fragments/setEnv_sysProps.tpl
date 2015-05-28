<% attributes.each{k, v -> %>SET STP_OPTS=%STP_OPTS% -D${k}=${v}\n<% }%>
