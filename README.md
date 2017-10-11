# Grubyjar

Grubyjar is a gradle plugin for building self-contained jruby jars.

Given a jruby script `foo.rb` and this `build.gradle`:

    apply plugin: 'ca.neitsch.grubyjar'

    repositories {
        mavenCentral()
    }

    dependencies {
        runtimeOnly "org.jruby:jruby-complete:9.1.12.0"
    }

`gradle grubyjar` will build a self-contained jar that runs `foo.rb`
when you run `java -jar build/libs/<project name>.jar`.

## Tasks

  - **`grubyjar`**
    Builds the Grubyjar

  - **`grubyjarRequire`**
    Copies jar dependencies locally and generates a
    `lib/<gem_name>_jars.rb` file you can `require` to use java libraries
    when not running from a jar

  - **`shadowJar`**
    Grubyjar configures the shadow plugin, so some things can be tweaked by
    adjusting the standard shadow configuration.

  - **`grubyjarPrep`**
      is something that grubyjar makes `shadowJar` depend on so that some
      needed files are in place when building the shadow jar

## Configuration

You can configure the name of the script to be run with

    grubyjar {
        script 'foo.rb'
    }

By default, if there is a single `*.rb` file in the project directory, it
will be used, otherwise configuration will be needed.
