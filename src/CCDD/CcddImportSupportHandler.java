/**
 * CFS Command & Data Dictionary import support handler.
 *
 * Copyright 2017 United States Government as represented by the Administrator of the National
 * Aeronautics and Space Administration. No copyright is claimed in the United States under Title
 * 17, U.S. Code. All Other Rights Reserved.
 */
package CCDD;

import static CCDD.CcddConstants.CANCEL_BUTTON;
import static CCDD.CcddConstants.IGNORE_BUTTON;

import java.awt.Component;

import CCDD.CcddClassesDataTable.CCDDException;
import CCDD.CcddClassesDataTable.TableDefinition;
import CCDD.CcddClassesDataTable.TableTypeDefinition;
import CCDD.CcddConstants.ApplicabilityType;
import CCDD.CcddConstants.InputDataType;
import CCDD.CcddConstants.InternalTable.FieldsColumn;
import CCDD.CcddConstants.TableTypeEditorColumnInfo;

/**************************************************************************************************
 * CFS Command & Data Dictionary import support handler class
 *************************************************************************************************/
public class CcddImportSupportHandler
{
    /**********************************************************************************************
     * Argument column name types
     *********************************************************************************************/
    protected static enum ArgumentColumnName
    {
        APP_ID("ApplicationID", "Application ID"),
        FUNC_CODE("CommandFunctionCode", "Command Function Code");

        private final String ancillaryName;
        private final String defaultArgColName;

        /******************************************************************************************
         * Argument column name types constructor
         *
         * @param ancillaryName
         *            name used as the identifying tag in the ancillary data
         *
         * @param defaultArgColName
         *            default argument column name
         *****************************************************************************************/
        ArgumentColumnName(String ancillaryName, String defaultArgColName)
        {
            this.ancillaryName = ancillaryName;
            this.defaultArgColName = defaultArgColName;
        }

        /******************************************************************************************
         * Get the name used as the identifying tag in the ancillary data
         *
         * @return Name used as the identifying tag in the ancillary data
         *****************************************************************************************/
        protected String getAncillaryName()
        {
            return ancillaryName;
        }

        /******************************************************************************************
         * Get the default argument column name
         *
         * @return Default argument column name
         *****************************************************************************************/
        protected String getDefaultArgColName()
        {
            return defaultArgColName;
        }
    }

