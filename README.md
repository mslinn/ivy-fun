# Fun With Apache Ivy and Scala

[![Build Status](https://travis-ci.org/mslinn/ivy-fun.svg?branch=master)](https://travis-ci.org/mslinn/ivy-fun)
[![GitHub version](https://badge.fury.io/gh/mslinn%2Fivy-fun.svg)](https://badge.fury.io/gh/mslinn%2Fivy-fun)

## Running the Program
The `bin/run` Bash script assembles this project into a fat jar and runs it.
Sample usage, which runs the default entry point in `src/main/scala/`:

```
$ bin/run 
```

The `-j` option forces a rebuild of the fat jar. 
Use it after modifying the source code.

```
$ bin/run -j
```
