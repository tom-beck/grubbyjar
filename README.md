# Grubbyjar

Grubbyjar is a gradle plugin for building self-contained jruby jars.

Given a jruby script `foo.rb` and this `build.gradle`:

    apply plugin: 'ca.neitsch.grubbyjar'

    repositories {
        mavenCentral()
    }

    dependencies {
        runtimeOnly "org.jruby:jruby-complete:9.1.12.0"
    }

`gradle grubbyjar` will build a self-contained jar that runs `foo.rb`
when you run `java -jar build/libs/<project name>.jar`.

## Tasks

  - **`grubbyjar`**
    Builds the Grubbyjar

  - **`grubbyjarRequire`**
    Copies jar dependencies locally and generates a
    `lib/<gem_name>_jars.rb` file you can `require` to use java libraries
    when not running from a jar

  - **`shadowJar`**
    Grubbyjar configures the shadow plugin, so some things can be tweaked by
    adjusting the standard shadow configuration.

  - **`grubbyjarPrep`**
      is something that grubbyjar makes `shadowJar` depend on so that some
      needed files are in place when building the shadow jar

## Configuration

You can configure the name of the script to be run with

    grubbyjar {
        script 'foo.rb'
    }

By default, the gem’s default executable is used, or if not building a gem and
there is a single `*.rb` file in the project directory, it will be used.
Otherwise configuration will be needed.
