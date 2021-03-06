<%@ page import="com.controlj.green.addonsupport.access.*" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Stack" %>
<%@ page import="com.controlj.green.addonsupport.access.util.LocationSort" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%--
  ~ Copyright (c) 2010 Automated Logic Corporation
  ~
  ~ Permission is hereby granted, free of charge, to any person obtaining a copy
  ~ of this software and associated documentation files (the "Software"), to deal
  ~ in the Software without restriction, including without limitation the rights
  ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  ~ copies of the Software, and to permit persons to whom the Software is
  ~ furnished to do so, subject to the following conditions:
  ~
  ~ The above copyright notice and this permission notice shall be included in
  ~ all copies or substantial portions of the Software.
  ~
  ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  ~ THE SOFTWARE.
  --%>

<html>
  <head>
      <title>Equipment Maintenance</title>
      <style type="text/css">
          body {font-family:sans-serif; color:black; }
          .label { text-align:right; }
          .error { border:solid red 1px; }
      </style>
  </head>
  <body>
  <%
     SystemConnection connection;
     try
     {
        class MyData
        {
           String name;
           boolean isEq;
           String lookupString;
           int depth;
        }

        final List<MyData> locs = new ArrayList<MyData>();
        connection = DirectAccess.getDirectAccess().getUserSystemConnection(request);
        connection.runReadAction(new ReadAction()
        {
           private Stack<Location> lastLocs = new Stack<Location>();
           public void execute(final SystemAccess access) throws Exception
           {
              Location treeRoot = access.getTree(SystemTree.Geographic).getRoot();
              access.visit(treeRoot, LocationSort.PRESENTATION, new Visitor(false)
              {
                 @Override public void visit(Location location)
                 {
                    try
                    {
                       while (!lastLocs.isEmpty() && !lastLocs.peek().equals(location.getParent()))
                          lastLocs.pop();
                    }
                    catch (UnresolvableException e)
                    {
                       return; // should never happen
                    }

                    lastLocs.push(location);

                    MyData data = new MyData();
                    data.name = location.getDisplayName();
                    data.isEq = location.getType() == LocationType.Equipment;
                    if (data.isEq)
                       data.lookupString = location.getPersistentLookupString(true);
                    data.depth = lastLocs.size();
                    locs.add(data);
                 }
              });
           }
        });

        for (MyData loc : locs)
        {
           if (loc.isEq)
           {
              %>
              <div style="margin-left:<%=loc.depth%>em;color:blue;cursor:pointer" onclick="parent.document.getElementById('main').src='main.jsp?lookup='+escape('<%=loc.lookupString%>')"><%=loc.name%></div>
              <%
           }
           else
           {
              %>
              <div style="margin-left:<%=loc.depth%>em"><%=loc.name%></div>
              <%
           }
        }
     }
     catch (Exception e)
     {
        e.printStackTrace();
     }
  %>
  </body>
</html>