    /**********************************************************************************************
     * Add a table type column definition after verifying the input parameters
     *
     * @param continueOnError
     *            current state of the flag that indicates if all table type errors should be
     *            ignored
     *
     * @param defn
     *            reference to the TableTypeDefinition to which this column definition applies
     *
     * @param columnDefn
     *            array containing the table type column definition
     *
     * @param fileName
     *            import file name
     *
     * @param parent
     *            GUI component calling this method
     *
     * @return true if the user elected to ignore the column error
     *
     * @throws CCDDException
     *             If the column name is missing or the user elects to stop the import operation
     *             due to an invalid input data type
     *********************************************************************************************/
    protected boolean addImportedTableTypeDefinition(boolean continueOnError,
                                                     TableTypeDefinition tableTypeDefn,
                                                     String[] columnDefn,
                                                     String fileName,
                                                     Component parent) throws CCDDException
    {
        // Check if the column name is empty
        if (columnDefn[TableTypeEditorColumnInfo.NAME.ordinal()].isEmpty())
        {
            // Inform the user that the column name is missing
            throw new CCDDException("Table type '"
                                    + tableTypeDefn.getTypeName()
                                    + "' definition column name missing in import file '</b>"
                                    + fileName
                                    + "<b>'");
        }

        // Check if the input type is empty
        if (columnDefn[TableTypeEditorColumnInfo.INPUT_TYPE.ordinal()].isEmpty())
        {
            // Default to text
            columnDefn[TableTypeEditorColumnInfo.INPUT_TYPE.ordinal()] = InputDataType.TEXT.getInputName();
        }
        // Check if the input type name is invalid
        else if (InputDataType.getInputTypeByName(columnDefn[TableTypeEditorColumnInfo.INPUT_TYPE.ordinal()]) == null)
        {
            // Check if the error should be ignored or the import canceled
            continueOnError = getErrorResponse(continueOnError,
                                               "<html><b>Table type '"
                                                                + tableTypeDefn.getTypeName()
                                                                + "' definition input type '"
                                                                + columnDefn[TableTypeEditorColumnInfo.INPUT_TYPE.ordinal()]
                                                                + "' unrecognized in import file '</b>"
                                                                + fileName
                                                                + "<b>'; continue?",
                                               "Table Type Error",
                                               "Ignore this error (default to 'Text')",
                                               "Ignore this and any remaining invalid table types (use default "
                                                                                        + "values where possible, or skip the affected table type)",
                                               "Stop importing",
                                               parent);

            // Default to text
            columnDefn[TableTypeEditorColumnInfo.INPUT_TYPE.ordinal()] = InputDataType.TEXT.getInputName();
        }

        // Add the table type column definition
        tableTypeDefn.addColumn(new Object[] {Integer.valueOf(columnDefn[TableTypeEditorColumnInfo.INDEX.ordinal()]),
                                              columnDefn[TableTypeEditorColumnInfo.NAME.ordinal()],
                                              columnDefn[TableTypeEditorColumnInfo.DESCRIPTION.ordinal()],
                                              columnDefn[TableTypeEditorColumnInfo.INPUT_TYPE.ordinal()],
                                              Boolean.valueOf(columnDefn[TableTypeEditorColumnInfo.UNIQUE.ordinal()]),
                                              Boolean.valueOf(columnDefn[TableTypeEditorColumnInfo.REQUIRED.ordinal()]),
                                              Boolean.valueOf(columnDefn[TableTypeEditorColumnInfo.STRUCTURE_ALLOWED.ordinal()]),
                                              Boolean.valueOf(columnDefn[TableTypeEditorColumnInfo.POINTER_ALLOWED.ordinal()])});

        return continueOnError;
    }

