/**************************************************************************************************
 * /** \file CcddMathExpressionHandler.java
 *
 * \author Kevin McCluney Bryan Willis
 *
 * \brief Class for evaluating simple mathematical expressions.
 *
 * \copyright MSC-26167-1, "Core Flight System (cFS) Command and Data Dictionary (CCDD)"
 *
 * Copyright (c) 2016-2021 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 *
 * This software is governed by the NASA Open Source Agreement (NOSA) License and may be used,
 * distributed and modified only pursuant to the terms of that agreement. See the License for the
 * specific language governing permissions and limitations under the License at
 * https://software.nasa.gov/.
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * expressed or implied.
 *
 * \par Limitations, Assumptions, External Events and Notes: - TBD
 *
 **************************************************************************************************/
package CCDD;

import java.util.ArrayList;
import java.util.List;

/**************************************************************************************************
 * CFS Command and Data Dictionary mathematical expression handler class
 *************************************************************************************************/
public class CcddMathExpressionHandler
{
    /**********************************************************************************************
     * Mathematical expression nest level class
     *********************************************************************************************/
    private static class NestLevel
    {
        private Double dValue;
        private int sign;
        private char operator;
        private boolean isInteger;

        /******************************************************************************************
         * Mathematical expression nest level class constructor
         *****************************************************************************************/
        protected NestLevel()
        {
            dValue = null;
            sign = 1;
            operator = '\0';
            isInteger = false;
         }

        /******************************************************************************************
         * Get the value of this nest level
         *
         * @return Value of this nest level
         *****************************************************************************************/
        protected Double getValue()
        {
            return dValue;
        }

        /******************************************************************************************
         * Set the value of this nest level
         *
         * @param dValue Value of this nest level
         *****************************************************************************************/
        protected void setValue(Double dValue)
        {
            this.dValue = dValue;
        }

        /******************************************************************************************
         * Get the operator for this nest level
         *
         * @return Operator token
         *****************************************************************************************/
        protected char getOperator()
        {
            return operator;
        }

        /******************************************************************************************
         * Set the operator for this nest level
         *
         * @param operator Operator token
         *****************************************************************************************/
        protected void setOperator(char operator)
        {
            this.operator = operator;
        }

        /******************************************************************************************
         * Get the sign value (1 or -1) for this nest level
         *
         * @return Sign value (1 or -1)
         *****************************************************************************************/
        protected int getSign()
        {
            return sign;
        }

        /******************************************************************************************
         * Set the sign value (1 or -1) for this nest level
         *
         * @param sign Sign value (1 or -1)
         *****************************************************************************************/
        protected void setSign(int sign)
        {
            this.sign = sign;
        }

        /******************************************************************************************
         * Get the nest level integer cast flag (when true the value of this nest level is cast as
         * an integer, rounding it down)
         *
         * @return sign Sign value (1 or -1)
         *****************************************************************************************/
        public boolean getIsInteger() {
            return isInteger;
        }

        /******************************************************************************************
         * Set the nest level integer cast flag (when true the value of this nest level is cast as
         * an integer, rounding it down)
         *
         * @param isInteger True if the value of this nest level is to be cast as an integer
         *****************************************************************************************/
        public void setIsInteger(boolean isInteger) {
            this.isInteger = isInteger;
        }
    }

