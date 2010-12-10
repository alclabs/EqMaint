/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2008 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)Database

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.controlj.addon.eqmaint.database;

import com.controlj.green.addonsupport.xdatabase.*;
import com.controlj.green.addonsupport.xdatabase.type.SQLBool;
import com.controlj.addon.eqmaint.database.schema.OperatorTable;
import com.controlj.addon.eqmaint.database.schema.TypeTable;
import com.controlj.addon.eqmaint.database.schema.NotesTable;

import java.util.*;

/**
 Provides high-level access to the database.  Abstracts how notes are stored
 and retrieved to provide better encapsulation of the storage mechanism.
 */
public class EqMaintDatabase extends Database
{
   /** the singleton instance */
   private static final EqMaintDatabase instance = new EqMaintDatabase(XDatabase.getXDatabase().newDatabaseConnectionInfo());

   /** constant used to define an ID that is not possible to get normally */
   private static final int INVALID_ID = -1;

   /** the connection info -- storing this allows the tests to specify different connections */
   private final DatabaseConnectionInfo connInfo;

   /** the operator table -- stores all known operators and an associated id */
   private final OperatorTable operatorTable;

   /** the types table -- stores all known note types and an associated id */
   private final TypeTable typeTable;

   /** the notes table -- primary table that stores all the notes */
   private final NotesTable notesTable;

   /**
    Accessor for the singleton instance.  Because the constructors are private
    this is the only way to get an instance of the database.  This ensures
    that only one instance will ever be created.
    @return the one and only database instance.
    */
   public static EqMaintDatabase getDatabase()
   {
      return instance;
   }

   /**
    Alternate accessor for tests.  This breaks the singleton rule by creating a
    seperate instance of the database for each invokation.  The tests are aware
    of this, however, and need that behavior to allow each test to be isolated
    from the other tests.
    @param connInfo the connection info to use.
    @return a new database instance that will connect using the given connInfo.
    */
   public static EqMaintDatabase getDatabaseForTest(DatabaseConnectionInfo connInfo)
   {
      return new EqMaintDatabase(connInfo);
   }

   /**
    Constructor.  Initializes the database by creating the tables.
    @param connInfo
    */
   private EqMaintDatabase(DatabaseConnectionInfo connInfo)
   {
      super("EqMaint", 1);
      this.connInfo = connInfo;
      operatorTable = new OperatorTable(schema);
      typeTable = new TypeTable(schema);
      notesTable = new NotesTable(schema, operatorTable, typeTable);
   }

   /**
    Overrides {@link Database#getConnection()} to allow for "connect on demand"
    semantics.  Because this method is not defined to throw an exception, failures
    to create or connect to the database are rethrown as a RuntimeException.  This
    is OK because we are using Derby and these types errors should not normally
    occur (the user can't have specified the wrong machine for the database server,
    for instance).
    @return
    */
   @Override protected DatabaseConnection getConnection()
   {
      try
      {
         return super.getConnection();
      }
      catch (IllegalStateException e)
      {
         // this is thrown if the database is not yet connected, so
         // we will connect on demand.  If the database has not been
         // created, we will also create it on demand.
         try
         {
            connectOrCreate();
         }
         catch (DatabaseException databaseException)
         {
            throw new RuntimeException(databaseException);
         }

         // now that we've connected/created the database,
         // return the connection.
         return super.getConnection();
      }
   }

   /**
    Helper method to connect to the database.  If the database has
    not yet been created, create it.  Either way, when this method
    returns, the database is connected (unless an exception occurs).
    @throws DatabaseException if any database error occurs.
    */
   private void connectOrCreate() throws DatabaseException
   {
      try
      {
         connect(connInfo);
      }
      catch (DatabaseConnectionException connectionException)
      {
         throw new DatabaseException("Cannot establish communications with the database server", connectionException);
      }
      catch (DatabaseException databaseException)
      {
         // this exception means that the database has not been
         // created, so we'll create it now
         create(connInfo);
      }
      catch (DatabaseVersionMismatchException mismatchException)
      {
         throw new DatabaseException("Cannot open the database because it is a different version than expected", mismatchException);
      }
   }

