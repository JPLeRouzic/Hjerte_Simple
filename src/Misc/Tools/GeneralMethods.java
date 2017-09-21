
package Misc.Tools;


public class GeneralMethods
{

    public GeneralMethods()
    {
    }

    public static Object[] removeNullEntriesFromArray(Object array[])
    {
        if(array == null) {
            return null;
        }
        int number_null_entries = 0;
        for(int i = 0; i < array.length; i++) {
            if(array[i] == null) {
                number_null_entries++;
            }
        }

        int number_valid_entries = array.length - number_null_entries;
        if(number_valid_entries == 0) {
            return null;
        }
        Object new_array[] = new Object[number_valid_entries];
        int current_index = 0;
        for(int i = 0; i < array.length; i++) {
            if(array[i] != null)
            {
                new_array[current_index] = array[i];
                current_index++;
            }
        }

        return new_array;
    }
}