    /**********************************************************************************************
     * Evaluate the supplied text as a mathematical expression
     *
     * @param expression Text to evaluate as a mathematical expression
     *
     * @return The result of the evaluated expression; null if the supplied text doesn't evaluate
     *         to a numeric value (not a mathematical expression or the syntax is in error)
     *********************************************************************************************/
    protected static Double evaluateExpression(String expression)
    {
        int levelIndex = 0;
        boolean isExpression = true;
        Double result = null;
        boolean isInteger = false;

        // Create a list to contain the operators and results for each nested portion of the
        // expression
        List<NestLevel> nestLevels = new ArrayList<NestLevel>();
        NestLevel nestLevel = new NestLevel();
        nestLevels.add(nestLevel);

        // Step through each character in the expression
        for (int index = 0; index < expression.length() && isExpression; index++)
        {
            switch (expression.charAt(index))
            {
                // Addition operator or positive value sign
                case '+':
                    // Check if the value for this level is set
                    if (nestLevel.getValue() != null)
                    {
                        // Set the operator
                        nestLevel.setOperator('+');
                    }

                    break;

                // Subtraction operator or negative value sign
                case '-':
                    // Check if the value and operator for this level are set
                    if (nestLevel.getValue() != null && nestLevel.getOperator() == '\0')
                    {
                        // Set the operator
                        nestLevel.setOperator('-');
                    }
                    // The value and operator for this level aren't set (this is a sign and not an
                    // operator)
                    else
                    {
                        // Set the sign
                        nestLevel.setSign(-1);
                    }

                    break;

                // Multiplication operator
                case '*':
                    // Check if the value for this level is set
                    if (nestLevel.getValue() != null)
                    {
                        nestLevel.setOperator('*');
                    }
                    // The level doesn't have a value
                    else
                    {
                        // Set the expression flag to false to indicate that the supplied text
                        // isn't an expression (or has a syntax error)
                        isExpression = false;
                    }

                    break;

                // Division operator
                case '/':
                    // Check if the value for this level is set
                    if (nestLevel.getValue() != null)
                    {
                        nestLevel.setOperator('/');
                    }
                    // The level doesn't have a value
                    else
                    {
                        // Set the expression flag to false to indicate that the supplied text
                        // isn't an expression (or has a syntax error)
                        isExpression = false;
                    }

                    break;

                // Start of a nest level operator
                case '(':
                    // Check if this '(' is part of an integer cast, '(int)' (ignore spaces)
                    if (expression.substring(index + 1).matches(" *int *\\).*"))
                    {
                        // Set the flag to indicate that the following individual value or nest
                        // level will be cast as an integer value (rounded down)
                        isInteger = true;

                        // Update the text index to skip the cast text and any trailing spaces
                        index += expression.substring(index + 1).replaceFirst("( *int *\\) *).*", "$1").length();
                    }
                    // This is the start of a nest level
                    else
                    {
                        // Create a new nest level
                        nestLevel = new NestLevel();
                        nestLevels.add(nestLevel);
                        levelIndex++;

                        // Set the nest level's integer cast flag to true if a cast precedes the
                        // level. Reset the individual cast flag since a nest level follows
                        nestLevel.setIsInteger(isInteger);
                        isInteger = false;
                    }

                    break;

                // End of a nest level operator
                case ')':
                    // Check if the current nest level isn't the initial one
                    if (levelIndex != 0 && nestLevel.getValue() != null)
                    {
                        // Check if this nest level has an integer cast in effect
                        if (nestLevel.getIsInteger())
                        {
                            // Cast the nest level value to an integer (round down)
                            nestLevel.setValue(Double.valueOf(nestLevel.getValue().intValue()));
                        }

                        // Check if the nest level has a trailing operator or if the operation
                        // using the current nest level value and the previous level's running
                        // value fails
                        if (nestLevel.getOperator() != '\0'
                            || !performOperation(nestLevel.getValue(), nestLevels.get(levelIndex - 1)))
                        {
                            // Set the expression flag to false to indicate that the supplied text
                            // isn't an expression (or has a syntax error)
                            isExpression = false;
                        }

                        // Remove the nest level
                        nestLevels.remove(levelIndex);
                        levelIndex--;
                        nestLevel = nestLevels.get(levelIndex);

                        // Since this terminates the nest level set the individual cast flag to
                        // false
                        isInteger = false;
                    }
                    // This is the initial nest level
                    else
                    {
                        // Set the expression flag to false to indicate that the supplied text
                        // isn't an expression (or has a syntax error)
                        isExpression = false;
                    }

                    break;

                // Bit-wise AND operator
                case '&':
                    // Check if the value for this level is set
                    if (nestLevel.getValue() != null)
                    {
                        nestLevel.setOperator('&');
                    }
                    // The level doesn't have a value
                    else
                    {
                        // Set the expression flag to false to indicate that the supplied text
                        // isn't an expression (or has a syntax error)
                        isExpression = false;
                    }

                    break;

                case '|':
                    // Bit-wise OR operator Check if the value for this level is set
                    if (nestLevel.getValue() != null)
                    {
                        nestLevel.setOperator('|');
                    }
                    // The level doesn't have a value
                    else
                    {
                        // Set the expression flag to false to indicate that the supplied text
                        // isn't an expression (or has a syntax error)
                        isExpression = false;
                    }

                    break;

                // Left bit shift operator
                case '<':
                    // Check if the next character is also a '<'
                    if (expression.length() > index + 1 && expression.charAt(index + 1) == '<')
                    {
                        // Check if the value for this level is set
                        if (nestLevel.getValue() != null)
                        {
                            nestLevel.setOperator('<');
                            index++;
                        }
                        // The level doesn't have a value
                        else
                        {
                            // Set the expression flag to false to indicate that the supplied text
                            // isn't an expression (or has a syntax error)
                            isExpression = false;
                        }
                    }
                    // The next character isn't a '<'
                    else
                    {
                        // Set the expression flag to false to indicate that the supplied text
                        // isn't an expression (or has a syntax error)
                        isExpression = false;
                    }

                    break;

                // Right bit shift operator
                case '>':
                    // Check if the next character is also a '>'
                    if (expression.length() > index + 1 && expression.charAt(index + 1) == '>')
                    {
                        // Check if the value for this level is set
                        if (nestLevel.getValue() != null)
                        {
                            nestLevel.setOperator('>');
                            index++;
                        }
                        // The level doesn't have a value
                        else
                        {
                            // Set the expression flag to false to indicate that the supplied text
                            // isn't an expression (or has a syntax error)
                            isExpression = false;
                        }
                    }
                    // The next character isn't a '>'
                    else
                    {
                        // Set the expression flag to false to indicate that the supplied text
                        // isn't an expression (or has a syntax error)
                        isExpression = false;
                    }

                    break;

                // Space character
                case ' ':
                    break;

                // Numeral or decimal point character
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '.':
                    // Extract the numeric value (integer or floating point) as a string from the
                    // text beginning at the current text index
                    String sValue = expression.substring(index).replaceFirst("([0-9\\.\\+\\-eE]+).*", "$1");

                    try
                    {
                        // Convert the string to its floating point numeric value
                        Double dValue = Double.valueOf(sValue);

                        // Check if an integer cast is in effect
                        if (isInteger)
                        {
                            // Convert the floating point value to an integer (round down)
                            dValue = Double.valueOf(dValue.intValue());

                            // Reset the individual value integer cast flag
                            isInteger = false;
                        }

                        // Perform the operation using the current value and the nest level's
                        // running value. Set the expression flag to false if the evaluation fails
                        isExpression = performOperation(dValue, nestLevel);
                    }
                    catch (NumberFormatException nfe)
                    {
                        // Not an actual numeric value; set the expression flag to false to
                        // indicate that the supplied text isn't an expression (or has a syntax
                        // error)
                        isExpression = false;
                    }

                    // Update the text index to skip the numerals and decimal point encompassed by
                    // the numeric value
                    index += sValue.length() - 1;
                    break;

                // Non-mathematical expression character
                default:
                    // Set the expression flag to false to indicate that the supplied text isn't an
                    // expression (or has a syntax error)
                    isExpression = false;
                    break;
            }
        }

        // Check if the text is a mathematical expression, there is no unclosed nest level, and
        // there is no trailing operator
        if (isExpression && levelIndex == 0 && nestLevel.getOperator() == '\0')
        {
            // Get the expression result
            result = nestLevel.getValue();
        }

        return result;
    }

