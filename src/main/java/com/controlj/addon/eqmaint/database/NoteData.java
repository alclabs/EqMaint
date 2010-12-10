/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2008 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)NoteData

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.controlj.addon.eqmaint.database;

import java.util.Date;

/**
 A very simple data holder class.  The getXXX() methods are defined (even though
 the fields are public) because it provides a bean-like interface for the web
 pages so that expression syntax works as expected.
 */
public class NoteData
{
   public long id;
   public Date date;
   public String operator;
   public String type;
   public String notes;

   public long getId()
   {
      return id;
   }

   public Date getDate()
   {
      return date;
   }

   public String getOperator()
   {
      return operator;
   }

   public String getType()
   {
      return type;
   }

   public String getNotes()
   {
      return notes;
   }

   @Override public String toString()
   {
      return date+" "+operator+" ("+type+") - "+notes;
   }
}

