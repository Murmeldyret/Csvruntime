package src.compilerdeps;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.swing.text.html.parser.Element;

import com.opencsv.*;
import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvValidationException;

public class Csvruntime {
    private ArrayList<ArrayList<String>> csvData; // First row, second column.
    private ArrayList<TypeChecker> columnTypes = new ArrayList<TypeChecker>();
    private ArrayList<String> header = new ArrayList<String>();

    public Csvruntime(String pathString) throws IOException {
        Path path;

        try {
            path = FileSystems.getDefault().getPath(pathString);

            csvData = readLineByLine(path);
        } catch (Exception e) {
            throw new IOException("Wrong path", e);
        }

        defineTypes();
    }

    public Csvruntime(Csvruntime csv) {
        this.csvData = new ArrayList<ArrayList<String>>(csv.csvData);
        this.columnTypes = new ArrayList<TypeChecker>(csv.columnTypes);
        this.header = new ArrayList<String>(csv.header);
    }

    public Csvruntime(ArrayList<ArrayList<String>> csv, ArrayList<String> header, ArrayList<TypeChecker>... types) {
        this.csvData = new ArrayList<ArrayList<String>>(csv);
        this.header = new ArrayList<String>(header);

        if (types.length > 0) {
            this.columnTypes = new ArrayList<TypeChecker>(types[0]);
        } else {
            defineTypes();
        }
    }

    /*
     * Code received from stackoverflow:
     * https://stackoverflow.com/a/9498331/18462146
     * It has been slightly modified to fit the needs of this program.
     */
    static abstract class TypeChecker {
        public final Class type;

        public abstract boolean check(String s);

        TypeChecker(Class type) {
            this.type = type;
        }
    }

    // A list of the code for all the checks
    static TypeChecker[] typesToCheck = {

            new TypeChecker(Integer.class) // is Integer
            {
                public boolean check(String s) {
                    try {
                        Integer.parseInt(s);
                    } catch (Exception e) {
                        return false;
                    }
                    return true;
                }
            },

            new TypeChecker(Double.class) // is Double
            {
                public boolean check(String s) {
                    try {
                        Double.parseDouble(s);
                    } catch (Exception e) {
                        return false;
                    }
                    return true;
                }
            },

            new TypeChecker(Boolean.class) {
                public boolean check(String s) {
                    try {
                        if (s.equals("true") || s.equals("false")) {
                            return true;
                        }
                    } catch (Exception e) {
                        return false;
                    }
                    return false;
                }
            },

            new TypeChecker(Character.class) {
                public boolean check(String s) {
                    if (s.length() != 1) {
                        return false;
                    }
                    return true;
                }
            },

            new TypeChecker(String.class) {
                public boolean check(String s) {
                    if (s.length() == 1) {
                        return false;
                    }
                    return true;
                }
            }
    };

    interface adder<T extends Number> {
        T zero(); // Adding zero items

        T add(T lhs, T rhs); // Adding two items
    }

    class CalcSum<T extends Number> {
        // This is your method; it takes an adder now
        public T sumValue(ArrayList<T> list, adder<T> adder) {
            T total = adder.zero();
            for (T n : list) {
                total = adder.add(total, n);
            }
            return total;
        }
    }

    public interface Filter<T> {
        Boolean compare(T entry);
    }

    public <T> T getValueAt(String column, int row) {
        int i = searchHeaderIndex(column);

        Class<T> type = getType(i);

        return ObjectConverter.convert(csvData.get(row).get(i), type);
    }

    public ArrayList<String> getHeader() {
        return header;
    }

    public ArrayList<ArrayList<String>> getData() {
        return csvData;
    }

