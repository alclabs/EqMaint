/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2008 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)OperatorTable

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.controlj.addon.eqmaint.database.schema;

import com.controlj.green.addonsupport.xdatabase.DatabaseSchema;
import com.controlj.green.addonsupport.xdatabase.TableSchema;
import com.controlj.green.addonsupport.xdatabase.column.IntColumn;
import com.controlj.green.addonsupport.xdatabase.column.StringColumn;

/**
 Simple class for defining the operators table.  Because the fields are
 final, there is not safety probem making them public.  And not having
 accessor method makes code that references these columns much easier
 to read.
 */
public class OperatorTable
{
   public final TableSchema schema;
   public final IntColumn id;
   public final StringColumn name;

   public OperatorTable(DatabaseSchema db)
   {
      schema = db.addTable("Operators");
      id = schema.addIntColumn("ID_");
      name = schema.addStringColumn("Name_", 100);

      schema.setPrimaryKey(id);
      schema.setAutoGenerate(id);
   }
}

