Equipment Maintenance Sample
============================

The Equipment Maintenance Add-On is intended to demonstrate the use of the database API.

Try it out
----------
Deploy the Equipment Maintenance sample add-on by executing the 'deploy' task and starting (if necessary) the server.

Browse to http://yourserver/eqmaint. This should present a login page. Log in with any valid operator and password.

Clicking a link in the tree to the left will open the notes page for that location. This page has three basic sections:

* The top section allows new notes to be added. Adding a note consists of typing in a type and then some notes into the labeled
  fields. Then click the "Add Note" button to save that to the database.

* The middle section allows the list of shown notes to be filtered according to several criteria. Simply select the desired
  criteria and click the "Find Notes" button to filter.

* The bottom section shows the notes that have been entered for this location filtered according to any filtering criteria. Each
  note can be edited or deleted with the buttons to it's right.

Important Lessons
-----------------

Notice that the EqMaintDatabase class does not expose it's tables and columns, but instead interacts with a data holder (NoteData)
for most of methods. This provides encapsulation of the logic of the database, and allows it to maximize hiding of it's internal
implementation details.

The EqMaintDatabase wishes to implement a "connect on demand" for the database. That solves the application having to track state
and remember if the database has already been opened. This is easily done by EqMaintDatabase overriding the getConnection method.
See comments in that method and it's helper method connectOrCreate for details of how this is done.

SQL inner joins are an important feature of relational databases, but are not obvious to use. See the EqMaintDatabase.getNotes()
method for a thorough example of building a non-trivial query by using logic to build up the necessary query objects, and for using
joins. This method is the heart of the sample.

Notice how each table is defined in a seperate class and put in a "schema" subpackage below the "database" package. If the database
needs to be modified, this design will make it easier to provide the "old schema" that upgrade needs. Simply copy the "schema"
package to "v1" subpackage, for instance and then modify the classes in "schema" to the new database design (and change the version
in EqMaintDatabase to 2). This way, no "program logic" is getting copied and maintained but the old schema is still available.

The MainPageUtility class also shows an example of adding your own audit messages using the AuditLogManager. As maintenance messages are added, deleted, or edited, these changes are logged.