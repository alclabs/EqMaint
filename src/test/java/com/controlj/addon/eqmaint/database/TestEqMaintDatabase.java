/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2008 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)TestEqMaintDatabase

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.controlj.addon.eqmaint.database;

import com.controlj.green.addonsupport.AddOnInfo;
import com.controlj.green.addonsupport.xdatabase.DatabaseException;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.*;

@SuppressWarnings({ "deprecation" })
public class TestEqMaintDatabase extends TestCase
{
   private static final String lookupString = "my fake location";

   public NoteData data1;
   public NoteData data2;
   public NoteData data3;
   public NoteData data4;
   private EqMaintDatabase db;

   @Override protected void setUp() throws Exception
   {
      super.setUp();
      createTestNotes();

      AddOnInfo addOnInfo = AddOnInfo.getAddOnInfo();
      deleteDerbyDatabaseFiles(addOnInfo);

      List<NoteData> notes = Arrays.asList(data1, data2, data3, data4);
      db = EqMaintDatabase.getDatabaseForTest(addOnInfo.newDatabaseConnectionInfo());
      try
      {
         for (NoteData note : notes)
            db.addNote(lookupString, note);
      }
      catch (Throwable e)
      {
         e.printStackTrace();
      }
   }

   private void deleteDerbyDatabaseFiles(AddOnInfo addOnInfo) throws IOException
   {
      // we don't have a way to know where the files are stored, so this little hack will
      // get that information in a fragile way.  Users are NOT encouraged to do this outside
      // of tests.
      File publicDir = addOnInfo.getPublicDir();
      File databaseDir = new File(publicDir, "../../webapp_data/"+publicDir.getName()+"/database");
      if (databaseDir.exists())
         delete(databaseDir);
   }

   public static void delete(File fileOrDirectory)
      throws IOException
   {
      if (fileOrDirectory.isDirectory())
      {
         File[] filesInDir = fileOrDirectory.listFiles();
         if (filesInDir != null)
         {
            // delete an files that might be in the directory
            for (int i = 0; i < filesInDir.length; i++)
            {
               File file = filesInDir[i];
               delete(file);
            }
         }

         // delete the directory itself
         if (!fileOrDirectory.delete())
            throw new IOException("Cannot delete directory "+fileOrDirectory);
      }
      else
      {
         if (!fileOrDirectory.delete())
            throw new IOException("Cannot delete file "+fileOrDirectory);
      }
   }

   @Override protected void tearDown() throws Exception
   {
      db.close();
      super.tearDown();
   }

   public void testNothing() {}

   private void createTestNotes()
   {
      data1 = new NoteData();
      data1.date = new Date(74, 6, 18, 10, 15);
      data1.operator = "Papa";
      data1.type = "Filter";
      data1.notes = "Changed filter";

      data2 = new NoteData();
      data2.date = new Date(74, 6, 18, 11, 5);
      data2.operator = "Mama";
      data2.type = "Filter";
      data2.notes = "Changed filter back because Papa is dumb.  Filter was fine, just had to shake it to clean out the dirt.";

      data3 = new NoteData();
      data3.date = new Date(74, 6, 19, 4, 30);
      data3.operator = "Papa";
      data3.type = "Check";
      data3.notes = "Unit was making loud thumping noise.  Noticed that the filter was already dirty, but I changed it yesterday!";

      data4 = new NoteData();
      data4.date = new Date(74, 6, 20, 14, 0);
      data4.operator = "Papa";
      data4.type = "Repair";
      data4.notes = "Had to repair bad fan in unit, looks like it worked too hard.  Also replaced filter (again!).";
   }

   public void testGetAllNotes() throws DatabaseException
   {
      List<NoteData> foundNotes = db.getNotes(lookupString, null, null, null);
      compareNoteLists(Arrays.asList(data1, data2, data3, data4), foundNotes);
   }

   public void testGetNotesForDate() throws DatabaseException
   {
      List<NoteData> foundNotes = db.getNotes(lookupString, new Date(74, 6, 18), null, null);
      compareNoteLists(Arrays.asList(data1, data2), foundNotes);

      foundNotes = db.getNotes(lookupString, new Date(74, 6, 19), null, null);
      compareNoteLists(Arrays.asList(data3), foundNotes);

      foundNotes = db.getNotes(lookupString, new Date(74, 6, 20), null, null);
      compareNoteLists(Arrays.asList(data4), foundNotes);
   }

