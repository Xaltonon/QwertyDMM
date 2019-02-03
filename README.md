# QwertyDMM

QwertyDMM is a fork of FastDMM, an alternative map editor for BYOND that is designed to be more flexible.

# Features

- Very specific display filters! More flexible than just object, mob, area and turf!
- The ability to easily place down directional objects.
- Can easily render and display icons larger than 32x32 without any hassle!
- Can work with projects that utilize planes.
- Prevents large diffs by minimizing changes.
- Randomizes keys to prevent merge conflicts.
- Requires no external dependencies! Just run a single `.jar` file!
- Autosaving Option

# Requirements

- QwertyDMM requires Java 8 to be installed.

# Usage

- Download the latest QwertyDMM version from the releases page, or compile it yourself.
- Run `QwertyDMM.jar`
- Use the `Open DME` option to open an environment file.
- Upon completion, use the `Open` option to select the desired map from the list.
- Use middle mouse button or alt click and drag to move your view and scroll wheel to zoom in/out.
- If you select an object and hold down control and click on a tile, and while holding your mouse button down, you move your mouse in a direction, you can specify a direction for the var.

# Building QwertyDMM.

QwertyDMM uses [Gradle](https://docs.gradle.org/current/userguide/userguide.html) for its dependency management!

The project uses the `us.kirchmeier.capsule` plugin for packing the source and dependencies into a fully runnable JAR. Use this to generate a jar, which will reside in /build/libs:

```
gradlew fatCapsule
```

In order to run the project you can replace `gradle fatCapsule` with `gradle run`, as shown below.

```
gradlew run
```

# Credits

Credits and thanks to original authors and contributors:

[monster860](https://github.com/monster860) for originally creating FastDMM, the alternative map editor for BYOND.

# License

QwertyDMM is licensed under [GNU GPL v3](https://www.gnu.org/licenses/gpl-3.0.html), with some exceptions which are marked as such.