    public void addRow(String[] list, int... iparameter) throws Exception {
        int index = iparameter.length > 0 ? iparameter[0] : csvData.size();

        // Compare the lenght of the new row with a row from the csv.
        if (list.length != header.size()) {
            throw new Exception("Incorrect lenght");
        }

        // Check the type and see if it fits the csv.
        ArrayList<TypeChecker> listTypeCheckers = new ArrayList<>();

        for (String element : list) {
            for (TypeChecker typeChecker : typesToCheck) {
                if (typeChecker.check(element)) {
                    listTypeCheckers.add(typeChecker);
                    break;
                }
            }
        }

        // Compare the types of the csv with the types of the new row.
        for (int i = 0; i < listTypeCheckers.size(); i++) {
            if (!(listTypeCheckers.get(i).type.equals(columnTypes.get(i).type))) {
                throw new Exception("Incorrect type");
            }
        }

        // Add row to csv
        csvData.add(index, new ArrayList<String>(Arrays.asList(list)));
    }

    public <E> ArrayList<E> getColumn(String headerName) {
        ArrayList<E> columnList = new ArrayList<>();

        // Get column from csvData
        int i = searchHeaderIndex(headerName);

        // Get column type.
        Class<E> type = getType(i);

        for (List<String> rowList : csvData) {
            String element = rowList.get(i);

            // Cast element to correct type.
            columnList.add(ObjectConverter.convert(element, type));
        }

        return columnList;
    }

    public ArrayList<String> getRow(int i) {
        return csvData.get(i);
    }

    public <E> void addColumn(String columnHeader, ArrayList<E> columnList, int... iparameter) throws Exception {
        int index = iparameter.length > 0 ? iparameter[0] : header.size();

        // Check length of collumns
        int csvLenght = csvData.size();

        if (columnList.size() != csvLenght) {
            throw new Exception("Incorrect collumn length");
        }

        // Find type of the list
        String firstValue = ObjectConverter.convert(columnList.get(0), String.class);

        for (TypeChecker checker : typesToCheck) {
            if (checker.check(firstValue)) {
                columnTypes.add(index, checker);
                break;
            }
        }

        // Add the column to the csvStructure
        header.add(index, columnHeader);
        for (int i = 0; i < columnList.size(); i++) {
            csvData.get(i).add(index, ObjectConverter.convert(columnList.get(i), String.class));
        }

    }

    public void addColumn(String columnHeader, String[] columnList) throws Exception {
        ArrayList<String> cupsizeArrayList = new ArrayList<String>(Arrays.asList(columnList));

        addColumn(columnHeader, cupsizeArrayList);
    }

    public void removeRow() {
        removeRow(csvData.size() - 1);
    }

    public void removeRow(int index) {
        csvData.remove(index);
    }

    public void removeColumn(int index) {
        // Remove header
        int columnIndex = index;

        header.remove(columnIndex);
        columnTypes.remove(columnIndex);
        for (ArrayList<String> rowArrayList : csvData) {
            rowArrayList.remove(columnIndex);
        }
    }

    public void removeColumn() {
        removeColumn(header.size() - 1);
    }

    public void insertRow(int index, String[] list) throws Exception {
        addRow(list, index);
    }

    public <E> void insertColumn(int index, String columnHeader, ArrayList<E> columnList) throws Exception {
        addColumn(columnHeader, columnList, index);
    }

    public void insertColumn(int index, String columnHeader, String[] columnList) throws Exception {
        ArrayList<String> columnArrayList = new ArrayList<String>(Arrays.asList(columnList));

        insertColumn(index, columnHeader, columnArrayList);
    }

    public <E> E sum(String columnHeader) {
        ArrayList<E> column = getColumn(columnHeader);
        Class<E> type = getType(searchHeaderIndex(columnHeader));

        E sum = ObjectConverter.convert(0, type);

        String name = type.getSimpleName();

        switch (type.getSimpleName()) {
            case "Integer":
                CalcSum<Integer> icalc = new CalcSum<Integer>();

                Integer total = icalc.sumValue((ArrayList<Integer>) column, new adder<Integer>() {
                    public Integer add(Integer a, Integer b) {
                        return a + b;
                    }

                    public Integer zero() {
                        return 0;
                    }
                });

                return ObjectConverter.convert(total, type);
            case "Double":
                CalcSum<Double> dcalc = new CalcSum<Double>();

                Double dsum = dcalc.sumValue((ArrayList<Double>) column, new adder<Double>() {
                    public Double add(Double a, Double b) {
                        return a + b;
                    }

                    public Double zero() {
                        return 0.0;
                    }
                });
                return ObjectConverter.convert(dsum, type);

            default:
                return null;
        }
    }

