import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class OnlineCoursesAnalyzer {
    public OnlineCoursesAnalyzer(String datasetPath){
        // 创建 reader
        try (BufferedReader br = Files.newBufferedReader(Paths.get(datasetPath))) {
            // CSV文件的分隔符
            String DELIMITER = ",";
            // 按行读取
            String line;
            while ((line = br.readLine()) != null) {
                // 分割
                String[] columns = line.split(DELIMITER);
                // 打印行
                System.out.println(String.join(", ", columns));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
