
package helpers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import entities.FileType;


public class DocumentManager
{
    private static HttpServletRequest request;

    public static void Init(HttpServletRequest req, HttpServletResponse resp){
        request = req;
    }

    public static long GetMaxFileSize()
    {
        long size;
        
        try
        {
            size = Long.parseLong(ConfigManager.GetProperty("filesize-max"));
        }
        catch(Exception ex)
        {
            size = 0;
        }

        return size > 0 ? size : 5 * 1024 * 1024;
    }

    public static List<String> GetFileExts()
    {
        List<String> res = new ArrayList<>();
        
        res.addAll(GetViewedExts());
        res.addAll(GetEditedExts());
        res.addAll(GetConvertExts());

        return  res;
    }

    public static List<String> GetViewedExts()
    {
        String exts = ConfigManager.GetProperty("files.docservice.viewed-docs");
        return Arrays.asList(exts.split("\\|"));
    }

    public static List<String> GetEditedExts()
    {
        String exts = ConfigManager.GetProperty("files.docservice.edited-docs");
        return Arrays.asList(exts.split("\\|"));
    }

    public static List<String> GetConvertExts()
    {
        String exts = ConfigManager.GetProperty("files.docservice.convert-docs");
        return Arrays.asList(exts.split("\\|"));
    }

    public static String CurUserHostAddress(String userAddress)
    {
        if(userAddress == null)
        {
            try
            {
                userAddress = InetAddress.getLocalHost().getHostAddress();
            }
            catch(Exception ex)
            {
                userAddress = "";
            }
        }

        return userAddress.replaceAll("[^0-9a-zA-Z.=]", "_");
    }

    public static String StoragePath(String fileName, String userAddress)
    {
        String serverPath = request.getSession().getServletContext().getRealPath("");
        String storagePath = ConfigManager.GetProperty("storage-folder");
        String hostAddress = CurUserHostAddress(userAddress);
        
        String directory = serverPath + "\\" + storagePath + "\\";

        File file = new File(directory);
        
        if (!file.exists())
        {
            file.mkdir();
        }
        
        directory = directory + hostAddress + "\\";
        file = new File(directory);

        if (!file.exists())
        {
            file.mkdir();
        }

        return directory + fileName;
    }

    public static String GetCorrectName(String fileName)
    {
        String baseName = FileUtility.GetFileNameWithoutExtension(fileName);
        String ext = FileUtility.GetFileExtension(fileName);
        String name = baseName + ext;

        File file = new File(StoragePath(name, null));

        for (int i = 1; file.exists(); i++)
        {
            name = baseName + " (" + i + ")" + ext;
            file = new File(StoragePath(name, null));
        }

        return name;
    }

    public static String CreateDemo(String fileExt) throws Exception
    {
        String demoName = "sample." + fileExt;
        String fileName = GetCorrectName(demoName);
        
        try
        {
            InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(demoName);
            
            File file = new File(StoragePath(fileName, null));
            
            try (FileOutputStream out = new FileOutputStream(file)) {
                int read;
                final byte[] bytes = new byte[1024];
                while ((read = stream.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                out.flush();
            }
        }
        catch(Exception ex)
        {
            throw ex;
        }
        
        return fileName;
    }

    public static String GetFileUri(String fileName) throws Exception
    {
        try
        {
            String serverPath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
            String storagePath = ConfigManager.GetProperty("storage-folder");
            String hostAddress = CurUserHostAddress(null);
            
            String filePath = serverPath + "/" + storagePath + "/" + hostAddress + "/" + URLEncoder.encode(fileName);
            
            return filePath;
        }
        catch(Exception ex)
        {
            throw ex;
        }
    }

    public static String GetServerUrl()
    {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
    }

    public static String GetCallback(String fileName)
    {
        String serverPath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        String hostAddress = CurUserHostAddress(null);
        String query = "?type=track&fileName=" + URLEncoder.encode(fileName) + "&userAddress=" + URLEncoder.encode(hostAddress);
        
        return serverPath + "/IndexServlet" + query;
    }

    public static String GetInternalExtension(FileType fileType)
    {
        if(fileType.equals(FileType.Text))
            return ".docx";

        if(fileType.equals(FileType.Spreadsheet))
            return ".xlsx";

        if(fileType.equals(FileType.Presentation))
            return ".pptx";

        return ".docx";
    }
}