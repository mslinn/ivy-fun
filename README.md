# Scala Project Template

[![Build Status](https://travis-ci.org/mslinn/sbtTemplate.svg?branch=master)](https://travis-ci.org/mslinn/sbtTemplate)
[![GitHub version](https://badge.fury.io/gh/mslinn%2FsbtTemplate.svg)](https://badge.fury.io/gh/mslinn%2FsbtTemplate)

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
