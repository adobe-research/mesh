# The Mesh Programming Language

This is the repo for the Mesh language project.

**NOTE: Mesh is pre-Alpha at present, pretty much undocumented,
and in general not that much fun to explore, yet.** For more information
on the project and its timeline, check out the 
[FAQ](https://github.com/adobe-research/mesh/wiki/Mesh-FAQ).

That said:

## Setup Requirements

To build the Mesh compiler and interactive shell from source you will 
need the following:

* Java JDK 1.6+
* Apache Ant 1.8+

## Compiling

Compiling the Mesh compiler and REPL shell is done using Ant

    $ ant

## Interactive Shell

Once you have successfully compiled Mesh you will be able to run an interacive
shell and also run the included sample applications.

To launch the interactive shell

    $ ./bin/shell
    >

The Mesh scripts found in the src/script directory are mostly demos, and can
be run from the shell, for example as follows:

    $ ./bin/shell
    > $load prballs.m

To get a list of commands in the shell run "$help"

    $ ./bin/shell
    > $help

## Licensing

Mesh is distributed under the MIT license, as described in
[LICENSE.txt](LICENSE.txt). The Mesh project also uses a number
of open-source libraries, which can be found in the [lib](lib)
directory. Their licenses can be found in [LICENSES.md](lib/LICENSES.md)
in that directory.