   /**
    Adds the given note to the location identified by lookup string.
    @param locationLookupString the lookup string of the location.
    @param note the note to add.
    @throws DatabaseException if any database error occurs.
    */
   public void addNote(String locationLookupString, NoteData note) throws DatabaseException
   {
      // find the id for the operator, or add the operator to the database and return it's new id
      int operId = findOrAddOperator(note.operator);

      // find the id for the note type, or add the type to the database and return it's new id
      int typeId = findOrAddType(note.type);

      // insert the data for the note.  We should always get an autogenerated id for this new note.
      Number n = insert(notesTable.date.set(note.date), notesTable.location.set(locationLookupString), notesTable.operId.set(operId), notesTable.typeId.set(typeId), notesTable.notes.set(note.notes));
      if (n == null)
         throw new DatabaseException("Internal Error: insert did not auto-generate a new note id");

      // update the id field in the note to refer to it's new id
      note.id = n.longValue();
   }

   /**
    Edit the given note.  We know what to edit in the database by using
    the note.id.  The rest of the note data could have changed, so just
    be lazy and update all fields.
    @param note the note containing the changes to update in the database.
    @throws DatabaseException if any database error occurs.
    */
   public void editNote(NoteData note) throws DatabaseException
   {
      // find the id for the note type, or add the type to the database and return it's new id
      int typeId = findOrAddType(note.type);

      // update the note in the database.  The where clause limits the changes to effect just
      // the one row whose id matches the note.id.
      execute(buildUpdate(notesTable.typeId.set(typeId), notesTable.notes.set(note.notes)).where(notesTable.id.eq(note.id)));
   }

   /**
    Delete the note with the given id.
    @param id the id of the note to delete from the database.
    @throws DatabaseException if any database error occurs.
    */
   public void deleteNote(long id) throws DatabaseException
   {
      execute(buildDelete(notesTable.schema).where(notesTable.id.eq(id)));
   }

   /**
    Returns notes that match the given criteria.  If any arguments are null,
    the results are not filtered by that criteria (null means "don't care").
    @param locationLookupString lookup string of the current location - cannot be null.
    @param date filters to 24 hours (so hours, minutes, seconds should be zero) - can be null.
    @param operator filters for the given operator only - can be null.
    @param type filters for the given type only - can be null.
    */
   public List<NoteData> getNotes(final String locationLookupString, final Date date, final String operator, final String type) throws DatabaseException
   {
      // run a query whose result is a List of NoteData objects, and just return that list.
      return runQuery(new QueryTask<List<NoteData>>()
      {
         public List<NoteData> execute(DatabaseReadAccess access) throws DatabaseException
         {
            // build the body of the select, but without the predicate (where clause)
            Query query = schema.buildSelect(notesTable.id, notesTable.date, operatorTable.name, typeTable.name, notesTable.notes);

            // start buiding the predicate -- we always have a location lookup string, so no test for null
            SQLBool predicate = notesTable.location.eq(locationLookupString);

            // if a date was not specified, it just doesn't go into the predicate
            if (date != null)
            {
               // a date was specified, so figure out the day after so that we can do a "between"
               // on the date column.
               Calendar nextDay = Calendar.getInstance();
               nextDay.setTime(date);
               nextDay.add(Calendar.DAY_OF_YEAR, 1);

               // by using the "before" next day this predicate excludes next day, but the
               // "not before" date includes date.  So, we get only the date we can around.
               predicate = predicate.and(notesTable.date.before(nextDay.getTime()).and(notesTable.date.notBefore(date)));
            }

            // if an operator was specified, add it to the predicate.  Note that this is referencing
            // the operator table.  So, we'll need a join in the query to support this.
            if (operator != null)
               predicate = predicate.and(operatorTable.name.eq(operator));

            // if a type was specified, add it to the predicate.  Note that this is referencing
            // the type table.  So, we'll need a join in the query to support this.
            if (type != null)
               predicate = predicate.and(typeTable.name.eq(type));

            // execute the query, with the predicate we've built up.  Order by date (for the UI's
            // convenience), and specify the joins.  If we were not filtering by operator or type
            // then the joinUsing wouldn't be necessary, but it's ignored if not needed so we just
            // always specify it.
            Result result = access.execute(query.where(predicate).orderBy(notesTable.date.asc()).joinUsing(notesTable.operFK, notesTable.typeFK));

            // extract the results of the query and put them into a list.  Note that getting the
            // id uses the auto-unboxing feature of Java5 to convert the Integer return into an
            // int primative.  This is safe because that field is marked as not allowing null, so
            // we should never read a null from the database.  If we did, that line would generate
            // a NullPointerException.
            List<NoteData> notes = new ArrayList<NoteData>();
            while (result.next())
            {
               NoteData data = new NoteData();
               data.id = result.get(notesTable.id);
               data.date = result.get(notesTable.date);
               data.operator = result.get(operatorTable.name);
               data.type = result.get(typeTable.name);
               data.notes = result.get(notesTable.notes);
               notes.add(data);
            }

            // return our list of notes that have been read from the database
            return notes;
         }
      });
   }

