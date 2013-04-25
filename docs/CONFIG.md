## Compiler Configuration Properties

TODO: document other config props

Compile time constant folding can be disabled by starting the shell with the following
additonal argument

    $ JAVA_PROPS="-Dcompile.analyze.ConstantReducer.ENABLED=false" ./bin/shell

Compile time inlining of apply terms can be disabled by starting the shell with the following
additonal argument

    $ JAVA_PROPS="-Dcompile.gen.java.inline.TermInliner.ENABLED=false" ./bin/shell

