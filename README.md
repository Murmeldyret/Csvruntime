# CSVruntime?

This library is a naive attempt to handle CSV files in a self-made language as part of a student project at AAU. Since the language had some flaws in the Context-free grammar, it resulted in that the language should be able to handle everything regarding CSV files in runtime. There, were no way to specify the type of each column in the CSV file, which led to some headaches.

The library is an attempt to lessen this headache, by handling everything at runtime while still making sure that a column has a consistent type throughout the program. It uses OpenCSV for parsing the file, together with its libraries dependencies. 

# The problem to be solved
The language that this library is made for, compiles to Java and since Java is a type-safe environment then it would very much like to know what type a given value is stored as. However, this is kinda an oversight of the design process of our language, which resulted in that we need to handle variable types at runtime. So when a new Csvruntime object is created, then the constructor will make sure to extract the header of the file and figure out what type of value a field is. This is done by reading the first line and trying casting those strings to a value type through trial and error (I know it's not perfect, I don't care). It works (somewhat, but there is no time to create something better. The work began 2 weeks beforehand in.)).

# Dependencies
`OpenCSV` and its sub dependencies.