   public void testGetNotesForOperator() throws DatabaseException
   {
      List<NoteData> foundNotes = db.getNotes(lookupString, null, "Papa", null);
      compareNoteLists(Arrays.asList(data1, data3, data4), foundNotes);

      foundNotes = db.getNotes(lookupString, null, "Mama", null);
      compareNoteLists(Arrays.asList(data2), foundNotes);
   }

   public void testGetNotesForType() throws DatabaseException
   {
      List<NoteData> foundNotes = db.getNotes(lookupString, null, null, "Filter");
      compareNoteLists(Arrays.asList(data1, data2), foundNotes);

      foundNotes = db.getNotes(lookupString, null, null, "Check");
      compareNoteLists(Arrays.asList(data3), foundNotes);

      foundNotes = db.getNotes(lookupString, null, null, "Repair");
      compareNoteLists(Arrays.asList(data4), foundNotes);
   }

   public void testGetNotesForMulti() throws DatabaseException
   {
      List<NoteData> foundNotes = db.getNotes(lookupString, new Date(74, 6, 18), "Papa", "Filter");
      compareNoteLists(Arrays.asList(data1), foundNotes);

      foundNotes = db.getNotes(lookupString, new Date(74, 6, 18), "Mama", "Filter");
      compareNoteLists(Arrays.asList(data2), foundNotes);

      foundNotes = db.getNotes(lookupString, new Date(74, 6, 19), "Papa", "Filter");
      compareNoteLists(Collections.EMPTY_LIST, foundNotes);

      foundNotes = db.getNotes(lookupString, new Date(74, 6, 19), "Papa", "Check");
      compareNoteLists(Arrays.asList(data3), foundNotes);
   }

   public void testGetKnownOperators() throws DatabaseException
   {
      final List<String> expectedOpers = Arrays.asList("Mama", "Papa");
      final List<String> opers = db.getKnownOperators();
      assertEquals("Known operators incorrect", expectedOpers, opers);
   }

   public void testGetKnownTypes() throws DatabaseException
   {
      final List<String> expectedTypes = Arrays.asList("Check", "Filter", "Repair");
      final List<String> types = db.getKnownTypes();
      assertEquals("Known types incorrect", expectedTypes, types);
   }

   public void testEditNote() throws DatabaseException
   {
      NoteData note = db.getNotes(lookupString, null, null, null).iterator().next();
      compareNoteLists(Arrays.asList(data1), Collections.singletonList(note));

      note.type = "replacement";
      note.notes = "What, am I being replaced?";
      db.editNote(note);

      NoteData note2 = db.getNotes(lookupString, null, null, null).iterator().next();
      compareNoteLists(Collections.singletonList(note), Collections.singletonList(note2));
   }

   public void testDeleteNode() throws DatabaseException
   {
      List<NoteData> foundNotes = db.getNotes(lookupString, null, null, null);
      compareNoteLists(Arrays.asList(data1, data2, data3, data4), foundNotes);

      db.deleteNote(data3.id);

      List<NoteData> foundNotes2 = db.getNotes(lookupString, null, null, null);
      compareNoteLists(Arrays.asList(data1, data2, data4), foundNotes2);
   }

   private void compareNoteLists(List<NoteData> expected, List<NoteData> actual)
   {
      assertEquals("Lists not the same size", expected.size(), actual.size());
      int idx = 0;
      for (Iterator<NoteData> exIt = expected.iterator(), acIt = actual.iterator();exIt.hasNext();++idx)
      {
         NoteData expectedData = exIt.next();
         NoteData actualData = acIt.next();

         assertEquals("Date differs for index "+idx, expectedData.date, actualData.date);
         assertEquals("Operator differs for index "+idx, expectedData.operator, actualData.operator);
         assertEquals("Type differs for index "+idx, expectedData.type, actualData.type);
         assertEquals("Notes differs for index "+idx, expectedData.notes, actualData.notes);
      }
   }
}

