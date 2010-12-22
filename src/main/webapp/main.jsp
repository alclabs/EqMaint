<%@ page import="com.controlj.addon.eqmaint.database.NoteData" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.List" %>
<%@ page import="com.controlj.addon.eqmaint.MainPageUtility" %>
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
          .bordered { border:solid gray 1px; }
          td { padding:3px;  }
          .borderless { border-style:none; }
      </style>
  </head>
  <body>
  <%
     MainPageUtility util = new MainPageUtility(request);

     // extract the lookup string and find the location's display name
     String lookupString = util.getParameter("lookup");
     pageContext.setAttribute("location", util.getDisplayName(lookupString));

     // determine if a date filter has been set
     String dateString = util.getParameter("filter_date");
     Date date = util.parseDate(dateString);
     pageContext.setAttribute("filter_date", date != null ? dateString : "MM/DD/YY");

     // determine if an operator filter has been set
     String operatorString = util.getParameter("filter_operator");
     pageContext.setAttribute("filter_operator", operatorString != null ? operatorString: "");

     // determine if a note type filter has been set
     String typeString = util.getParameter("filter_type");
     pageContext.setAttribute("filter_type", typeString != null ? typeString : "");

     // determine the current operator
     String operName = request.getUserPrincipal().getName();
     pageContext.setAttribute("cur_oper", operName);

     // determine if we are editing a note
     boolean wasEditing = util.getParameter("editid") != null;

     // handle the add and delete buttons
     if (util.getParameter("add_button") != null && !wasEditing)
        util.addNote(lookupString, operName, new Date(), util.getParameter("add_type"), util.getParameter("add_note"));
     else if (util.getParameter("delete_button") != null)
        util.deleteNote(Long.parseLong(request.getParameter("noteid")));

     // get notes that match the filtering criteria
     List<NoteData> dataList = util.getNotes(lookupString, date, operatorString, typeString);
     pageContext.setAttribute("datalist", dataList);

     // handle editing notes
     pageContext.setAttribute("is_edit", false);
     if (util.getParameter("edit_button") != null)
     {
        long editingId = Long.parseLong(util.getParameter("noteid"));
        NoteData data = util.getNoteWithId(dataList, editingId);
        if (data != null)
        {
           pageContext.setAttribute("is_edit", true);
           pageContext.setAttribute("edit_type", data.type);
           pageContext.setAttribute("edit_note", data.notes);
           pageContext.setAttribute("edit_id", data.id);
        }
     }
     else if (wasEditing)
     {
        long editingId = Long.parseLong(util.getParameter("editid"));
        NoteData data = util.getNoteWithId(dataList, editingId);
        if (data != null)
           util.editNote(data, request.getParameter("add_type"), request.getParameter("add_note"));
     }

     // add the known operators and types to their droplists
     pageContext.setAttribute("operators", util.getKnownOperators());
     pageContext.setAttribute("types", util.getKnownTypes());
  %>
  <form action="main.jsp" name="form1">
     <div>Location:&nbsp;${location}</div>
     <div>Add Note:</div>
     <table id="newnote" cellspacing="0" cellpadding="0">
        <tr>
           <td>Type: </td>
           <td><input name="add_type" type="text" value="${edit_type}" size="40"/></td>
        </tr>
        <tr>
           <td>Note:</td>
           <td>
              <textarea name="add_note" width="100%" height="100%" cols="50" rows="4">${edit_note}</textarea>
           </td>
        </tr>
        <tr>
           <td>&nbsp;</td>
           <td nowrap=""><input type="submit" name="add_button" value="${is_edit ? "Save Changes" : "Add Note"}"/></td>
        </tr>
     </table>

     <br/>
     <div>Filter by:</div>
     <table id="filter" cellspacing="0" cellpadding="0" >
        <tr>
           <td>Date:</td>
           <td><input type="text" name="filter_date" value="${filter_date}"/></td>
           <td style="padding-left:30px;"><input type="submit" value="Find Notes" name="find_button"/></td>
        </tr>
        <tr>
           <td>Operator:</td>
           <td>
              <select name="filter_operator">
                 <option value="">All</option>
                 <c:forEach var="name" items="${operators}">
                    <option value="${name}" ${name eq filter_operator ? "SELECTED":""}>${name}</option>
                 </c:forEach>
              </select>
           </td>
        </tr>
        <tr>
           <td>Type:</td>
           <td>
              <select name="filter_type">
                 <option value="">All</option>
                 <c:forEach var="name" items="${types}">
                    <option value="${name}" ${name eq filter_type ? "SELECTED":""}>${name}</option>
                 </c:forEach>
              </select>
           </td>
        </tr>
     </table>
     <input type="hidden" name="lookup" value="${param.lookup}"/>
     <input type="hidden" name="editid" value="${edit_id}"/>
     <input type="hidden" name="noteid"/>

     <br/>
     <table id="results" cellspacing="0" cellpadding="0">
        <c:forEach var="data" items="${datalist}">
           <tr height="0">
              <td colspan="2" class="borderless">&nbsp;</td>
           </tr>
           <tr>
              <td nowrap="" class="bordered">${data.date}</td>
              <td rowspan="3" width="100%" valign="top" class="bordered">${data.notes}</td>
              <td rowspan="3" valign="middle" class="borderless">
                 <input type="submit" name="edit_button" ${data.operator eq cur_oper ? "":"DISABLED"} onclick="document.form1.noteid.value=${data.id}" value="Edit Note"/>
                 <br/>
                 <input type="submit" name="delete_button" ${data.operator eq cur_oper ? "":"DISABLED"} onclick="document.form1.noteid.value=${data.id}" value="Delete Note"/>
              </td>
           </tr>
           <tr>
              <td class="bordered">${data.operator}</td>
           </tr>
           <tr>
              <td class="bordered">${data.type}</td>
           </tr>
        </c:forEach>
     </table>
  </form>
  </body>
</html>
