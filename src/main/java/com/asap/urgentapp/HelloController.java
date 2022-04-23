package com.asap.urgentapp;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.*;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() throws IOException {
        welcomeText.setText("");

        Stage stage = (Stage)welcomeText.getScene().getWindow();

        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);

        if(selectedDirectory == null){
            //No Directory selected
        }else{
            SXSSFWorkbook workbook = new SXSSFWorkbook(-1); // keep 100 rows in memory, exceeding rows will be flushed to disk
            Sheet spreadsheet = workbook.createSheet("Sheet1");
            Row row = spreadsheet.createRow(0);
            String[] columns = "Node Name,Sequence Number,Time Stamp,User,IP Address,Operation,Type of Access,User Domain".split(",");
            int colSize = columns.length;
            for (int c = 0; c < colSize; c++) row.createCell(c).setCellValue(columns[c]);
            int sheetCount = 1;
            int r = 1;
            Path dir = selectedDirectory.toPath();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path file: stream) {
                    String nodeName = "";
                    Matcher matcher = Pattern.compile("(?<=_)(.*)(?=-)-...", Pattern.DOTALL).matcher(file.getFileName().toString());
                    if (matcher.find()) {
                        nodeName = matcher.group();
                    }
                    try (BufferedReader reader = new BufferedReader(new FileReader(file.toFile()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] split = line.split("\t");
                            if (r == 1048576) {
                                spreadsheet = workbook.createSheet(String.format("Sheet%d", ++sheetCount));
                                row = spreadsheet.createRow(0);
                                for (int c = 0; c < colSize; c++) row.createCell(c).setCellValue(columns[c]);
                                r = 1;
                            }
                            row = spreadsheet.createRow(r++);
                            row.createCell(0).setCellValue(nodeName);
                            row.createCell(1).setCellValue(split[0]);
                            row.createCell(2).setCellValue(split[1]);
                            row.createCell(3).setCellValue(split[2]);
                            row.createCell(4).setCellValue(split[3]);
                            row.createCell(5).setCellValue(split[4]);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException | DirectoryIteratorException x) {
                System.err.println(x);
            }
            FileOutputStream out = new FileOutputStream(dir.toAbsolutePath() + File.separator + "Audit_Exported.xlsx");
            workbook.write(out);
            out.flush();
            out.close();
            workbook.dispose();
            welcomeText.setText("File Saved");
        }
    }
}
