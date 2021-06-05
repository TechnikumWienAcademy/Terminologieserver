<%-- 
    Document   : checkAlive
    Created on : 26.02.2018, 14:36:11
    Author     : dabac
--%>

<%@page contentType="text/html" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<%@ page import="de.fhdo.terminologie.helper.CheckAliveHelper"%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title>Terminologieserver</title>
    </head>
    <body>
        <%
            CheckAliveHelper cah = new CheckAliveHelper();
            out.print(cah.checkAlive());
        %>
    </body>
</html>
