package test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.text.translate.UnicodeEscaper;

import src.compilerdeps.Csvruntime;
import src.compilerdeps.Csvruntime.Filter;

public class Test {
    public static void main(String[] args) {

        int passedUnits = 0;
        int failedUnits = 0;

        System.out.println("Running " + Units.length + " unit tests.");
        for (UnitTest unit : Units) {
            unit.testFunction();
            if (unit.testPass == true) {
                System.out.print("[Passed] : ");
                passedUnits++;
            } else {
                System.out.print("[Failed] : ");
                failedUnits++;
            }

            System.out.println(unit.testName);
        }

        System.out.println(passedUnits + " Passed");
        System.out.println(failedUnits + " Failed");
    }

    static abstract class UnitTest {

        public boolean testPass = false;
        public String testName;

        public abstract void testFunction();

        UnitTest(String name) {
            this.testName = name;
        }
    }

    // A list of the code for all the checks
    static UnitTest[] Units = {
            new UnitTest("Import csv file") {
                public void testFunction() {
                    Csvruntime csvData;
                    try {
                        csvData = new Csvruntime("starbucks.csv");
                        if (csvData.getHeader() != null) {
                            testPass = true;
                        }
                    } catch (Exception e) {
                        testPass = false;
                    }
                }
            },

            new UnitTest("Return header from csv object") {
                public void testFunction() {
                    Csvruntime csvData;
                    try {
                        csvData = new Csvruntime("starbucks.csv");
                        if (csvData.getHeader().size() == 2) {
                            testPass = true;
                        }
                    } catch (Exception e) {
                        testPass = false;
                    }
                }
            },

            new UnitTest("Get csv data array") {
                public void testFunction() {
                    Csvruntime csvData = importCSV("starbucks.csv");

                    ArrayList<ArrayList<String>> data = csvData.getData();

                    try {
                        if (data.size() == 2) {
                            testPass = true;
                        }
                    } catch (Exception e) {
                        testPass = false;
                    }

                }
            },

            new UnitTest("Get collumn of correct type") {
                public void testFunction() {
                    Csvruntime csvData = importCSV("starbucks.csv");

                    try {
                        List<Integer> numbers = csvData.getColumn("Number");

                        if (numbers.get(0) == 1) {
                            testPass = true;
                        }
                    } catch (Exception e) {
                        testPass = false;
                    }

                }
            },

            new UnitTest("Get single value") {
                public void testFunction() {
                    Csvruntime csvData = importCSV("starbucks.csv");

                    try {
                        String value = csvData.getValueAt("String", 0);

                        if (value.equals("Text")) {
                            testPass = true;
                        }
                    } catch (Exception e) {
                        testPass = false;
                    }
                }
            },

            new UnitTest("Get Row") {
                public void testFunction() {
                    Csvruntime csvData = importCSV("starbucks.csv");

                    try {
                        List<String> value = csvData.getRow(1);

                        if (value.get(1).equals("Something")) {
                            testPass = true;
                        }
                    } catch (Exception e) {
                        testPass = false;
                    }
                }
            },

            new UnitTest("Add row to csv") {
                public void testFunction() {
                    Csvruntime csvData = importCSV("starbucks.csv");
                    ArrayList<String> value;

                    try {
                        csvData.addRow(new String[] { "4", "SomeText" });
                        value = csvData.getRow(2);
                        if (value.get(0).equals("4")) {
                            testPass = true;
                        }
                    } catch (Exception e) {
                        testPass = false;
                    }
                }
            },

            new UnitTest("Add row of incorrect lenght") {
                public void testFunction() {
                    Csvruntime csvData = importCSV("starbucks.csv");

                    try {
                        csvData.addRow(new String[] { "4", "SomeText", "Too Much text" });
                        testPass = false;
                    } catch (Exception e) {
                        if (e.getMessage().equals("Incorrect lenght")) {
                            testPass = true;
                        }
                    }
                }
            },

            new UnitTest("Add row of incorrect type") {
                public void testFunction() {
                    Csvruntime csvData = importCSV("starbucks.csv");

                    try {
                        csvData.addRow(new String[] { "4", "S" });
                        testPass = false;
                    } catch (Exception e) {
                        if (e.getMessage().equals("Incorrect type")) {
                            testPass = true;
                        }
                    }
                }
            },

            new UnitTest("Add column to csv") {
                public void testFunction() {
                    try {
                        Csvruntime csvData = importCSV("starbucks.csv");

                        String header = "Cup Size (L)";
                        ArrayList<Double> cupsizeList = new ArrayList<>();

                        cupsizeList.add(0.33);
                        cupsizeList.add(0.5);

                        csvData.addColumn(header, cupsizeList);

                        ArrayList<Double> addedColumn = csvData.getColumn("Cup Size (L)");

                        if (addedColumn.equals(cupsizeList)) {
                            testPass = true;
                        }
                    } catch (Exception e) {
                        testPass = false;
                        System.out.println(e);
                    }
                }
            },

            new UnitTest("Add incorrect lenght column to csv") {
                public void testFunction() {
                    try {
                        Csvruntime csvData = importCSV("starbucks.csv");

                        String header = "Cup Size (L)";
                        ArrayList<Double> cupsizeList = new ArrayList<>();

                        cupsizeList.add(0.33);
                        cupsizeList.add(0.5);
                        cupsizeList.add(0.66);

                        csvData.addColumn(header, cupsizeList);

                        List<Double> addedColumn = csvData.getColumn("Cup Size (L)");

                        if (addedColumn.equals(cupsizeList)) {
                            testPass = true;
                        }
                    } catch (Exception e) {
                        if (e.getMessage().equals("Incorrect collumn length")) {
                            testPass = true;
                        }
                    }
                }
            },

            new UnitTest("Add column as string type to csv") {
                public void testFunction() {
                    try {
                        Csvruntime csvData = importCSV("starbucks.csv");

                        String header = "Cup Size (L)";
                        csvData.addColumn(header, new String[] { "0.33", "0.5" });

                        ArrayList<Double> addedColumn = csvData.getColumn("Cup Size (L)");

                        if (addedColumn.get(0).equals(0.33)) {
                            testPass = true;
                        }

                    } catch (Exception e) {
                        testPass = false;
                    }
                }
            },

            new UnitTest("Remove the last row in csv") {
                public void testFunction() {
                    try {
                        Csvruntime csvData = importCSV("starbucks.csv");

                        csvData.removeRow();

                        int length = csvData.getData().size();

                        if (length == 1) {
                            testPass = true;
                        }
                    } catch (Exception e) {
                        testPass = false;
                    }
                }
            },

            new UnitTest("Remove specific index in csv") {
                public void testFunction() {
                    try {
                        Csvruntime csvData = importCSV("starbucks.csv");

                        csvData.removeRow(0);

                        ArrayList<String> list = csvData.getRow(0);

                        if (list.get(0).equals("2")) {
                            testPass = true;
                        }
                    } catch (Exception e) {
                        testPass = false;
                    }
                }
            },

            new UnitTest("Remove column in csv") {
                public void testFunction() {
                    try {
                        Csvruntime csvData = importCSV("starbucks.csv");

                        csvData.removeColumn();

                        int lenght = csvData.getRow(0).size();

                        if (lenght == 1) {
                            testPass = true;
                        }
                    } catch (Exception e) {
                        testPass = false;
                    }
                }
            },

            new UnitTest("Insert row in csv") {
                public void testFunction() {
                    try {
                        Csvruntime csvData = importCSV("starbucks.csv");

                        csvData.insertRow(1, new String[] { "4", "SomeText" });

                        ArrayList<String> returnedList = csvData.getRow(1);

                        if (returnedList.get(0).equals("4")) {
                            testPass = true;
                        }

                    } catch (Exception e) {
                        testPass = false;
                    }
                }
            },

            new UnitTest("Insert column in csv") {
                public void testFunction() {
                    try {
                        Csvruntime csvData = importCSV("starbucks.csv");

                        String header = "Cup Size (L)";
                        ArrayList<Double> cupsizeList = new ArrayList<>();

                        cupsizeList.add(0.33);
                        cupsizeList.add(0.5);

                        csvData.insertColumn(1, header, cupsizeList);

                        Double addedColumn = Double.parseDouble(csvData.getData().get(0).get(1));

                        if (addedColumn == 0.33) {
                            testPass = true;
                        }

                    } catch (Exception e) {
                        testPass = false;
                    }
                }
            },

            new UnitTest("Insert column as string in csv") {
                public void testFunction() {
                    try {
                        Csvruntime csvData = importCSV("starbucks.csv");

                        String header = "Cup Size (L)";

                        csvData.insertColumn(1, header, new String[] { "0.33", "0.5" });

                        Double addedColumn = Double.parseDouble(csvData.getData().get(0).get(1));

                        if (addedColumn == 0.33) {
                            testPass = true;
                        }

                    } catch (Exception e) {
                        testPass = false;
                    }
                }
            },

            new UnitTest("Sum a column in a csv file") {
                public void testFunction() {
                    try {
                        Csvruntime csvData = importCSV("starbucks.csv");

                        int sum = csvData.sum("Number");

                        if (sum == 3) {
                            testPass = true;
                        }

                    } catch (Exception e) {
                        testPass = false;
                    }
                }
            },

            new UnitTest("Sum a column of floats in a csv file") {
                public void testFunction() {
                    try {
                        Csvruntime csvData = importCSV("calcSum.csv");

                        double sum = csvData.sum("Number");

                        if (sum > 4.62 && sum < 4.64) {
                            testPass = true;
                        }

                    } catch (Exception e) {
                        testPass = false;
                    }
                }
            },

            new UnitTest("Get the mean value of a column") {
                public void testFunction() {
                    try {
                        Csvruntime csvData = importCSV("starbucks.csv");

                        double mean = csvData.mean("Number");

                        if (mean == 1.5) {
                            testPass = true;
                        }

                    } catch (Exception e) {
                        testPass = false;
                    }
                }
            },

            new UnitTest("Get count (Amount of rows in a column)") {
                public void testFunction() {
                    try {
                        Csvruntime csvData = importCSV("starbucks.csv");

                        int count = csvData.count();

                        if (count == 2) {
                            testPass = true;
                        }
                    } catch (Exception e) {
                        testPass = false;
                    }
                }
            },

            new UnitTest("Construct csv from existing file") {
                public void testFunction() {
                    try {
                        Csvruntime csvData = importCSV("starbucks.csv");

                        Csvruntime csvData2 = new Csvruntime(csvData.getData(), csvData.getHeader());

                        if ((int) csvData2.getValueAt("Number", 0) == 1) {
                            testPass = true;
                        }
                    } catch (Exception e) {
                        testPass = false;
                    }
                }
            },

            new UnitTest("Sort the csv file by column (Integer)") {
                public void testFunction() {
                    try {
                        Csvruntime csvData = importCSV("sort.csv");

                        Csvruntime sortedCsvData = csvData.sort("Numbers");

                        String Value = sortedCsvData.getValueAt("Strings", 0);

                        if (Value.equals("Soda")) {
                            testPass = true;
                        }
                    } catch (Exception e) {
                        testPass = false;
                    }
                }
            },

            new UnitTest("Sort the csv file by column (Strings)") {
                public void testFunction() {
                    try {
                        Csvruntime csvData = importCSV("sort.csv");

                        Csvruntime sortedCsvData = csvData.sort("Strings");

                        int Value = sortedCsvData.getValueAt("Numbers", 2);

                        if (Value == 3) {
                            testPass = true;
                        }
                    } catch (Exception e) {
                        testPass = false;
                    }
                }
            },

            new UnitTest("Sort the csv file by column descending (Integer)") {
                public void testFunction() {
                    try {
                        Csvruntime csvData = importCSV("sort.csv");

                        Csvruntime sortedCsvData = csvData.sort("Numbers", true);

                        int Value = sortedCsvData.getValueAt("Numbers", 0);

                        if (Value == 19) {
                            testPass = true;
                        }
                    } catch (Exception e) {
                        testPass = false;
                    }
                }
            },

            new UnitTest("Filter the list for an entry in a column") {
                public void testFunction() {
                    try {
                        Csvruntime csvData = importCSV("cars.csv");

                        Csvruntime filteredCsvData = csvData.filter("Brand", new Filter<String>() {

                            @Override
                            public Boolean compare(String entry) {
                                return entry.equals("BMW");
                            }

                        });

                        if (filteredCsvData.count() == 2) {
                            testPass = true;
                        }
                    } catch (Exception e) {
                        testPass = false;
                    }
                }
            },

            new UnitTest("Export the csv file") {
                public void testFunction() {
                    try {
                        Csvruntime csvData = importCSV("cars.csv");

                        csvData.addRow(new String[] {"Peugeot","RS","191"});

                        csvData.export("cars2.csv");

                        testPass = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        testPass = false;
                    }
                }
            }
    };

    public static Csvruntime importCSV(String path) {
        try {
            Csvruntime data = new Csvruntime(path);
            return data;
        } catch (Exception e) {
            System.out.println("Does not work.");
        }

        return null;
    }
}
