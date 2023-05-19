package src;

import com.opencsv.exceptions.CsvDataTypeMismatchException;

import src.compilerdeps.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("hello");
        Csvruntime csvData;
        try {
             csvData = new Csvruntime("starbucks.csv");
             
        } catch (Exception e) {
            System.out.println("Wrong path");
        }
        
    }
}
