package tool.excel;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceExcel {

    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new FileReader("/Users/a58/Documents/code/yinfujianTest/tools/src/main/java/tool/excel/2.txt"));
            String line = br.readLine();
            br.close();

            BufferedReader br1 = new BufferedReader(new FileReader("/Users/a58/Documents/code/yinfujianTest/tools/src/main/java/tool/excel/1.txt"));
            String line1 = br1.readLine();
            br1.close();

            JSONObject diaoyongfangServer = JSONObject.parseObject(line);
            Map<String, JSONObject> diaoyongMap = new HashMap<>();
            JSONArray jsonServerArray = diaoyongfangServer.getJSONArray("result");
            for (int i = 0; i < jsonServerArray.size(); i++) {
                JSONObject jsonObject = jsonServerArray.getJSONObject(i);
                // 调用方名称
                diaoyongMap.put(jsonObject.getString("callerName"), jsonObject);


            }

            // 创建一个excel
            Workbook workbook = new XSSFWorkbook();
            // 创建一个sheet
            Sheet sheet = workbook.createSheet("Sheet1");
            // 第一行
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("调用方名称");
            headerRow.createCell(1).setCellValue("所属部门");
            headerRow.createCell(2).setCellValue("负责人");
            headerRow.createCell(3).setCellValue("函数");
            headerRow.createCell(4).setCellValue("是否使用");
            headerRow.createCell(5).setCellValue("备注");

            JSONObject hanshuJSON = JSONObject.parseObject(line1);
            JSONArray hanshuArray = hanshuJSON.getJSONObject("result").getJSONArray("caller_function_info");
            int rowCount = 1;
            for (int i = 0; i < hanshuArray.size(); i++) {
                JSONObject jsonObject = hanshuArray.getJSONObject(i);
                String callerName = jsonObject.getString("callerName");

                List<String> functions = (List<String>) jsonObject.get("functions");

                JSONObject diaoyongfangJson = diaoyongMap.get(callerName);
                for (int j = 0; j < functions.size(); j++) {
                    Row row = sheet.createRow(rowCount);
                    row.createCell(0).setCellValue(callerName);
                    row.createCell(1).setCellValue(diaoyongfangJson.getString("organization"));
                    row.createCell(2).setCellValue(diaoyongfangJson.getString("owners"));
                    row.createCell(3).setCellValue(functions.get(j));
                    rowCount++;
                }
            }
            FileOutputStream fileOut = new FileOutputStream("./workbook.xlsx");
            workbook.write(fileOut);
            fileOut.close();
        } catch (Exception e) {

        }
    }
}
