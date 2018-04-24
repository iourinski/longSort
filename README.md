# longSort

## how to run and test

this is an sbt project, you can clone it and then either run whatever you like from
your IDE of you can create a fat jar and run it by

Run in the project root folder 

*\$ sbt assembly*

this will generate a "fat jar" in target folder, you can then run the sorter as follows

*\$ java -cp target/scala-2.12/joom-assembly-0.1.jar joom.longsort.runners.LongFileSorter src/main/resources/bible.txt reverse fastStreaming*

where *src/main/resources/bible.txt* is path to file you want to sort
 
*reverse* is a type of sort you want to do (descending with distinguishing between upper/lower cases)
*fastStreaming* type of sorter used (see below)

You can also run tests by just typing 

*\$ sbt test* 

in the root folder of the project

# implementation

As the problem is defined in a general way, several assumptions were made:  we expect lines to
be of reasonable length (i.e. a line is guaranteed to fit into memory). 

There could be different ways to sort text data, so several sorting functions are available

The data can be filtered and sanitized differently, so there is a possibility to pass various functions:
filtering through regex match, clean up can be done using and string fucntions (currently by default we trim
the lines).

The sorting itself is done by partitioning data into several files, which are saved on the harddrive (in a specified
folder) and then each partition is sorted and merged. The same approach may work on a cluster
but in this case partitioned data should be send over the network to other computers
this can be easily implemented using trait Sorter (in project).

Large chunks of data can come from both files and various streams: in case of large files,
we can theoretically (not always) see how large a file is and decide on the number of partitions needed
in advance. Such approach is implemented in LocalSorter: the object accepts the path to the file to sort,
then it checks the  file size and compares it with available memory (weighted by coefficient passed in config)
and then decides on number of partitions.

StreamSorter does not know anyhting about where the information comes from, but it knows
the size of partition in lines, a new partition file is created when enough lines was received.
The partitions are sorted by the tools available in scala. 

Merging of sorted partitions can be done using different approaches too (merging network
data and data from local machine would have some differences in implementation), for 
that matter a trait for merger is provided and two implementations are presented: merging with a list-based buffer and 
a slightly faster merging with linked-list based buffer (in case of linked list updating a buffer is *O(n)* of 
buffer length, which is a number partitions, list-based buffer takes *O(n\* log (n))* time
in worst case).

# Generator 

There is a script that generates text files, class TextGenerator, it can be run
similarly to the sorter class, it takes three parameters:
*\$ java -cp target/scala-2.12/joom-assembly-0.1.jar joom.longsort.runners.LongFileSorter bible.txt 100000 /tmp/rnd_bible.txt*
where *bible.txt* is the text file that is used for dictionary building,
*1000000* number of lines in the generated file and  */tmp/rnd_bible.txt* path to resulting file.
The script DOES not generate meaningfult texts, it generated random sequences of 0 to 15 random words.

