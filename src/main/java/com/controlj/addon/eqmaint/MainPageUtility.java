/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2008 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)MainPageUtility

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.controlj.addon.eqmaint;

import com.controlj.green.addonsupport.access.*;
import com.controlj.green.addonsupport.AddOnInfo;
import com.controlj.green.addonsupport.InvalidConnectionRequestException;
import com.controlj.green.addonsupport.xdatabase.DatabaseException;
import com.controlj.addon.eqmaint.database.NoteData;
import com.controlj.addon.eqmaint.database.EqMaintDatabase;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.text.DateFormat;
import java.text.ParseException;

/**
 Utility class used by main.jsp
 */
public class MainPageUtility
{
   private final HttpServletRequest request;
   private final SystemConnection connection;
   private final DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT);

   public MainPageUtility(HttpServletRequest request) throws InvalidConnectionRequestException
   {
      this.request = request;
      connection = AddOnInfo.getAddOnInfo().getUserSystemConnection(request);
   }

   public String getParameter(String paramName)
   {
      String paramValue = request.getParameter(paramName);
      if (paramValue != null && paramValue.length() == 0)
         paramValue = null;

      return paramValue;
   }
   
   public String getDisplayName(final String lookupString) throws SystemException, ActionExecutionException
   {
      return connection.runReadAction(new ReadActionResult<String>()
      {
         public String execute(SystemAccess access) throws Exception
         {
            return access.getTree(SystemTree.Geographic).resolve(lookupString).getDisplayName();
         }
      });
   }

   private Location getLocation(final String lookupString) throws SystemException, ActionExecutionException
   {
      return connection.runReadAction(new ReadActionResult<Location>()
      {
         public Location execute(SystemAccess access) throws Exception
         {
            return access.getTree(SystemTree.Geographic).resolve(lookupString);
         }
      });
   }

   public Date parseDate(String dateString)
   {
      if (dateString == null)
      {
         return null;
      }
      
      try
      {
         return dateFormatter.parse(dateString);
      }
      catch (ParseException e)
      {
         return null;
      }
   }

   public void addNote(String lookupString, String operName, Date date, String type, String note) throws DatabaseException
   {
      NoteData data = new NoteData();
      data.operator = operName;
      data.date = date;
      data.type = type;
      data.notes = note;

       /*
       try {
           connection.getAuditLogManager().addEntry(getLocation(lookupString), "Adding maintenance note");
       } catch (SystemException e) { } // ignore exceptions and just don't add message
       catch (ActionExecutionException e) { }
       */
       EqMaintDatabase.getDatabase().addNote(lookupString, data);
   }

   public List<NoteData> getNotes(String lookupString, Date date, String operatorString, String typeString) throws DatabaseException
   {
      return EqMaintDatabase.getDatabase().getNotes(lookupString, date, operatorString, typeString);
   }

   public NoteData getNoteWithId(List<NoteData> dataList, long editingId)
   {
      for (NoteData data : dataList)
      {
         if (data.id == editingId)
         return data;
      }
      return null;
   }

   public void editNote(String lookupString, NoteData data, String type, String note) throws DatabaseException
   {
      data.type = type;
      data.notes = note;
       /*
       try {
           connection.getAuditLogManager().addEntry(getLocation(lookupString), "Editing maintenance note");
       } catch (SystemException e) { } // ignore exceptions and just don't add message
       catch (ActionExecutionException e) { }
       */
       EqMaintDatabase.getDatabase().editNote(data);
   }

   public void deleteNote(String lookupString, long noteId) throws DatabaseException
   {
       /*
       try {
           connection.getAuditLogManager().addEntry(getLocation(lookupString), "Removing maintenance note");
       } catch (SystemException e) { } // ignore exceptions and just don't add message
       catch (ActionExecutionException e) { }
       */
       EqMaintDatabase.getDatabase().deleteNote(noteId);
   }

   public List<String> getKnownOperators() throws DatabaseException
   {
      return EqMaintDatabase.getDatabase().getKnownOperators();
   }

   public List<String> getKnownTypes() throws DatabaseException
   {
      return EqMaintDatabase.getDatabase().getKnownTypes();
   }
}

