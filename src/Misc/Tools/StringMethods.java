
package Misc.Tools;

import java.io.File;

// Referenced classes of package Misc.Tools:
//            GeneralMethods

public class StringMethods
{

    public StringMethods()
    {
    }

    public static String convertFilePathToFileName(String file_path)
    {
        return file_path.substring(file_path.lastIndexOf(File.separator) + 1, file_path.length());
    }
}
