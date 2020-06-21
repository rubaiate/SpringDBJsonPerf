### Spring boot DB to json conversion perf test

Performance test application for db to json conversion.

#### jackson serialize performance
##### Test results for converting list of 10000 trades
1. Directly convert data array to json using object mapper  
  13ms.
2. Use jackson stream. Add trades using `objectMapper.writeValue`. Use in-memory buffer before writing into the file  
13ms.
3. Use jackson stream. Add trades using `objectMapper.writeValue`. Write directly into file stream  
12ms.

So there is no significant advantage over tested methods. 

#### DB to json conversion performance
Test results for loading 10000 trades from db and converting into json
1. Load trades into a list from repository. 
Converting to json directly from the list.  
59ms.
2. Load trades into a list from repository. 
Use jackson stream.
Add trades using `objectMapper.writeValue`.  
55ms. 
3. Load trades using a jdbc connection. 
Use jackson stream. 
Add trades using `objectMapper.writeValue`, while iterating result set.  
31ms.
4. Load trades using hibernate stream. 
Use jackson stream. 
Add trades using `objectMapper.writeValue`, while iterating result set.  
60ms.
5. Load trades using a jdbc connection. 
Use jackson stream. 
Add trades using jackson stream api, while iterating result set.  
28ms.

#### Conclusion
1. Using jackson stream api has minor advantage for serialization
2. Using jdbc connection provides 2x gain over using hibernate. When iterating over jdbc result set, java object(trades)
creation is minimum, causing less pressure on garbage collector.

##### Notes
If jackson stream is directly writing to file stream make sure to disable FLUSH_AFTER_WRITE_VALUE, 
otherwise flush will be called for every write value causing significant performance degradation.

##### Dependencies
1. Spring boot - 2.3.1  
2. fasterxml.jackson - 2.11.0
3. PostgreSQL 12.2, compiled by Visual C++ build 1914, 64-bit 

##### System
1. Windows 10 
2. Intel(R) Core(TM) i7-9750H CPU @ 2.60GHz 
3. RAM 16.0 GB 