   /**
    Returns all the operators that are in the database (in the operator table).
    This method returns the operators in a list that has been sorted by name (ascending).
    @throws DatabaseException if any database error occurs.
    */
   public List<String> getKnownOperators() throws DatabaseException
   {
      // run a query whose result is a List of Strings, and just return that list.
      return runQuery(new QueryTask<List<String>>()
      {
         public List<String> execute(DatabaseReadAccess access) throws DatabaseException
         {
            // select all operators and order them by name (ascending)
            Result result = access.execute(schema.buildSelect(operatorTable.name).orderBy(operatorTable.name.asc()));

            // put the query results into a list
            List<String> names = new ArrayList<String>();
            while (result.next())
               names.add(result.get(operatorTable.name));

            // return the list of all operators in the database
            return names;
         }
      });
   }

   /**
    Returns all the note types that are in the database (in the type table).
    This method returns the types in a list that has been sorted by name (ascending).
    @throws DatabaseException if any database error occurs.
    */
   public List<String> getKnownTypes() throws DatabaseException
   {
      // run a query whose result is a List of Strings, and just return that list.
      return runQuery(new QueryTask<List<String>>()
      {
         public List<String> execute(DatabaseReadAccess access) throws DatabaseException
         {
            // select all types and order them by name (ascending)
            Result result = access.execute(schema.buildSelect(typeTable.name).orderBy(typeTable.name.asc()));

            // put the query results into a list
            List<String> names = new ArrayList<String>();
            while (result.next())
               names.add(result.get(typeTable.name));

            // return the list of all operators in the database
            return names;
         }
      });
   }

   /**
    Helper method to find the id for the given operator.  If the operator cannot be
    found, then add the operator and return the id generated by the insert.
    @param operator the operator to find or add.
    @return the id for the given operator.
    @throws DatabaseException if any database error occurs.
    */
   private int findOrAddOperator(final String operator) throws DatabaseException
   {
      synchronized (operatorTable)
      {
         // run a query whose result is an Integer, and because we'll never return null, put it
         // in a primitive int.
         int id = runQuery(new QueryTask<Integer>()
         {
            public Integer execute(DatabaseReadAccess access) throws DatabaseException
            {
               // select the id of the specified operator
               Result result = access.execute(schema.buildSelect(operatorTable.id).where(operatorTable.name.eq(operator)));

               // if the operator was found, we'll have a result so just return the found id.  If the
               // operator was not found, we won't get a result, so return an invalid id that can be
               // tested.
               if (result.next())
                  return result.get(operatorTable.id);
               else
                  return INVALID_ID;
            }
         });

         // if the operator was not found (we returned an INVALID_ID, which can only happen if
         // we couldn't find the operator), then insert the operator into the table and save off
         // the newly generated id.  Because the id should always be generated, we won't check
         // for a null return value.
         if (id == INVALID_ID)
            id = insert(operatorTable.name.set(operator)).intValue();

         // if the id returned from the query was invalid, we just picked a new valid one.
         // Either way, id should now be valid for this operator, so return it.
         return id;
      }
   }

   /**
    Helper method to find the id for the given note type.  If the type cannot be
    found, then add the type and return the id generated by the insert.
    @param type the note type to find or add.
    @return the id for the given type.
    @throws DatabaseException if any database error occurs.
    */
   private int findOrAddType(final String type) throws DatabaseException
   {
      synchronized (typeTable)
      {
         // run a query whose result is an Integer, and because we'll never return null, put it
         // in a primitive int.
         int id = runQuery(new QueryTask<Integer>()
         {
            public Integer execute(DatabaseReadAccess access) throws DatabaseException
            {
               // select the id of the specified type
               Result result = access.execute(schema.buildSelect(typeTable.id).where(typeTable.name.eq(type)));

               // if the type was found, we'll have a result so just return the found id.  If the
               // type was not found, we won't get a result, so return an invalid id that can be
               // tested.
               if (result.next())
                  return result.get(typeTable.id);
               else
                  return INVALID_ID;
            }
         });

         // if the type was not found (we returned an INVALID_ID, which can only happen if
         // we couldn't find the type), then insert the type into the table and save off
         // the newly generated id.  Because the id should always be generated, we won't check
         // for a null return value.
         if (id == INVALID_ID)
            id = insert(typeTable.name.set(type)).intValue();

         // if the id returned from the query was invalid, we just picked a new valid one.
         // Either way, id should now be valid for this type, so return it.
         return id;
      }
   }
}