    public double mean(String columnHeader) {
        int lenght = csvData.size();

        double sum = ObjectConverter.convert(sum(columnHeader), Double.class);

        return sum / (double) lenght;
    }

    public int count() {
        return csvData.size();
    }

    public <T> Csvruntime sort(String header, boolean... isDecending) {
        Csvruntime sortedCsvruntime = new Csvruntime(this);

        int orderMultiplier = isDecending.length > 0 ? isDecending[0] == true ? -1 : 1 : 1;

        int index = sortedCsvruntime.searchHeaderIndex(header);

        Class<T> type = getType(index);

        Collections.sort(sortedCsvruntime.csvData, new Comparator<ArrayList<String>>() {
            public int compare(ArrayList<String> a, ArrayList<String> b) {
                switch (type.getSimpleName()) {
                    case "Integer":
                        return Integer.compare((int) ObjectConverter.convert(a.get(index), type),
                                (int) ObjectConverter.convert(b.get(index), type)) * orderMultiplier;

                    case "Double":
                        return Double.compare((double) ObjectConverter.convert(a.get(index), type),
                                (double) ObjectConverter.convert(b.get(index), type)) * orderMultiplier;

                    case "Boolean":
                        return Boolean.compare((Boolean) ObjectConverter.convert(a.get(index), type),
                                (Boolean) ObjectConverter.convert(b.get(index), type)) * orderMultiplier;

                    case "Character":
                        return Character.compare((Character) ObjectConverter.convert(a.get(index), type),
                                (Character) ObjectConverter.convert(b.get(index), type)) * orderMultiplier;
                    default:
                        return a.get(index).compareTo(b.get(index)) * orderMultiplier;
                }
            }
        });

        return sortedCsvruntime;
    }

    private ArrayList<ArrayList<String>> readLineByLine(Path filePath) throws Exception {
        ArrayList<ArrayList<String>> list = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(filePath)) {
            try (CSVReader csvReader = new CSVReader(reader)) {
                String[] line;
                if ((line = csvReader.readNext()) != null) {
                    header = new ArrayList<String>(Arrays.asList(line));
                }

                while ((line = csvReader.readNext()) != null) {
                    list.add(new ArrayList<String>(Arrays.asList(line)));
                }
                csvReader.close();
            }
            reader.close();
        }
        return list;
    }

    private <T> Class<T> getType(int typeIndex) {
        @SuppressWarnings("unchecked") // This shit has been checked before by the typechecker, so it is perfectly
                                       // fine.
        Class<T> type = columnTypes.get(typeIndex).type;
        return type;
    }

    private int searchHeaderIndex(String name) {
        for (int i = 0; i < header.size(); i++) {
            if (header.get(i).equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private void defineTypes() {
        // Read the second line in the csv to check for types.
        for (String value : csvData.get(0)) {
            for (TypeChecker checker : typesToCheck) {
                if (checker.check(value)) {
                    columnTypes.add(checker);
                    break;
                }
            }
        }
    }

    public static <E extends Number> E add(E x, E y) {

        if (x == null || y == null) {
            return null;
        }

        if (x instanceof Double) {
            return (E) Double.valueOf(x.doubleValue() + y.doubleValue());
        } else if (x instanceof Integer) {
            return (E) Integer.valueOf(x.intValue() + y.intValue());
        } else {
            throw new IllegalArgumentException("Type " + x.getClass() + " is not supported by this method");
        }
    }

    public <T> Csvruntime filter(String columnHeader, Filter<T> comparer) {

        ArrayList<ArrayList<String>> Data = new ArrayList<ArrayList<String>>();

        ArrayList<T> operationalColumn = getColumn(columnHeader);

        for (int i = 0; i < count(); i++) {
            if(comparer.compare(operationalColumn.get(i)))
            {
                Data.add(csvData.get(i));
            }
        }



        return new Csvruntime(Data, header, columnTypes);
    }

}