    /**********************************************************************************************
     * Add a data field definition after verifying the input parameters
     *
     * @param continueOnError
     *            current state of the flag that indicates if all data field errors should be
     *            ignored
     *
     * @param defn
     *            TableTypeDefinition or TableDefinition object to which this data field applies
     *
     * @param fieldDefn
     *            array containing the data field definition
     *
     * @param fileName
     *            import file name
     *
     * @param parent
     *            GUI component calling this method
     *
     * @return true if the user elected to ignore the data field error
     *
     * @throws CCDDException
     *             If the data field name is missing or the user elects to stop the import
     *             operation due to an invalid input data type
     *********************************************************************************************/
    protected boolean addImportedDataFieldDefinition(boolean continueOnError,
                                                     Object defn,
                                                     String[] fieldDefn,
                                                     String fileName,
                                                     Component parent) throws CCDDException
    {
        // Check if the field name is empty
        if (fieldDefn[FieldsColumn.FIELD_NAME.ordinal()].isEmpty())
        {
            // Inform the user that the field name is missing
            throw new CCDDException("Data field name missing in import file <br>'</b>"
                                    + fileName
                                    + "<b>'");
        }

        // Check if the field size is empty
        if (fieldDefn[FieldsColumn.FIELD_SIZE.ordinal()].isEmpty())
        {
            // Use the default value
            fieldDefn[FieldsColumn.FIELD_SIZE.ordinal()] = "10";
        }

        // Check if the input type is empty
        if (fieldDefn[FieldsColumn.FIELD_TYPE.ordinal()].isEmpty())
        {
            // Default to text
            fieldDefn[FieldsColumn.FIELD_TYPE.ordinal()] = InputDataType.TEXT.getInputName();
        }
        // Check if the input type name is invalid
        else if (InputDataType.getInputTypeByName(fieldDefn[FieldsColumn.FIELD_TYPE.ordinal()]) == null)
        {
            // Check if the error should be ignored or the import canceled
            continueOnError = getErrorResponse(continueOnError,
                                               "<html><b>Data field '"
                                                                + fieldDefn[FieldsColumn.FIELD_NAME.ordinal()]
                                                                + "' definition input type '"
                                                                + fieldDefn[FieldsColumn.FIELD_TYPE.ordinal()]
                                                                + "' unrecognized in import file '</b>"
                                                                + fileName
                                                                + "<b>'; continue?",
                                               "Data Field Error",
                                               "Ignore this data field error (default to 'Text')",
                                               "Ignore this and any remaining invalid data fields (use default "
                                                                                                   + "values where possible, or skip the affected data field)",
                                               "Stop importing",
                                               parent);

            // Default to text
            fieldDefn[FieldsColumn.FIELD_TYPE.ordinal()] = InputDataType.TEXT.getInputName();
        }

        // Check if the applicability is empty
        if (fieldDefn[FieldsColumn.FIELD_APPLICABILITY.ordinal()].isEmpty())
        {
            // Default to all tables being applicable
            fieldDefn[FieldsColumn.FIELD_APPLICABILITY.ordinal()] = ApplicabilityType.ALL.getApplicabilityName();
        }
        // Check if the applicability is invalid
        else if (ApplicabilityType.getApplicabilityByName(fieldDefn[FieldsColumn.FIELD_APPLICABILITY.ordinal()]) == null)
        {
            // Check if the error should be ignored or the import canceled
            continueOnError = getErrorResponse(continueOnError,
                                               "<html><b>Data field '"
                                                                + fieldDefn[FieldsColumn.FIELD_NAME.ordinal()]
                                                                + "' definition applicability type '"
                                                                + fieldDefn[FieldsColumn.FIELD_APPLICABILITY.ordinal()]
                                                                + "' unrecognized in import file '</b>"
                                                                + fileName
                                                                + "<b>'; continue?",
                                               "Data Field Error",
                                               "Ignore this data field error (default to 'All tables')",
                                               "Ignore this and any remaining invalid data fields (use default values)",
                                               "Stop importing",
                                               parent);

            // Default to all tables being applicable
            fieldDefn[FieldsColumn.FIELD_APPLICABILITY.ordinal()] = ApplicabilityType.ALL.getApplicabilityName();
        }

        // Check if the field belongs to a table
        if (defn instanceof TableDefinition)
        {
            // Add the data field to the table
            ((TableDefinition) defn).addDataField(fieldDefn);
        }
        // Check if the field belongs to a table type
        else if (defn instanceof TableTypeDefinition)
        {
            // Add the data field to the table type
            ((TableTypeDefinition) defn).addDataField(fieldDefn);
        }

        return continueOnError;
    }

    /**********************************************************************************************
     * Display an Ignore/Ignore All/Cancel dialog in order to get the response to an error
     * condition. The user may elect to ignore the one instance of this type of error, all
     * instances of this type of error, or cancel the operation
     *
     * @param continueOnError
     *            current state of the flag that indicates if all errors of this type should be
     *            ignored
     *
     * @param message
     *            text message to display
     *
     * @param title
     *            title to display in the dialog window frame
     *
     * @param ignoreToolTip
     *            Ignore button tool tip text; null if no tool tip is to be displayed
     *
     * @param ignoreAllToolTip
     *            Ignore All button tool tip text; null if no tool tip is to be displayed
     *
     * @param cancelToolTip
     *            Cancel button tool tip text; null if no tool tip is to be displayed
     *
     * @param parent
     *            GUID component calling this method
     *
     * @return true if the user elected to ignore errors of this type
     *
     * @throws CCDDException
     *             If the user selects the Cancel button
     *********************************************************************************************/
    protected boolean getErrorResponse(boolean continueOnError,
                                       String message,
                                       String title,
                                       String ignoreToolTip,
                                       String ignoreAllToolTip,
                                       String cancelToolTip,
                                       Component parent) throws CCDDException
    {
        // Check if the user hasn't already elected to ignore this type of error
        if (!continueOnError)
        {
            // Inform the user that the imported item is incorrect
            int buttonSelected = new CcddDialogHandler().showIgnoreCancelDialog(parent,
                                                                                message,
                                                                                title,
                                                                                ignoreToolTip,
                                                                                ignoreAllToolTip,
                                                                                cancelToolTip);

            // Check if the Ignore All button was pressed
            if (buttonSelected == IGNORE_BUTTON)
            {
                // Set the flag to ignore subsequent errors of this type
                continueOnError = true;
            }
            // Check if the Cancel button was pressed
            else if (buttonSelected == CANCEL_BUTTON)
            {
                // No error message is provided since the user chose this action
                throw new CCDDException();
            }
        }

        return continueOnError;
    }

