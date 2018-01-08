# Function arities of different Scala projects

## Overview

I was curious what is a disctibution of function arities between different Scala projects.

To gather the statistics I wrote a simple Scala compiler plugin, that writes function name and its arity to a file.

[Here](stats.ipynb) is the visualisations of this data for Scala 2.12 compiler, Dotty 0.5, Akka 2.4, Spark 2.2, Cats 1.0, and Scalaz 7.2.9.

## How to reproduce/extend

Build the compiler plugin. This will cross-compile the plugin for Scala 2.11 and 2.12.

    sbt +publishLocal

Add the following to build settings of a project you'd like to inspect.

    addCompilerPlugin("plugin" %% "scala-stats-plugin" % "0.1.0-SNAPSHOT"),
    concurrentRestrictions in Global := Seq(Tags.limit(Tags.Compile, 1))

Then compile the project. After compilation `stats.txt` will be generated.
Use `stats.ipynb` to visualize the results.