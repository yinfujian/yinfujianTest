package tool.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

public class Test {

    public static void main(String[] args) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data");

        // 假设要导出的数据
        Object[][] columnData = {{"Jane Smith"}, {"John Doe"}, {"Jane Smith", "Lisi"}};

        // 创建一行并写入数据
        Row row = sheet.createRow(0);
        for (int i = 0; i < columnData.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(String.valueOf(columnData[i][0]));
        }

        // 合并行
        if (columnData.length > 1) {
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columnData.length - 1));
        }

        // 将数据写入到文件
        FileOutputStream fileOut = new FileOutputStream("output.xlsx");
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
    }
}