    /**********************************************************************************************
     * Perform the operation (# operator #), as specified by the supplied nest level's operator,
     * using the supplied value and the nest level's running value
     *
     * @param dValue    First value
     *
     * @param nestLevel Nest level that determines the second value and the operator
     *
     * @return True if the operation is valid; false if an error occurs (divide by zero condition
     *         exists)
     *********************************************************************************************/
    private static boolean performOperation(Double dValue, NestLevel nestLevel)
    {
        boolean isValid = true;

        // Check if the sign is negative for the nest level
        if (nestLevel.getSign() == -1)
        {
            // Negate the value and reset the sign
            dValue = -dValue;
            nestLevel.setSign(1);
        }

        // Check if the nest level doesn't have a value
        if (nestLevel.getValue() == null)
        {
            // Set the nest level's value to the supplied value
            nestLevel.setValue(dValue);
        }
        // The nest level has a value
        else
        {
            switch (nestLevel.getOperator())
            {
                // Addition operator
                case '+':
                    nestLevel.setValue(nestLevel.getValue() + dValue);
                    break;

                // Subtraction operator
                case '-':
                    nestLevel.setValue(nestLevel.getValue() - dValue);
                    break;

                // Multiplication operator
                case '*':
                    nestLevel.setValue(nestLevel.getValue() * dValue);
                    break;

                // Division operator
                case '/':
                    // Check if the supplied value greater than zero (including a tolerance value)
                    if (Math.abs(dValue) > 0.00000001)
                    {
                        nestLevel.setValue(nestLevel.getValue() / dValue);
                    }
                    // The supplied value is effectively zero, which would result in a divide by
                    // zero
                    else
                    {
                        // Set the flag to indicate the operation failed
                        isValid = false;
                    }

                    break;

                // Bit-wise AND operator
                case '&':
                    nestLevel.setValue(Double.parseDouble(String.valueOf((nestLevel.getValue().longValue() & dValue.longValue()))));
                    break;

                // Bit-wise OR operator
                case '|':
                    nestLevel.setValue(Double.parseDouble(String.valueOf((nestLevel.getValue().longValue() | dValue.longValue()))));
                    break;

                // Left bit shift operator
                case '<':
                    // Check if the right operand is non-negative
                    if (dValue >= 0)
                    {
                        nestLevel.setValue(Double.parseDouble(String.valueOf((nestLevel.getValue().longValue() << dValue.longValue()))));
                    }
                    // The right operand is negative; the operation is undefined
                    else
                    {
                        // Set the flag to indicate the operation failed
                        isValid = false;
                    }

                    break;

                // Right bit shift operator
                case '>':
                    // Check if the right operand is non-negative
                    if (dValue >= 0)
                    {
                        // Set the result to 0 if the right operand is negative
                        nestLevel.setValue(Double.parseDouble(String.valueOf((nestLevel.getValue().longValue() >> dValue.longValue()))));
                    }
                    // The right operand is negative; the operation is undefined
                    else
                    {
                        // Set the flag to indicate the operation failed
                        isValid = false;
                    }

                    break;

                // No operator
                default:
                    break;
            }
        }

        // Reset the nest level's operator
        nestLevel.setOperator('\0');

        return isValid;
    }
}