    /**********************************************************************************************
     * Get the data type name determined by the specified data type size and match criteria
     *
     * @param dataTypeHandler
     *            reference to the data type handler
     *
     * @param sizeInBytes
     *            data type size in bytes
     *
     * @param isInteger
     *            true if the data type to match is an integer
     *
     * @param isUnsigned
     *            true if the data type to match is an unsigned integer
     *
     * @param isFloat
     *            true if the data type to match is a floating point
     *
     * @param isString
     *            true if the data type to match is a character or string
     *
     * @return The name of the data type from the existing data type definitions that matches the
     *         input criteria; null if there is no match
     *********************************************************************************************/
    protected String getDataType(CcddDataTypeHandler dataTypeHandler,
                                 long sizeInBytes,
                                 boolean isInteger,
                                 boolean isUnsigned,
                                 boolean isFloat,
                                 boolean isString)
    {
        String dataType = null;

        // Step through each defined data type
        for (String[] dataTypeDefn : dataTypeHandler.getDataTypeData())
        {
            String dataTypeName = CcddDataTypeHandler.getDataTypeName(dataTypeDefn);

            // Check if the type to match is a string (vs a character)
            if (isString && sizeInBytes > 1 && dataTypeHandler.isString(dataTypeName))
            {
                // Store the matching string data type and stop searching
                dataType = CcddDataTypeHandler.getDataTypeName(dataTypeDefn);
                break;
            }

            // Check if the size in bytes matches the one for this data type
            if (sizeInBytes == dataTypeHandler.getDataTypeSize(dataTypeName))
            {
                // Check if the type indicated by the input flags matches the data type
                if ((isInteger && !isUnsigned && dataTypeHandler.isInteger(dataTypeName))

                    || (isInteger && isUnsigned && dataTypeHandler.isUnsignedInt(dataTypeName))

                    || (isFloat && dataTypeHandler.isFloat(dataTypeName))

                    || (isString && dataTypeHandler.isCharacter(dataTypeName)))
                {
                    // Store the matching data type and stop searching
                    dataType = CcddDataTypeHandler.getDataTypeName(dataTypeDefn);
                    break;
                }
            }

        }

        return dataType;
    }

    /**********************************************************************************************
     * Convert a name space path name in the format </path1</path2<...>>>name into a valid
     * structure name by replacing all invalid characters (e.g., '/'s, spaces, etc.) with an
     * underscore
     *
     * @param path
     *            space system path
     *
     * @return Space system path converted to a valid structure name
     *********************************************************************************************/
    protected String convertPathToTableName(String path)
    {
        // Replace all invalid characters with an underscore
        path = path.replaceAll("[^a-zA-Z0-9_]", "_");

        // Check if the initial character is invalid (i.e., a numeral after the above replacement
        // is performed)
        if (path.matches("[^a-zA-Z_].*"))
        {
            // Preface the path with an underscore to make it valid
            path = "_" + path;
        }

        return path;
    }
}
